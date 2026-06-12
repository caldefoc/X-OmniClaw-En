import re
with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt','r',encoding='utf-8') as f:
    content = f.read()
# Find all Chinese string literals
chinese_strings = re.findall(r'"([^"]*[\u4e00-\u9fff][^"]*)"', content)
with open('app/src/main/res/values/strings.xml','r',encoding='utf-8') as f:
    strings = f.read()
print(f'Total Chinese strings in MainActivityCompose: {len(chinese_strings)}')
missing = []
for s in set(chinese_strings):
    # Simple check - see if the exact text exists in strings.xml
    if s not in strings:
        missing.append(s)
print(f'Missing from strings.xml: {len(missing)}')
for s in missing[:30]:
    print(f'  - {s}')
