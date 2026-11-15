import { Col, Container, Row } from 'react-bootstrap';
import Logo from './Logo.jsx';

/** Khung chung của trang đăng nhập và đăng ký: ảnh ruộng lúa bên trái, biểu mẫu bên phải (ảnh ẩn trên điện thoại). */
export default function KhungDangNhap({ tieuDe, moTa, children }) {
  return (
    <Container fluid className="trang-dang-nhap d-flex justify-content-center align-items-center py-4">
      <div className="khung-dang-nhap">
        <Row className="g-0">
          <Col md={6} className="anh-dang-nhap d-none d-md-block">
            <div className="chu-thich">
              <div className="fw-semibold mb-1">Phát hiện sớm, cảnh báo kịp thời</div>
              <div>Nông dân gửi báo cáo kèm ảnh, chuyên gia xác minh, hệ thống cảnh báo dịch cho cả khu vực.</div>
              <div className="mt-2 opacity-75" style={{ fontSize: '0.7rem' }}>Ảnh: Mig Gilbert, CC BY-SA 2.0</div>
            </div>
          </Col>
          <Col md={6}>
            <div className="form-dang-nhap">
              <div className="d-flex align-items-center gap-2 mb-4">
                <Logo size={34} nen="#2f6b3f" la="#ffffff" />
                <span className="fw-semibold" style={{ color: '#24552f' }}>AGR-05</span>
              </div>
              <h1 className="mb-1">{tieuDe}</h1>
              {moTa && <p className="text-muted mb-4">{moTa}</p>}
              {children}
            </div>
          </Col>
        </Row>
      </div>
    </Container>
  );
}
