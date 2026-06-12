with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt', 'r', encoding='utf-8') as f:
    content = f.read()

replacements = {
    'stringResource(R.string.mac_cn69b0)': '"Disabled"',
    'stringResource(R.string.mac_cn71dc)': '"Not configured"',
    'stringResource(R.string.mac_cn1622)': '"Unknown"',
    'stringResource(R.string.mac_cnebc1)': '"One-time"',
    'stringResource(R.string.mac_cn7862)': '"Daily"',
    'stringResource(R.string.mac_cn09b0)': '"Weekly"',
    'stringResource(R.string.mac_cnc7ea)': '"Workdays"',
    'stringResource(R.string.mac_cnebda)': '"Fixed interval"',
    'stringResource(R.string.mac_cnf765)': '"Preparing"',
    'stringResource(R.string.mac_cn0615_1)': '"Scanning"',
    'stringResource(R.string.mac_cn43b7)': '"Generating memory"',
    'stringResource(R.string.mac_cn1984)': '"Writing"',
    'stringResource(R.string.mac_cnfad5)': '"Completed"',
    'stringResource(R.string.mac_cnacd5)': '"Failed"',
    'stringResource(R.string.mac_cn39bc)': '"Standby"',
}

for old, new in replacements.items():
    content = content.replace(old, new)

with open('app/src/main/java/com/shijing/xomniclaw/ui/activity/MainActivityCompose.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print('Fixed helper functions')
