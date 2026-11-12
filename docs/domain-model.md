# Mô hình miền (lớp thực thể, trạng thái)

### 3.1.2 Danh sách các lớp đối tượng và quan hệ
Bảng đầu liệt kê 12 lớp thực thể. Bảng sau liệt kê các quan hệ giữa chúng, đọc theo chiều Lớp A – Lớp B.

a) Danh sách các lớp

| Lớp | Định danh | Vai trò |
|---|---|---|
| NguoiDung | maNguoiDung | Tài khoản đăng nhập, có vai trò Nông dân, Chuyên gia hoặc Quản trị viên. |
| KhuVuc | maKhuVuc | Vùng canh tác. Báo cáo thuộc một khu vực, chuyên gia được giao phụ trách. |
| LoaiSauBenh | maLoai | Danh mục sâu bệnh, kèm triệu chứng và biện pháp phòng trừ. |
| BaoCaoHienTruong | maBaoCao | Báo cáo của nông dân: mô tả, tọa độ, trạng thái, khuyến nghị của chuyên gia. |
| HinhAnh | maHinhAnh | Ảnh hiện trường của báo cáo (lưu đường dẫn file). |
| LichSuXacMinh | maLichSu | Nhật ký mỗi lần đổi trạng thái: ai làm, lúc nào, ghi chú. |
| CanhBao | maCanhBao | Cảnh báo dịch theo khu vực và loại sâu bệnh, do BR06 hoặc do dự báo AI (BR08) tạo ra. |
| ThongBao | maThongBao | Thông báo nội bộ gửi cho một người dùng. Có thể gắn với một báo cáo. loai gồm: báo cáo mới, kết quả xác minh, quá hạn, đóng hồ sơ, cảnh báo dịch. |
| ThamSoHeThong | maThamSo | Bảng tham số cấu hình (thời hạn xác minh, ngưỡng cảnh báo, số ngày xét ngưỡng, hạn gửi lại, ngưỡng nguy cơ của mô hình AI). |
| DuBaoNguyCo | maDuBao | Kết quả một lần dự báo sau khi duyệt: xác suất, mức nguy cơ, số ngày dự báo. Khu vực và loại sâu bệnh lấy qua báo cáo kích hoạt. |
| MoHinhAI | maMoHinh | Một phiên bản mô hình đã huấn luyện: ngày huấn luyện, số mẫu, độ chính xác, có đang dùng hay không. |
| PhanCongKhuVuc | (maNguoiDung, maKhuVuc) | Lớp liên kết: chuyên gia nào phụ trách khu vực nào, từ ngày nào. |

b) Quan hệ giữa các lớp

| Lớp A | Quan hệ | Lớp B | Bội số và ý nghĩa |
|---|---|---|---|
| NguoiDung | Liên kết n-n, lớp liên kết PhanCongKhuVuc | KhuVuc | 0..* – 0..*: chuyên gia phụ trách nhiều khu vực, khu vực có nhiều chuyên gia. |
| NguoiDung | Liên kết | BaoCaoHienTruong | 1 – 0..*: một nông dân gửi nhiều báo cáo. |
| NguoiDung | Liên kết | LichSuXacMinh | 0..1 – 0..*: người thực hiện; rỗng khi hệ thống tự làm. |
| NguoiDung | Liên kết | ThongBao | 1 – 0..*: một người nhận nhiều thông báo. |
| KhuVuc | Liên kết | BaoCaoHienTruong | 1 – 0..*: báo cáo thuộc một khu vực. |
| KhuVuc | Liên kết | CanhBao | 1 – 0..*: cảnh báo cho một khu vực. |
| LoaiSauBenh | Liên kết | BaoCaoHienTruong | 0..1 – 0..*: báo cáo chưa phân loại thì chưa có loại. |
| LoaiSauBenh | Liên kết | CanhBao | 1 – 0..*: cảnh báo về một loại. |
| BaoCaoHienTruong | Hợp thành | HinhAnh | 1 – 1..5: ảnh không tồn tại khi thiếu báo cáo. |
| BaoCaoHienTruong | Hợp thành | LichSuXacMinh | 1 – 0..*: lịch sử thuộc về báo cáo. |
| BaoCaoHienTruong | Liên kết | ThongBao | 0..1 – 0..*: thông báo có thể gắn với một báo cáo. |
| BaoCaoHienTruong | Liên kết | DuBaoNguyCo | 1 – 0..1: mỗi lần duyệt sinh tối đa một dự báo. |
| DuBaoNguyCo | Liên kết | CanhBao | 0..1 – 0..1: dự báo mức Cao sinh tối đa một cảnh báo. |
| MoHinhAI | Liên kết | DuBaoNguyCo | 1 – 0..*: mỗi dự báo ghi phiên bản mô hình đã dùng. |
| ThamSoHeThong | Không có |  | Bảng tham số độc lập. |


### 3.1.3 Mô tả chi tiết từng đối tượng
Mục này mô tả thuộc tính và phương thức của từng lớp thực thể. Kiểu dữ liệu ghi ở mức phân tích; kiểu SQL cụ thể nằm ở Chương IV.

a) NguoiDung

| Thuộc tính / phương thức | Kiểu | Ý nghĩa |
|---|---|---|
| maNguoiDung | Số nguyên | Định danh. |
| tenDangNhap | Chuỗi | Duy nhất. |
| matKhauHash, muoi | Chuỗi | Mật khẩu đã băm PBKDF2 và muối (SEC01). |
| hoTen, dienThoai | Chuỗi |  |
| vaiTro | Nông dân / Chuyên gia / Quản trị viên |  |
| biKhoa | Logic | Tài khoản bị khóa. |
| xacThucMatKhau(mk) | Phương thức | Băm mật khẩu nhập vào và so với giá trị lưu. |

b) KhuVuc

| Thuộc tính / phương thức | Kiểu | Ý nghĩa |
|---|---|---|
| maKhuVuc | Số nguyên | Định danh. |
| tenKhuVuc | Chuỗi | Duy nhất. |
| moTa | Chuỗi |  |

c) PhanCongKhuVuc

| Thuộc tính / phương thức | Kiểu | Ý nghĩa |
|---|---|---|
| (maNguoiDung, maKhuVuc) | Định danh ghép | Một chuyên gia không bị giao hai lần cùng khu vực. |
| ngayPhanCong | Ngày giờ |  |

d) LoaiSauBenh

| Thuộc tính / phương thức | Kiểu | Ý nghĩa |
|---|---|---|
| maLoai | Số nguyên | Định danh. |
| tenLoai | Chuỗi | Duy nhất. |
| trieuChung, bienPhapPhongTru | Chuỗi | Dùng để gợi ý khuyến nghị và nội dung cảnh báo. |

e) BaoCaoHienTruong

| Thuộc tính / phương thức | Kiểu | Ý nghĩa |
|---|---|---|
| maBaoCao | Số nguyên | Định danh. |
| tieuDe, tenCayTrong, moTa | Chuỗi | Bắt buộc. |
| mucDo | 1..3 |  |
| viDo, kinhDo | Số thực | BR01. |
| trangThai | Chờ xác minh / Đã xác minh / Bị bác bỏ / Đã xử lý |  |
| khuyenNghi | Chuỗi | Do chuyên gia ghi. |
| ngayPhatHien, ngayGui, ngayVaoTrangThai | Ngày giờ | ngayVaoTrangThai dùng để tính hạn. |
| soLanGuiLai | 0 hoặc 1 | BR04. |
| quaHan | Logic | BR02. |
| kiemTraHopLe(soAnh), chuyenTrangThai(tt), laQuaHan(hanGio), choPhepSua(maND, hanNgay) | Phương thức | Các kiểm tra nghiệp vụ nằm trên entity. |

f) HinhAnh

| Thuộc tính / phương thức | Kiểu | Ý nghĩa |
|---|---|---|
| maHinhAnh | Số nguyên | Định danh. |
| duongDan | Chuỗi | Đường dẫn file trong thư mục ứng dụng. |
| ngayTai | Ngày giờ |  |

g) LichSuXacMinh

| Thuộc tính / phương thức | Kiểu | Ý nghĩa |
|---|---|---|
| maLichSu | Số nguyên | Định danh. |
| trangThaiCu, trangThaiMoi | Trạng thái | trangThaiCu rỗng ở dòng đầu. |
| ghiChu | Chuỗi | Bắt buộc khi bác bỏ. |
| thoiGian | Ngày giờ |  |

h) CanhBao

| Thuộc tính / phương thức | Kiểu | Ý nghĩa |
|---|---|---|
| maCanhBao | Số nguyên | Định danh. |
| noiDung | Chuỗi |  |
| ngayTao | Ngày giờ |  |

i) ThongBao

| Thuộc tính / phương thức | Kiểu | Ý nghĩa |
|---|---|---|
| maThongBao | Số nguyên | Định danh. |
| loai | Báo cáo mới / Kết quả xác minh / Quá hạn / Đóng hồ sơ / Cảnh báo dịch |  |
| tieuDe, noiDung | Chuỗi |  |
| daDoc | Logic | BR07. |

j) ThamSoHeThong

| Thuộc tính / phương thức | Kiểu | Ý nghĩa |
|---|---|---|
| maThamSo | Chuỗi | HAN_XAC_MINH_GIO, NGUONG_CANH_BAO, SO_NGAY_XET_NGUONG, HAN_GUI_LAI_NGAY, NGUONG_NGUY_CO. |
| giaTri | Số nguyên dương |  |
| moTa | Chuỗi |  |

k) DuBaoNguyCo

| Thuộc tính / phương thức | Kiểu | Ý nghĩa |
|---|---|---|
| maDuBao | Số nguyên | Định danh. |
| xacSuat | Số thực 0..1 | Xác suất bùng phát trong soNgayDuBao ngày tới. |
| mucNguyCo | Thấp / Trung bình / Cao | Xếp theo NGUONG_NGUY_CO. |
| soNgayDuBao | Số nguyên | Mặc định 7. |
| ngayDuBao | Ngày giờ |  |
| laNguyCoCao() | Phương thức | Trả về true khi mức Cao. |

l) MoHinhAI

| Thuộc tính / phương thức | Kiểu | Ý nghĩa |
|---|---|---|
| maMoHinh | Số nguyên | Định danh. |
| phienBan | Chuỗi | Duy nhất. |
| ngayHuanLuyen, soMauHuanLuyen | Ngày giờ, số nguyên |  |
| accuracy, f1, auc | Số thực 0..1 | Đo trên tập kiểm tra. |
| dangSuDung | Logic | Chỉ một phiên bản được bật. |
| khongKemHon(mh) | Phương thức | So F1 với phiên bản khác (ARCH05). |


### 3.1.4 Lớp tham gia các use case chính
Ba bảng dưới liệt kê các lớp tham gia ba use case chính, chia theo Boundary, Control và Entity. Đây là cầu nối từ biểu đồ lớp phân tích sang các biểu đồ tuần tự ở mục 3.3.

a) Lớp tham gia UC02: Gửi báo cáo hiện trường

Bảng dưới liệt kê các lớp tham gia UC02 theo ba loại Boundary, Control, Entity. Lớp Boundary và Control chỉ dùng trong sơ đồ tuần tự (mục 3.3), lớp Entity lấy từ biểu đồ lớp phân tích.

| Lớp | Loại | Vai trò |
|---|---|---|
| BaoCaoHienTruongPage, BaoCaoController | Boundary | Trang nhập thông tin, chọn ảnh, nhập tọa độ; API nhận báo cáo. |
| BaoCaoService | Control | Kiểm tra BR01, điều khiển transaction lưu báo cáo. |
| AnhStorage | Control | Kiểm tra định dạng, dung lượng ảnh; lưu và xóa file ảnh. |
| ThongBaoService | Control | Tạo thông báo cho chuyên gia khu vực trong cùng transaction. |
| BaoCaoHienTruong | Entity | Báo cáo mới, tự kiểm tra hợp lệ (kiemTraHopLe). |
| HinhAnh | Entity | Đường dẫn từng ảnh của báo cáo. |
| LichSuXacMinh | Entity | Dòng lịch sử Khởi tạo → Chờ xác minh. |
| ThongBao | Entity | Thông báo cho chuyên gia. |

b) Lớp tham gia UC07: Xác minh báo cáo

UC07 dùng thêm lớp AIService để gọi mô hình dự báo sau khi duyệt. AIService không truy cập CSDL trực tiếp mà qua lớp DAL như các Service khác (ARCH01).

| Lớp | Loại | Vai trò |
|---|---|---|
| XacMinhPage, XacMinhController | Boundary | Danh sách chờ, chi tiết báo cáo, ô chọn loại sâu bệnh, nút Duyệt / Từ chối; API xác minh. |
| XacMinhService | Control | Kiểm tra quyền, trạng thái, đổi trạng thái trong transaction. |
| CanhBaoService | Control | Kiểm tra ngưỡng BR06, tạo cảnh báo. |
| AIService | Control | Tạo đặc trưng, nạp mô hình, tính xác suất, lưu dự báo (BR08). |
| BaoCaoHienTruong | Entity | Báo cáo được duyệt hoặc bác bỏ. |
| LoaiSauBenh | Entity | Loại sâu bệnh chuyên gia chọn. |
| LichSuXacMinh | Entity | Dòng lịch sử của lần xác minh. |
| DuBaoNguyCo | Entity | Kết quả dự báo sau khi duyệt. |
| MoHinhAI | Entity | Phiên bản mô hình đang dùng. |
| CanhBao, ThongBao | Entity | Cảnh báo và thông báo phát sinh. |

c) Lớp tham gia UC19: Huấn luyện lại mô hình AI

UC19 chỉ đọc dữ liệu nghiệp vụ, không sửa báo cáo nào. Kết quả duy nhất được ghi là dòng MoHinhAI và file mô hình.

| Lớp | Loại | Vai trò |
|---|---|---|
| MoHinhAIPage, MoHinhAIController | Boundary | Hiện phiên bản đang dùng, nút Huấn luyện lại, bảng kết quả đánh giá, nút Kích hoạt. |
| AIService | Control | Tạo bảng mẫu, chia tập, huấn luyện, đánh giá, lưu file mô hình. |
| BaoCaoHienTruong | Entity | Nguồn dữ liệu huấn luyện (chỉ Đã xác minh, Đã xử lý). |
| MoHinhAI | Entity | Phiên bản mới và phiên bản đang dùng. |


## 3.2 Sơ đồ trạng thái

### 3.2.1 Sơ đồ trạng thái “Báo cáo sâu bệnh”
*(Sơ đồ trong báo cáo: Biểu đồ 9. Biểu đồ trạng thái báo cáo sâu bệnh)*
Bảng chuyển trạng thái

| Từ trạng thái | Sự kiện | Điều kiện (guard) | Hành động | Sang trạng thái | Tác nhân |
|---|---|---|---|---|---|
| Khởi tạo | guiBaoCao() | Đủ 1 đến 5 ảnh, tọa độ hợp lệ, đủ trường | luuBaoCao() | Chờ xác minh | Nông dân |
| Khởi tạo | guiBaoCao() | Thiếu ảnh, tọa độ hoặc trường bắt buộc | hienLoi() | Khởi tạo | Nông dân |
| Chờ xác minh | xacMinh() | Đã chọn loại sâu bệnh, đúng khu vực, không phải người gửi | ganLoaiSauBenh() | Đã xác minh | Chuyên gia |
| Chờ xác minh | tuChoi() | Có lý do | luuLyDo() | Bị bác bỏ | Chuyên gia |
| Bị bác bỏ | guiLai() | Chưa gửi lại lần nào và chưa quá 7 ngày | soLanGuiLai tăng 1 | Chờ xác minh | Nông dân |
| Bị bác bỏ | hetHanGuiLai | Chưa gửi lại lần nào và quá 7 ngày | dongHoSo() | Đã xử lý | Hệ thống |
| Bị bác bỏ | (tự chuyển) | Đã gửi lại một lần mà vẫn bị bác bỏ | dongHoSo() | Đã xử lý | Hệ thống |
| Đã xác minh | ghiNhanXuLy() | Có khuyến nghị | capNhatKhuyenNghi() | Đã xử lý | Chuyên gia |


### 3.2.2 Sơ đồ trạng thái “Mô hình AI”
Mỗi phiên bản mô hình đi qua các trạng thái dưới đây. Tại một thời điểm chỉ có một phiên bản ở trạng thái Đang dùng. Phiên bản bị loại hoặc ngừng dùng vẫn được giữ lại để truy lại các dự báo cũ (mỗi DuBaoNguyCo lưu MaMoHinh).

*(Sơ đồ trong báo cáo: Biểu đồ 10. Biểu đồ trạng thái vòng đời mô hình AI)*
Bảng chuyển trạng thái

| Từ trạng thái | Sự kiện | Điều kiện (guard) | Hành động | Sang trạng thái | Tác nhân |
|---|---|---|---|---|---|
| (bắt đầu) | huanLuyenLai() | Có báo cáo đã xác minh, đủ hai loại nhãn | taoBangMau() | Đang huấn luyện | Quản trị viên |
| Đang huấn luyện | huấn luyện xong | Không lỗi | luuFileMoHinh() | Chờ đánh giá | Hệ thống |
| Đang huấn luyện | lỗi | Lỗi dữ liệu hoặc bộ nhớ | ghiNhatKy() | Bị loại | Hệ thống |
| Chờ đánh giá | kichHoat() | F1 không thấp hơn bản đang dùng | datDangSuDung() | Đang dùng | Quản trị viên |
| Chờ đánh giá | kichHoat() | F1 thấp hơn bản đang dùng | tuChoiKichHoat() | Bị loại | Hệ thống |
| Đang dùng | có bản mới được kích hoạt |  | boDangSuDung() | Ngừng dùng | Hệ thống |
