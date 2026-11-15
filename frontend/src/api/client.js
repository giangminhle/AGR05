// Lớp gọi REST API dùng chung. Không chứa nghiệp vụ. Lỗi API được ném ra dạng LoiApi { loi, truong, ma }.

export class LoiApi extends Error {
  constructor(loi, truong, ma) {
    super(loi);
    this.loi = loi;
    this.truong = truong;
    this.ma = ma;
  }
}

let xuLyHetPhien = null;

/** App đăng ký hàm này để quay về trang đăng nhập khi API trả 401. */
export function datXuLyHetPhien(fn) {
  xuLyHetPhien = fn;
}

export async function goiApi(duongDan, { method = 'GET', json, formData } = {}) {
  const tuyChon = { method, credentials: 'same-origin', headers: {} };
  if (json !== undefined) {
    tuyChon.headers['Content-Type'] = 'application/json';
    tuyChon.body = JSON.stringify(json);
  } else if (formData) {
    tuyChon.body = formData;
  }
  let res;
  try {
    res = await fetch('/api' + duongDan, tuyChon);
  } catch {
    throw new LoiApi('Không kết nối được máy chủ.', null, 0);
  }
  if (res.status === 204) return null;
  let du = null;
  const kieu = res.headers.get('content-type') || '';
  if (kieu.includes('application/json')) {
    du = await res.json();
  }
  if (!res.ok) {
    // 401 khi đang dùng nghĩa là hết phiên; riêng các API kiểm tra phiên, đăng nhập, đăng ký thì không chuyển trang
    const khongChuyen = ['/auth/toi', '/auth/dang-nhap', '/auth/dang-ky'].includes(duongDan);
    if (res.status === 401 && xuLyHetPhien && !khongChuyen) xuLyHetPhien();
    throw new LoiApi(du?.loi || `Lỗi ${res.status}`, du?.truong || null, res.status);
  }
  return du;
}
