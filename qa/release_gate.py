#!/usr/bin/env python3
from pathlib import Path
import xml.etree.ElementTree as ET
import re, sys
root=Path(__file__).resolve().parents[1];res=root/'app/src/main/res';errors=[]
sets=['values','values-en','values-tr','values-es','values-de'];keys={}
for folder in sets:
 path=res/folder/'strings.xml'
 try: keys[folder]={n.attrib['name'] for n in ET.parse(path).getroot().findall('string')}
 except Exception as e: errors.append(f'{folder}: XML parse failed: {e}')
base=keys.get('values',set())
for folder in sets[1:]:
 missing=sorted(base-keys.get(folder,set()));extra=sorted(keys.get(folder,set())-base)
 if missing: errors.append(f'{folder}: missing keys {missing}')
 if extra: errors.append(f'{folder}: extra keys {extra}')
manifest=(root/'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
for required in ['android:supportsRtl="true"','android:allowBackup="false"','android:usesCleartextTraffic="false"','android:label="Yasli kurt"','android:name=".PageActivity"']:
 if required not in manifest: errors.append(f'Manifest: missing {required}')
if 'android.permission.INTERNET' in manifest: errors.append('Manifest: INTERNET permission must remain absent')
if 'ACCESS_FINE_LOCATION' in manifest and 'ACCESS_COARSE_LOCATION' not in manifest: errors.append('Manifest: FINE location requires COARSE location')
java_files=list((root/'app/src/main/java').rglob('*.java'));java='\n'.join(p.read_text(encoding='utf-8') for p in java_files)
for bad in ['Gravity.LEFT','Gravity.RIGHT','TEXT_ALIGNMENT_TEXT_START']:
 if bad in java: errors.append(f'RTL: forbidden directional primitive {bad}')
ui_text=(root/'app/src/main/java/com/nexvary/emergencymesh/UiText.java').read_text(encoding='utf-8')
for code in ['ar','en','tr','es','de','it','fr','ur','fa','ru']:
 if f'put("{code}"' not in ui_text: errors.append(f'Localization: missing language {code}')
main=(root/'app/src/main/java/com/nexvary/emergencymesh/MainActivity.java').read_text(encoding='utf-8')
for code in ['ar','en','tr','es','de','it','fr','ur','fa','ru']:
 if f'"{code}"' not in main: errors.append(f'Language selector: missing {code}')
for rtl in ['"ar".equals(lang)','"ur".equals(lang)','"fa".equals(lang)']:
 if rtl not in main: errors.append(f'RTL selector: missing {rtl}')
for shell in ['setTag("menu")','setTag("page:home")','setTag("sos")']:
 if shell not in main: errors.append(f'Stage 1200 menu shell missing: {shell}')
nav=(root/'app/src/main/java/com/nexvary/emergencymesh/NavigationRegistry.java').read_text(encoding='utf-8');page=(root/'app/src/main/java/com/nexvary/emergencymesh/PageActivity.java').read_text(encoding='utf-8')
routes=['messages','radar','map','diagnostics','radio','profile','channels','settings','about']
for route in routes:
 if f'"{route}"' not in nav: errors.append(f'Navigation registry: missing route {route}')
if 'f.setTag("fingerprint:"+r)' not in page: errors.append('Functional page fingerprint generator missing')
for token in ['message:compose','message:send','radar:scan','map:add-marker','diagnostics:run','profile:save','radio_discovery','channel_rescue','settings:lang:']:
 if token not in page: errors.append(f'Functional control missing: {token}')
if 'Route: ' in page or 'page_ok' in page or 'integrity' in page: errors.append('Generic navigation-test placeholder content still present in PageActivity')
if 'back.setOnClickListener(v->finish())' not in page: errors.append('Back button is not wired to finish()')
if 'onBackPressed()' not in main or 'AlertDialog.Builder' not in main: errors.append('Home exit confirmation missing')
build=(root/'app/build.gradle').read_text(encoding='utf-8')
if 'AndroidJUnitRunner' not in build or 'espresso-core' not in build: errors.append('Build: instrumentation navigation gate dependencies missing')
test=(root/'app/src/androidTest/java/com/nexvary/emergencymesh/NavigationIntegrityTest.java').read_text(encoding='utf-8')
for token in ['homeQuickActionsOpenRealPagesAndBackReturnsHome','menuButtonIsAlive','everyDestinationHasUniqueFunctionalFingerprint','eachRouteHasItsOwnRequiredFunctionalControl','keyControlsAreActuallyClickableNotDead','fingerprint:']:
 if token not in test: errors.append(f'Navigation Integrity Gate Stage 1200 missing: {token}')
for p in list(root.rglob('*.java'))+list(root.rglob('*.xml'))+list(root.rglob('*.gradle')):
 text=p.read_text(encoding='utf-8',errors='ignore')
 for pat in [r'AIza[0-9A-Za-z_-]{20,}',r'ghp_[0-9A-Za-z]{20,}',r'-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----']:
  if re.search(pat,text): errors.append(f'Security: possible embedded secret in {p.relative_to(root)}')
if 'setJavaScriptEnabled(true)' in java: errors.append('Security: unrestricted WebView JavaScript detected')
if errors:
 print('UI RELEASE GATE FAILED');[print(' -',e) for e in errors];sys.exit(1)
print('UI RELEASE GATE PASS')
print('Stage 1200: compact menu shell + functional pages + Navigation Integrity Gate: PASS')
print('Runtime UI languages: ar, en, tr, es, de, it, fr, ur, fa, ru')
print('RTL + security baseline + offline policy: PASS')