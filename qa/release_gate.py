#!/usr/bin/env python3
from pathlib import Path
import xml.etree.ElementTree as ET
import re, sys

root = Path(__file__).resolve().parents[1]
res = root / 'app/src/main/res'
errors = []

# Existing Android resource localization sets must stay internally consistent.
sets = ['values', 'values-en', 'values-tr', 'values-es', 'values-de']
keys = {}
for folder in sets:
    path = res / folder / 'strings.xml'
    try:
        tree = ET.parse(path)
        keys[folder] = {n.attrib['name'] for n in tree.getroot().findall('string')}
    except Exception as e:
        errors.append(f'{folder}: XML parse failed: {e}')
base = keys.get('values', set())
for folder in sets[1:]:
    missing = sorted(base - keys.get(folder, set()))
    extra = sorted(keys.get(folder, set()) - base)
    if missing: errors.append(f'{folder}: missing keys {missing}')
    if extra: errors.append(f'{folder}: extra keys {extra}')

manifest = (root / 'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
required_manifest = [
    'android:supportsRtl="true"',
    'android:allowBackup="false"',
    'android:usesCleartextTraffic="false"',
    'android:label="Yasli kurt"',
    'android:name=".PageActivity"'
]
for required in required_manifest:
    if required not in manifest: errors.append(f'Manifest: missing {required}')
if 'android.permission.INTERNET' in manifest: errors.append('Manifest: INTERNET permission must remain absent')
if 'ACCESS_FINE_LOCATION' in manifest and 'ACCESS_COARSE_LOCATION' not in manifest: errors.append('Manifest: FINE location requires COARSE location')

java_files = list((root/'app/src/main/java').rglob('*.java'))
java = '\n'.join(p.read_text(encoding='utf-8') for p in java_files)
for bad in ['Gravity.LEFT', 'Gravity.RIGHT', 'TEXT_ALIGNMENT_TEXT_START']:
    if bad in java: errors.append(f'RTL: forbidden directional primitive {bad}')

# 10-language requirement including three RTL languages.
ui_text = (root/'app/src/main/java/com/nexvary/emergencymesh/UiText.java').read_text(encoding='utf-8')
for code in ['ar','en','tr','es','de','it','fr','ur','fa','ru']:
    if f'put("{code}"' not in ui_text: errors.append(f'Localization: missing language {code}')
main = (root/'app/src/main/java/com/nexvary/emergencymesh/MainActivity.java').read_text(encoding='utf-8')
for code in ['ar','en','tr','es','de','it','fr','ur','fa','ru']:
    if f'"{code}"' not in main: errors.append(f'Language selector: missing {code}')
for rtl in ['"ar".equals(lang)','"ur".equals(lang)','"fa".equals(lang)']:
    if rtl not in main: errors.append(f'RTL selector: missing {rtl}')

nav = (root/'app/src/main/java/com/nexvary/emergencymesh/NavigationRegistry.java').read_text(encoding='utf-8')
page = (root/'app/src/main/java/com/nexvary/emergencymesh/PageActivity.java').read_text(encoding='utf-8')
for route in ['messages','radar','map','diagnostics','radio','profile','channels','settings','about']:
    if f'"{route}"' not in nav: errors.append(f'Navigation registry: missing route {route}')
    if f'nav:"+target' not in page and 'nav:' not in page: errors.append('Page navigation tags missing')
if 'back.setOnClickListener(v->finish())' not in page: errors.append('Back button is not wired to finish()')
if 'onBackPressed()' not in main or 'AlertDialog.Builder' not in main: errors.append('Home exit confirmation missing')

build = (root / 'app/build.gradle').read_text(encoding='utf-8')
if "versionCode 1100" not in build or "versionName '2.2.0-stage1100'" not in build:
    errors.append('Build: stage 1100 version metadata not set')
if 'AndroidJUnitRunner' not in build or 'espresso-core' not in build:
    errors.append('Build: instrumentation navigation gate dependencies missing')

# Basic secret / unsafe web surface scan.
secret_patterns = [r'AIza[0-9A-Za-z_-]{20,}', r'ghp_[0-9A-Za-z]{20,}', r'-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----']
for p in list(root.rglob('*.java')) + list(root.rglob('*.xml')) + list(root.rglob('*.gradle')):
    text = p.read_text(encoding='utf-8', errors='ignore')
    for pat in secret_patterns:
        if re.search(pat, text): errors.append(f'Security: possible embedded secret in {p.relative_to(root)}')
if 'setJavaScriptEnabled(true)' in java: errors.append('Security: unrestricted WebView JavaScript detected')

if errors:
    print('UI RELEASE GATE FAILED')
    for e in errors: print(' -', e)
    sys.exit(1)
print('UI RELEASE GATE PASS')
print('Resource languages:', ', '.join(sets), 'keys:', len(base))
print('Runtime UI languages: ar, en, tr, es, de, it, fr, ur, fa, ru')
print('Navigation + RTL + security baseline + offline policy: PASS')
