import { Badge } from 'react-bootstrap';
import { MAU_MUC_NGUY_CO, MAU_TRANG_THAI, TEN_MUC_NGUY_CO, TEN_TRANG_THAI } from '../hangSo.js';

export function TrangThaiBadge({ ma }) {
  return <Badge bg={MAU_TRANG_THAI[ma] || 'light'} text={ma === 'CHO_XAC_MINH' ? 'dark' : undefined}>{TEN_TRANG_THAI[ma] || ma}</Badge>;
}

export function NguyCoBadge({ duBao }) {
  if (!duBao) return <span className="text-muted">Chưa có dự báo (chỉ áp dụng BR06)</span>;
  return (
    <Badge bg={MAU_MUC_NGUY_CO[duBao.mucNguyCo]} text={duBao.mucNguyCo === 'TRUNG_BINH' ? 'dark' : undefined}>
      {TEN_MUC_NGUY_CO[duBao.mucNguyCo]} ({Math.round(duBao.xacSuat * 100)}%)
    </Badge>
  );
}
