export function ngayGio(s) {
  if (!s) return '';
  const d = new Date(s);
  return d.toLocaleString('vi-VN', { hour12: false });
}

export function ngay(s) {
  if (!s) return '';
  return new Date(s).toLocaleDateString('vi-VN');
}

export function homNay() {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 10);
}

export function truNgay(chuoiNgay, soNgay) {
  const d = new Date(chuoiNgay);
  d.setDate(d.getDate() - soNgay);
  return d.toISOString().slice(0, 10);
}
