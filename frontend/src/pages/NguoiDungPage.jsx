import { useEffect, useState } from 'react';
import { Alert, Badge, Button, Card, Col, Form, Modal, Row, Table } from 'react-bootstrap';
import { nguoiDungApi } from '../api/index.js';
import { TEN_VAI_TRO, VaiTro } from '../hangSo.js';
import TieuDeTrang from '../components/TieuDeTrang.jsx';
import { useXacNhan } from '../components/XacNhan.jsx';
import { usePhanTrang } from '../components/PhanTrang.jsx';
import ONhap from '../components/ONhap.jsx';
import { useThongBaoNho } from '../components/ThongBaoNho.jsx';

const RONG = { maNguoiDung: null, tenDangNhap: '', matKhau: '', hoTen: '', dienThoai: '', vaiTro: VaiTro.NONG_DAN };

/** UC15 Người dùng: tạo, sửa vai trò, khóa / mở khóa (bố cục như KhuVucPage). */
export default function NguoiDungPage() {
  const hienToast = useThongBaoNho();
  const xacNhan = useXacNhan();
  const [ds, setDs] = useState([]);
  const [form, setForm] = useState(RONG);
  const [loi, setLoi] = useState(null);
  const [tim, setTim] = useState('');
  const [hopMatKhau, setHopMatKhau] = useState(false);
  const [matKhauMoi, setMatKhauMoi] = useState('');
  const [loiMatKhau, setLoiMatKhau] = useState(null);

  const tai = () => nguoiDungApi.layTatCa().then(setDs).catch(setLoi);
  useEffect(() => { tai(); }, []);

  const doi = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  async function luu(e) {
    e.preventDefault();
    setLoi(null);
    try {
      if (form.maNguoiDung) await nguoiDungApi.sua(form.maNguoiDung, form);
      else await nguoiDungApi.tao(form);
      hienToast('Đã lưu người dùng.');
      setForm(RONG);
      tai();
    } catch (er) {
      setLoi(er);
    }
  }

  async function khoa(nd) {
    setLoi(null);
    const ok = await xacNhan(nd.biKhoa
      ? { tieuDe: 'Mở khóa tài khoản', noiDung: `Mở khóa tài khoản ${nd.tenDangNhap}?`, nutDongY: 'Mở khóa', mau: 'success' }
      : { tieuDe: 'Khóa tài khoản', noiDung: `Khóa tài khoản ${nd.tenDangNhap}? Người này sẽ không đăng nhập được.`, nutDongY: 'Khóa', mau: 'danger' });
    if (!ok) return;
    try {
      const kq = await nguoiDungApi.khoa(nd.maNguoiDung, !nd.biKhoa);
      hienToast(kq.canPhanCongLai ? 'Đã khóa. Chuyên gia này đang phụ trách khu vực, hãy phân công người khác.'
        : (nd.biKhoa ? 'Đã mở khóa.' : 'Đã khóa tài khoản.'), kq.canPhanCongLai ? 'warning' : 'success');
      tai();
    } catch (er) {
      setLoi(er);
    }
  }

  async function datLaiMatKhau() {
    setLoiMatKhau(null);
    try {
      await nguoiDungApi.datLaiMatKhau(form.maNguoiDung, matKhauMoi);
      hienToast(`Đã đặt lại mật khẩu cho ${form.tenDangNhap}.`);
      setHopMatKhau(false);
      setMatKhauMoi('');
    } catch (er) {
      setLoiMatKhau(er);
    }
  }

  const tk = tim.trim().toLowerCase();
  const hien = ds.filter((n) => !tk || n.tenDangNhap.toLowerCase().includes(tk) || n.hoTen.toLowerCase().includes(tk));
  const { dsTrang, dieuKhien } = usePhanTrang(hien, tk);

  return (
    <>
    <TieuDeTrang tieuDe="Người dùng" moTa="Tạo tài khoản, đổi vai trò, khóa hoặc mở khóa. Bấm một dòng để sửa." />
    <Row>
      <Col lg={7}>
        <Card>
          <Card.Header className="d-flex justify-content-between align-items-center gap-2">
            <span className="fw-semibold">Danh sách tài khoản ({hien.length})</span>
            <Form.Control size="sm" style={{ width: 220 }} placeholder="Tìm theo tên..." value={tim} onChange={(e) => setTim(e.target.value)} />
          </Card.Header>
          <Table hover responsive size="sm" className="bang-chon mb-0">
            <thead><tr><th>Tên đăng nhập</th><th>Họ tên</th><th>Vai trò</th><th>Trạng thái</th><th /></tr></thead>
            <tbody>
              {dsTrang.map((n) => (
                <tr key={n.maNguoiDung} className={form.maNguoiDung === n.maNguoiDung ? 'dong-chon' : ''}
                  onClick={() => { setForm({ ...n, matKhau: '', dienThoai: n.dienThoai || '' }); setLoi(null); }}>
                  <td>{n.tenDangNhap}</td><td>{n.hoTen}</td><td>{TEN_VAI_TRO[n.vaiTro]}</td>
                  <td>{n.biKhoa ? <Badge bg="danger">Đã khóa</Badge> : <Badge bg="success">Hoạt động</Badge>}</td>
                  <td className="text-end">
                    <Button size="sm" variant={n.biKhoa ? 'outline-success' : 'outline-danger'} onClick={(e) => { e.stopPropagation(); khoa(n); }}>
                      {n.biKhoa ? 'Mở khóa' : 'Khóa'}
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
          {dieuKhien}
        </Card>
      </Col>
      <Col lg={5}>
        <Card>
          <Card.Header>{form.maNguoiDung ? `Sửa ${form.tenDangNhap}` : 'Tạo tài khoản'}</Card.Header>
          <Card.Body>
            {loi && !loi.truong && <Alert variant="danger">{loi.loi}</Alert>}
            <Form onSubmit={luu}>
              <ONhap ten="tenDangNhap" nhan="Tên đăng nhập" batBuoc loi={loi} name="tenDangNhap" value={form.tenDangNhap}
                onChange={doi} disabled={Boolean(form.maNguoiDung)} maxLength={50} />
              {!form.maNguoiDung && (
                <ONhap ten="matKhau" nhan="Mật khẩu (ít nhất 6 ký tự)" batBuoc loi={loi} type="password" name="matKhau" value={form.matKhau} onChange={doi} />
              )}
              <ONhap ten="hoTen" nhan="Họ tên" batBuoc loi={loi} name="hoTen" value={form.hoTen} onChange={doi} maxLength={100} />
              <ONhap ten="dienThoai" nhan="Điện thoại" loi={loi} name="dienThoai" value={form.dienThoai} onChange={doi} maxLength={15} />
              <ONhap ten="vaiTro" nhan="Vai trò" batBuoc loi={loi}>
                {(sai) => (
                  <Form.Select name="vaiTro" value={form.vaiTro} onChange={doi} isInvalid={sai}>
                    {Object.keys(TEN_VAI_TRO).map((k) => <option key={k} value={k}>{TEN_VAI_TRO[k]}</option>)}
                  </Form.Select>
                )}
              </ONhap>
              <div className="d-flex justify-content-end gap-2">
                {form.maNguoiDung && (
                  <Button variant="outline-warning" onClick={() => { setHopMatKhau(true); setLoiMatKhau(null); setMatKhauMoi(''); }}>
                    Đặt lại mật khẩu
                  </Button>
                )}
                <Button variant="outline-secondary" onClick={() => { setForm(RONG); setLoi(null); }}>Mới</Button>
                <Button type="submit" variant="success">Lưu</Button>
              </div>
            </Form>
          </Card.Body>
        </Card>
      </Col>
    </Row>

    <Modal show={hopMatKhau} onHide={() => setHopMatKhau(false)} centered>
      <Modal.Header closeButton><Modal.Title as="h5">Đặt lại mật khẩu cho {form.tenDangNhap}</Modal.Title></Modal.Header>
      <Modal.Body>
        <ONhap ten="matKhauMoi" nhan="Mật khẩu mới (ít nhất 6 ký tự)" batBuoc loi={loiMatKhau} type="text"
          value={matKhauMoi} onChange={(e) => setMatKhauMoi(e.target.value)} autoFocus />
        <div className="text-muted small">Báo mật khẩu mới cho người dùng, nhắc họ đổi lại sau khi đăng nhập.</div>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="outline-secondary" onClick={() => setHopMatKhau(false)}>Hủy</Button>
        <Button variant="warning" onClick={datLaiMatKhau}>Đặt lại</Button>
      </Modal.Footer>
    </Modal>
    </>
  );
}
