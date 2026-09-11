#!/usr/bin/env python3
from pathlib import Path
import xml.etree.ElementTree as ET
import re, sys

root = Path(__file__).resolve().parents[1]
res = root / 'app/src/main/res'
sets = ['values', 'values-en', 'values-tr', 'values-es', 'values-de']
errors = []
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
if 'android:supportsRtl="true"' not in manifest:
    errors.append('Manifest: supportsRtl=true missing')
if 'android.permission.INTERNET' in manifest:
    errors.append('Manifest: INTERNET permission must remain absent')
if 'ACCESS_FINE_LOCATION' in manifest and 'ACCESS_COARSE_LOCATION' not in manifest:
    errors.append('Manifest: FINE location requires COARSE location')

java = '\n'.join(p.read_text(encoding='utf-8') for p in (root/'app/src/main/java').rglob('*.java'))
for bad in ['Gravity.LEFT', 'Gravity.RIGHT', 'setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START)']:
    if bad in java:
        errors.append(f'RTL: forbidden directional primitive {bad}')

build = (root / 'app/build.gradle').read_text(encoding='utf-8')
if "versionCode 1050" not in build:
    errors.append('Build: stage 1050 versionCode not set')

if errors:
    print('RELEASE GATE FAILED')
    for e in errors: print(' -', e)
    sys.exit(1)
print('RELEASE GATE PASS')
print('languages:', ', '.join(sets), 'keys:', len(base))
print('RTL + manifest + offline policy: PASS')
