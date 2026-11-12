import { useState } from 'react';
import { Alert, Button, Card, Container, Form, Spinner } from 'react-bootstrap';
import { authApi } from '../api/index.js';

/** 6.2.1 Đăng nhập (UC01). Sai tên hoặc sai mật khẩu báo chung một câu (do backend trả về). */
export default function DangNhapPage({ onDangNhap }) {
  const [tenDangNhap, setTenDangNhap] = useState('');
  const [matKhau, setMatKhau] = useState('');
  const [thongBaoLoi, setThongBaoLoi] = useState('');
  const [dangGui, setDangGui] = useState(false);

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
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Card style={{ width: 380 }} className="shadow-sm">
        <Card.Body>
          <h4 className="mb-1 text-success">AGR-05</h4>
          <p className="text-muted mb-4">Hệ thống cảnh báo sâu bệnh</p>
          {thongBaoLoi && <Alert variant="danger">{thongBaoLoi}</Alert>}
          <Form onSubmit={nutDangNhap}>
            <Form.Group className="mb-3" controlId="tenDangNhap">
              <Form.Label>Tên đăng nhập</Form.Label>
              <Form.Control value={tenDangNhap} maxLength={50} autoFocus onChange={(e) => setTenDangNhap(e.target.value)} />
            </Form.Group>
            <Form.Group className="mb-4" controlId="matKhau">
              <Form.Label>Mật khẩu</Form.Label>
              <Form.Control type="password" value={matKhau} onChange={(e) => setMatKhau(e.target.value)} />
            </Form.Group>
            <div className="d-flex justify-content-end">
              <Button type="submit" variant="success" disabled={dangGui}>
                {dangGui && <Spinner size="sm" className="me-2" />}Đăng nhập
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
}
