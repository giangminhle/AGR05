import { useEffect, useState } from 'react';
import { Alert, Button, Form, Spinner } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { authApi } from '../api/index.js';
import KhungDangNhap from '../components/KhungDangNhap.jsx';

/** 6.2.1 Đăng nhập (UC01). Sai tên hoặc sai mật khẩu báo chung một câu (do backend trả về). */
export default function DangNhapPage({ onDangNhap }) {
  const [tenDangNhap, setTenDangNhap] = useState('');
  const [matKhau, setMatKhau] = useState('');
  const [thongBaoLoi, setThongBaoLoi] = useState('');
  const [dangGui, setDangGui] = useState(false);

  useEffect(() => {
    document.title = 'Đăng nhập – AGR-05';
  }, []);

  async function nutDangNhap(e) {
    e.preventDefault();
    setThongBaoLoi('');
    setDangGui(true);
    try {
      const nd = await authApi.dangNhap(tenDangNhap, matKhau);
      onDangNhap(nd);
    } catch (loi) {
      setThongBaoLoi(loi.loi);
      setDangGui(false);
    }
  }

  return (
    <KhungDangNhap tieuDe="Đăng nhập" moTa="Hệ thống cảnh báo sâu bệnh dựa trên báo cáo hiện trường có xác minh">
      {thongBaoLoi && <Alert variant="danger">{thongBaoLoi}</Alert>}
      <Form onSubmit={nutDangNhap}>
        <Form.Group className="mb-3" controlId="tenDangNhap">
          <Form.Label>Tên đăng nhập</Form.Label>
          <Form.Control value={tenDangNhap} maxLength={50} autoFocus autoComplete="username"
            onChange={(e) => setTenDangNhap(e.target.value)} />
        </Form.Group>
        <Form.Group className="mb-4" controlId="matKhau">
          <Form.Label>Mật khẩu</Form.Label>
          <Form.Control type="password" value={matKhau} autoComplete="current-password"
            onChange={(e) => setMatKhau(e.target.value)} />
        </Form.Group>
        <Button type="submit" variant="success" className="w-100" disabled={dangGui}>
          {dangGui && <Spinner size="sm" className="me-2" />}Đăng nhập
        </Button>
      </Form>
      <div className="mt-4 small">
        Nông dân chưa có tài khoản? <Link to="/dang-ky">Đăng ký</Link>
      </div>
      <div className="text-muted small mt-1">Quên mật khẩu: liên hệ Quản trị viên để được đặt lại.</div>
    </KhungDangNhap>
  );
}
