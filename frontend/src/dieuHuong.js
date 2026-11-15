import { VaiTro } from './hangSo.js';

/** Trang chính của từng vai trò: bấm logo hoặc Quay lại khi không còn lịch sử thì về đây. */
export function trangChinh(vaiTro) {
  if (vaiTro === VaiTro.NONG_DAN) return '/bao-cao/cua-toi';
  if (vaiTro === VaiTro.CHUYEN_GIA) return '/xac-minh';
  return '/thong-ke';
}
