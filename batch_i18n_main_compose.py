import re
import hashlib

def sanitize_key(text):
    """Generate a snake_case key from Chinese/English text"""
    # Take first 30 chars, transliterate common patterns
    t = text.strip()
    # Remove interpolation placeholders for key generation
    t = re.sub(r'\$\{[^}]+\}', '_var_', t)
    t = re.sub(r'\$\w+', '_var_', t)
    # Keep only alphanumeric and some separators
    t = re.sub(r'[^\w\s\u4e00-\u9fff]', ' ', t)
    words = t.split()
    # Take first 4 meaningful words
    key_words = []
    for w in words[:5]:
        w = w.strip()
        if not w:
            continue
        if re.match(r'[\u4e00-\u9fff]', w):
            # For Chinese words, use pinyin-like transliteration or hash
            # We'll use a short hash for Chinese-only words
            key_words.append('cn' + hashlib.md5(w.encode()).hexdigest()[:4])
        else:
            key_words.append(w.lower())
    if not key_words:
        return 'str_' + hashlib.md5(text.encode()).hexdigest()[:6]
    return '_'.join(key_words)[:50]

def key_exists(key, xml_content):
    return f'name="{key}"' in xml_content

# Read files
with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt', 'r', encoding='utf-8') as f:
    kt_content = f.read()
    kt_lines = kt_content.split('\n')

with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    en_xml = f.read()

with open('app/src/main/res/values-zh/strings.xml', 'r', encoding='utf-8') as f:
    zh_xml = f.read()

# Find Chinese string literals (not in comments)
chinese_pattern = re.compile(r'"([^"]*[\u4e00-\u9fff][^"]*)"')

# Collect unique strings with their line context
entries = []
seen = set()
for i, line in enumerate(kt_lines, 1):
    stripped = line.strip()
    if stripped.startswith('//') or stripped.startswith('*'):
        continue
    for match in chinese_pattern.finditer(line):
        text = match.group(1)
        if text not in seen:
            seen.add(text)
            entries.append((i, text, line))

print(f"Total unique Chinese strings: {len(entries)}")

# Categorize strings
simple = []  # No interpolation
interpolated = []  # Has $var or ${expr}
for line_num, text, full_line in entries:
    if re.search(r'\$\{[^}]*\}|\$\w', text):
        interpolated.append((line_num, text, full_line))
    else:
        simple.append((line_num, text, full_line))

print(f"Simple: {len(simple)}, Interpolated: {len(interpolated)}")

# Generate keys and add to XML
new_en_strings = []
new_zh_strings = []
key_map = {}  # text -> key

# First, check if any strings already exist in XML
for line_num, text, full_line in entries:
    # Try to find if this exact text exists in strings.xml
    # Escape special regex chars in text
    escaped = re.escape(text)
    if re.search(f'<string[^>]*>.*{escaped}.*</string>', en_xml):
        # Find the key
        m = re.search(f'<string name="([^"]+)">.*{escaped}.*</string>', en_xml)
        if m:
            key_map[text] = m.group(1)
            continue
    
    # Generate new key
    base_key = 'mac_' + sanitize_key(text)
    key = base_key
    counter = 1
    while key_exists(key, en_xml) or key in [k for k in key_map.values()]:
        key = f"{base_key}_{counter}"
        counter += 1
    key_map[text] = key
    
    # Escape for XML
    xml_text = text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
    new_en_strings.append(f'    <string name="{key}">{xml_text}</string>')
    new_zh_strings.append(f'    <string name="{key}">{xml_text}</string>')

print(f"New strings to add: {len(new_en_strings)}")

# Add to XML before </resources>
en_insert_pos = en_xml.rfind('</resources>')
zh_insert_pos = zh_xml.rfind('</resources>')

if new_en_strings:
    en_xml = en_xml[:en_insert_pos] + '\n    <!-- MainActivityCompose -->\n' + '\n'.join(new_en_strings) + '\n' + en_xml[en_insert_pos:]
    zh_xml = zh_xml[:zh_insert_pos] + '\n    <!-- MainActivityCompose -->\n' + '\n'.join(new_zh_strings) + '\n' + zh_xml[zh_insert_pos:]

# Replace in Kotlin code - handle simple strings first
new_kt = kt_content
for text, key in key_map.items():
    if re.search(r'\$\{[^}]*\}|\$\w', text):
        continue  # Skip interpolated for now
    
    # Find context to determine replacement pattern
    # Search for occurrences in the file
    pattern = re.compile(r'(?<!["\\])' + re.escape(f'"{text}"') + r'(?!["\\])')
    
    def replacer(match):
        # Find the line this is on
        pos = match.start()
        line_start = kt_content.rfind('\n', 0, pos) + 1
        line_end = kt_content.find('\n', pos)
        line = kt_content[line_start:line_end]
        
        # Determine context
        if 'Toast.makeText' in line or 'toast(' in line.lower():
            return f'getString(R.string.{key})'
        elif 'setTitle(' in line or 'title = ' in line:
            return f'stringResource(R.string.{key})'
        elif 'setMessage(' in line or 'text = ' in line or 'Text(' in line:
            return f'stringResource(R.string.{key})'
        elif 'setPositiveButton(' in line or 'setNegativeButton(' in line or 'contentDescription = ' in line:
            return f'stringResource(R.string.{key})'
        else:
            return f'stringResource(R.string.{key})'
    
    # For now, do a simpler replacement - just replace the string literal
    # in appropriate contexts
    new_kt = pattern.sub(f'stringResource(R.string.{key})', new_kt)

# Write outputs
with open('app/src/main/res/values/strings.xml', 'w', encoding='utf-8') as f:
    f.write(en_xml)

with open('app/src/main/res/values-zh/strings.xml', 'w', encoding='utf-8') as f:
    f.write(zh_xml)

with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt', 'w', encoding='utf-8') as f:
    f.write(new_kt)

print("Done! Files updated.")
print(f"Added {len(new_en_strings)} new strings.")
if interpolated:
    print(f"\nInterpolated strings need manual handling ({len(interpolated)}):")
    for line_num, text, _ in interpolated[:20]:
        print(f"  Line {line_num}: {text[:60]}")
