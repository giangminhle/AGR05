import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { trangChinh } from '../dieuHuong.js';
import { usePhien } from './PhienDangNhap.jsx';

/**
 * Tiêu đề đầu mỗi trang: nút Quay lại, tên trang, mô tả ngắn, các nút thao tác bên phải.
 * Quay lại: về trang trước nếu có lịch sử trong ứng dụng, không thì về trang chính của vai trò.
 */
export default function TieuDeTrang({ tieuDe, moTa, quayLai = true, children }) {
  const dieuHuong = useNavigate();
  const nguoiDung = usePhien();

  useEffect(() => {
    document.title = `${tieuDe} – AGR-05`;
  }, [tieuDe]);

  function veTruoc() {
    if ((window.history.state?.idx ?? 0) > 0) dieuHuong(-1);
    else dieuHuong(trangChinh(nguoiDung?.vaiTro));
  }

  return (
    <div className="tieu-de-trang">
      {quayLai && (
        <button type="button" className="nut-quay-lai" onClick={veTruoc} title="Quay lại trang trước">
          ← Quay lại
        </button>
      )}
      <div className="d-flex flex-wrap align-items-end gap-2">
        <div className="me-auto">
          <h1>{tieuDe}</h1>
          {moTa && <div className="mo-ta">{moTa}</div>}
        </div>
        {children && <div className="d-flex flex-wrap gap-2">{children}</div>}
      </div>
    </div>
  );
}
