import { TEN_MUC_NGUY_CO, TEN_TRANG_THAI } from '../hangSo.js';

/** Nhãn trạng thái báo cáo, màu theo lớp .nhan-<MÃ> trong style.css. */
export function TrangThaiBadge({ ma }) {
  return <span className={`nhan nhan-${ma}`}>{TEN_TRANG_THAI[ma] || ma}</span>;
}

export function NguyCoBadge({ duBao }) {
  if (!duBao) return <span className="text-muted">Chưa có dự báo (chỉ áp dụng BR06)</span>;
  return (
    <span className={`nhan nhan-${duBao.mucNguyCo}`}>
      {TEN_MUC_NGUY_CO[duBao.mucNguyCo]} · {Math.round(duBao.xacSuat * 100)}%
    </span>
  );
}
