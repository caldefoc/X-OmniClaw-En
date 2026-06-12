import re
import os

# Read the source file
with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Read existing strings.xml
with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    strings_en = f.read()

with open('app/src/main/res/values-zh/strings.xml', 'r', encoding='utf-8') as f:
    strings_zh = f.read()

# Find all Chinese strings that are NOT in comments
chinese_strings = []
for i, line in enumerate(lines, 1):
    if re.search(r'[\u4e00-\u9fff]', line):
        stripped = line.strip()
        if stripped.startswith('//') or stripped.startswith('*'):
            continue
        # Extract string literals with Chinese
        matches = re.findall(r'"([^"]*[\u4e00-\u9fff][^"]*)"', line)
        for m in matches:
            chinese_strings.append((i, m, line))

# Deduplicate while preserving order
seen = set()
unique_strings = []
for line_num, text, full_line in chinese_strings:
    if text not in seen:
        seen.add(text)
        unique_strings.append((line_num, text, full_line))

print(f"Found {len(unique_strings)} unique Chinese strings")
for i, (line_num, text, full_line) in enumerate(unique_strings[:50]):
    print(f"{i+1}. Line {line_num}: {text[:80]}")
