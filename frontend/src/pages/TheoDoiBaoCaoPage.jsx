import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Form, Row, Spinner, Table } from 'react-bootstrap';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { baoCaoApi } from '../api/index.js';
import { TEN_TRANG_THAI, TrangThai } from '../hangSo.js';
import { ngayGio } from '../tienIch.js';
import { TrangThaiBadge } from '../components/HuyHieu.jsx';
import BangLichSu from '../components/BangLichSu.jsx';
import XemAnh from '../components/XemAnh.jsx';

/** 6.2.4 Báo cáo của tôi (UC04). Nút Sửa / Gửi lại bật theo cờ duocSua do backend tính (BR03, BR04). */
export default function TheoDoiBaoCaoPage() {
  const [ds, setDs] = useState(null);
  const [loi, setLoi] = useState('');
  const [locTrangThai, setLocTrangThai] = useState('');
  const [thamSo] = useSearchParams();
  const [chon, setChon] = useState(Number(thamSo.get('ma')) || null);
  const dieuHuong = useNavigate();

  useEffect(() => {
    baoCaoApi.layCuaToi().then(setDs).catch((e) => setLoi(e.loi));
  }, []);

  if (loi) return <Alert variant="danger">{loi}</Alert>;
  if (!ds) return <Spinner animation="border" />;

  const hien = ds.filter((b) => !locTrangThai || b.trangThai === locTrangThai);
  const bc = ds.find((b) => b.maBaoCao === chon);
  const lyDoBacBo = bc && [...bc.dsLichSu].reverse().find((l) => l.trangThaiMoi === TrangThai.BI_BAC_BO);

  return (
    <Row>
      <Col lg={7}>
        <Card>
          <Card.Header className="d-flex justify-content-between align-items-center">
            <h5 className="mb-0">Báo cáo của tôi</h5>
            <Form.Select size="sm" style={{ width: 200 }} value={locTrangThai} onChange={(e) => setLocTrangThai(e.target.value)}>
              <option value="">Tất cả trạng thái</option>
              {Object.keys(TEN_TRANG_THAI).map((k) => <option key={k} value={k}>{TEN_TRANG_THAI[k]}</option>)}
            </Form.Select>
          </Card.Header>
          <Card.Body className="p-0">
            {hien.length === 0 ? (
              <p className="text-muted p-3 mb-0">Chưa có báo cáo nào.</p>
            ) : (
              <Table hover className="bang-chon mb-0" size="sm">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Tiêu đề</th>
                    <th>Khu vực</th>
                    <th>Ngày gửi</th>
                    <th>Trạng thái</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {hien.map((b) => (
                    <tr key={b.maBaoCao} className={b.maBaoCao === chon ? 'dong-chon' : ''} onClick={() => setChon(b.maBaoCao)}>
                      <td>{b.maBaoCao}</td>
                      <td>{b.tieuDe}</td>
                      <td>{b.tenKhuVuc}</td>
                      <td>{ngayGio(b.ngayGui)}</td>
                      <td><TrangThaiBadge ma={b.trangThai} /></td>
                      <td className="text-end">
                        {b.duocSua && b.trangThai === TrangThai.CHO_XAC_MINH && (
                          <Button size="sm" variant="outline-primary" onClick={(e) => { e.stopPropagation(); dieuHuong(`/bao-cao/${b.maBaoCao}/sua`); }}>Sửa</Button>
                        )}
                        {b.duocSua && b.trangThai === TrangThai.BI_BAC_BO && (
                          <Button size="sm" variant="outline-warning" onClick={(e) => { e.stopPropagation(); dieuHuong(`/bao-cao/${b.maBaoCao}/sua`); }}>Gửi lại</Button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            )}
          </Card.Body>
        </Card>
      </Col>
      <Col lg={5}>
        <Card>
          <Card.Header as="h6">Kết quả</Card.Header>
          <Card.Body>
            {!bc ? (
              <p className="text-muted">Chọn một báo cáo để xem kết quả.</p>
            ) : (
              <>
                <p className="mb-1"><b>{bc.tieuDe}</b> <TrangThaiBadge ma={bc.trangThai} /></p>
                <p className="mb-1">Cây trồng: {bc.tenCayTrong} – Mức độ {bc.mucDo} – Tọa độ ({bc.viDo}, {bc.kinhDo})</p>
                {bc.tenLoai && <p className="mb-1">Loại sâu bệnh: <b>{bc.tenLoai}</b></p>}
                {bc.khuyenNghi && <Alert variant="success" className="py-2">Khuyến nghị: {bc.khuyenNghi}</Alert>}
                {bc.trangThai === TrangThai.BI_BAC_BO && lyDoBacBo && (
                  <Alert variant="danger" className="py-2">Lý do bác bỏ: {lyDoBacBo.ghiChu}</Alert>
                )}
                <p className="mb-1">Số lần gửi lại: {bc.soLanGuiLai}/1</p>
                <XemAnh dsHinhAnh={bc.dsHinhAnh} />
                <h6 className="mt-3">Lịch sử xử lý</h6>
                <BangLichSu ds={bc.dsLichSu} />
              </>
            )}
          </Card.Body>
        </Card>
      </Col>
    </Row>
  );
}
