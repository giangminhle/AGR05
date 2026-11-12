// Hằng số dùng chung, khớp với các lớp hằng trong backend (luật 5). Không viết chuỗi rải rác.
export const TrangThai = {
  CHO_XAC_MINH: 'CHO_XAC_MINH',
  DA_XAC_MINH: 'DA_XAC_MINH',
  BI_BAC_BO: 'BI_BAC_BO',
  DA_XU_LY: 'DA_XU_LY',
};

export const TEN_TRANG_THAI = {
  CHO_XAC_MINH: 'Chờ xác minh',
  DA_XAC_MINH: 'Đã xác minh',
  BI_BAC_BO: 'Bị bác bỏ',
  DA_XU_LY: 'Đã xử lý',
};

export const MAU_TRANG_THAI = {
  CHO_XAC_MINH: 'warning',
  DA_XAC_MINH: 'success',
  BI_BAC_BO: 'danger',
  DA_XU_LY: 'secondary',
};

export const VaiTro = {
  NONG_DAN: 'NONG_DAN',
  CHUYEN_GIA: 'CHUYEN_GIA',
  QUAN_TRI: 'QUAN_TRI',
};

export const TEN_VAI_TRO = {
  NONG_DAN: 'Nông dân',
  CHUYEN_GIA: 'Chuyên gia',
  QUAN_TRI: 'Quản trị viên',
};

export const TEN_LOAI_THONG_BAO = {
  BAO_CAO_MOI: 'Báo cáo mới',
  KET_QUA_XAC_MINH: 'Kết quả xác minh',
  QUA_HAN: 'Báo cáo quá hạn',
  DONG_HO_SO: 'Đóng hồ sơ',
  CANH_BAO_DICH: 'Cảnh báo dịch hại',
};

export const MucNguyCo = { THAP: 'THAP', TRUNG_BINH: 'TRUNG_BINH', CAO: 'CAO' };

export const TEN_MUC_NGUY_CO = { THAP: 'Thấp', TRUNG_BINH: 'Trung bình', CAO: 'Cao' };

export const MAU_MUC_NGUY_CO = { THAP: 'success', TRUNG_BINH: 'warning', CAO: 'danger' };

export const TOI_DA_ANH = 5;
export const TOI_DA_BYTE_ANH = 5 * 1024 * 1024;

/** Chu kỳ gọi API đếm thông báo chưa đọc (ARCH04). */
export const CHU_KY_THONG_BAO_MS = 60_000;
