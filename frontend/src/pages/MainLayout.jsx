import { useCallback, useEffect, useState } from 'react';
import { Button, Container, Nav, Navbar, NavDropdown } from 'react-bootstrap';
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { authApi, thongBaoApi } from '../api/index.js';
import { CHU_KY_THONG_BAO_MS, TEN_VAI_TRO, VaiTro } from '../hangSo.js';
import { trangChinh } from '../dieuHuong.js';
import { usePhien } from '../components/PhienDangNhap.jsx';
import { useXacNhan } from '../components/XacNhan.jsx';
import Logo from '../components/Logo.jsx';

/** Chữ cái đầu của tên (tên người Việt nằm cuối họ tên). */
function chuCaiDau(hoTen) {
  const cac = (hoTen || '?').trim().split(/\s+/);
  return cac[cac.length - 1].charAt(0).toUpperCase();
}

/** 6.2.2 Khung chính: menu theo vai trò, nút thông báo có số đếm (hẹn giờ 60 giây, ARCH04), chân trang. */
export default function MainLayout({ onDangXuat }) {
  const nguoiDung = usePhien();
  const xacNhan = useXacNhan();
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

  // Đổi trang thì cập nhật số chưa đọc và cuộn lên đầu trang
  useEffect(() => {
    capNhatSoChuaDoc();
    window.scrollTo(0, 0);
  }, [viTri.pathname, capNhatSoChuaDoc]);

  async function dangXuat() {
    const ok = await xacNhan({ tieuDe: 'Đăng xuất', noiDung: 'Bạn muốn đăng xuất khỏi hệ thống?', nutDongY: 'Đăng xuất', mau: 'danger' });
    if (!ok) return;
    try {
      await authApi.dangXuat();
    } finally {
      onDangXuat();
    }
  }

  const vt = nguoiDung.vaiTro;
  return (
    <div className="d-flex flex-column min-vh-100">
      <Navbar variant="dark" expand="lg" collapseOnSelect className="thanh-menu sticky-top">
        <Container fluid>
          <Navbar.Brand as={Link} to={trangChinh(vt)} className="d-flex align-items-center gap-2" title="Về trang chính">
            <Logo size={28} la="#24552f" />
            <span>AGR-05</span>
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="menuChinh" />
          <Navbar.Collapse id="menuChinh">
            <Nav className="me-auto">
              {vt === VaiTro.NONG_DAN && (
                <>
                  <Nav.Link as={NavLink} to="/bao-cao/moi" eventKey="moi">Gửi báo cáo</Nav.Link>
                  <Nav.Link as={NavLink} to="/bao-cao/cua-toi" eventKey="cuaToi">Báo cáo của tôi</Nav.Link>
                </>
              )}
              {vt === VaiTro.CHUYEN_GIA && <Nav.Link as={NavLink} to="/xac-minh" eventKey="xacMinh">Xác minh báo cáo</Nav.Link>}
              {(vt === VaiTro.CHUYEN_GIA || vt === VaiTro.QUAN_TRI) && (
                <Nav.Link as={NavLink} to="/thong-ke" eventKey="thongKe">Thống kê và cảnh báo</Nav.Link>
              )}
              {vt === VaiTro.QUAN_TRI && (
                <NavDropdown title="Quản trị" id="menuQuanTri" active={viTri.pathname.startsWith('/quan-tri')}>
                  <NavDropdown.Item as={NavLink} to="/quan-tri/bao-cao" eventKey="baoCao">Tra cứu báo cáo</NavDropdown.Item>
                  <NavDropdown.Divider />
                  <NavDropdown.Item as={NavLink} to="/quan-tri/sau-benh" eventKey="sauBenh">Danh mục sâu bệnh</NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/quan-tri/khu-vuc" eventKey="khuVuc">Khu vực và phân công</NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/quan-tri/nguoi-dung" eventKey="nguoiDung">Người dùng</NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/quan-tri/cau-hinh" eventKey="cauHinh">Cấu hình hệ thống</NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/quan-tri/mo-hinh-ai" eventKey="moHinh">Mô hình AI</NavDropdown.Item>
                </NavDropdown>
              )}
            </Nav>
            <Nav className="align-items-lg-center gap-lg-2">
              <Button size="sm" className={'nut-thong-bao my-2 my-lg-0' + (viTri.pathname === '/thong-bao' ? ' dang-mo' : '')}
                onClick={() => dieuHuong('/thong-bao')} aria-label={`Thông báo, ${soChuaDoc} chưa đọc`}>
                Thông báo{soChuaDoc > 0 && <span className="so-dem">{soChuaDoc}</span>}
              </Button>
              <NavDropdown align="end" id="menuNguoiDung"
                title={<span className="d-inline-flex align-items-center"><span className="vong-ten">{chuCaiDau(nguoiDung.hoTen)}</span>{nguoiDung.hoTen}</span>}>
                <NavDropdown.Header>
                  <div className="fw-semibold">{nguoiDung.hoTen}</div>
                  <div className="small">{nguoiDung.tenDangNhap} – {TEN_VAI_TRO[vt]}</div>
                </NavDropdown.Header>
                <NavDropdown.Divider />
                <NavDropdown.Item as={Link} to={trangChinh(vt)}>Trang chính</NavDropdown.Item>
                <NavDropdown.Item as={Link} to="/thong-bao">Thông báo của tôi</NavDropdown.Item>
                <NavDropdown.Item as={Link} to="/doi-mat-khau">Đổi mật khẩu</NavDropdown.Item>
                <NavDropdown.Divider />
                <NavDropdown.Item className="text-danger" onClick={dangXuat}>Đăng xuất</NavDropdown.Item>
              </NavDropdown>
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>

      <main className="noi-dung-trang flex-grow-1">
        <Outlet />
      </main>

      <footer className="chan-trang py-2 px-3 d-flex flex-wrap justify-content-between align-items-center gap-2">
        <span>AGR-05 · Cảnh báo sâu bệnh dựa trên báo cáo hiện trường có xác minh</span>
        <span>
          {nguoiDung.hoTen} · {TEN_VAI_TRO[vt]}
          <Button variant="link" size="sm" className="text-danger p-0 ms-3 align-baseline" onClick={dangXuat}>Đăng xuất</Button>
        </span>
      </footer>
    </div>
  );
}
