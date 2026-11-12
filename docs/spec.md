# Đặc tả yêu cầu AGR-05

Trích từ báo cáo. Đây là nguồn sự thật cho nghiệp vụ: code phải làm đúng các quy tắc BR, FR và luồng use case dưới đây.

### 1.2.3 Danh sách các yêu cầu
Yêu cầu hệ thống gồm bốn nhóm: đầu vào, xử lý và đầu ra; quy tắc nghiệp vụ; yêu cầu chức năng; yêu cầu phi chức năng.

a) Đầu vào, xử lý, đầu ra

| Nhóm | Đầu vào | Xử lý | Đầu ra |
|---|---|---|---|
| Tài khoản | Tên đăng nhập, mật khẩu, họ tên, vai trò | Đăng ký (băm mật khẩu), đăng nhập, khóa/mở khóa | Phiên làm việc theo vai trò |
| Báo cáo | Tiêu đề, cây trồng, mô tả, mức độ, ngày phát hiện, khu vực | Kiểm tra dữ liệu, lưu báo cáo, đặt trạng thái Chờ xác minh | Mã báo cáo, thông báo gửi thành công |
| Ảnh hiện trường | 1 đến 5 file ảnh, tọa độ (vĩ độ, kinh độ) | Kiểm tra định dạng và dung lượng, lưu file, lưu đường dẫn | Ảnh gắn với báo cáo |
| Xác minh | Mã báo cáo, loại sâu bệnh, ghi chú hoặc lý do từ chối | Kiểm tra quyền và trạng thái, đổi trạng thái, ghi lịch sử (một transaction) | Trạng thái mới, lịch sử xác minh |
| Danh mục sâu bệnh | Tên loại, triệu chứng, biện pháp phòng trừ | Thêm, sửa, xóa, tìm kiếm | Danh sách loại sâu bệnh |
| Khu vực | Tên khu vực, chuyên gia được giao | Quản lý khu vực, phân công | Danh sách khu vực, bảng phân công |
| Cảnh báo | Số báo cáo đã xác minh theo khu vực và loại, ngưỡng, số ngày; kết quả dự báo nguy cơ | Đếm, so với ngưỡng, hoặc nhận kết quả dự báo nguy cơ Cao, rồi tạo cảnh báo | Cảnh báo hiện cho người dùng trong khu vực |
| Dự báo nguy cơ (AI) | Báo cáo vừa duyệt (khu vực, loại sâu bệnh, mức độ), số ca đã xác minh trong 7, 14, 30 ngày trước, tháng trong năm | Mô hình AI tính xác suất bùng phát trong 7 ngày tới, so với ngưỡng NGUONG_NGUY_CO | Bản ghi dự báo (xác suất, mức Thấp / Trung bình / Cao); cảnh báo nếu mức Cao |
| Mô hình AI | Các báo cáo Đã xác minh trong CSDL | Huấn luyện lại, đo độ chính xác trên tập kiểm tra, so với phiên bản đang dùng | Phiên bản mô hình mới, chỉ đưa vào dùng khi không kém bản cũ |
| Thống kê | Khoảng thời gian, khu vực, loại sâu bệnh | Tổng hợp theo trạng thái, loại, khu vực, thời gian | Bảng và biểu đồ thống kê |
| Thông báo | Sự kiện phát sinh (gửi báo cáo, duyệt, từ chối, quá hạn, cảnh báo dịch), người nhận | Tạo thông báo cùng transaction với thao tác gây ra nó, đánh dấu đã đọc | Danh sách thông báo của từng người, số thông báo chưa đọc |

b) Quy tắc nghiệp vụ và quyết định triển khai

| Mã | Nội dung |
|---|---|
| BR01 | Mỗi báo cáo có từ 1 đến 5 ảnh (JPG hoặc PNG, mỗi ảnh tối đa 5 MB), có tọa độ (vĩ độ -90 đến 90, kinh độ -180 đến 180) và có khu vực. Thiếu một mục thì không gửi được. |
| BR02 | Chuyên gia phải xử lý báo cáo trong 48 giờ kể từ lúc vào Chờ xác minh. Quá hạn thì gắn cờ "Quá hạn" và tạo thông báo nội bộ cho Quản trị viên để phân công lại. |
| BR03 | Trạng thái đi theo thứ tự: Khởi tạo → Chờ xác minh → Đã xác minh / Bị bác bỏ → Đã xử lý. Không nhảy cách bước. Từ Bị bác bỏ chỉ sang Đã xử lý khi đóng hồ sơ theo BR04. Báo cáo Đã xử lý không được sửa. Mỗi lần đổi trạng thái ghi một dòng vào lịch sử xác minh. |
| BR04 | Từ chối phải có lý do. Duyệt phải chọn loại sâu bệnh trong danh mục. Báo cáo bị bác bỏ được bổ sung ảnh và gửi lại tối đa 1 lần, trong 7 ngày. Quá 7 ngày không gửi lại, hoặc đã gửi lại một lần mà vẫn bị bác bỏ, thì hệ thống đóng hồ sơ (chuyển sang Đã xử lý). |
| BR05 | Chuyên gia chỉ xác minh báo cáo thuộc khu vực được giao và không được xác minh báo cáo của chính mình. Nông dân chỉ xem và sửa báo cáo của mình, chỉ sửa khi còn Chờ xác minh. |
| BR06 | Khi số báo cáo Đã xác minh cùng loại sâu bệnh, cùng khu vực đạt ngưỡng (mặc định 5 báo cáo trong 7 ngày), hệ thống tạo cảnh báo. Quy tắc này chạy cùng với dự báo của BR08 và là phương án dự phòng khi không dùng được mô hình AI. |
| BR07 | Mọi thông báo lưu trong bảng ThongBao. Thông báo được tạo khi: gửi báo cáo (cho chuyên gia khu vực), duyệt, từ chối, đóng hồ sơ (cho nông dân), quá 48 giờ (cho Quản trị viên), có cảnh báo dịch theo BR06 hoặc BR08 (cho nông dân và chuyên gia trong khu vực). Mỗi người chỉ xem được thông báo của mình. |
| BR08 | Mô hình AI chỉ học và dự báo trên báo cáo Đã xác minh; báo cáo đang chờ hoặc bị bác bỏ không được dùng. Sau mỗi lần duyệt, nếu xác suất bùng phát trong 7 ngày tới đạt ngưỡng (mặc định 70%) thì hệ thống tạo cảnh báo. AI không đổi trạng thái báo cáo và không thay kết luận của chuyên gia. Mô hình lỗi hoặc chưa được huấn luyện thì chỉ áp dụng BR06. |
| SEC01 | Mật khẩu lưu dạng băm PBKDF2 kèm salt ngẫu nhiên. Không lưu mật khẩu thường. |
| SEC02 | Mọi câu truy vấn dùng tham số (parameterized query) để chống SQL Injection. |
| SEC03 | Ghi nhật ký mọi thao tác đổi trạng thái báo cáo (người làm, thời điểm, ghi chú). |
| ARCH01 | Chia 3 tầng: Presentation (trang React và REST Controller), BLL, DAL. Tầng trình bày không viết SQL, chỉ gọi BLL (trang React gọi qua REST API). BLL gọi DAL. |
| ARCH02 | Ảnh lưu thành file trong thư mục của ứng dụng. CSDL chỉ lưu đường dẫn. |
| ARCH03 | Năm tham số lưu trong bảng ThamSoHeThong, không viết cứng trong code: HAN_XAC_MINH_GIO = 48, NGUONG_CANH_BAO = 5, SO_NGAY_XET_NGUONG = 7, HAN_GUI_LAI_NGAY = 7, NGUONG_NGUY_CO = 70 (%). |
| ARCH04 | Không dùng email hay SMS. Khung chính của giao diện web tự gọi API mỗi 60 giây để tải số thông báo mới. Phía server có tác vụ nền chạy mỗi 60 giây (HanXuLyScheduler) gọi BLL kiểm tra báo cáo quá hạn, hồ sơ hết hạn gửi lại. |
| ARCH05 | Mô hình AI là bài toán phân loại nhị phân (bùng phát / không bùng phát), làm bằng thư viện Smile (Java) và chạy ở tầng BLL qua lớp AIService. File mô hình lưu trong thư mục mô hình của ứng dụng; bảng MoHinhAI lưu phiên bản, ngày huấn luyện, số mẫu và độ chính xác. Quản trị viên chạy huấn luyện lại; mô hình mới chỉ được dùng khi độ chính xác không thấp hơn bản đang chạy. |

c) Yêu cầu chức năng

| ID | Tên chức năng | Mô tả |
|---|---|---|
| FR-01 | Đăng nhập / Đăng xuất | Kiểm tra tài khoản, mở giao diện theo vai trò. |
| FR-02 | Gửi báo cáo hiện trường | Nhập thông tin, chọn khu vực, đính kèm ảnh và tọa độ. Lưu ở trạng thái Chờ xác minh. Tạo thông báo cho chuyên gia khu vực. |
| FR-03 | Sửa báo cáo | Nông dân sửa báo cáo khi còn Chờ xác minh. Khi bị bác bỏ, bổ sung ảnh và gửi lại một lần trong 7 ngày. |
| FR-04 | Theo dõi báo cáo của tôi | Xem danh sách, trạng thái, kết quả và khuyến nghị. |
| FR-05 | Xem danh sách chờ xác minh | Chuyên gia xem báo cáo trong khu vực phụ trách, có bộ lọc và dấu quá hạn. |
| FR-06 | Xem chi tiết báo cáo | Hiện thông tin, ảnh, tọa độ, lịch sử xử lý. |
| FR-07 | Xác minh báo cáo | Duyệt, chọn loại sâu bệnh, nhập ghi chú và khuyến nghị. Tạo thông báo kết quả cho nông dân. |
| FR-08 | Từ chối báo cáo | Bác bỏ và nhập lý do. Tạo thông báo cho nông dân. |
| FR-09 | Đánh dấu đã xử lý | Ghi nhận đã có biện pháp xử lý. Chuyển sang Đã xử lý. |
| FR-10 | Thống kê | Theo trạng thái, loại sâu bệnh, khu vực, thời gian. |
| FR-11 | Cảnh báo dịch hại | Tự tạo cảnh báo khi vượt ngưỡng (BR06) hoặc khi dự báo nguy cơ Cao (BR08). Tạo thông báo cho nông dân và chuyên gia trong khu vực. |
| FR-12 | Quản lý danh mục sâu bệnh | Thêm, sửa, xóa, tìm. |
| FR-13 | Quản lý khu vực và phân công | Quản lý khu vực, giao chuyên gia phụ trách. |
| FR-14 | Quản lý người dùng | Tạo, khóa, mở khóa tài khoản, phân quyền. |
| FR-15 | Cấu hình hệ thống | Chỉnh thời hạn xác minh, ngưỡng cảnh báo, số ngày xét ngưỡng, hạn gửi lại, ngưỡng nguy cơ của mô hình AI. |
| FR-16 | Xem thông báo | Mọi người dùng xem danh sách thông báo của mình. Khung chính của giao diện hiện số thông báo chưa đọc. Mở thông báo thì đánh dấu đã đọc. |
| FR-17 | Kiểm tra quá hạn tự động | Hệ thống gắn cờ báo cáo quá 48 giờ (BR02) và đóng hồ sơ hết hạn gửi lại (BR04). Đây là chức năng nền chạy định kỳ phía server (HanXuLyScheduler), không vẽ thành Use Case. |
| FR-18 | Dự báo nguy cơ dịch | Sau mỗi lần duyệt, mô hình AI tính xác suất loại sâu bệnh đó bùng phát trong khu vực ở 7 ngày tới. Lưu kết quả dự báo; mức Cao thì tạo cảnh báo (BR08). Là phần bắt buộc của UC07 (include). |
| FR-19 | Huấn luyện lại mô hình AI | Quản trị viên chạy huấn luyện trên dữ liệu đã xác minh, xem độ chính xác, đưa phiên bản mới vào dùng (ARCH05). |

d) Yêu cầu phi chức năng

| Tiêu chuẩn | Yêu cầu |
|---|---|
| Toàn vẹn | Ghi báo cáo, ảnh, lịch sử trong một transaction. Lỗi thì rollback. |
| Bảo mật | SEC01, SEC02, SEC03. Phân quyền theo vai trò ở tầng BLL. |
| Kiểm tra dữ liệu | Trường bắt buộc, số ảnh 1 đến 5, ảnh tối đa 5 MB, tọa độ đúng miền giá trị. |
| Hiệu năng | Tải danh sách có phân trang trong 3 giây với 10.000 bản ghi. Lưu báo cáo kèm ảnh trong 5 giây. Có index cho cột trạng thái, khu vực, ngày gửi. |
| Thông báo | Mọi thao tác báo thành công hoặc nêu rõ nguyên nhân lỗi. |
| Kiến trúc | Giao diện và Controller không viết SQL. Truy cập dữ liệu qua BLL và DAL (ARCH01). |
| Khả dụng | Giao diện tiếng Việt. Tác vụ chính làm trong tối đa 3 lần nhấp. |
| Mô hình AI | Dự báo xong trong 2 giây sau khi duyệt. Mô hình lỗi thì thao tác duyệt vẫn thành công và hệ thống dùng BR06. Mỗi bản ghi dự báo lưu phiên bản mô hình đã dùng để truy lại khi cần. |

## 2.2 Danh sách các Actor
Hệ thống có một tác nhân tổng quát và ba tác nhân cụ thể. Không có hệ thống ngoài vì thông báo lưu nội bộ và mô hình AI chạy ngay trong ứng dụng.

| Tác nhân | Loại | Vai trò |
|---|---|---|
| Người dùng | Actor tổng quát | Đăng nhập hệ thống. Ba tác nhân bên dưới kế thừa tác nhân này. |
| Nông dân | Actor chính | Gửi báo cáo, sửa báo cáo khi chưa xác minh, theo dõi trạng thái, nhận cảnh báo. |
| Chuyên gia / Nhân viên kiểm định | Actor chính | Xem danh sách chờ, xác minh hoặc bác bỏ, phân loại sâu bệnh, đánh dấu đã xử lý, xem thống kê, nhận cảnh báo. |
| Quản trị viên | Actor chính | Quản lý tài khoản, danh mục sâu bệnh, khu vực, phân công, cấu hình hệ thống, xem thống kê, huấn luyện lại mô hình AI. |
| Hệ thống ngoài | Không có | Thông báo là chức năng nội bộ, lưu trong bảng ThongBao. Hệ thống không gửi email hay SMS. Mô hình AI chạy ngay trong ứng dụng bằng thư viện Smile, không gọi dịch vụ bên ngoài. |


## 2.3 Danh sách các UseCase
Bảng dưới liệt kê 19 use case. Năm use case có sơ đồ phân rã ở mục 2.1 là UC02, UC07, UC03, UC14 và UC19.

| Mã | Tên use case | Tác nhân | Mô tả ngắn |
|---|---|---|---|
| UC01 | Đăng nhập | Người dùng | Vào hệ thống theo vai trò. |
| UC02 | Gửi báo cáo hiện trường | Nông dân | Nhập thông tin, ảnh, tọa độ và gửi. |
| UC03 | Sửa và gửi lại báo cáo | Nông dân | Sửa báo cáo chờ xác minh, gửi lại báo cáo bị bác bỏ. |
| UC04 | Theo dõi báo cáo của tôi | Nông dân | Xem trạng thái, kết quả, khuyến nghị. |
| UC05 | Xem danh sách chờ xác minh | Chuyên gia | Báo cáo chờ trong khu vực phụ trách. |
| UC06 | Xem chi tiết báo cáo | Chuyên gia | Ảnh, tọa độ, mô tả, lịch sử. |
| UC07 | Xác minh báo cáo | Chuyên gia | Duyệt báo cáo, chọn loại sâu bệnh, ghi khuyến nghị. |
| UC08 | Từ chối báo cáo | Chuyên gia | Bác bỏ báo cáo kèm lý do. |
| UC09 | Phân loại sâu bệnh | Chuyên gia | Chọn loại trong danh mục. |
| UC10 | Đánh dấu đã xử lý | Chuyên gia | Ghi nhận đã có biện pháp xử lý. |
| UC11 | Xem thống kê | Chuyên gia, Quản trị viên | Số liệu theo kỳ, khu vực, loại. |
| UC12 | Nhận cảnh báo dịch hại | Nông dân, Chuyên gia | Xem cảnh báo của khu vực. |
| UC13 | Quản lý danh mục sâu bệnh | Quản trị viên | Thêm, sửa, xóa loại sâu bệnh. |
| UC14 | Quản lý khu vực và phân công | Quản trị viên | Khu vực và chuyên gia phụ trách. |
| UC15 | Quản lý người dùng | Quản trị viên | Tạo, khóa tài khoản, đổi vai trò. |
| UC16 | Cấu hình hệ thống | Quản trị viên | Sửa năm tham số. |
| UC17 | Xem thông báo | Người dùng | Thông báo nội bộ của mình. |
| UC18 | Dự báo nguy cơ dịch (AI) | Không có (UC07 include) | Dự báo khả năng bùng phát sau khi duyệt. |
| UC19 | Huấn luyện lại mô hình AI | Quản trị viên | Huấn luyện, đánh giá, kích hoạt phiên bản. |


## 2.4 Đặc tả UseCase
Mỗi use case gồm bảng đặc tả, luồng chính và các luồng thay thế, ngoại lệ (nếu có). Năm use case đã phân rã được viết đầy đủ hơn các use case còn lại.


### 2.4.1 Đặc tả usecase “Đăng nhập”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Đăng nhập |
| Tác nhân chính | Người dùng |
| Mô tả | Người dùng nhập tên đăng nhập, mật khẩu để vào hệ thống theo vai trò. |
| Điều kiện tiên quyết | Có tài khoản trong bảng NguoiDung. |
| Hậu điều kiện (thành công) | AppSession (phiên đăng nhập) lưu người dùng, vai trò, các khu vực phụ trách (nếu là chuyên gia). Khung chính mở menu theo vai trò. |
| Hậu điều kiện (thất bại) | Không mở phiên làm việc. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Mở ứng dụng. |  |
| 2 |  | Hiện trang đăng nhập (DangNhapPage). |
| 3 | Nhập tên đăng nhập, mật khẩu, nhấn "Đăng nhập". |  |
| 4 |  | AccountService lấy tài khoản theo tên, băm mật khẩu nhập vào bằng PBKDF2 với Muoi, so với MatKhauHash (SEC01). |
| 5 |  | Đúng thì lưu AppSession, mở khung chính (MainLayout) theo vai trò. |

Luồng thay thế và ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Sai tên hoặc mật khẩu. | Báo "Tên đăng nhập hoặc mật khẩu không đúng", không nói sai phần nào. |
| A2 | Tài khoản bị khóa (BiKhoa = 1). | Báo tài khoản bị khóa, liên hệ Quản trị viên. |


### 2.4.2 Đặc tả usecase “Gửi báo cáo hiện trường”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Gửi báo cáo hiện trường |
| Tác nhân chính | Nông dân |
| Mô tả | Nông dân báo cáo sâu bệnh thấy ngoài ruộng, kèm ảnh và vị trí, để chuyên gia xác minh. |
| Điều kiện tiên quyết | Đã đăng nhập. Danh mục khu vực có ít nhất một khu vực. |
| Hậu điều kiện (thành công) | Báo cáo mới được lưu với trạng thái Chờ xác minh. Ảnh được lưu. Lịch sử trạng thái được ghi. |
| Hậu điều kiện (thất bại) | Không ghi dữ liệu nào. Form giữ nguyên nội dung đã nhập. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Chọn chức năng "Gửi báo cáo hiện trường". |  |
| 2 |  | Hiện form báo cáo, nạp danh sách khu vực. |
| 3 | Nhập tiêu đề, tên cây trồng, mô tả triệu chứng, mức độ, ngày phát hiện, chọn khu vực. |  |
| 4 | Nhập tọa độ (vĩ độ, kinh độ) và chọn 1 đến 5 ảnh. | Hiện ảnh thu nhỏ để xem trước. |
| 5 | Nhấn "Gửi báo cáo". |  |
| 6 |  | Kiểm tra dữ liệu theo BR01: đủ trường bắt buộc, có ảnh, tọa độ đúng miền giá trị. |
| 7 |  | Lưu file ảnh. Ghi báo cáo, ảnh, lịch sử trạng thái trong một transaction. Trạng thái là Chờ xác minh. |
| 8 |  | Hệ thống tạo thông báo nội bộ cho chuyên gia khu vực báo gửi thành công kèm mã báo cáo. Báo cáo vào danh sách chờ của chuyên gia khu vực. |

Luồng thay thế

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Bước 6: thiếu trường bắt buộc. | Tô đỏ ô thiếu, hiện thông báo, quay lại bước 3. |
| A2 | Bước 4 hoặc 6: chưa có ảnh. | Báo "Báo cáo phải có ít nhất 1 ảnh", quay lại bước 4. |
| A3 | Bước 4: ảnh sai định dạng, quá 5 MB hoặc chọn quá 5 ảnh. | Từ chối ảnh đó, nêu lý do, cho chọn lại. |
| A4 | Bước 4: tọa độ ngoài miền hợp lệ (vĩ độ -90 đến 90, kinh độ -180 đến 180). | Báo lỗi tọa độ, quay lại bước 4. |
| A5 | Bước 5: nhấn "Hủy". | Hỏi xác nhận. Đồng ý thì đóng form, không lưu gì. |

Luồng ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| E1 | Bước 7: mất kết nối hoặc lỗi ghi CSDL. | Rollback, xóa file ảnh đã lưu tạm, báo lỗi, giữ dữ liệu đã nhập. |
| E2 | Bước 7: lỗi ghi file ảnh (hết dung lượng, thiếu quyền). | Rollback, báo lỗi, đề nghị liên hệ quản trị viên. |


### 2.4.3 Đặc tả usecase “Sửa và gửi lại báo cáo”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Sửa và gửi lại báo cáo |
| Tác nhân chính | Nông dân |
| Mô tả | Nông dân sửa báo cáo còn Chờ xác minh, hoặc bổ sung ảnh và gửi lại báo cáo bị bác bỏ. |
| Điều kiện tiên quyết | Đã đăng nhập. Báo cáo là của chính nông dân. Báo cáo ở Chờ xác minh, hoặc Bị bác bỏ với SoLanGuiLai = 0 và chưa quá HAN_GUI_LAI_NGAY ngày. |
| Hậu điều kiện (thành công) | Báo cáo được cập nhật. Nếu là gửi lại: trạng thái về Chờ xác minh, SoLanGuiLai = 1, NgayVaoTrangThai được đặt lại, chuyên gia khu vực nhận thông báo. |
| Hậu điều kiện (thất bại) | Báo cáo giữ nguyên nội dung và trạng thái cũ. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Mở UC04, chọn một báo cáo, nhấn "Sửa" hoặc "Gửi lại". |  |
| 2 |  | Kiểm tra báo cáo là của người đang đăng nhập (BR05) và trạng thái cho phép sửa (BR03, BR04). |
| 3 |  | Nạp dữ liệu cũ lên trang BaoCaoHienTruongPage, kể cả ảnh đã gửi. |
| 4 | Sửa mô tả, mức độ, tọa độ; thêm hoặc bỏ ảnh (tổng vẫn 1 đến 5 ảnh). |  |
| 5 | Nhấn "Lưu" (hoặc "Gửi lại"). |  |
| 6 |  | Kiểm tra dữ liệu theo BR01. |
| 7 |  | Trong một transaction: cập nhật báo cáo, thêm hoặc xóa ảnh, ghi lịch sử. Nếu là gửi lại: chuyển Bị bác bỏ → Chờ xác minh, tăng SoLanGuiLai. |
| 8 |  | Tạo thông báo cho chuyên gia khu vực, báo thành công. |

Luồng thay thế

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Bước 2: báo cáo đã Đã xác minh hoặc Đã xử lý. | Báo "Báo cáo đã được xử lý, không sửa được", chỉ cho xem. |
| A2 | Bước 2: báo cáo bị bác bỏ đã gửi lại một lần, hoặc quá 7 ngày. | Báo hết lượt gửi lại (BR04). Hồ sơ sẽ được đóng bởi FR-17. |
| A3 | Bước 6: dữ liệu không hợp lệ. | Xử lý như A1 đến A4 của UC02. |

Luồng ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| E1 | Bước 7: chuyên gia vừa xác minh báo cáo trong lúc nông dân đang sửa. | UPDATE có điều kiện theo trạng thái cũ trả về 0 dòng. Rollback, báo "Báo cáo vừa được chuyên gia xử lý". |
| E2 | Bước 7: lỗi CSDL hoặc lỗi ghi file ảnh. | Rollback, xóa file ảnh mới lưu, giữ dữ liệu trên form. |


### 2.4.4 Đặc tả usecase “Theo dõi báo cáo của tôi”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Theo dõi báo cáo của tôi |
| Tác nhân chính | Nông dân |
| Mô tả | Nông dân xem các báo cáo mình đã gửi, trạng thái, kết quả xác minh và khuyến nghị. |
| Điều kiện tiên quyết | Đã đăng nhập với vai trò Nông dân. |
| Hậu điều kiện (thành công) | Không đổi dữ liệu. |
| Hậu điều kiện (thất bại) | Không đổi dữ liệu. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Chọn "Báo cáo của tôi". |  |
| 2 |  | Hiện danh sách báo cáo có MaNguoiGui là người đang đăng nhập (BR05): tiêu đề, ngày gửi, trạng thái, loại sâu bệnh. |
| 3 | Lọc theo trạng thái, chọn một báo cáo. |  |
| 4 |  | Hiện chi tiết, ảnh, lịch sử xác minh, khuyến nghị hoặc lý do bác bỏ. |

Luồng thay thế và ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Chưa có báo cáo nào. | Hiện "Bạn chưa gửi báo cáo nào". |
| A2 | Báo cáo Chờ xác minh, hoặc Bị bác bỏ còn lượt gửi lại. | Hiện nút "Sửa" / "Gửi lại" (mở rộng sang UC03). |


### 2.4.5 Đặc tả usecase “Xem danh sách chờ xác minh”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Xem danh sách chờ xác minh |
| Tác nhân chính | Chuyên gia / Nhân viên kiểm định |
| Mô tả | Chuyên gia xem các báo cáo Chờ xác minh thuộc khu vực mình phụ trách. |
| Điều kiện tiên quyết | Đã đăng nhập với vai trò Chuyên gia. |
| Hậu điều kiện (thành công) | Không đổi dữ liệu. |
| Hậu điều kiện (thất bại) | Không đổi dữ liệu. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Mở trang XacMinhPage. |  |
| 2 |  | Lấy các khu vực trong PhanCongKhuVuc của chuyên gia, rồi lấy báo cáo Chờ xác minh trong các khu vực đó, bỏ báo cáo do chính chuyên gia gửi (BR05). |
| 3 |  | Đọc HAN_XAC_MINH_GIO, đánh dấu báo cáo quá hạn hoặc còn dưới 6 giờ; xếp theo NgayVaoTrangThai, cũ nhất lên đầu. |
| 4 | Lọc theo khu vực, mức độ. |  |

Luồng thay thế và ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Chưa được phân công khu vực nào. | Báo "Bạn chưa được phân công khu vực". |
| A2 | Không có báo cáo chờ. | Hiện "Không có báo cáo cần xử lý". |


### 2.4.6 Đặc tả usecase “Xem chi tiết báo cáo”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Xem chi tiết báo cáo |
| Tác nhân chính | Chuyên gia / Nhân viên kiểm định (được UC07 include) |
| Mô tả | Hiện đủ thông tin một báo cáo để chuyên gia đối chiếu trước khi kết luận. |
| Điều kiện tiên quyết | Báo cáo thuộc khu vực chuyên gia phụ trách. |
| Hậu điều kiện (thành công) | Không đổi dữ liệu. |
| Hậu điều kiện (thất bại) | Không đổi dữ liệu. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Chọn một báo cáo trong danh sách chờ. |  |
| 2 |  | Hiện tiêu đề, cây trồng, mô tả, mức độ, ngày phát hiện, tọa độ, người gửi. |
| 3 |  | Hiện 1 đến 5 ảnh (tải qua API theo DuongDan); hiện lịch sử xác minh nếu là báo cáo gửi lại. |

Luồng thay thế và ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| E1 | File ảnh không còn trong thư mục. | Hiện khung "Không tìm thấy ảnh", vẫn hiện các thông tin khác. |


### 2.4.7 Đặc tả usecase “Xác minh báo cáo”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Xác minh báo cáo |
| Tác nhân chính | Chuyên gia / Nhân viên kiểm định |
| Mô tả | Chuyên gia xem báo cáo, kết luận đúng hay sai, chọn loại sâu bệnh và ghi khuyến nghị. Sau khi duyệt, hệ thống dự báo nguy cơ dịch trong khu vực bằng mô hình AI. |
| Điều kiện tiên quyết | Đã đăng nhập, được giao khu vực của báo cáo. Báo cáo ở trạng thái Chờ xác minh. |
| Hậu điều kiện (thành công) | Báo cáo chuyển sang Đã xác minh (kèm loại sâu bệnh, ghi chú) hoặc Bị bác bỏ (kèm lý do). Lịch sử xác minh được ghi. Với báo cáo được duyệt, có một bản ghi dự báo nguy cơ (nếu mô hình AI đang dùng được). |
| Hậu điều kiện (thất bại) | Báo cáo vẫn ở Chờ xác minh. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Mở chức năng "Xác minh báo cáo". |  |
| 2 |  | Hiện danh sách báo cáo Chờ xác minh trong khu vực phụ trách, xếp theo thời gian gửi. Đánh dấu báo cáo sắp hoặc đã quá 48 giờ. |
| 3 | Chọn một báo cáo. |  |
| 4 |  | Hiện chi tiết: mô tả, ảnh, tọa độ, người gửi. |
| 5 | Đối chiếu ảnh, mô tả, vị trí. Đi kiểm tra thực địa nếu cần. Kết luận hợp lệ. |  |
| 6 | Chọn loại sâu bệnh, nhập ghi chú và khuyến nghị. |  |
| 7 | Nhấn "Duyệt". |  |
| 8 |  | Kiểm tra quyền (BR05) và báo cáo còn ở Chờ xác minh (BR03). |
| 9 |  | Trong một transaction: cập nhật trạng thái Đã xác minh, gán loại sâu bệnh, ghi lịch sử xác minh. |
| 10 |  | Báo thành công, tạo thông báo nội bộ cho nông dân, kiểm tra ngưỡng cảnh báo (BR06). |
| 11 |  | Gọi UC18: mô hình AI tính xác suất bùng phát của loại sâu bệnh vừa duyệt trong khu vực ở 7 ngày tới và lưu bản ghi dự báo. Nguy cơ Cao thì tạo cảnh báo và thông báo cho khu vực (BR08). |
| 12 |  | Làm mới danh sách chờ. |

Luồng thay thế

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Bước 5: báo cáo không hợp lệ. | Chuyên gia nhấn "Từ chối", nhập lý do (BR04). Thực hiện UC08 Từ chối báo cáo. Không chạy dự báo AI. Kết thúc. |
| A2 | Bước 7: chưa chọn loại sâu bệnh. | Yêu cầu chọn (BR04), quay lại bước 6. |
| A3 | Bước 5: cần thêm thông tin mới kết luận được. | Thoát màn hình chi tiết. Báo cáo giữ nguyên Chờ xác minh. |
| A4 | Bước 2: không có báo cáo nào đang chờ. | Hiện "Không có báo cáo cần xử lý". |

Luồng ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| E1 | Bước 8: chuyên gia khác đã xử lý báo cáo trước. | Báo "Báo cáo đã được xử lý", làm mới danh sách, không ghi đè. |
| E2 | Bước 8: chuyên gia không thuộc khu vực, hoặc là người gửi báo cáo. | Từ chối, báo không đủ quyền (BR05). |
| E3 | Bước 9: lỗi CSDL. | Rollback, giữ nguyên trạng thái, báo lỗi. |
| E4 | Bước 11: không nạp được mô hình AI hoặc chưa có mô hình đang dùng. | Ghi nhật ký lỗi, bỏ qua dự báo, chỉ áp dụng BR06. Việc duyệt ở bước 9 vẫn giữ nguyên. |


### 2.4.8 Đặc tả usecase “Từ chối báo cáo”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Từ chối báo cáo |
| Tác nhân chính | Chuyên gia / Nhân viên kiểm định (extend UC07) |
| Mô tả | Chuyên gia bác bỏ báo cáo không đúng sự thật hoặc thiếu thông tin, kèm lý do. |
| Điều kiện tiên quyết | Như UC07; chuyên gia kết luận báo cáo không hợp lệ. |
| Hậu điều kiện (thành công) | Báo cáo sang Bị bác bỏ, lịch sử ghi lý do, nông dân nhận thông báo kèm hạn gửi lại. Nếu báo cáo đã gửi lại một lần thì hồ sơ được đóng (Đã xử lý). |
| Hậu điều kiện (thất bại) | Báo cáo vẫn Chờ xác minh. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Nhập lý do, nhấn "Từ chối". |  |
| 2 |  | Kiểm tra lý do không rỗng (BR04), quyền khu vực và trạng thái (BR05, BR03). |
| 3 |  | Trong một transaction: chuyển Bị bác bỏ, ghi lịch sử với GhiChu = lý do, tạo thông báo cho nông dân. |
| 4 |  | Nếu SoLanGuiLai = 1: chuyển tiếp sang Đã xử lý (đóng hồ sơ), ghi thêm một dòng lịch sử. |

Luồng thay thế và ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Lý do để trống. | Chặn, báo phải nhập lý do. |
| E1 | Chuyên gia khác đã xử lý trước. | Báo "Báo cáo đã được xử lý", không ghi đè. |


### 2.4.9 Đặc tả usecase “Phân loại sâu bệnh”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Phân loại sâu bệnh |
| Tác nhân chính | Chuyên gia / Nhân viên kiểm định (được UC07 include) |
| Mô tả | Chuyên gia chọn loại sâu bệnh trong danh mục và ghi khuyến nghị xử lý. |
| Điều kiện tiên quyết | Chuyên gia đã xem chi tiết báo cáo (UC06). |
| Hậu điều kiện (thành công) | Báo cáo có MaLoai và KhuyenNghi khi được duyệt. |
| Hậu điều kiện (thất bại) | Báo cáo chưa được duyệt. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Chọn loại sâu bệnh trong ComboBox. |  |
| 2 |  | Hiện triệu chứng và biện pháp phòng trừ của loại đó để chuyên gia đối chiếu. |
| 3 |  | Điền sẵn ô khuyến nghị bằng BienPhapPhongTru. |
| 4 | Sửa khuyến nghị cho phù hợp với ruộng của nông dân. |  |

Luồng thay thế và ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Loại sâu bệnh chưa có trong danh mục. | Chuyên gia giữ báo cáo ở Chờ xác minh, báo Quản trị viên thêm loại mới (UC13). |


### 2.4.10 Đặc tả usecase “Đánh dấu đã xử lý”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Đánh dấu đã xử lý |
| Tác nhân chính | Chuyên gia / Nhân viên kiểm định |
| Mô tả | Chuyên gia ghi nhận đã có biện pháp xử lý cho báo cáo đã xác minh. |
| Điều kiện tiên quyết | Báo cáo ở Đã xác minh, thuộc khu vực chuyên gia phụ trách. |
| Hậu điều kiện (thành công) | Báo cáo sang Đã xử lý, ghi lịch sử, nông dân nhận thông báo. Báo cáo không được sửa nữa (BR03). |
| Hậu điều kiện (thất bại) | Báo cáo vẫn Đã xác minh. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Chọn báo cáo Đã xác minh, nhấn "Đã xử lý", nhập ghi chú biện pháp đã làm. |  |
| 2 |  | Kiểm tra quyền, trạng thái và báo cáo đã có khuyến nghị. |
| 3 |  | Trong một transaction: chuyển Đã xử lý, ghi lịch sử, tạo thông báo cho nông dân. |

Luồng thay thế và ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Báo cáo chưa có khuyến nghị. | Yêu cầu nhập khuyến nghị trước. |


### 2.4.11 Đặc tả usecase “Xem thống kê”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Xem thống kê |
| Tác nhân chính | Chuyên gia / Nhân viên kiểm định, Quản trị viên |
| Mô tả | Xem số báo cáo theo trạng thái, loại sâu bệnh, khu vực, thời gian, cùng các cảnh báo đã phát. |
| Điều kiện tiên quyết | Đã đăng nhập với vai trò Chuyên gia hoặc Quản trị viên. |
| Hậu điều kiện (thành công) | Không đổi dữ liệu. |
| Hậu điều kiện (thất bại) | Không đổi dữ liệu. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Chọn khoảng thời gian, khu vực, loại sâu bệnh. |  |
| 2 |  | Chuyên gia chỉ được chọn khu vực mình phụ trách; Quản trị viên chọn được tất cả. |
| 3 |  | Đếm báo cáo theo trạng thái; số ca dịch chỉ tính báo cáo Đã xác minh và Đã xử lý. |
| 4 |  | Hiện bảng, biểu đồ cột theo tuần và danh sách cảnh báo, dự báo nguy cơ trong kỳ. |

Luồng thay thế và ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Ngày bắt đầu sau ngày kết thúc. | Báo lỗi khoảng thời gian. |


### 2.4.12 Đặc tả usecase “Nhận cảnh báo dịch hại”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Nhận cảnh báo dịch hại |
| Tác nhân chính | Nông dân, Chuyên gia / Nhân viên kiểm định |
| Mô tả | Người dùng trong khu vực nhận và xem cảnh báo dịch do BR06 hoặc dự báo AI (BR08) tạo ra. |
| Điều kiện tiên quyết | Có cảnh báo mới. Người nhận là chuyên gia phụ trách khu vực và nông dân đã từng gửi báo cáo trong khu vực đó. |
| Hậu điều kiện (thành công) | Thông báo cảnh báo được đánh dấu đã đọc. |
| Hậu điều kiện (thất bại) | Thông báo vẫn chưa đọc. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 |  | Mỗi 60 giây giao diện cập nhật số thông báo chưa đọc trên khung chính. |
| 2 | Mở thông báo cảnh báo. |  |
| 3 |  | Hiện khu vực, loại sâu bệnh, số ca đã xác minh, mức nguy cơ và xác suất (nếu từ dự báo AI), biện pháp phòng trừ. |
| 4 |  | Đánh dấu thông báo đã đọc. |


### 2.4.13 Đặc tả usecase “Quản lý danh mục sâu bệnh”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Quản lý danh mục sâu bệnh |
| Tác nhân chính | Quản trị viên |
| Mô tả | Thêm, sửa, xóa, tìm loại sâu bệnh cùng triệu chứng và biện pháp phòng trừ. |
| Điều kiện tiên quyết | Đã đăng nhập với vai trò Quản trị viên. |
| Hậu điều kiện (thành công) | Bảng LoaiSauBenh được cập nhật. |
| Hậu điều kiện (thất bại) | Danh mục giữ nguyên. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Mở "Danh mục sâu bệnh". |  |
| 2 |  | Hiện danh sách, ô tìm theo tên. |
| 3 | Thêm hoặc sửa: nhập tên, triệu chứng, biện pháp phòng trừ, nhấn "Lưu". |  |
| 4 |  | Kiểm tra tên không rỗng, không trùng (UQ TenLoai), rồi lưu. |

Luồng thay thế và ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Xóa loại đã được dùng trong báo cáo hoặc cảnh báo. | Không cho xóa (ràng buộc khóa ngoại), báo loại đang được dùng. |


### 2.4.14 Đặc tả usecase “Quản lý khu vực và phân công”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Quản lý khu vực và phân công |
| Tác nhân chính | Quản trị viên |
| Mô tả | Quản trị viên thêm, sửa khu vực và giao chuyên gia phụ trách khu vực. |
| Điều kiện tiên quyết | Đã đăng nhập với vai trò Quản trị viên. |
| Hậu điều kiện (thành công) | Bảng KhuVuc hoặc PhanCongKhuVuc được cập nhật. Chuyên gia thấy ngay báo cáo của khu vực mới được giao. |
| Hậu điều kiện (thất bại) | Dữ liệu khu vực và phân công giữ nguyên. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Mở chức năng "Khu vực và phân công". |  |
| 2 |  | Hiện danh sách khu vực, mỗi khu vực kèm chuyên gia đang phụ trách. |
| 3 | Chọn một khu vực, nhấn "Phân công", chọn một chuyên gia. |  |
| 4 |  | Kiểm tra người được chọn có vai trò Chuyên gia, tài khoản không bị khóa, chưa được giao khu vực này. |
| 5 |  | Thêm dòng PhanCongKhuVuc (MaNguoiDung, MaKhuVuc, NgayPhanCong). Tạo thông báo cho chuyên gia. |
| 6 |  | Làm mới danh sách. |

Luồng thay thế

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Bước 3: nhấn "Thêm khu vực". | Nhập tên và mô tả. Tên trùng thì báo lỗi (UQ TenKhuVuc). |
| A2 | Bước 3: nhấn "Sửa". | Cho sửa tên, mô tả. Không đổi mã khu vực. |
| A3 | Bước 3: nhấn "Gỡ phân công". | Hỏi xác nhận rồi xóa dòng PhanCongKhuVuc. Nếu khu vực không còn chuyên gia nào thì cảnh báo Quản trị viên phân công người khác. |
| A4 | Bước 4: chuyên gia đã được giao khu vực này. | Báo trùng, không ghi (khóa chính là cặp MaNguoiDung, MaKhuVuc). |

Luồng ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| E1 | Bước 5: lỗi CSDL. | Báo lỗi, không đổi dữ liệu. |


### 2.4.15 Đặc tả usecase “Quản lý người dùng”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Quản lý người dùng |
| Tác nhân chính | Quản trị viên |
| Mô tả | Tạo tài khoản, khóa, mở khóa, đổi vai trò. |
| Điều kiện tiên quyết | Đã đăng nhập với vai trò Quản trị viên. |
| Hậu điều kiện (thành công) | Bảng NguoiDung được cập nhật. |
| Hậu điều kiện (thất bại) | Dữ liệu giữ nguyên. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Mở "Người dùng". |  |
| 2 |  | Hiện danh sách, lọc theo vai trò, trạng thái khóa. |
| 3 | Tạo tài khoản: nhập tên đăng nhập, họ tên, điện thoại, vai trò, mật khẩu ban đầu. |  |
| 4 |  | Sinh muối ngẫu nhiên, băm mật khẩu PBKDF2 (SEC01), lưu. |

Luồng thay thế và ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Tên đăng nhập đã có. | Báo trùng (UQ TenDangNhap). |
| A2 | Quản trị viên khóa chính tài khoản mình. | Không cho phép. |
| A3 | Khóa chuyên gia đang phụ trách khu vực. | Cảnh báo cần phân công người khác (UC14). |


### 2.4.16 Đặc tả usecase “Cấu hình hệ thống”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Cấu hình hệ thống |
| Tác nhân chính | Quản trị viên |
| Mô tả | Sửa năm tham số trong ThamSoHeThong. |
| Điều kiện tiên quyết | Đã đăng nhập với vai trò Quản trị viên. |
| Hậu điều kiện (thành công) | Tham số mới có hiệu lực ở lần đọc kế tiếp. |
| Hậu điều kiện (thất bại) | Tham số giữ nguyên. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Mở "Cấu hình". |  |
| 2 |  | Hiện HAN_XAC_MINH_GIO, NGUONG_CANH_BAO, SO_NGAY_XET_NGUONG, HAN_GUI_LAI_NGAY, NGUONG_NGUY_CO cùng mô tả. |
| 3 | Sửa giá trị, nhấn "Lưu". |  |
| 4 |  | Kiểm tra giá trị nguyên dương (CHECK GiaTri > 0); NGUONG_NGUY_CO từ 1 đến 100. |

Luồng thay thế và ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Giá trị không hợp lệ. | Tô đỏ ô sai, không lưu. |


### 2.4.17 Đặc tả usecase “Xem thông báo”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Xem thông báo |
| Tác nhân chính | Người dùng |
| Mô tả | Người dùng xem các thông báo nội bộ của mình. |
| Điều kiện tiên quyết | Đã đăng nhập. |
| Hậu điều kiện (thành công) | Thông báo được mở chuyển sang đã đọc. |
| Hậu điều kiện (thất bại) | Không đổi dữ liệu. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 |  | Mỗi 60 giây giao diện gọi API đếm thông báo chưa đọc (demChuaDoc), hiện số trên khung chính. |
| 2 | Nhấn biểu tượng thông báo. |  |
| 3 |  | Mở trang ThongBaoPage, nạp thông báo có MaNguoiNhan là người đang đăng nhập, dòng chưa đọc in đậm. |
| 4 | Chọn một thông báo. |  |
| 5 |  | Đặt DaDoc = 1; nếu thông báo gắn với báo cáo thì mở chi tiết báo cáo. |


### 2.4.18 Đặc tả usecase “Dự báo nguy cơ dịch (AI)”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Dự báo nguy cơ dịch (AI) |
| Tác nhân chính | Không có tác nhân trực tiếp (được UC07 include) |
| Mô tả | Sau khi một báo cáo được duyệt, mô hình AI dự báo khả năng loại sâu bệnh đó bùng phát trong khu vực ở 7 ngày tới. |
| Điều kiện tiên quyết | Báo cáo vừa chuyển sang Đã xác minh. Có một dòng MoHinhAI với DangSuDung = 1. |
| Hậu điều kiện (thành công) | Có một dòng DuBaoNguyCo. Nếu mức Cao và chưa có cảnh báo cùng loại, cùng khu vực trong SO_NGAY_XET_NGUONG ngày: có thêm CanhBao (MaDuBao) và thông báo cho khu vực. |
| Hậu điều kiện (thất bại) | Không có bản ghi dự báo; hệ thống chỉ áp dụng BR06. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 |  | AIService nhận MaBaoCao sau khi transaction duyệt đã commit. |
| 2 |  | Lấy đặc trưng: số ca đã xác minh cùng loại, cùng khu vực trong 7, 14, 30 ngày trước, mức độ trung bình, tháng trong năm. |
| 3 |  | Nạp mô hình đang dùng, tính xác suất bùng phát. |
| 4 |  | Xếp mức: Thấp (dưới một nửa NGUONG_NGUY_CO), Trung bình (từ một nửa đến dưới ngưỡng), Cao (từ ngưỡng trở lên). Lưu DuBaoNguyCo kèm MaMoHinh. |
| 5 |  | Mức Cao: tạo CanhBao, gửi thông báo cho chuyên gia khu vực và nông dân đã gửi báo cáo trong khu vực, kèm biện pháp phòng trừ. |

Luồng thay thế và ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Mức Cao nhưng đã có cảnh báo cùng loại, cùng khu vực trong kỳ. | Chỉ lưu dự báo, không tạo cảnh báo trùng. |
| E1 | Lỗi nạp mô hình hoặc chưa có mô hình. | Ghi nhật ký, chỉ áp dụng BR06. |


### 2.4.19 Đặc tả usecase “Huấn luyện lại mô hình AI”
| Mục | Nội dung |
|---|---|
| Tên Use Case | Huấn luyện lại mô hình AI |
| Tác nhân chính | Quản trị viên |
| Mô tả | Quản trị viên chạy huấn luyện mô hình dự báo nguy cơ từ dữ liệu đã xác minh, xem kết quả đánh giá và quyết định dùng phiên bản mới. |
| Điều kiện tiên quyết | Đã đăng nhập với vai trò Quản trị viên. CSDL có báo cáo Đã xác minh hoặc Đã xử lý. |
| Hậu điều kiện (thành công) | Có một dòng MoHinhAI mới. Nếu được kích hoạt: DangSuDung = 1 cho bản mới, 0 cho bản cũ; các lần dự báo sau dùng bản mới. |
| Hậu điều kiện (thất bại) | Phiên bản đang dùng giữ nguyên. |

Luồng chính

| Bước | Tác nhân | Hệ thống |
|---|---|---|
| 1 | Mở chức năng "Mô hình AI". |  |
| 2 |  | Hiện phiên bản đang dùng: ngày huấn luyện, số mẫu, độ chính xác. |
| 3 | Nhấn "Huấn luyện lại". |  |
| 4 |  | Lấy báo cáo Đã xác minh và Đã xử lý (BR08), tạo bảng mẫu theo khu vực, loại sâu bệnh, ngày. |
| 5 |  | Chia mẫu theo thời gian: 80% cũ hơn để huấn luyện, 20% mới hơn để kiểm tra. |
| 6 |  | Huấn luyện mô hình phân loại nhị phân bằng Smile, tính Accuracy, Precision, Recall, F1, AUC trên tập kiểm tra. |
| 7 |  | Lưu file mô hình và một dòng MoHinhAI (DangSuDung = 0). Hiện kết quả so với bản đang dùng. |
| 8 | Nhấn "Kích hoạt". |  |
| 9 |  | Kiểm tra F1 không thấp hơn bản đang dùng (ARCH05). Đặt DangSuDung = 1 cho bản mới, 0 cho bản cũ, trong một transaction. |

Luồng thay thế

| Mã | Điều kiện | Xử lý |
|---|---|---|
| A1 | Bước 4: dữ liệu chỉ có một loại nhãn (toàn bùng phát hoặc toàn không). | Báo chưa đủ dữ liệu để huấn luyện, dừng. Hệ thống tiếp tục dùng bản cũ hoặc BR06. |
| A2 | Bước 9: mô hình mới kém hơn bản đang dùng. | Không cho kích hoạt, giữ bản cũ. Bản mới vẫn được lưu để đối chiếu. |
| A3 | Bước 8: Quản trị viên đóng màn hình. | Bản mới giữ ở trạng thái chờ đánh giá, không ảnh hưởng dự báo. |

Luồng ngoại lệ

| Mã | Điều kiện | Xử lý |
|---|---|---|
| E1 | Bước 6: lỗi khi huấn luyện (thiếu bộ nhớ, dữ liệu lỗi). | Ghi nhật ký, báo lỗi, không tạo dòng MoHinhAI. |
| E2 | Bước 7: không ghi được file mô hình. | Báo lỗi quyền ghi thư mục, không tạo dòng MoHinhAI. |
