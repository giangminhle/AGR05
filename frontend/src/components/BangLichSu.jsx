import { Table } from 'react-bootstrap';
import { TEN_TRANG_THAI } from '../hangSo.js';
import { ngayGio } from '../tienIch.js';

/** Bảng lịch sử xử lý của một báo cáo (LichSuXacMinh). */
export default function BangLichSu({ ds }) {
  if (!ds || ds.length === 0) return <p className="text-muted">Chưa có lịch sử.</p>;
  return (
    <Table size="sm" bordered>
      <thead>
        <tr>
          <th>Thời gian</th>
          <th>Người thực hiện</th>
          <th>Từ → Sang</th>
          <th>Ghi chú</th>
        </tr>
      </thead>
      <tbody>
        {ds.map((l) => (
          <tr key={l.maLichSu}>
            <td>{ngayGio(l.thoiGian)}</td>
            <td>{l.tenNguoiThucHien}</td>
            <td>
              {l.trangThaiCu ? TEN_TRANG_THAI[l.trangThaiCu] : 'Khởi tạo'} → {TEN_TRANG_THAI[l.trangThaiMoi]}
            </td>
            <td>{l.ghiChu}</td>
          </tr>
        ))}
      </tbody>
    </Table>
  );
}
