import { useCallback, useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Form, Row, Spinner, Table } from 'react-bootstrap';
import { useSearchParams } from 'react-router-dom';
import { danhMucApi, quanTriBaoCaoApi } from '../api/index.js';
import { TEN_TRANG_THAI } from '../hangSo.js';
import { ngayGio } from '../tienIch.js';
import TieuDeTrang from '../components/TieuDeTrang.jsx';
import ThongTin from '../components/ThongTin.jsx';
import { NguyCoBadge, TrangThaiBadge } from '../components/HuyHieu.jsx';
import { usePhanTrang } from '../components/PhanTrang.jsx';
import BangLichSu from '../components/BangLichSu.jsx';
import XemAnh from '../components/XemAnh.jsx';
import LinkBanDo from '../components/LinkBanDo.jsx';

/** Quản trị viên tra cứu báo cáo (chỉ đọc): tìm theo trạng thái, khu vực, từ khóa; xem chi tiết. */
export default function QuanLyBaoCaoPage() {
  const [thamSo] = useSearchParams();
  const [loc, setLoc] = useState({ trangThai: '', maKhuVuc: '', tuKhoa: thamSo.get('ma') || '' });
  const [dsKhuVuc, setDsKhuVuc] = useState([]);
  const [ds, setDs] = useState(null);
  const [chon, setChon] = useState(Number(thamSo.get('ma')) || null);
  const [chiTiet, setChiTiet] = useState(null);
  const [loi, setLoi] = useState('');

  const tim = useCallback((l) => {
    setDs(null);
    quanTriBaoCaoApi.timKiem(l).then(setDs).catch((e) => setLoi(e.loi));
  }, []);

  useEffect(() => {
    danhMucApi.layKhuVuc().then(setDsKhuVuc);
    tim(loc);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    setChiTiet(null);
    if (chon) quanTriBaoCaoApi.chiTiet(chon).then(setChiTiet).catch((e) => setLoi(e.loi));
  }, [chon]);

  const { dsTrang, dieuKhien } = usePhanTrang(ds || [], JSON.stringify(loc) + (ds ? ds.length : ''));
  const doi = (e) => setLoc({ ...loc, [e.target.name]: e.target.value });

  return (
    <>
      <TieuDeTrang tieuDe="Tra cứu báo cáo" moTa="Xem mọi báo cáo để theo dõi báo cáo quá hạn và phân công lại khu vực. Trang chỉ đọc." />
      <Card className="mb-3">
        <Card.Body>
          <Form onSubmit={(e) => { e.preventDefault(); setLoi(''); tim(loc); }}>
            <Row className="g-2 align-items-end">
              <Col md={3}>
                <Form.Label>Trạng thái</Form.Label>
                <Form.Select name="trangThai" value={loc.trangThai} onChange={doi}>
                  <option value="">Tất cả</option>
                  {Object.keys(TEN_TRANG_THAI).map((k) => <option key={k} value={k}>{TEN_TRANG_THAI[k]}</option>)}
                </Form.Select>
              </Col>
              <Col md={3}>
                <Form.Label>Khu vực</Form.Label>
                <Form.Select name="maKhuVuc" value={loc.maKhuVuc} onChange={doi}>
                  <option value="">Mọi khu vực</option>
                  {dsKhuVuc.map((k) => <option key={k.maKhuVuc} value={k.maKhuVuc}>{k.tenKhuVuc}</option>)}
                </Form.Select>
              </Col>
              <Col md={4}>
                <Form.Label>Từ khóa</Form.Label>
                <Form.Control name="tuKhoa" value={loc.tuKhoa} onChange={doi} placeholder="Mã báo cáo, tiêu đề hoặc tên người gửi" />
              </Col>
              <Col md={2} className="d-flex justify-content-end">
                <Button type="submit" variant="success">Tìm</Button>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>
      {loi && <Alert variant="danger">{loi}</Alert>}
      <Row>
        <Col lg={6}>
          <Card className="mb-3">
            <Card.Header>Kết quả {ds ? `(${ds.length})` : ''}</Card.Header>
            {!ds ? <Spinner animation="border" className="m-3" /> : ds.length === 0 ? (
              <p className="text-muted p-3 mb-0">Không có báo cáo phù hợp.</p>
            ) : (
              <>
                <Table hover responsive size="sm" className="bang-chon mb-0">
                  <thead><tr><th>#</th><th>Tiêu đề</th><th>Khu vực</th><th>Người gửi</th><th>Trạng thái</th></tr></thead>
                  <tbody>
                    {dsTrang.map((b) => (
                      <tr key={b.maBaoCao} onClick={() => setChon(b.maBaoCao)}
                        className={(b.quaHan ? 'dong-qua-han ' : '') + (b.maBaoCao === chon ? 'dong-chon' : '')}>
                        <td>{b.maBaoCao}</td><td>{b.tieuDe}</td><td>{b.tenKhuVuc}</td><td>{b.tenNguoiGui}</td>
                        <td><TrangThaiBadge ma={b.trangThai} /></td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
                {dieuKhien}
              </>
            )}
          </Card>
        </Col>
        <Col lg={6}>
          <Card>
            <Card.Header>Chi tiết</Card.Header>
            <Card.Body>
              {!chiTiet ? <p className="text-muted mb-0">Chọn một báo cáo để xem.</p> : (
                <>
                  <div className="d-flex flex-wrap align-items-center gap-2 mb-3">
                    <span className="fw-semibold">#{chiTiet.maBaoCao} · {chiTiet.tieuDe}</span>
                    <TrangThaiBadge ma={chiTiet.trangThai} />
                    {chiTiet.quaHan && <span className="nhan nhan-BI_BAC_BO">Quá hạn xác minh</span>}
                  </div>
                  <ThongTin dong={[
                    ['Người gửi', chiTiet.tenNguoiGui],
                    ['Khu vực', chiTiet.tenKhuVuc],
                    ['Cây trồng', `${chiTiet.tenCayTrong} · mức độ ${chiTiet.mucDo}`],
                    ['Gửi lúc', ngayGio(chiTiet.ngayGui)],
                    ['Tọa độ', <>{chiTiet.viDo}, {chiTiet.kinhDo}<LinkBanDo viDo={chiTiet.viDo} kinhDo={chiTiet.kinhDo} /></>],
                    ['Loại sâu bệnh', chiTiet.tenLoai && <b>{chiTiet.tenLoai}</b>],
                    ['Khuyến nghị', chiTiet.khuyenNghi],
                  ]} />
                  <p className="mb-2 small"><span className="text-muted">Dự báo AI:</span> <NguyCoBadge duBao={chiTiet.duBao} /></p>
                  <div className="mo-ta-hien-truong mb-3">{chiTiet.moTa}</div>
                  <XemAnh dsHinhAnh={chiTiet.dsHinhAnh} />
                  <div className="tieu-de-muc">Lịch sử xử lý</div>
                  <BangLichSu ds={chiTiet.dsLichSu} />
                </>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </>
  );
}
