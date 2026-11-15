# -*- coding: utf-8 -*-
"""
Kiểm thử đầu-cuối qua API trên CSDL thật (khoảng 47 tình huống: phân quyền, gửi/sửa/gửi lại, duyệt/từ chối,
BR02, BR04, BR05, BR06, AI, quản trị). Cần: dữ liệu mẫu vừa nạp (profile demo) và server đang chạy.
CHÚ Ý: script THAY ĐỔI dữ liệu (dùng hết tình huống demo). Chạy xong thì làm mới bằng db/lam_moi_du_lieu_mau.sql
rồi nạp lại dữ liệu mẫu.
Cách dùng:  python cong-cu/kiem_thu_api.py [http://localhost:8081]
"""
import os, sys, time, requests
sys.stdout.reconfigure(encoding='utf-8')
B = (sys.argv[1] if len(sys.argv) > 1 else 'http://localhost:8081').rstrip('/') + '/api'
MK = os.environ.get('AGR05_MAT_KHAU_MAU') or sys.exit('Đặt biến môi trường AGR05_MAT_KHAU_MAU = mật khẩu tài khoản mẫu')
ANH = open(os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'backend', 'src', 'main', 'resources', 'anh-mau', 'cuon-la-1.jpg'), 'rb').read()
loi = []

def kt(ten, dk, chi_tiet=''):
    print(('  OK  ' if dk else '  LOI ') + ten + ('' if dk else f'  -> {chi_tiet}'))
    if not dk:
        loi.append(ten)

def dn(u, mk=MK):
    s = requests.Session()
    r = s.post(B + '/auth/dang-nhap', json={'tenDangNhap': u, 'matKhau': mk})
    assert r.status_code == 200, (u, r.status_code, r.text)
    return s

def kv(s, ten):
    return next(k['maKhuVuc'] for k in s.get(B + '/khu-vuc').json() if k['tenKhuVuc'].startswith(ten))

def loai(s, ten):
    return next(l['maLoai'] for l in s.get(B + '/loai-sau-benh').json() if l['tenLoai'] == ten)

def gui(s, maKV, tieu_de='Thử nghiệm sâu cuốn lá', anh=True, **them):
    data = {'tieuDe': tieu_de, 'tenCayTrong': 'Lúa', 'moTa': 'Mô tả thử', 'mucDo': 2, 'viDo': '10.03',
            'kinhDo': '105.56', 'maKhuVuc': maKV, 'ngayPhatHien': time.strftime('%Y-%m-%d')}
    data.update(them)
    files = [('anh', ('a.jpg', ANH, 'image/jpeg'))] if anh else None
    return s.post(B + '/bao-cao', data=data, files=files)

print('== Xác thực, phân quyền')
kt('Chưa đăng nhập gọi API -> 401', requests.get(B + '/bao-cao/cua-toi').status_code == 401)
r = requests.post(B + '/auth/dang-nhap', json={'tenDangNhap': 'nd.vanut', 'matKhau': 'sai'})
r2 = requests.post(B + '/auth/dang-nhap', json={'tenDangNhap': 'khongco', 'matKhau': 'sai'})
kt('Sai mật khẩu và sai tên cùng một thông báo', r.status_code == r2.status_code and r.json() == r2.json(), (r.text, r2.text))
nd = dn('nd.vanut'); cg = dn('cg.ngocanh'); cg2 = dn('cg.thanhtam'); qt = dn('qt.minhkhoa')
kt('Nông dân gọi API quản trị -> 403', nd.get(B + '/nguoi-dung').status_code == 403)
kt('Nông dân gọi API xác minh -> 403', nd.get(B + '/xac-minh/cho').status_code == 403)
kt('Chuyên gia gọi API tham số -> 403', cg.put(B + '/tham-so/NGUONG_NGUY_CO', json={'giaTri': 50}).status_code == 403)
XT = kv(nd, 'Xã Xuân Thắng')

print('== Gửi báo cáo')
r = nd.post(B + '/bao-cao', data={'tieuDe': 'x', 'tenCayTrong': 'Lúa', 'moTa': 'm', 'mucDo': 1, 'viDo': '10', 'kinhDo': '105',
            'maKhuVuc': XT, 'ngayPhatHien': time.strftime('%Y-%m-%d')}, files={'_': ('', b'', 'application/octet-stream')})
kt('Không có ảnh -> 400 "ít nhất 1 ảnh"', r.status_code == 400 and 'ảnh' in r.text, r.text)
kt('Gửi form thường cho API multipart -> 415 (không phải 500)', gui(nd, XT, anh=False).status_code == 415)
kt('Vĩ độ 95 -> 400', gui(nd, XT, viDo='95').status_code == 400)
kt('Mức độ 7 -> 400', gui(nd, XT, mucDo=7).status_code == 400)
kt('Tiêu đề rỗng -> 400', gui(nd, XT, tieuDe='').status_code == 400)
r = nd.post(B + '/bao-cao', data={'tieuDe': 'x', 'tenCayTrong': 'Lúa', 'mucDo': 1, 'viDo': '10', 'kinhDo': '105', 'maKhuVuc': XT},
            files=[('anh', ('a.txt', b'khong phai anh', 'text/plain'))])
kt('File không phải ảnh -> 400', r.status_code == 400, r.text)
r = nd.post(B + '/bao-cao', data={'tieuDe': 'x', 'tenCayTrong': 'Lúa', 'mucDo': 1, 'viDo': '10', 'kinhDo': '105', 'maKhuVuc': XT},
            files=[('anh', ('gia.jpg', b'GIF89a....', 'image/jpeg'))])
kt('File đổi đuôi .jpg nhưng không phải JPG -> 400', r.status_code == 400, r.text)
r = gui(nd, XT, ngayPhatHien='2099-01-01')
kt('Ngày phát hiện ở tương lai -> 400', r.status_code == 400, f'{r.status_code} {r.text}')
r = gui(nd, XT)
kt('Gửi hợp lệ -> 200', r.status_code == 200, r.text)
ma = r.json().get('maBaoCao') if r.status_code == 200 else None

print('== Sửa, quyền xem')
nd2 = dn('nd.vanbay')
kt('Nông dân khác xem để sửa -> 403', nd2.get(B + f'/bao-cao/{ma}/de-sua').status_code == 403)
kt('Chuyên gia không phụ trách xem -> 403', cg2.get(B + f'/xac-minh/{ma}').status_code == 403)
r = nd.put(B + f'/bao-cao/{ma}', data={'tieuDe': 'Đã sửa tiêu đề', 'tenCayTrong': 'Lúa', 'moTa': 'm', 'mucDo': 2,
                                       'viDo': '10.03', 'kinhDo': '105.56', 'maKhuVuc': XT, 'ngayPhatHien': time.strftime('%Y-%m-%d')},
           files={'_': ('', b'', 'application/octet-stream')})
kt('Sửa khi Chờ xác minh -> 200', r.status_code == 200, r.text)

print('== Xác minh, từ chối, gửi lại (BR03, BR04, BR05)')
kt('Chuyên gia không phụ trách duyệt -> 403',
   cg2.post(B + f'/xac-minh/{ma}/duyet', json={'maLoai': loai(cg, 'Sâu cuốn lá nhỏ')}).status_code == 403)
kt('Duyệt thiếu loại -> 400', cg.post(B + f'/xac-minh/{ma}/duyet', json={}).status_code == 400)
kt('Từ chối thiếu lý do -> 400', cg.post(B + f'/xac-minh/{ma}/tu-choi', json={'lyDo': ' '}).status_code == 400)
kt('Từ chối có lý do -> 200', cg.post(B + f'/xac-minh/{ma}/tu-choi', json={'lyDo': 'Ảnh mờ'}).status_code == 200)
kt('Chuyên gia duyệt báo cáo đã bị bác bỏ -> 409',
   cg.post(B + f'/xac-minh/{ma}/duyet', json={'maLoai': loai(cg, 'Sâu cuốn lá nhỏ')}).status_code == 409)
r = nd.put(B + f'/bao-cao/{ma}', data={'tieuDe': 'Gửi lại', 'tenCayTrong': 'Lúa', 'moTa': 'm', 'mucDo': 2,
                                       'viDo': '10.03', 'kinhDo': '105.56', 'maKhuVuc': XT, 'ngayPhatHien': time.strftime('%Y-%m-%d')},
           files=[('anhMoi', ('b.jpg', ANH, 'image/jpeg'))])
kt('Gửi lại lần 1 -> 200', r.status_code == 200, r.text)
ct = cg.get(B + f'/xac-minh/{ma}').json()
kt('Sau gửi lại: Chờ xác minh, số lần 1, 2 ảnh', ct['trangThai'] == 'CHO_XAC_MINH' and ct['soLanGuiLai'] == 1
   and len(ct['dsHinhAnh']) == 2, (ct['trangThai'], ct['soLanGuiLai'], len(ct['dsHinhAnh'])))
kt('Từ chối lần 2 -> 200', cg.post(B + f'/xac-minh/{ma}/tu-choi', json={'lyDo': 'Vẫn chưa rõ'}).status_code == 200)
ct = cg.get(B + f'/xac-minh/{ma}')
tt = nd.get(B + '/bao-cao/cua-toi').json()
bc = next(b for b in tt if b['maBaoCao'] == ma)
kt('Bị bác bỏ lần 2 -> tự đóng hồ sơ (Đã xử lý)', bc['trangThai'] == 'DA_XU_LY', bc['trangThai'])
kt('Không còn được sửa', not bc['duocSua'])
kt('Gửi lại lần 2 -> 409', nd.get(B + f'/bao-cao/{ma}/de-sua').status_code == 409)

print('== Duyệt, AI, đã xử lý, duyệt trùng')
r = gui(nd, XT, tieu_de='Bạc lá sau mưa'); ma2 = r.json()['maBaoCao']
r = cg.post(B + f'/xac-minh/{ma2}/duyet', json={'maLoai': loai(cg, 'Bệnh bạc lá')})
kt('Duyệt -> 200 và có dự báo AI', r.status_code == 200 and r.json().get('duBao') is not None, r.text[:200])
kt('Duyệt lần 2 -> 409', cg.post(B + f'/xac-minh/{ma2}/duyet', json={'maLoai': loai(cg, 'Bệnh bạc lá')}).status_code == 409)
kt('Đã xử lý khi không có khuyến nghị -> 400', cg.post(B + f'/xac-minh/{ma2}/da-xu-ly', json={'ghiChu': ''}).status_code == 400)
kt('Đã xử lý có ghi chú -> 200', cg.post(B + f'/xac-minh/{ma2}/da-xu-ly', json={'ghiChu': 'Phun thuốc'}).status_code == 200)
kt('Nông dân sửa báo cáo đã xử lý -> 409', nd.get(B + f'/bao-cao/{ma2}/de-sua').status_code == 409)

print('== Quản trị')
kt('NGUONG_NGUY_CO = 150 -> 400', qt.put(B + '/tham-so/NGUONG_NGUY_CO', json={'giaTri': 150}).status_code == 400)
kt('HAN_XAC_MINH_GIO = 0 -> 400', qt.put(B + '/tham-so/HAN_XAC_MINH_GIO', json={'giaTri': 0}).status_code == 400)
toi = qt.get(B + '/auth/toi').json()
kt('Tự khóa mình -> 400/409', qt.post(B + f"/nguoi-dung/{toi['maNguoiDung']}/khoa", json={'khoa': True}).status_code in (400, 409))
r = requests.post(B + '/auth/dang-ky', json={'tenDangNhap': 'nd.vanut', 'matKhau': '123456', 'hoTen': 'X'})
kt('Đăng ký trùng tên bị chặn', r.status_code in (400, 409), f'{r.status_code} {r.text}')
r = requests.post(B + '/auth/dang-ky', json={'tenDangNhap': 'ab cd', 'matKhau': '123456', 'hoTen': 'X'})
kt('Đăng ký tên có dấu cách -> 400', r.status_code == 400, f'{r.status_code} {r.text}')
r = requests.post(B + '/auth/dang-ky', json={'tenDangNhap': 'e2e_moi', 'matKhau': '12', 'hoTen': 'X'})
kt('Đăng ký mật khẩu 2 ký tự -> 400', r.status_code == 400, f'{r.status_code} {r.text}')
ma_nd = next(u['maNguoiDung'] for u in qt.get(B + '/nguoi-dung').json() if u['tenDangNhap'] == 'nd.vanut')
kt('Khóa nông dân -> 200', qt.post(B + f'/nguoi-dung/{ma_nd}/khoa', json={'khoa': True}).status_code == 200)
r = requests.post(B + '/auth/dang-nhap', json={'tenDangNhap': 'nd.vanut', 'matKhau': MK})
kt('Tài khoản bị khóa không đăng nhập được', r.status_code in (401, 403), r.status_code)
kt('Phiên cũ của tài khoản bị khóa bị chặn', nd.get(B + '/bao-cao/cua-toi').status_code in (401, 403),
   nd.get(B + '/bao-cao/cua-toi').status_code)
qt.post(B + f'/nguoi-dung/{ma_nd}/khoa', json={'khoa': False})

print('== Quá hạn (BR02), BR06')
ds = cg2.get(B + '/xac-minh/cho').json()
qh = [b for b in ds if b['tieuDe'].startswith('Lúa vàng lá')]
kt('Báo cáo gửi 3 ngày trước có cờ quá hạn', qh and qh[0]['quaHan'], qh)
tb = qt.get(B + '/thong-bao').json()
kt('Quản trị nhận thông báo quá hạn', any('quá hạn' in t['tieuDe'] for t in tb), [t['tieuDe'] for t in tb][:3])
ray = next(b for b in ds if b['tieuDe'].startswith('Rầy nâu bám dày'))
r = cg2.post(B + f"/xac-minh/{ray['maBaoCao']}/duyet", json={'maLoai': loai(cg2, 'Rầy nâu'), 'khuyenNghi': 'Rút nước, phun thuốc'})
kt('Duyệt ca Rầy nâu thứ 5 -> 200', r.status_code == 200, r.text)
tk = qt.get(B + '/thong-ke').json()
cb = tk.get('dsCanhBao') or tk.get('canhBao') or []
kt('Có cảnh báo BR06 cho Thới Thạnh', any('Thới Thạnh' in (c.get('noiDung') or '') for c in cb), list(tk.keys()))
print('\nTỔNG: ' + ('KHÔNG CÓ LỖI' if not loi else f'{len(loi)} LỖI: ' + '; '.join(loi)))
