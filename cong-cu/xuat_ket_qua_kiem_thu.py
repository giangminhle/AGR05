# -*- coding: utf-8 -*-
"""
Xuất bảng kết quả kiểm thử TC01–TC16 cho mục 7.2 của báo cáo từ kết quả JUnit thật.
Cách dùng:  cd backend && mvn verify      (tạo target/surefire-reports)
            python cong-cu/xuat_ket_qua_kiem_thu.py
Kết quả ghi vào docs/ket-qua-kiem-thu.md.
"""
import glob
import sys
import os
import re
import xml.etree.ElementTree as ET
from datetime import datetime

sys.stdout.reconfigure(encoding='utf-8')

GOC = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BAO_CAO = os.path.join(GOC, 'backend', 'target', 'surefire-reports')
TEST_CASES = os.path.join(GOC, 'docs', 'test-cases.md')
RA = os.path.join(GOC, 'docs', 'ket-qua-kiem-thu.md')

# 1. Đọc mô tả TC từ docs/test-cases.md
tc = {}
for dong in open(TEST_CASES, encoding='utf-8'):
    m = re.match(r'\|\s*(TC\d\d)\s*\|\s*(.*?)\s*\|\s*(.*?)\s*\|\s*(.*?)\s*\|', dong)
    if m:
        tc[m.group(1)] = {'uc': m.group(2), 'du_lieu': m.group(3), 'mong_doi': m.group(4), 'test': []}

# 2. Đọc kết quả JUnit
tong = dat = 0
khac = []
files = glob.glob(os.path.join(BAO_CAO, 'TEST-*.xml'))
if not files:
    raise SystemExit('Chưa có kết quả test. Chạy "cd backend && mvn verify" trước.')
for f in files:
    for case in ET.parse(f).getroot().iter('testcase'):
        ten = case.get('name')
        lop = case.get('classname').split('.')[-1]
        loi = case.find('failure') if case.find('failure') is not None else case.find('error')
        bo_qua = case.find('skipped') is not None
        ket_qua = 'Bỏ qua' if bo_qua else ('Không đạt' if loi is not None else 'Đạt')
        tong += 1
        dat += ket_qua == 'Đạt'
        m = re.match(r'(TC\d\d)_', ten)
        muc = (f'{lop}.{ten}', ket_qua, (loi.get('message') or '')[:200] if loi is not None else '')
        if m and m.group(1) in tc:
            tc[m.group(1)]['test'].append(muc)
        else:
            khac.append(muc)

# 3. Ghi bảng
so_tich_hop = sum(1 for ten, _, _ in khac if ten.startswith('DalTichHopTest.'))
dong = [
    '# Kết quả kiểm thử (mục 7.2)',
    '',
    f'Chạy lúc {datetime.now():%H:%M %d/%m/%Y} bằng JUnit 5 (`mvn verify`). Tổng {tong} test, đạt {dat}.',
    'Các test chức năng dùng DAL giả trong bộ nhớ, không cần SQL Server. '
    + (f'Lần chạy này có cả {so_tich_hop} test tích hợp với SQL Server thật (`DalTichHopTest`).' if so_tich_hop
       else 'Lần chạy này chưa gồm test tích hợp với CSDL thật (chạy bằng `mvn verify -Pintegration`).'),
    '',
    '## Bảng test case',
    '',
    '| Mã | UC | Dữ liệu / thao tác | Kết quả mong đợi | Kết quả thực tế | Test JUnit |',
    '|---|---|---|---|---|---|',
]
for ma in sorted(tc):
    t = tc[ma]
    if not t['test']:
        kq = 'Chưa có test'
    elif all(x[1] == 'Đạt' for x in t['test']):
        kq = 'Đạt'
    else:
        kq = 'Không đạt: ' + '; '.join(x[2] for x in t['test'] if x[1] != 'Đạt')
    ten_test = '<br>'.join(f'`{x[0]}`' for x in sorted(t['test']))
    dong.append(f"| {ma} | {t['uc']} | {t['du_lieu']} | {t['mong_doi']} | {kq} | {ten_test} |")

dong += ['', '## Các test khác (quy tắc trên entity, quyền, chức năng nền, kiến trúc)', '',
         '| Test | Kết quả |', '|---|---|']
for ten, kq, _ in sorted(khac):
    dong.append(f'| `{ten}` | {kq} |')

open(RA, 'w', encoding='utf-8', newline='\n').write('\n'.join(dong) + '\n')
print(f'Đã ghi {RA}: {tong} test, đạt {dat}.')
