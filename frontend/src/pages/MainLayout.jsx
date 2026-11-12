import { useCallback, useEffect, useState } from 'react';
import { Badge, Button, Container, Nav, Navbar, NavDropdown } from 'react-bootstrap';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { authApi, thongBaoApi } from '../api/index.js';
import { CHU_KY_THONG_BAO_MS, TEN_VAI_TRO, VaiTro } from '../hangSo.js';
import { usePhien } from '../components/PhienDangNhap.jsx';

/** 6.2.2 Khung chính: menu theo vai trò, nút thông báo có số đếm (hẹn giờ 60 giây, ARCH04), chân trang. */
export default function MainLayout({ onDangXuat }) {
  const nguoiDung = usePhien();
  const dieuHuong = useNavigate();
  const viTri = useLocation();
  const [soChuaDoc, setSoChuaDoc] = useState(0);

  const capNhatSoChuaDoc = useCallback(() => {
    thongBaoApi.demChuaDoc().then((r) => setSoChuaDoc(r.soLuong)).catch(() => {});
  }, []);

  useEffect(() => {
    capNhatSoChuaDoc();
    const henGio = setInterval(capNhatSoChuaDoc, CHU_KY_THONG_BAO_MS);
    window.addEventListener('agr05:thong-bao-thay-doi', capNhatSoChuaDoc);
    return () => {
      clearInterval(henGio);
      window.removeEventListener('agr05:thong-bao-thay-doi', capNhatSoChuaDoc);
    };
  }, [capNhatSoChuaDoc]);

  // Đổi trang thì cập nhật lại số chưa đọc (vừa đọc thông báo xong chẳng hạn)
  useEffect(capNhatSoChuaDoc, [viTri.pathname, capNhatSoChuaDoc]);

  async function dangXuat() {
    try {
      await authApi.dangXuat();
    } finally {
      onDangXuat();
    }
  }

  const vt = nguoiDung.vaiTro;
  return (
    <>
      <Navbar bg="success" variant="dark" expand="lg" className="mb-3">
        <Container fluid>
          <Navbar.Brand>AGR-05</Navbar.Brand>
          <Navbar.Toggle aria-controls="menuChinh" />
          <Navbar.Collapse id="menuChinh">
            <Nav className="me-auto">
              {vt === VaiTro.NONG_DAN && (
                <>
                  <Nav.Link as={NavLink} to="/bao-cao/moi">Gửi báo cáo</Nav.Link>
                  <Nav.Link as={NavLink} to="/bao-cao/cua-toi">Báo cáo của tôi</Nav.Link>
                </>
              )}
              {vt === VaiTro.CHUYEN_GIA && <Nav.Link as={NavLink} to="/xac-minh">Xác minh báo cáo</Nav.Link>}
              {(vt === VaiTro.CHUYEN_GIA || vt === VaiTro.QUAN_TRI) && (
                <Nav.Link as={NavLink} to="/thong-ke">Thống kê và cảnh báo</Nav.Link>
              )}
              {vt === VaiTro.QUAN_TRI && (
                <NavDropdown title="Quản trị" id="menuQuanTri">
                  <NavDropdown.Item as={NavLink} to="/quan-tri/sau-benh">Danh mục sâu bệnh</NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/quan-tri/khu-vuc">Khu vực và phân công</NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/quan-tri/nguoi-dung">Người dùng</NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/quan-tri/cau-hinh">Cấu hình hệ thống</NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/quan-tri/mo-hinh-ai">Mô hình AI</NavDropdown.Item>
                </NavDropdown>
              )}
            </Nav>
            <Button variant="light" size="sm" onClick={() => dieuHuong('/thong-bao')} aria-label="Thông báo">
              Thông báo <Badge bg={soChuaDoc > 0 ? 'danger' : 'secondary'}>{soChuaDoc}</Badge>
            </Button>
          </Navbar.Collapse>
        </Container>
      </Navbar>
      <Container fluid className="vung-noi-dung">
        <Outlet />
      </Container>
      <footer className="border-top bg-white py-2 px-3 mt-3 d-flex justify-content-between align-items-center">
        <small>
          {nguoiDung.hoTen} ({nguoiDung.tenDangNhap}) – {TEN_VAI_TRO[vt]}
        </small>
        <Button size="sm" variant="outline-secondary" onClick={dangXuat}>Đăng xuất</Button>
      </footer>
    </>
  );
}
