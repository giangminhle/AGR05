import { useCallback, useEffect, useState } from 'react';
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { Spinner } from 'react-bootstrap';
import { authApi } from './api/index.js';
import { datXuLyHetPhien } from './api/client.js';
import { VaiTro } from './hangSo.js';
import { PhienContext } from './components/PhienDangNhap.jsx';
import { ThongBaoNhoProvider } from './components/ThongBaoNho.jsx';
import DangNhapPage from './pages/DangNhapPage.jsx';
import MainLayout from './pages/MainLayout.jsx';
import BaoCaoHienTruongPage from './pages/BaoCaoHienTruongPage.jsx';
import TheoDoiBaoCaoPage from './pages/TheoDoiBaoCaoPage.jsx';
import XacMinhPage from './pages/XacMinhPage.jsx';
import ThongBaoPage from './pages/ThongBaoPage.jsx';
import ThongKePage from './pages/ThongKePage.jsx';
import DanhMucSauBenhPage from './pages/DanhMucSauBenhPage.jsx';
import KhuVucPage from './pages/KhuVucPage.jsx';
import NguoiDungPage from './pages/NguoiDungPage.jsx';
import CauHinhPage from './pages/CauHinhPage.jsx';
import MoHinhAIPage from './pages/MoHinhAIPage.jsx';

const { NONG_DAN, CHUYEN_GIA, QUAN_TRI } = VaiTro;

/** Trang chính theo vai trò. */
function trangChinh(vaiTro) {
  if (vaiTro === NONG_DAN) return '/bao-cao/cua-toi';
  if (vaiTro === CHUYEN_GIA) return '/xac-minh';
  return '/thong-ke';
}

/** Chỉ hiện trang đúng vai trò; sai vai trò thì về trang chính (quyền thật do backend kiểm tra). */
function TheoVaiTro({ nguoiDung, cho, children }) {
  if (!cho.includes(nguoiDung.vaiTro)) return <Navigate to={trangChinh(nguoiDung.vaiTro)} replace />;
  return children;
}

export default function App() {
  const [nguoiDung, setNguoiDung] = useState(undefined);
  const dieuHuong = useNavigate();

  useEffect(() => {
    authApi.toi().then(setNguoiDung).catch(() => setNguoiDung(null));
  }, []);

  const hetPhien = useCallback(() => {
    setNguoiDung(null);
    dieuHuong('/dang-nhap');
  }, [dieuHuong]);

  useEffect(() => datXuLyHetPhien(hetPhien), [hetPhien]);

  if (nguoiDung === undefined) {
    return (
      <div className="d-flex justify-content-center mt-5">
        <Spinner animation="border" />
      </div>
    );
  }

  if (!nguoiDung) {
    return (
      <ThongBaoNhoProvider>
        <Routes>
          <Route path="*" element={<DangNhapPage onDangNhap={(nd) => { setNguoiDung(nd); dieuHuong(trangChinh(nd.vaiTro)); }} />} />
        </Routes>
      </ThongBaoNhoProvider>
    );
  }

  const bao = (cho, el) => <TheoVaiTro nguoiDung={nguoiDung} cho={cho}>{el}</TheoVaiTro>;

  return (
    <PhienContext.Provider value={nguoiDung}>
      <ThongBaoNhoProvider>
        <Routes>
          <Route element={<MainLayout onDangXuat={() => { setNguoiDung(null); dieuHuong('/dang-nhap'); }} />}>
            <Route path="/bao-cao/moi" element={bao([NONG_DAN], <BaoCaoHienTruongPage />)} />
            <Route path="/bao-cao/:ma/sua" element={bao([NONG_DAN], <BaoCaoHienTruongPage />)} />
            <Route path="/bao-cao/cua-toi" element={bao([NONG_DAN], <TheoDoiBaoCaoPage />)} />
            <Route path="/xac-minh" element={bao([CHUYEN_GIA], <XacMinhPage />)} />
            <Route path="/thong-bao" element={<ThongBaoPage />} />
            <Route path="/thong-ke" element={bao([CHUYEN_GIA, QUAN_TRI], <ThongKePage />)} />
            <Route path="/quan-tri/sau-benh" element={bao([QUAN_TRI], <DanhMucSauBenhPage />)} />
            <Route path="/quan-tri/khu-vuc" element={bao([QUAN_TRI], <KhuVucPage />)} />
            <Route path="/quan-tri/nguoi-dung" element={bao([QUAN_TRI], <NguoiDungPage />)} />
            <Route path="/quan-tri/cau-hinh" element={bao([QUAN_TRI], <CauHinhPage />)} />
            <Route path="/quan-tri/mo-hinh-ai" element={bao([QUAN_TRI], <MoHinhAIPage />)} />
            <Route path="*" element={<Navigate to={trangChinh(nguoiDung.vaiTro)} replace />} />
          </Route>
        </Routes>
      </ThongBaoNhoProvider>
    </PhienContext.Provider>
  );
}
