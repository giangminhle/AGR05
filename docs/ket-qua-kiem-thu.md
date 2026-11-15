# Kết quả kiểm thử (mục 7.2)

Chạy lúc 22:30 12/11/2026 bằng JUnit 5 (`mvn verify`). Tổng 72 test, đạt 72.
Các test chức năng dùng DAL giả trong bộ nhớ, không cần SQL Server. Lần chạy này có cả 2 test tích hợp với SQL Server thật (`DalTichHopTest`).

## Bảng test case

| Mã | UC | Dữ liệu / thao tác | Kết quả mong đợi | Kết quả thực tế | Test JUnit |
|---|---|---|---|---|---|
| TC01 | UC02 | Đủ trường, 3 ảnh JPG 2 MB, tọa độ (10.77, 106.70) | Lưu Chờ xác minh, chuyên gia khu vực nhận thông báo (BR01, BR07) | Đạt | `BaoCaoServiceTest.TC01_guiBaoCao_hopLe_luuChoXacMinh_vaBaoChuyenGia` |
| TC02 | UC02 | Không chọn ảnh nào | Báo phải có ít nhất 1 ảnh, không lưu (BR01) | Đạt | `BaoCaoServiceTest.TC02_guiBaoCao_khongCoAnh_baoLoi_khongLuu` |
| TC03 | UC02 | Chọn 6 ảnh, hoặc 1 ảnh 7 MB, hoặc vĩ độ = 95 | Từ chối, nêu rõ lý do, giữ dữ liệu (BR01) | Đạt | `BaoCaoServiceTest.TC03_guiBaoCao_anh7MB_baoLoi`<br>`BaoCaoServiceTest.TC03_guiBaoCao_fileDoiDuoi_khongPhaiAnh_baoLoi`<br>`BaoCaoServiceTest.TC03_guiBaoCao_sauAnh_baoLoi`<br>`BaoCaoServiceTest.TC03_guiBaoCao_viDo95_baoLoi_chiRoO` |
| TC04 | UC02 | Mất kết nối CSDL khi đang ghi | Rollback, xóa file ảnh đã lưu, form giữ nội dung | Đạt | `BaoCaoServiceTest.TC04_guiBaoCao_matKetNoiKhiGhi_rollback_xoaFileAnh` |
| TC05 | UC07 | Bấm Duyệt khi chưa chọn loại sâu bệnh | Yêu cầu chọn loại, trạng thái không đổi (BR04) | Đạt | `XacMinhServiceTest.TC05_duyet_khiChuaChonLoai_baoLoi_trangThaiKhongDoi` |
| TC06 | UC08 | Bấm Từ chối với lý do để trống | Chặn thao tác, báo phải nhập lý do (BR04) | Đạt | `XacMinhServiceTest.TC06_tuChoi_lyDoTrong_baoLoi` |
| TC07 | UC07 | Chuyên gia khu vực B duyệt báo cáo khu vực A | Báo không đủ quyền, không ghi (BR05) | Đạt | `XacMinhServiceTest.TC07_chuyenGiaKhuVucB_duyetBaoCaoKhuVucA_khongDuQuyen` |
| TC08 | UC07 | Hai chuyên gia cùng duyệt một báo cáo | Người sau nhận "Báo cáo đã được xử lý" (BR03) | Đạt | `XacMinhServiceTest.TC08_duyetBaoCaoDaDuyet_baoDaXuLy`<br>`XacMinhServiceTest.TC08_haiChuyenGiaCungDuyet_nguoiSauNhanDaXuLy` |
| TC09 | UC07 | Duyệt ca thứ 5 cùng loại, cùng khu vực trong 7 ngày | Tạo cảnh báo, thông báo cho khu vực (BR06) | Đạt | `XacMinhServiceTest.TC09_duyetCaThu5_cungLoaiCungKhuVuc_taoCanhBao` |
| TC10 | UC03 | Gửi lại báo cáo bị bác bỏ lần thứ hai | Báo hết lượt gửi lại, không lưu (BR04) | Đạt | `BaoCaoServiceTest.TC10_guiLai_lanThuHai_baoHetLuot_khongLuu` |
| TC11 | UC18 | Duyệt báo cáo khi chưa có mô hình đang dùng | Duyệt thành công, không có dự báo, chỉ áp dụng BR06 | Đạt | `AIServiceTest.TC11_duyet_khiChuaCoMoHinh_thanhCong_khongCoDuBao`<br>`AIServiceTest.TC11_moHinhLoiKhiNap_duyetVanThanhCong` |
| TC12 | UC18 | Dự báo xác suất bằng NGUONG_NGUY_CO, chưa có cảnh báo trong kỳ | Mức Cao, tạo một cảnh báo (BR08) | Đạt | `AIServiceTest.TC12_xacSuatBangNguong_mucCao_taoMotCanhBao` |
| TC13 | UC18 | Hai lần dự báo mức Cao cùng loại, cùng khu vực trong 7 ngày | Chỉ tạo một cảnh báo, lần sau chỉ lưu dự báo | Đạt | `AIServiceTest.TC13_haiLanDuBaoCao_cungLoaiCungKhuVuc_chiMotCanhBao` |
| TC14 | UC19 | Huấn luyện khi dữ liệu chỉ có nhãn 0 | Báo chưa đủ dữ liệu, giữ phiên bản cũ | Đạt | `AIServiceTest.TC14_huanLuyen_khiChiCoNhan0_baoChuaDuDuLieu_giuBanCu` |
| TC15 | UC19 | Kích hoạt bản mới có F1 thấp hơn bản đang dùng | Từ chối kích hoạt (ARCH05) | Đạt | `AIServiceTest.TC15_kichHoat_banMoiF1ThapHon_tuChoi` |
| TC16 | UC16 | Đặt NGUONG_NGUY_CO = 0 hoặc 150 | Báo giá trị không hợp lệ, không lưu | Đạt | `QuanTriServiceTest.TC16_nguongNguyCo_0_hoac_150_baoKhongHopLe_khongLuu` |

## Các test khác (quy tắc trên entity, quyền, chức năng nền, kiến trúc)

| Test | Kết quả |
|---|---|
| `AIServiceTest.huanLuyen_duHaiNhan_luuPhienBanChuaDung` | Đạt |
| `AIServiceTest.kichHoat_banMoiF1BangNhau_duocKichHoat` | Đạt |
| `AIServiceTest.smileGradientTreeBoost_hocDuocVaLuuNapFile` | Đạt |
| `AIServiceTest.xepMuc_theoNguong` | Đạt |
| `BaoCaoHienTruongTest.choPhepSua_theoTrangThaiVaNguoiGui` | Đạt |
| `BaoCaoHienTruongTest.chuyenTrangThai_dungThuTu` | Đạt |
| `BaoCaoHienTruongTest.chuyenTrangThai_nhayBuoc_bao_loi` | Đạt |
| `BaoCaoHienTruongTest.duBao_vaMoHinh` | Đạt |
| `BaoCaoHienTruongTest.kiemTraHopLe_bienSoAnh(int, String)[1]` | Đạt |
| `BaoCaoHienTruongTest.kiemTraHopLe_bienSoAnh(int, String)[2]` | Đạt |
| `BaoCaoHienTruongTest.kiemTraHopLe_bienSoAnh(int, String)[3]` | Đạt |
| `BaoCaoHienTruongTest.kiemTraHopLe_bienSoAnh(int, String)[4]` | Đạt |
| `BaoCaoHienTruongTest.kiemTraHopLe_bienViDo(String, String)[1]` | Đạt |
| `BaoCaoHienTruongTest.kiemTraHopLe_bienViDo(String, String)[2]` | Đạt |
| `BaoCaoHienTruongTest.kiemTraHopLe_bienViDo(String, String)[3]` | Đạt |
| `BaoCaoHienTruongTest.kiemTraHopLe_bienViDo(String, String)[4]` | Đạt |
| `BaoCaoHienTruongTest.kiemTraHopLe_ngayPhatHienSauHomNay_sai` | Đạt |
| `BaoCaoHienTruongTest.laQuaHan_sau48Gio` | Đạt |
| `BaoCaoHienTruongTest.matKhau_xacThuc` | Đạt |
| `BaoCaoServiceTest.guiLai_lanDau_chuyenVeChoXacMinh_tangSoLan` | Đạt |
| `BaoCaoServiceTest.layDeSua_baoCaoNguoiKhac_khongDuQuyen` | Đạt |
| `BaoCaoServiceTest.quanTri_timKiemVaXemChiTiet` | Đạt |
| `BaoCaoServiceTest.sua_khiChuyenGiaVuaDuyet_baoDaXuLy` | Đạt |
| `DalTichHopTest.baoCaoDal_themDoc_doiTrangThaiCoDieuKien` | Đạt |
| `DalTichHopTest.thongBaoDal_demChuaDoc_vaChiNguoiNhanDanhDauDuoc` | Đạt |
| `KienTrucTest.apiKhongDungJdbc` | Đạt |
| `KienTrucTest.chiAIServiceGoiSmile` | Đạt |
| `KienTrucTest.chiDalCoJdbc` | Đạt |
| `KienTrucTest.entityKhongPhuThuocSpring` | Đạt |
| `KienTrucTest.phanTang` | Đạt |
| `QuanTriServiceTest.chucNangNen_ganCoQuaHan_vaDongHoSoHetHan` | Đạt |
| `QuanTriServiceTest.dangKy_luonLaNongDan_dangNhapDuoc` | Đạt |
| `QuanTriServiceTest.dangNhap_dung_luuAppSession` | Đạt |
| `QuanTriServiceTest.dangNhap_saiTen_vaSaiMatKhau_cungMotCau` | Đạt |
| `QuanTriServiceTest.dangNhap_taiKhoanBiKhoa_baoRieng` | Đạt |
| `QuanTriServiceTest.datLaiMatKhau_choNguoiQuen` | Đạt |
| `QuanTriServiceTest.doiMatKhau_saiMatKhauCu_baoLoi_dungThiDoiDuoc` | Đạt |
| `QuanTriServiceTest.khongKhoaChinhTaiKhoanMinh` | Đạt |
| `QuanTriServiceTest.phanCong_trung_baoLoi` | Đạt |
| `QuanTriServiceTest.phienDangMo_biKhoaGiuaChung_khongConHieuLuc` | Đạt |
| `QuanTriServiceTest.taoTaiKhoan_bamMatKhau_dangNhapDuoc` | Đạt |
| `QuanTriServiceTest.themLoaiMoi_baoMoiChuyenGiaDangHoatDong` | Đạt |
| `QuanTriServiceTest.thongBao_khongDanhDauDuocCuaNguoiKhac` | Đạt |
| `QuanTriServiceTest.thongKe_chuyenGiaXemKhuVucKhac_khongDuQuyen` | Đạt |
| `XacMinhServiceTest.danhDauDaXuLy_tuDaXacMinh` | Đạt |
| `XacMinhServiceTest.deNghiThemLoai_baoQuanTri_ghiLichSu_baoCaoVanCho` | Đạt |
| `XacMinhServiceTest.deNghiThemLoai_tenTrong_daCoTrongDanhMuc_khongPhuTrach_baoLoi` | Đạt |
| `XacMinhServiceTest.duyetCaThu4_chuaDuNguong_khongTaoCanhBao` | Đạt |
| `XacMinhServiceTest.khongDuocXacMinhBaoCaoCuaChinhMinh` | Đạt |
| `XacMinhServiceTest.layDanhSachCho_chiKhuVucPhuTrach_vaGanCoQuaHan` | Đạt |
| `XacMinhServiceTest.tuChoi_baoCaoDaGuiLaiMotLan_dongHoSoLuon` | Đạt |
