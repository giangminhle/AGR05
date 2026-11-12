import { createContext, useContext } from 'react';

/** Người đang đăng nhập (lấy từ GET /api/auth/toi) dùng chung cho các trang. */
export const PhienContext = createContext(null);

export function usePhien() {
  return useContext(PhienContext);
}
