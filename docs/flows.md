# Luồng xử lý (tóm tắt các sơ đồ tuần tự và hoạt động)

Mỗi mục mô tả thứ tự gọi Trang React → Controller → Service → DAL. File .drawio cùng tên nằm trong báo cáo.

## 3.3 Sơ đồ tuần tự
Các biểu đồ tuần tự dùng mô hình Boundary – Control – Entity: Boundary gồm trang React nhận thao tác của người dùng và REST Controller nhận request; Service là Control, điều khiển và kiểm tra quy tắc; lớp DAL đọc ghi CSDL; entity chứa dữ liệu và các kiểm tra của chính nó. Transaction do Service mở và đóng bằng TransactionTemplate.


### 3.3.1 Đăng nhập
*(Sơ đồ trong báo cáo: Biểu đồ 11. Biểu đồ tuần tự Đăng nhập)*
Mô tả: Người dùng nhập tên đăng nhập, mật khẩu và bấm nút. DangNhapPage gọi `POST /api/auth/dang-nhap`; AuthController gọi AccountService.dangNhap. Service lấy tài khoản từ NguoiDungDal. Không có tài khoản hoặc sai mật khẩu thì báo một câu chung, không nói rõ sai phần nào. Tài khoản bị khóa thì báo riêng "Tài khoản đã bị khóa, liên hệ Quản trị viên" (UC01-A2). Có tài khoản thì NguoiDung.xacThucMatKhau băm mật khẩu nhập vào với salt rồi so với giá trị đã lưu. Đúng thì service nạp danh sách khu vực phụ trách, lưu người dùng vào AppSession (phiên đăng nhập của Spring), controller trả thông tin người dùng và vai trò, trang chuyển sang MainLayout với menu theo vai trò.


### 3.3.2 Gửi báo cáo hiện trường
*(Sơ đồ trong báo cáo: Biểu đồ 12. Biểu đồ tuần tự Gửi báo cáo hiện trường)*
Mô tả: Nông dân điền form, chọn ảnh, nhập tọa độ rồi bấm "Gửi". BaoCaoHienTruongPage gửi `POST /api/bao-cao` dạng multipart. BaoCaoController dựng đối tượng BaoCaoHienTruong và gọi guiBaoCao. Service nhờ entity kiểm tra trước (kiemTraHopLe): đủ trường bắt buộc, có 1 đến 5 ảnh, tọa độ đúng miền giá trị (BR01). Sau đó AnhStorage.kiemTraFile kiểm tra từng file về định dạng JPG/PNG và dung lượng tối đa 5 MB. Mọi thứ đúng thì service lưu file ảnh, đặt trạng thái Chờ xác minh và mở một transaction. Trong transaction, service ghi báo cáo, các ảnh, dòng lịch sử đầu tiên và thông báo cho chuyên gia khu vực. Thành công thì Commit, API trả mã báo cáo. Có lỗi thì Rollback, xóa các file ảnh đã lưu, API trả lỗi và trang giữ nguyên dữ liệu để nông dân gửi lại.


### 3.3.3 Xem danh sách chờ xác minh và xem chi tiết
*(Sơ đồ trong báo cáo: Biểu đồ 13. Biểu đồ tuần tự Xem danh sách chờ và chi tiết)*
Mô tả: Khi mở XacMinhPage, trang gọi `GET /api/xac-minh/cho`; XacMinhController gọi layDanhSachCho. Service lấy các khu vực chuyên gia phụ trách, rồi lấy báo cáo Chờ xác minh trong các khu vực đó (BR05). Service đọc tham số HAN_XAC_MINH_GIO và gọi laQuaHan cho từng báo cáo để trang tô màu dòng quá hạn. Danh sách rỗng thì trang báo "Không có báo cáo cần xử lý". Chuyên gia click một dòng thì trang gọi `GET /api/xac-minh/{ma}`, controller gọi layChiTiet. Service lấy báo cáo, danh sách ảnh và lịch sử xử lý; trang tải từng ảnh qua `GET /api/anh/{ma}`.


### 3.3.4 Xác minh báo cáo (Duyệt)
*(Sơ đồ trong báo cáo: Biểu đồ 14. Biểu đồ tuần tự Xác minh báo cáo (Duyệt))*
Mô tả: Chuyên gia chọn loại sâu bệnh, nhập ghi chú, khuyến nghị rồi bấm "Duyệt". Trang gọi `POST /api/xac-minh/{ma}/duyet`; controller gọi XacMinhService.xacMinh với mã chuyên gia lấy từ AppSession. Service kiểm tra theo thứ tự: đã chọn loại sâu bệnh chưa (BR04), báo cáo còn Chờ xác minh không (BR03), chuyên gia có đúng khu vực và không phải người gửi không (BR05). Hợp lệ thì entity ghi loại sâu bệnh, khuyến nghị và đổi trạng thái sang Đã xác minh. Service mở transaction và chạy updateTrangThai với điều kiện WHERE MaTrangThai = 'CHO_XAC_MINH'. Hàm này trả về false nếu không cập nhật được dòng nào, nghĩa là chuyên gia khác đã xử lý trước, và service Rollback. Nếu true thì service ghi lịch sử, tạo thông báo cho nông dân và gọi CanhBaoService.kiemTraNguong (BR06). Tất cả cùng một transaction rồi Commit.

Sau khi Commit, service gọi AIService.duBaoNguyCo (BR08). Bước này nằm ngoài transaction: mô hình lỗi thì AIService trả về null, kết quả duyệt vẫn giữ và chỉ còn BR06. API trả kết quả duyệt kèm dự báo (nếu có) để trang hiện mức nguy cơ. Chi tiết bước dự báo ở mục 3.3.9.


### 3.3.5 Từ chối báo cáo
*(Sơ đồ trong báo cáo: Biểu đồ 15. Biểu đồ tuần tự Từ chối báo cáo)*
Mô tả: Chuyên gia nhập lý do rồi bấm "Từ chối"; trang gọi `POST /api/xac-minh/{ma}/tu-choi`. Không có lý do thì service chặn ngay (BR04). Phần kiểm tra quyền và trạng thái giống chức năng Duyệt. Hợp lệ thì entity đổi trạng thái sang Bị bác bỏ, service ghi lịch sử kèm lý do và tạo thông báo cho nông dân. Nếu báo cáo này đã được gửi lại một lần (soLanGuiLai >= 1) thì không còn cơ hội gửi nữa, nên service đóng hồ sơ luôn trong cùng transaction: chuyển sang Đã xử lý, ghi thêm một dòng lịch sử và một thông báo đóng hồ sơ.


### 3.3.6 Xem thông báo
*(Sơ đồ trong báo cáo: Biểu đồ 16. Biểu đồ tuần tự Xem thông báo)*
Mô tả: Mỗi 60 giây, MainLayout gọi `GET /api/thong-bao/chua-doc` (ThongBaoService.demChuaDoc) để cập nhật số thông báo chưa đọc. Người dùng bấm nút thông báo thì ThongBaoPage mở, gọi `GET /api/thong-bao` (layThongBao) và nạp danh sách, dòng chưa đọc in đậm. Click một dòng thì trang gọi `POST /api/thong-bao/{ma}/da-doc` (danhDauDaDoc). Hàm markRead có điều kiện maNguoiNhan khớp với người trong AppSession, nên không ai đánh dấu được thông báo của người khác (BR07). Xong thì MainLayout cập nhật lại số chưa đọc.


### 3.3.7 Kiểm tra quá hạn tự động
*(Sơ đồ trong báo cáo: Biểu đồ 17. Biểu đồ tuần tự Kiểm tra quá hạn tự động)*
Mô tả: Đây là chức năng nền FR-17, không có tác nhân bấm nút. Mỗi 60 giây, HanXuLyScheduler phía server gọi hai hàm của HanXuLyService. kiemTraQuaHan đọc tham số HAN_XAC_MINH_GIO, lấy các báo cáo Chờ xác minh quá hạn mà chưa có cờ, gắn cờ quaHan và tạo thông báo cho Quản trị viên. dongHoSoHetHan lấy các báo cáo Bị bác bỏ chưa gửi lại mà quá HAN_GUI_LAI_NGAY, chuyển sang Đã xử lý, ghi lịch sử và báo cho nông dân. Mỗi báo cáo xử lý trong một transaction riêng, nên một báo cáo lỗi không làm hỏng các báo cáo còn lại.


### 3.3.8 Sửa và gửi lại báo cáo
*(Sơ đồ trong báo cáo: Biểu đồ 18. Biểu đồ tuần tự Sửa và gửi lại báo cáo)*
Mô tả: Nông dân chọn báo cáo của mình và bấm "Sửa" (hoặc "Gửi lại"). Trang gọi `GET /api/bao-cao/{ma}/de-sua`; BaoCaoService.layDeSua lấy báo cáo rồi gọi choPhepSua trên entity. Hàm này trả về true khi người sửa là người gửi (BR05) và báo cáo còn Chờ xác minh, hoặc đã Bị bác bỏ mà SoLanGuiLai = 0 và chưa quá HAN_GUI_LAI_NGAY (BR03, BR04). Sau khi nông dân sửa xong, trang gửi `PUT /api/bao-cao/{ma}`; service kiểm tra lại BR01, lưu file ảnh mới rồi mở transaction. updateNoiDung có điều kiện WHERE MaTrangThai = trạng thái cũ, nên nếu chuyên gia vừa xác minh báo cáo trong lúc nông dân đang sửa thì hàm trả về false và service Rollback. Thành công thì service thêm hoặc xóa ảnh, ghi lịch sử, báo cho chuyên gia khu vực và Commit.


### 3.3.9 Dự báo nguy cơ dịch (AI)
*(Sơ đồ trong báo cáo: Biểu đồ 19. Biểu đồ tuần tự Dự báo nguy cơ dịch)*
Mô tả: Đây là phần bên trong của bước gọi AI ở sơ đồ tuần tự Xác minh báo cáo (mục 3.3.4). XacMinhService chỉ gọi AIService.duBaoNguyCo sau khi transaction duyệt đã Commit, nên lỗi của mô hình không làm hỏng kết quả duyệt. AIService lấy phiên bản mô hình đang dùng. Không có mô hình hoặc không nạp được file thì ghi nhật ký và trả về null, khi đó hệ thống chỉ còn BR06. Có mô hình thì AIService đếm số ca đã xác minh cùng loại, cùng khu vực trong 7, 14 và 30 ngày trước, tạo vector đặc trưng và gọi mô hình Smile (hàm predict kèm xác suất) để lấy xác suất. Service đọc NGUONG_NGUY_CO, xếp mức Thấp, Trung bình hoặc Cao, rồi lưu bản ghi dự báo. Mức Cao thì gọi CanhBaoService.taoCanhBaoTuDuBao. Hàm này không tạo cảnh báo mới nếu cùng khu vực, cùng loại đã có cảnh báo trong SO_NGAY_XET_NGUONG ngày.


### 3.3.10 Huấn luyện lại mô hình AI
*(Sơ đồ trong báo cáo: Biểu đồ 20. Biểu đồ tuần tự Huấn luyện lại mô hình AI)*
Mô tả: Quản trị viên bấm "Huấn luyện lại" trên MoHinhAIPage; trang gọi `POST /api/mo-hinh-ai/huan-luyen`. AIService lấy toàn bộ báo cáo Đã xác minh và Đã xử lý (BR08), dựng bảng mẫu và kiểm tra bảng có đủ cả hai nhãn. Thiếu một nhãn thì dừng và báo chưa đủ dữ liệu. Đủ thì service chia mẫu theo thời gian, gọi Smile để huấn luyện, đánh giá trên tập kiểm tra, lưu file mô hình và ghi một dòng MoHinhAI với DangSuDung = 0. Trang hiện các chỉ số của bản mới cạnh bản đang dùng. Quản trị viên bấm "Kích hoạt" thì service so F1 của hai bản. Bản mới không kém hơn thì được đặt làm bản đang dùng, ngược lại service báo lỗi và giữ bản cũ.


## 3.4 Sơ đồ hoạt động

### 3.4.1 Chuyên gia xác minh báo cáo
*(Sơ đồ trong báo cáo: Biểu đồ 21. Biểu đồ hoạt động chuyên gia xác minh báo cáo)*
Nhánh Duyệt có thêm bước dự báo nguy cơ bằng mô hình AI sau khi Commit. Nếu mức Cao thì hệ thống tạo cảnh báo cho khu vực. Mô hình lỗi thì hệ thống bỏ qua bước này và chỉ dùng BR06, nên chuyên gia vẫn duyệt được bình thường.


### 3.4.2 Nông dân gửi báo cáo
*(Sơ đồ trong báo cáo: Biểu đồ 22. Biểu đồ hoạt động nông dân gửi báo cáo)*
Hai lần kiểm tra (BR01 và định dạng ảnh) đều quay lại bước sửa, không kết thúc luồng, vì nông dân chỉ cần sửa ô sai rồi gửi lại. Lỗi khi ghi CSDL thì hệ thống Rollback và xóa file ảnh đã lưu, nên không có ảnh mồ côi trong thư mục.


### 3.4.3 Quản trị viên huấn luyện lại mô hình AI
*(Sơ đồ trong báo cáo: Biểu đồ 23. Biểu đồ hoạt động huấn luyện lại mô hình AI)*
Biểu đồ có hai điểm dừng sớm: dữ liệu chỉ có một loại nhãn, và lỗi khi huấn luyện. Cả hai trường hợp đều không tạo phiên bản mới, nên dự báo vẫn chạy bằng bản cũ. Ở cuối, Quản trị viên có thể không kích hoạt bản mới; bản đó vẫn được lưu để đối chiếu.
