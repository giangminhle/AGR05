// Các hàm gọi API theo bảng Phụ lục A, docs/architecture.md.
import { goiApi } from './client.js';

function boc(obj) {
  const fd = new FormData();
  Object.entries(obj).forEach(([k, v]) => {
    if (v !== null && v !== undefined && v !== '') fd.append(k, v);
  });
  return fd;
}

export const authApi = {
  dangNhap: (tenDangNhap, matKhau) => goiApi('/auth/dang-nhap', { method: 'POST', json: { tenDangNhap, matKhau } }),
  dangXuat: () => goiApi('/auth/dang-xuat', { method: 'POST' }),
  toi: () => goiApi('/auth/toi'),
};

export const baoCaoApi = {
  guiBaoCao: (thongTin, dsAnh) => {
    const fd = boc(thongTin);
    dsAnh.forEach((f) => fd.append('anh', f));
    return goiApi('/bao-cao', { method: 'POST', formData: fd });
  },
  layCuaToi: () => goiApi('/bao-cao/cua-toi'),
  layDeSua: (ma) => goiApi(`/bao-cao/${ma}/de-sua`),
  guiLai: (ma, thongTin, anhMoi, dsXoa) => {
    const fd = boc(thongTin);
    anhMoi.forEach((f) => fd.append('anhMoi', f));
    dsXoa.forEach((m) => fd.append('dsXoa', m));
    return goiApi(`/bao-cao/${ma}`, { method: 'PUT', formData: fd });
  },
};

export const urlAnh = (maHinhAnh) => `/api/anh/${maHinhAnh}`;

export const xacMinhApi = {
  layDanhSachCho: () => goiApi('/xac-minh/cho'),
  layDaXacMinh: () => goiApi('/xac-minh/da-xac-minh'),
  layChiTiet: (ma) => goiApi(`/xac-minh/${ma}`),
  duyet: (ma, maLoai, khuyenNghi) => goiApi(`/xac-minh/${ma}/duyet`, { method: 'POST', json: { maLoai, khuyenNghi } }),
  tuChoi: (ma, lyDo) => goiApi(`/xac-minh/${ma}/tu-choi`, { method: 'POST', json: { lyDo } }),
  daXuLy: (ma, ghiChu) => goiApi(`/xac-minh/${ma}/da-xu-ly`, { method: 'POST', json: { ghiChu } }),
};

export const thongBaoApi = {
  layThongBao: () => goiApi('/thong-bao'),
  demChuaDoc: () => goiApi('/thong-bao/chua-doc'),
  danhDauDaDoc: (ma) => goiApi(`/thong-bao/${ma}/da-doc`, { method: 'POST' }),
};

export const thongKeApi = {
  thongKe: ({ tu, den, maKhuVuc, maLoai }) => {
    const q = new URLSearchParams();
    if (tu) q.set('tu', tu);
    if (den) q.set('den', den);
    if (maKhuVuc) q.set('maKhuVuc', maKhuVuc);
    if (maLoai) q.set('maLoai', maLoai);
    return goiApi('/thong-ke?' + q.toString());
  },
};

export const danhMucApi = {
  layLoai: () => goiApi('/loai-sau-benh'),
  themLoai: (l) => goiApi('/loai-sau-benh', { method: 'POST', json: l }),
  suaLoai: (ma, l) => goiApi(`/loai-sau-benh/${ma}`, { method: 'PUT', json: l }),
  xoaLoai: (ma) => goiApi(`/loai-sau-benh/${ma}`, { method: 'DELETE' }),
  layKhuVuc: () => goiApi('/khu-vuc'),
  themKhuVuc: (kv) => goiApi('/khu-vuc', { method: 'POST', json: kv }),
  suaKhuVuc: (ma, kv) => goiApi(`/khu-vuc/${ma}`, { method: 'PUT', json: kv }),
  layChuyenGia: (ma) => goiApi(`/khu-vuc/${ma}/chuyen-gia`),
  phanCong: (ma, maChuyenGia) => goiApi(`/khu-vuc/${ma}/phan-cong`, { method: 'POST', json: { maChuyenGia } }),
  goPhanCong: (ma, maCG) => goiApi(`/khu-vuc/${ma}/phan-cong/${maCG}`, { method: 'DELETE' }),
};

export const nguoiDungApi = {
  layTatCa: () => goiApi('/nguoi-dung'),
  tao: (nd) => goiApi('/nguoi-dung', { method: 'POST', json: nd }),
  sua: (ma, nd) => goiApi(`/nguoi-dung/${ma}`, { method: 'PUT', json: nd }),
  khoa: (ma, khoa) => goiApi(`/nguoi-dung/${ma}/khoa`, { method: 'POST', json: { khoa } }),
};

export const thamSoApi = {
  layTatCa: () => goiApi('/tham-so'),
  capNhat: (ma, giaTri) => goiApi(`/tham-so/${ma}`, { method: 'PUT', json: { giaTri } }),
};

export const moHinhApi = {
  layDanhSach: () => goiApi('/mo-hinh-ai'),
  huanLuyen: () => goiApi('/mo-hinh-ai/huan-luyen', { method: 'POST' }),
  kichHoat: (ma) => goiApi(`/mo-hinh-ai/${ma}/kich-hoat`, { method: 'POST' }),
};
