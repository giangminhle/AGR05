import { useEffect, useState } from 'react';
import { Alert, Button, Form, Spinner } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { authApi } from '../api/index.js';
import KhungDangNhap from '../components/KhungDangNhap.jsx';
import ONhap from '../components/ONhap.jsx';

const RONG = { tenDangNhap: '', hoTen: '', dienThoai: '', matKhau: '', nhapLai: '' };

/** Nông dân tự đăng ký tài khoản. Chuyên gia và Quản trị viên do Quản trị viên tạo. */
export default function DangKyPage({ onDangNhap }) {
  const [form, setForm] = useState(RONG);
  const [loi, setLoi] = useState(null);
  const [dangGui, setDangGui] = useState(false);

  useEffect(() => {
    document.title = 'Đăng ký – AGR-05';
  }, []);

  const doi = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  async function nutDangKy(e) {
    e.preventDefault();
    setLoi(null);
    if (form.matKhau !== form.nhapLai) {
      setLoi({ loi: 'Mật khẩu nhập lại không khớp.', truong: 'nhapLai' });
      return;
    }
    setDangGui(true);
    try {
      await authApi.dangKy({ tenDangNhap: form.tenDangNhap, hoTen: form.hoTen, dienThoai: form.dienThoai, matKhau: form.matKhau });
      const nd = await authApi.dangNhap(form.tenDangNhap.trim(), form.matKhau);
      onDangNhap(nd);
    } catch (er) {
      setLoi(er);
      setDangGui(false);
    }
  }

  return (
    <KhungDangNhap tieuDe="Đăng ký tài khoản" moTa="Dành cho nông dân gửi báo cáo sâu bệnh. Tài khoản chuyên gia do Chi cục cấp.">
      {loi && !loi.truong && <Alert variant="danger">{loi.loi}</Alert>}
      <Form onSubmit={nutDangKy} noValidate>
        <ONhap ten="tenDangNhap" nhan="Tên đăng nhập" batBuoc loi={loi} name="tenDangNhap" value={form.tenDangNhap}
          onChange={doi} maxLength={50} autoFocus placeholder="chữ không dấu, số, dấu chấm, gạch dưới" />
        <ONhap ten="hoTen" nhan="Họ tên" batBuoc loi={loi} name="hoTen" value={form.hoTen} onChange={doi} maxLength={100} />
        <ONhap ten="dienThoai" nhan="Điện thoại" loi={loi} name="dienThoai" value={form.dienThoai} onChange={doi} maxLength={15} inputMode="numeric" />
        <ONhap ten="matKhau" nhan="Mật khẩu (ít nhất 6 ký tự)" batBuoc loi={loi} type="password" name="matKhau" value={form.matKhau} onChange={doi} />
        <ONhap ten="nhapLai" nhan="Nhập lại mật khẩu" batBuoc loi={loi} type="password" name="nhapLai" value={form.nhapLai} onChange={doi} />
        <div className="d-flex justify-content-between align-items-center">
          <Link to="/dang-nhap">← Đã có tài khoản? Đăng nhập</Link>
          <Button type="submit" variant="success" disabled={dangGui}>
            {dangGui && <Spinner size="sm" className="me-2" />}Đăng ký
          </Button>
        </div>
      </Form>
    </KhungDangNhap>
  );
}
