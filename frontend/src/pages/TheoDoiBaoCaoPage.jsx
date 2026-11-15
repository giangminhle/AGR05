import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Form, Row, Spinner, Table } from 'react-bootstrap';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { baoCaoApi } from '../api/index.js';
import { TEN_TRANG_THAI, TrangThai } from '../hangSo.js';
import { ngayGio } from '../tienIch.js';
import { TrangThaiBadge } from '../components/HuyHieu.jsx';
import TieuDeTrang from '../components/TieuDeTrang.jsx';
import ThongTin from '../components/ThongTin.jsx';
import { usePhanTrang } from '../components/PhanTrang.jsx';
import LinkBanDo from '../components/LinkBanDo.jsx';
import BangLichSu from '../components/BangLichSu.jsx';
import XemAnh from '../components/XemAnh.jsx';

/** 6.2.4 Báo cáo của tôi (UC04). Nút Sửa / Gửi lại bật theo cờ duocSua do backend tính (BR03, BR04). */
export default function TheoDoiBaoCaoPage() {
  const [ds, setDs] = useState(null);
  const [loi, setLoi] = useState('');
  const [locTrangThai, setLocTrangThai] = useState('');
  const [tim, setTim] = useState('');
  const [thamSo] = useSearchParams();
  const [chon, setChon] = useState(Number(thamSo.get('ma')) || null);
  const dieuHuong = useNavigate();

  useEffect(() => {
    baoCaoApi.layCuaToi().then(setDs).catch((e) => setLoi(e.loi));
  }, []);

  const tk = tim.trim().toLowerCase();
  const hien = (ds || []).filter((b) => (!locTrangThai || b.trangThai === locTrangThai)
    && (!tk || b.tieuDe.toLowerCase().includes(tk) || String(b.maBaoCao) === tk));
  const { dsTrang, dieuKhien } = usePhanTrang(hien, locTrangThai + '|' + tk);

  if (loi) return <Alert variant="danger">{loi}</Alert>;
  if (!ds) return <Spinner animation="border" />;
  const bc = ds.find((b) => b.maBaoCao === chon);
  const lyDoBacBo = bc && [...bc.dsLichSu].reverse().find((l) => l.trangThaiMoi === TrangThai.BI_BAC_BO);

  return (
    <>
    <TieuDeTrang tieuDe="Báo cáo của tôi" moTa="Theo dõi trạng thái và kết quả xác minh các báo cáo bạn đã gửi" quayLai={false}>
      <Button variant="success" onClick={() => dieuHuong('/bao-cao/moi')}>+ Gửi báo cáo mới</Button>
    </TieuDeTrang>
    <Row>
      <Col lg={7}>
        <Card>
          <Card.Header className="d-flex justify-content-between align-items-center">
            <span className="fw-semibold">Danh sách báo cáo ({hien.length})</span>
            <div className="d-flex gap-2">
            <Form.Control size="sm" style={{ width: 180 }} placeholder="Tìm tiêu đề, mã..." value={tim} onChange={(e) => setTim(e.target.value)} />
            <Form.Select size="sm" style={{ width: 170 }} value={locTrangThai} onChange={(e) => setLocTrangThai(e.target.value)}>
              <option value="">Tất cả trạng thái</option>
              {Object.keys(TEN_TRANG_THAI).map((k) => <option key={k} value={k}>{TEN_TRANG_THAI[k]}</option>)}
            </Form.Select>
            </div>
          </Card.Header>
          <Card.Body className="p-0">
            {hien.length === 0 ? (
              <p className="text-muted p-3 mb-0">Không có báo cáo phù hợp.</p>
            ) : (
              <Table hover responsive className="bang-chon mb-0" size="sm">
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
                  {dsTrang.map((b) => (
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
            {dieuKhien}
          </Card.Body>
        </Card>
      </Col>
      <Col lg={5}>
        <Card>
          <Card.Header>Kết quả</Card.Header>
          <Card.Body>
            {!bc ? (
              <p className="text-muted">Chọn một báo cáo để xem kết quả.</p>
            ) : (
              <>
                <div className="d-flex flex-wrap align-items-center gap-2 mb-3">
                  <span className="fw-semibold">{bc.tieuDe}</span>
                  <TrangThaiBadge ma={bc.trangThai} />
                </div>
                <ThongTin dong={[
                  ['Cây trồng', `${bc.tenCayTrong} · mức độ ${bc.mucDo}`],
                  ['Tọa độ', <>{bc.viDo}, {bc.kinhDo}<LinkBanDo viDo={bc.viDo} kinhDo={bc.kinhDo} /></>],
                  ['Loại sâu bệnh', bc.tenLoai && <b>{bc.tenLoai}</b>],
                  ['Số lần gửi lại', `${bc.soLanGuiLai}/1`],
                ]} />
                {bc.khuyenNghi && <Alert variant="success" className="py-2">Khuyến nghị: {bc.khuyenNghi}</Alert>}
                {bc.trangThai === TrangThai.BI_BAC_BO && lyDoBacBo && (
                  <Alert variant="danger" className="py-2">Lý do bác bỏ: {lyDoBacBo.ghiChu}</Alert>
                )}
                <XemAnh dsHinhAnh={bc.dsHinhAnh} />
                <div className="tieu-de-muc">Lịch sử xử lý</div>
                <BangLichSu ds={bc.dsLichSu} />
              </>
            )}
          </Card.Body>
        </Card>
      </Col>
    </Row>
    </>
  );
}
