import re
import hashlib

def sanitize_key(text):
    t = text.strip()
    t = re.sub(r'\$\{[^}]+\}', '_var_', t)
    t = re.sub(r'\$\w+', '_var_', t)
    t = re.sub(r'[^\w\s\u4e00-\u9fff]', ' ', t)
    words = t.split()
    key_words = []
    for w in words[:5]:
        w = w.strip()
        if not w:
            continue
        if re.match(r'[\u4e00-\u9fff]', w):
            key_words.append('cn' + hashlib.md5(w.encode()).hexdigest()[:4])
        else:
            key_words.append(w.lower())
    if not key_words:
        return 'str_' + hashlib.md5(text.encode()).hexdigest()[:6]
    return '_'.join(key_words)[:50]

def key_exists(key, xml_content):
    return f'name="{key}"' in xml_content

with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt', 'r', encoding='utf-8') as f:
    kt_lines = f.readlines()

with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    en_xml = f.read()

with open('app/src/main/res/values-zh/strings.xml', 'r', encoding='utf-8') as f:
    zh_xml = f.read()

chinese_pattern = re.compile(r'"([^"]*[\u4e00-\u9fff][^"]*)"')

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
            entries.append((i, text))

new_en = []
new_zh = []
key_map = {}

for line_num, text in entries:
    escaped = re.escape(text)
    if re.search(f'<string[^>]*>.*{escaped}.*</string>', en_xml):
        m = re.search(f'<string name="([^"]+)">.*{escaped}.*</string>', en_xml)
        if m:
            key_map[text] = m.group(1)
            continue
    
    base_key = 'mac_' + sanitize_key(text)
    key = base_key
    counter = 1
    while key_exists(key, en_xml) or key in key_map.values():
        key = f"{base_key}_{counter}"
        counter += 1
    key_map[text] = key
    
    xml_text = text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
    new_en.append(f'    <string name="{key}">{xml_text}</string>')
    new_zh.append(f'    <string name="{key}">{xml_text}</string>')

en_insert = en_xml.rfind('</resources>')
zh_insert = zh_xml.rfind('</resources>')

if new_en:
    en_xml = en_xml[:en_insert] + '\n    <!-- MainActivityCompose -->\n' + '\n'.join(new_en) + '\n' + en_xml[en_insert:]
    zh_xml = zh_xml[:zh_insert] + '\n    <!-- MainActivityCompose -->\n' + '\n'.join(new_zh) + '\n' + zh_xml[zh_insert:]

with open('app/src/main/res/values/strings.xml', 'w', encoding='utf-8') as f:
    f.write(en_xml)
with open('app/src/main/res/values-zh/strings.xml', 'w', encoding='utf-8') as f:
    f.write(zh_xml)

# Save key map for manual replacement
with open('key_map.txt', 'w', encoding='utf-8') as f:
    for text, key in key_map.items():
        f.write(f'{key}={text}\n')

print(f"Added {len(new_en)} new strings. Key map saved to key_map.txt")
