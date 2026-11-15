import { useCallback, useEffect, useState } from 'react';
import { Alert, Button, ButtonGroup, Card, Col, Form, Modal, Row, Spinner, Table, ToggleButton } from 'react-bootstrap';
import { useSearchParams } from 'react-router-dom';
import { danhMucApi, xacMinhApi } from '../api/index.js';
import { TrangThai } from '../hangSo.js';
import { ngayGio } from '../tienIch.js';
import { usePhien } from '../components/PhienDangNhap.jsx';
import { NguyCoBadge, TrangThaiBadge } from '../components/HuyHieu.jsx';
import { useThongBaoNho } from '../components/ThongBaoNho.jsx';
import BangLichSu from '../components/BangLichSu.jsx';
import ThongTin from '../components/ThongTin.jsx';
import XemAnh from '../components/XemAnh.jsx';
import TieuDeTrang from '../components/TieuDeTrang.jsx';
import { usePhanTrang } from '../components/PhanTrang.jsx';
import LinkBanDo from '../components/LinkBanDo.jsx';
import ONhap from '../components/ONhap.jsx';

/** 6.2.5 Xác minh báo cáo (UC05–UC10, UC18). Trái: danh sách chờ; phải: chi tiết; dưới: kết luận. */
export default function XacMinhPage() {
  const nguoiDung = usePhien();
  const hienToast = useThongBaoNho();
  const [thamSo] = useSearchParams();
  const [che, setChe] = useState('cho');
  const [ds, setDs] = useState(null);
  const [dsKhuVuc, setDsKhuVuc] = useState([]);
  const [dsLoai, setDsLoai] = useState([]);
  const [locKhuVuc, setLocKhuVuc] = useState('');
  const [locMucDo, setLocMucDo] = useState('');
  const [tim, setTim] = useState('');
  const [chon, setChon] = useState(Number(thamSo.get('ma')) || null);
  const [chiTiet, setChiTiet] = useState(null);
  const [maLoai, setMaLoai] = useState('');
  const [khuyenNghi, setKhuyenNghi] = useState('');
  const [nguyCo, setNguyCo] = useState(undefined);
  const [loi, setLoi] = useState(null);
  const [dangGui, setDangGui] = useState(false);
  const [deNghi, setDeNghi] = useState(null); // null: đóng; { tenLoai, ghiChu, loi }: đang mở hộp đề nghị

  const taiDanhSach = useCallback(() => {
    const goi = che === 'cho' ? xacMinhApi.layDanhSachCho() : xacMinhApi.layDaXacMinh();
    goi.then(setDs).catch((e) => setLoi(e));
  }, [che]);

  useEffect(() => {
    setDs(null);
    taiDanhSach();
  }, [taiDanhSach]);

  useEffect(() => {
    danhMucApi.layKhuVuc().then((kv) => setDsKhuVuc(kv.filter((k) => nguoiDung.dsMaKhuVuc.includes(k.maKhuVuc))));
  }, [nguoiDung]);

  // Tải lại danh mục mỗi lần mở báo cáo: Quản trị viên có thể vừa thêm loại mới theo đề nghị
  useEffect(() => {
    danhMucApi.layLoai().then(setDsLoai).catch(() => {});
  }, [chon]);

  useEffect(() => {
    setChiTiet(null);
    setLoi(null);
    setMaLoai('');
    setKhuyenNghi('');
    setNguyCo(undefined);
    if (!chon) return;
    xacMinhApi.layChiTiet(chon).then((bc) => {
      setChiTiet(bc);
      if (bc.maLoai) setMaLoai(String(bc.maLoai));
      if (bc.khuyenNghi) setKhuyenNghi(bc.khuyenNghi);
      if (bc.trangThai !== TrangThai.CHO_XAC_MINH) setNguyCo(bc.duBao);
    }).catch((e) => setLoi(e));
  }, [chon]);

  async function thucHien(hanhDong, thanhCong) {
    setLoi(null);
    setDangGui(true);
    try {
      const kq = await hanhDong();
      hienToast(thanhCong);
      taiDanhSach();
      const bc = await xacMinhApi.layChiTiet(chon);
      setChiTiet(bc);
      return kq;
    } catch (e) {
      setLoi(e);
      if (e.ma === 409) taiDanhSach();
      return null;
    } finally {
      setDangGui(false);
    }
  }

  async function nutDuyet() {
    const kq = await thucHien(() => xacMinhApi.duyet(chon, maLoai ? Number(maLoai) : null, khuyenNghi), 'Đã duyệt báo cáo.');
    if (kq) setNguyCo(kq.duBao);
  }

  const nutTuChoi = () => thucHien(() => xacMinhApi.tuChoi(chon, khuyenNghi), 'Đã từ chối báo cáo.');
  async function guiDeNghi() {
    setDangGui(true);
    try {
      await xacMinhApi.deNghiLoai(chon, deNghi.tenLoai, deNghi.ghiChu);
      setDeNghi(null);
      hienToast('Đã gửi đề nghị cho Quản trị viên. Bạn sẽ nhận thông báo khi danh mục có loại mới.');
      setChiTiet(await xacMinhApi.layChiTiet(chon));
    } catch (e) {
      setDeNghi((d) => ({ ...d, loi: e }));
    } finally {
      setDangGui(false);
    }
  }

  const nutDaXuLy = () => thucHien(() => xacMinhApi.daXuLy(chon, khuyenNghi), 'Đã đánh dấu đã xử lý.');

  const loaiChon = dsLoai.find((l) => String(l.maLoai) === maLoai);
  const tk = tim.trim().toLowerCase();
  const hien = (ds || []).filter((b) => (!locKhuVuc || String(b.maKhuVuc) === locKhuVuc) && (!locMucDo || String(b.mucDo) === locMucDo)
    && (!tk || b.tieuDe.toLowerCase().includes(tk) || String(b.maBaoCao) === tk));
  const { dsTrang, dieuKhien } = usePhanTrang(hien, che + locKhuVuc + locMucDo + tk, 10);
  const choDuyet = chiTiet?.trangThai === TrangThai.CHO_XAC_MINH;
  const choXuLy = chiTiet?.trangThai === TrangThai.DA_XAC_MINH;

  return (
    <>
    <TieuDeTrang tieuDe="Xác minh báo cáo" quayLai={Boolean(thamSo.get('ma'))}
      moTa="Báo cáo trong các khu vực bạn phụ trách. Dòng đỏ là báo cáo đã quá hạn xác minh." />
    <Row>
      <Col lg={5}>
        <Card className="mb-3">
          <Card.Header>
            <ButtonGroup size="sm" className="mb-2">
              {[['cho', 'Chờ xác minh'], ['da', 'Đã xác minh (chờ xử lý)']].map(([gt, ten]) => (
                <ToggleButton key={gt} id={'che-' + gt} type="radio" variant="outline-success" value={gt} checked={che === gt}
                  onChange={() => { setChe(gt); setChon(null); }}>{ten}</ToggleButton>
              ))}
            </ButtonGroup>
            <Row className="g-2">
              <Col>
                <Form.Select size="sm" value={locKhuVuc} onChange={(e) => setLocKhuVuc(e.target.value)}>
                  <option value="">Mọi khu vực phụ trách</option>
                  {dsKhuVuc.map((k) => <option key={k.maKhuVuc} value={k.maKhuVuc}>{k.tenKhuVuc}</option>)}
                </Form.Select>
              </Col>
              <Col>
                <Form.Select size="sm" value={locMucDo} onChange={(e) => setLocMucDo(e.target.value)}>
                  <option value="">Mọi mức độ</option>
                  <option value="1">Mức 1</option>
                  <option value="2">Mức 2</option>
                  <option value="3">Mức 3</option>
                </Form.Select>
              </Col>
            </Row>
            <Form.Control size="sm" className="mt-2" placeholder="Tìm theo tiêu đề hoặc mã báo cáo..." value={tim} onChange={(e) => setTim(e.target.value)} />
          </Card.Header>
          <Card.Body className="p-0">
            {!ds ? (
              <Spinner animation="border" className="m-3" />
            ) : hien.length === 0 ? (
              <p className="text-muted p-3 mb-0">Không có báo cáo cần xử lý.</p>
            ) : (
              <Table hover responsive size="sm" className="bang-chon mb-0">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Tiêu đề</th>
                    <th>Khu vực</th>
                    <th>Mức</th>
                    <th>Gửi lúc</th>
                  </tr>
                </thead>
                <tbody>
                  {dsTrang.map((b) => (
                    <tr key={b.maBaoCao} onClick={() => setChon(b.maBaoCao)}
                      className={(b.quaHan ? 'dong-qua-han ' : '') + (b.maBaoCao === chon ? 'dong-chon' : '')}
                      title={b.quaHan ? 'Quá hạn xác minh' : ''}>
                      <td>{b.maBaoCao}</td>
                      <td>{b.tieuDe}</td>
                      <td>{b.tenKhuVuc}</td>
                      <td>{b.mucDo}</td>
                      <td>{ngayGio(b.ngayGui)}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            )}
            {dieuKhien}
          </Card.Body>
        </Card>
      </Col>

      <Col lg={7}>
        {loi && !loi.truong && <Alert variant={loi.ma === 409 ? 'warning' : 'danger'}>{loi.loi}</Alert>}
        {!chiTiet ? (
          <Card><Card.Body className="text-muted">Chọn một báo cáo để xem chi tiết.</Card.Body></Card>
        ) : (
          <>
            <Card className="mb-3">
              <Card.Header className="d-flex justify-content-between">
                <span>#{chiTiet.maBaoCao} · {chiTiet.tieuDe} {chiTiet.quaHan && <span className="nhan nhan-BI_BAC_BO ms-1">Quá hạn</span>}</span>
                <TrangThaiBadge ma={chiTiet.trangThai} />
              </Card.Header>
              <Card.Body>
                <Row>
                  <Col md={6}>
                    <XemAnh dsHinhAnh={chiTiet.dsHinhAnh} />
                  </Col>
                  <Col md={6}>
                    <ThongTin dong={[
                      ['Người gửi', chiTiet.tenNguoiGui],
                      ['Khu vực', chiTiet.tenKhuVuc],
                      ['Cây trồng', `${chiTiet.tenCayTrong} · mức độ ${chiTiet.mucDo}`],
                      ['Phát hiện', chiTiet.ngayPhatHien],
                      ['Gửi lúc', ngayGio(chiTiet.ngayGui)],
                      ['Tọa độ', <>{chiTiet.viDo}, {chiTiet.kinhDo}<LinkBanDo viDo={chiTiet.viDo} kinhDo={chiTiet.kinhDo} /></>],
                      ['Đã gửi lại', `${chiTiet.soLanGuiLai} lần`],
                    ]} />
                    <div className="mo-ta-hien-truong">{chiTiet.moTa}</div>
                  </Col>
                </Row>
                <div className="tieu-de-muc">Lịch sử xử lý</div>
                <BangLichSu ds={chiTiet.dsLichSu} />
              </Card.Body>
            </Card>

            <Card>
              <Card.Header>Kết luận</Card.Header>
              <Card.Body>
                <Row>
                  <Col md={6}>
                    <ONhap ten="maLoai" nhan="Loại sâu bệnh" batBuoc={choDuyet} loi={loi}>
                      {(sai) => (
                        <Form.Select value={maLoai} onChange={(e) => setMaLoai(e.target.value)} isInvalid={sai} disabled={!choDuyet}>
                          <option value="">-- Chọn loại --</option>
                          {dsLoai.map((l) => <option key={l.maLoai} value={l.maLoai}>{l.tenLoai}</option>)}
                        </Form.Select>
                      )}
                    </ONhap>
                    {choDuyet && (
                      <div className="small mb-3" style={{ marginTop: '-0.5rem' }}>
                        Không có trong danh sách?{' '}
                        <Button variant="link" size="sm" className="p-0 align-baseline"
                          onClick={() => setDeNghi({ tenLoai: '', ghiChu: '', loi: null })}>Đề nghị thêm loại mới</Button>
                      </div>
                    )}
                    <Form.Label>Triệu chứng, biện pháp phòng trừ</Form.Label>
                    <div className="border rounded p-2 bg-light small mb-3" style={{ minHeight: 70 }}>
                      {loaiChon ? (
                        <>
                          <div><b>Triệu chứng:</b> {loaiChon.trieuChung}</div>
                          <div><b>Biện pháp:</b> {loaiChon.bienPhapPhongTru}</div>
                        </>
                      ) : <span className="text-muted">Chọn loại để xem.</span>}
                    </div>
                  </Col>
                  <Col md={6}>
                    <ONhap ten="khuyenNghi" nhan="Khuyến nghị (khi duyệt, đã xử lý) hoặc lý do (khi từ chối)" loi={loi}
                      as="textarea" rows={5} value={khuyenNghi} onChange={(e) => setKhuyenNghi(e.target.value)}
                      disabled={!choDuyet && !choXuLy} maxLength={1000} />
                  </Col>
                </Row>
                <div className="mb-3">
                  Nguy cơ dịch: {nguyCo === undefined ? <span className="text-muted">(hiện sau khi duyệt)</span> : <NguyCoBadge duBao={nguyCo} />}
                </div>
                <div className="d-flex justify-content-end gap-2">
                  {choDuyet && (
                    <>
                      <Button variant="danger" onClick={nutTuChoi} disabled={dangGui}>Từ chối</Button>
                      <Button variant="success" onClick={nutDuyet} disabled={dangGui}>
                        {dangGui && <Spinner size="sm" className="me-2" />}Duyệt
                      </Button>
                    </>
                  )}
                  {choXuLy && <Button variant="secondary" onClick={nutDaXuLy} disabled={dangGui}>Đã xử lý</Button>}
                </div>
              </Card.Body>
            </Card>
          </>
        )}
      </Col>
    </Row>

    <Modal show={deNghi !== null} onHide={() => setDeNghi(null)} centered>
      <Modal.Header closeButton>
        <Modal.Title as="h5">Đề nghị thêm loại sâu bệnh</Modal.Title>
      </Modal.Header>
      {deNghi && (
        <Modal.Body>
          <p className="text-muted small">
            Chỉ Quản trị viên được sửa danh mục. Đề nghị được gửi kèm mã báo cáo #{chon}; báo cáo vẫn ở trạng thái Chờ xác minh
            và lần đề nghị được ghi vào lịch sử xử lý.
          </p>
          {deNghi.loi && !deNghi.loi.truong && <Alert variant="danger">{deNghi.loi.loi}</Alert>}
          <ONhap ten="tenLoai" nhan="Tên loại sâu bệnh nghi ngờ" batBuoc loi={deNghi.loi} value={deNghi.tenLoai} maxLength={100}
            autoFocus placeholder="Ví dụ: Bệnh lem lép hạt" onChange={(e) => setDeNghi({ ...deNghi, tenLoai: e.target.value })} />
          <ONhap ten="ghiChuDeNghi" nhan="Ghi chú cho Quản trị viên (triệu chứng nhận thấy)" as="textarea" rows={3} maxLength={300}
            value={deNghi.ghiChu} onChange={(e) => setDeNghi({ ...deNghi, ghiChu: e.target.value })} />
        </Modal.Body>
      )}
      <Modal.Footer>
        <Button variant="outline-secondary" onClick={() => setDeNghi(null)}>Hủy</Button>
        <Button variant="success" onClick={guiDeNghi} disabled={dangGui}>
          {dangGui && <Spinner size="sm" className="me-2" />}Gửi đề nghị
        </Button>
      </Modal.Footer>
    </Modal>
    </>
  );
}
