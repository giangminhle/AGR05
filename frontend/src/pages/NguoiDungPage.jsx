import { useEffect, useState } from 'react';
import { Alert, Badge, Button, Card, Col, Form, Row, Table } from 'react-bootstrap';
import { nguoiDungApi } from '../api/index.js';
import { TEN_VAI_TRO, VaiTro } from '../hangSo.js';
import ONhap from '../components/ONhap.jsx';
import { useThongBaoNho } from '../components/ThongBaoNho.jsx';

const RONG = { maNguoiDung: null, tenDangNhap: '', matKhau: '', hoTen: '', dienThoai: '', vaiTro: VaiTro.NONG_DAN };

/** UC15 Người dùng: tạo, sửa vai trò, khóa / mở khóa (bố cục như KhuVucPage). */
export default function NguoiDungPage() {
  const hienToast = useThongBaoNho();
  const [ds, setDs] = useState([]);
  const [form, setForm] = useState(RONG);
  const [loi, setLoi] = useState(null);

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
    try {
      const kq = await nguoiDungApi.khoa(nd.maNguoiDung, !nd.biKhoa);
      hienToast(kq.canPhanCongLai ? 'Đã khóa. Chuyên gia này đang phụ trách khu vực, hãy phân công người khác.'
        : (nd.biKhoa ? 'Đã mở khóa.' : 'Đã khóa tài khoản.'), kq.canPhanCongLai ? 'warning' : 'success');
      tai();
    } catch (er) {
      setLoi(er);
    }
  }

  return (
    <Row>
      <Col lg={7}>
        <Card>
          <Card.Header as="h5">Người dùng</Card.Header>
          <Table hover size="sm" className="bang-chon mb-0">
            <thead><tr><th>Tên đăng nhập</th><th>Họ tên</th><th>Vai trò</th><th>Trạng thái</th><th /></tr></thead>
            <tbody>
              {ds.map((n) => (
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
        </Card>
      </Col>
      <Col lg={5}>
        <Card>
          <Card.Header as="h6">{form.maNguoiDung ? `Sửa ${form.tenDangNhap}` : 'Tạo tài khoản'}</Card.Header>
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
                <Button variant="outline-secondary" onClick={() => { setForm(RONG); setLoi(null); }}>Mới</Button>
                <Button type="submit" variant="success">Lưu</Button>
              </div>
            </Form>
          </Card.Body>
        </Card>
      </Col>
    </Row>
  );
}
