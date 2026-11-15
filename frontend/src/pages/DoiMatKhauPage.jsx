import { useState } from 'react';
import { Button, Card, Col, Form, Row } from 'react-bootstrap';
import { authApi } from '../api/index.js';
import ONhap from '../components/ONhap.jsx';
import TieuDeTrang from '../components/TieuDeTrang.jsx';
import { useThongBaoNho } from '../components/ThongBaoNho.jsx';

const RONG = { matKhauCu: '', matKhauMoi: '', nhapLai: '' };

/** Người dùng tự đổi mật khẩu của mình. */
export default function DoiMatKhauPage() {
  const hienToast = useThongBaoNho();
  const [form, setForm] = useState(RONG);
  const [loi, setLoi] = useState(null);

  const doi = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  async function luu(e) {
    e.preventDefault();
    setLoi(null);
    if (form.matKhauMoi !== form.nhapLai) {
      setLoi({ loi: 'Mật khẩu nhập lại không khớp.', truong: 'nhapLai' });
      return;
    }
    try {
      await authApi.doiMatKhau(form.matKhauCu, form.matKhauMoi);
      hienToast('Đã đổi mật khẩu.');
      setForm(RONG);
    } catch (er) {
      setLoi(er);
    }
  }

  return (
    <>
      <TieuDeTrang tieuDe="Đổi mật khẩu" moTa="Nhập mật khẩu hiện tại và mật khẩu mới (ít nhất 6 ký tự)" />
      <Row>
        <Col md={8} lg={5}>
          <Card>
            <Card.Body>
              {loi && !loi.truong && <div className="alert alert-danger">{loi.loi}</div>}
              <Form onSubmit={luu} noValidate>
                <ONhap ten="matKhauCu" nhan="Mật khẩu hiện tại" batBuoc loi={loi} type="password" name="matKhauCu" value={form.matKhauCu} onChange={doi} />
                <ONhap ten="matKhauMoi" nhan="Mật khẩu mới" batBuoc loi={loi} type="password" name="matKhauMoi" value={form.matKhauMoi} onChange={doi} />
                <ONhap ten="nhapLai" nhan="Nhập lại mật khẩu mới" batBuoc loi={loi} type="password" name="nhapLai" value={form.nhapLai} onChange={doi} />
                <div className="d-flex justify-content-end">
                  <Button type="submit" variant="success">Đổi mật khẩu</Button>
                </div>
              </Form>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </>
  );
}
