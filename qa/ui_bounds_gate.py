#!/usr/bin/env python3
from pathlib import Path
import re, sys, xml.etree.ElementTree as ET

xml_path = Path(sys.argv[1]) if len(sys.argv) > 1 else Path('ci-artifacts/ui.xml')
if not xml_path.exists():
    print('UI BOUNDS GATE FAILED: missing', xml_path)
    sys.exit(1)

root = ET.parse(xml_path).getroot()
rx = re.compile(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]')
nodes=[]
for n in root.iter('node'):
    if n.attrib.get('package','') != 'com.nexvary.emergencymesh.debug':
        continue
    m=rx.fullmatch(n.attrib.get('bounds',''))
    if not m: continue
    x1,y1,x2,y2=map(int,m.groups())
    if x2<=x1 or y2<=y1:
        print('UI BOUNDS GATE FAILED: zero-sized node', n.attrib)
        sys.exit(1)
    if n.attrib.get('clickable')=='true':
        label=n.attrib.get('content-desc') or n.attrib.get('text') or n.attrib.get('resource-id') or 'unnamed'
        nodes.append((label,(x1,y1,x2,y2)))

if not nodes:
    print('UI BOUNDS GATE FAILED: no app clickable nodes')
    sys.exit(1)

# Severe overlap check for distinct clickable controls.
def overlap_ratio(a,b):
    ax1,ay1,ax2,ay2=a; bx1,by1,bx2,by2=b
    iw=max(0,min(ax2,bx2)-max(ax1,bx1)); ih=max(0,min(ay2,by2)-max(ay1,by1))
    inter=iw*ih
    if inter==0:return 0.0
    smaller=min((ax2-ax1)*(ay2-ay1),(bx2-bx1)*(by2-by1))
    return inter/smaller

bad=[]
for i in range(len(nodes)):
    for j in range(i+1,len(nodes)):
        if nodes[i][0]==nodes[j][0] and nodes[i][1]==nodes[j][1]: continue
        if overlap_ratio(nodes[i][1],nodes[j][1])>0.80:
            bad.append((nodes[i],nodes[j]))
if bad:
    print('UI BOUNDS GATE FAILED: overlapping clickable controls')
    for x in bad[:10]: print(' -',x)
    sys.exit(1)
print('UI BOUNDS GATE PASS:',len(nodes),'clickable app controls, no severe overlaps')
