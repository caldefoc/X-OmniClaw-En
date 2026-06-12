import re

# Load key map
key_map = {}
with open('key_map.txt', 'r', encoding='utf-8') as f:
    for line in f:
        line = line.strip()
        if '=' in line:
            key, text = line.split('=', 1)
            key_map[text] = key

with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Sort by length descending to avoid partial replacements
sorted_items = sorted(key_map.items(), key=lambda x: len(x[0]), reverse=True)

new_content = content
replaced_count = 0
skipped = []

for text, key in sorted_items:
    # Skip interpolated strings for now
    if re.search(r'\$\{[^}]*\}|\$\w', text):
        skipped.append((text, key))
        continue
    
    escaped = re.escape(text)
    pattern = re.compile(r'(?<!["\\])' + escaped + r'(?!["\\])')
    
    def replacer(m):
        pos = m.start()
        line_start = new_content.rfind('\n', 0, pos) + 1
        line_end = new_content.find('\n', pos)
        line = new_content[line_start:line_end]
        
        # Determine context
        if 'android.widget.Toast.makeText' in line:
            return f'context.getString(R.string.{key})'
        elif 'Toast.makeText' in line:
            # Check if context is available or if we need getString
            if 'this' in line or 'activity' in line:
                return f'getString(R.string.{key})'
            return f'context.getString(R.string.{key})'
        elif '.setTitle(' in line or '.setMessage(' in line or '.setPositiveButton(' in line or '.setNegativeButton(' in line:
            return f'getString(R.string.{key})'
        elif 'contentDescription = ' in line:
            return f'stringResource(R.string.{key})'
        elif 'Text(' in line or 'text = ' in line or 'title = ' in line or 'label = ' in line or 'supportingText = ' in line:
            return f'stringResource(R.string.{key})'
        else:
            # Default: try stringResource for Compose, getString for others
            # Check if we're inside a @Composable function by looking backwards
            prev = new_content[max(0, pos-500):pos]
            if '@Composable' in prev and 'fun ' in prev:
                return f'stringResource(R.string.{key})'
            return f'getString(R.string.{key})'
    
    # Replace all occurrences
    new_content, count = pattern.subn(replacer, new_content)
    if count > 0:
        replaced_count += count

with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

print(f"Replaced {replaced_count} occurrences")
print(f"Skipped {len(skipped)} interpolated strings")
