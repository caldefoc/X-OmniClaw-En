import re
with open('app/src/main/java/com/shijing/xomniclaw/ui/compose/ChatScreen.kt','r',encoding='utf-8') as f:
    content = f.read()
refs = re.findall(r'R\.string\.(\w+)', content)
with open('app/src/main/res/values/strings.xml','r',encoding='utf-8') as f:
    strings = f.read()
missing = [r for r in set(refs) if f'name="{r}"' not in strings]
print('Missing string resources in ChatScreen:', missing)
