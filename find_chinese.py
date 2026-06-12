import re
with open('app/src/main/java/com/shijing/xomniclaw/ui/compose/ChatScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()
for i, line in enumerate(lines, 1):
    if re.search(r'[\u4e00-\u9fff]', line):
        stripped = line.strip()
        if not stripped.startswith('//') and not stripped.startswith('*'):
            print(f'{i}: {line.rstrip()}')
