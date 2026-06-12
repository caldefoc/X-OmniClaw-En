import re

# Read key map
key_map = {}
with open('key_map.txt', 'r', encoding='utf-8') as f:
    for line in f:
        line = line.strip()
        if '=' in line:
            key, text = line.split('=', 1)
            key_map[text] = key

with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
replaced = 0
skipped = []

for line in lines:
    if not re.search(r'[\u4e00-\u9fff]', line):
        new_lines.append(line)
        continue
    
    stripped = line.strip()
    if stripped.startswith('//') or stripped.startswith('*'):
        new_lines.append(line)
        continue
    
    # Find all Chinese string literals on this line
    matches = list(re.finditer(r'"([^"]*[\u4e00-\u9fff][^"]*)"', line))
    if not matches:
        new_lines.append(line)
        continue
    
    new_line = line
    modified = False
    
    for m in matches:
        text = m.group(1)
        if text not in key_map:
            continue
        
        key = key_map[text]
        
        # Skip interpolated strings
        if re.search(r'\$\{[^}]*\}|\$\w', text):
            skipped.append((text, key))
            continue
        
        # Determine replacement based on line context
        if 'android.widget.Toast.makeText' in line:
            replacement = f'context.getString(R.string.{key})'
        elif 'Toast.makeText' in line:
            if 'activity' in line:
                replacement = f'activity.getString(R.string.{key})'
            else:
                replacement = f'context.getString(R.string.{key})'
        elif '.setTitle(' in line or '.setMessage(' in line or '.setPositiveButton(' in line or '.setNegativeButton(' in line:
            replacement = f'getString(R.string.{key})'
        elif 'contentDescription = ' in line:
            replacement = f'stringResource(R.string.{key})'
        elif re.search(r'Text\s*\(\s*"', line) or 'text = ' in line or 'title = ' in line or 'label = ' in line or 'supportingText = ' in line:
            # Check if it's inside a @Composable by looking at the function signature above
            replacement = f'stringResource(R.string.{key})'
        elif 'Log.' in line:
            # Keep logs as-is for now
            continue
        else:
            replacement = f'getString(R.string.{key})'
        
        # Only replace exact occurrences of the quoted string
        old = f'"{text}"'
        new_line = new_line.replace(old, replacement, 1)
        modified = True
        replaced += 1
    
    new_lines.append(new_line)

with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print(f"Replaced {replaced} occurrences")
print(f"Skipped {len(skipped)} interpolated strings")
