# Kịch bản demo khi vấn đáp

Thời lượng khoảng 10–15 phút. Mỗi bước ghi rõ bấm gì, hệ thống phản hồi gì và quy tắc nào được minh họa.

## 0. Chuẩn bị (làm trước buổi vấn đáp)

1. Bật SQL Server (dịch vụ `SQL Server (SQLEXPRESS)` đang chạy).
2. Lần đầu, hoặc sau khi sửa code: chạy `dong-goi.bat` ở thư mục `AGR05` (khoảng 1 phút, gồm cả chạy test). Kết quả nằm trong `phat-hanh\`.
3. Nạp dữ liệu mẫu (chỉ cần một lần, chạy lại cũng không tạo trùng):
   ```
   cd phat-hanh
   java -jar agr05.jar --spring.profiles.active=demo
   ```
   Thấy dòng `Đã tạo dữ liệu mẫu` (hoặc `Dữ liệu mẫu đã có`) thì bấm `Ctrl+C`. Lần đầu mất khoảng 30 giây vì huấn luyện luôn mô hình AI đầu tiên.
4. Đã tập demo rồi, muốn làm lại từ đầu: chạy `sqlcmd -S . -E -I -f 65001 -i db\lam_moi_du_lieu_mau.sql`, rồi làm lại bước 3. Script cũng xóa loại sâu bệnh thêm khi demo (ví dụ Bệnh vàng lùn). Script chỉ xóa tài khoản mẫu và báo cáo của họ; tài khoản, báo cáo do người dùng thật tạo được giữ lại.
5. Khi demo: chạy `phat-hanh\chay.bat` và mở http://localhost:8081 (cổng ghi trong `phat-hanh\application.yml`). Tắt bản đang chạy bằng `mvn spring-boot:run` trước, nếu không sẽ trùng cổng.
6. Mở sẵn 3 cửa sổ trình duyệt để mỗi vai trò có phiên riêng: Chrome thường (nông dân), Chrome ẩn danh (chuyên gia), Edge (quản trị viên).

Mọi tài khoản mẫu dùng mật khẩu `Agr05@2026`.

| Vai trò | Tài khoản | Họ tên | Ghi chú |
|---|---|---|---|
| Quản trị viên | `qt.minhkhoa` | Đặng Minh Khoa | |
| Chuyên gia | `cg.thanhtam` | Võ Thanh Tâm | Phụ trách Thới Thạnh, Định Môn (dùng khi demo) |
| Chuyên gia | `cg.ngocanh` | Huỳnh Ngọc Ánh | Phụ trách Trường Xuân, Xuân Thắng |
| Nông dân | `nd.vanbay` | Trần Văn Bảy | Có báo cáo bị bác bỏ để demo gửi lại |
| Nông dân | `nd.thimai`, `nd.vantu`, `nd.vanut` | Lê Thị Mai, Nguyễn Văn Tư, Phạm Văn Út | |

Dữ liệu mẫu gồm:
- 4 khu vực ở huyện Thới Lai (Cần Thơ) và 5 loại sâu bệnh hại lúa.
- Khoảng 880 báo cáo lịch sử trong 8 tháng, có ảnh thật, để huấn luyện AI. Số liệu này là **mô phỏng** (sinh ngẫu nhiên), không phải số liệu điều tra thật; không đưa vào báo cáo đồ án như kết quả thực tế.
- Tình huống hiện tại ở **Xã Thới Thạnh - Cánh đồng lớn**: 4 ca Rầy nâu đã xác minh trong 4 ngày qua, 3 báo cáo chờ xác minh (1 báo cáo đã quá hạn), 1 báo cáo bị bác bỏ.
- Ảnh là ảnh thật từ Wikimedia Commons; nguồn, tác giả và giấy phép ghi trong `backend/src/main/resources/anh-mau/NGUON.md`.

## 1. Đăng nhập và phân quyền (UC01, SEC01)

| Bấm | Thấy | Ý nghĩa |
|---|---|---|
| Đăng nhập `nd.vanbay` với mật khẩu sai, rồi tên tài khoản không tồn tại | Hai lần cùng một câu "Tên đăng nhập hoặc mật khẩu không đúng." | Không lộ tài khoản nào tồn tại |
| Đăng nhập đúng | Menu chỉ có Gửi báo cáo, Báo cáo của tôi | Menu theo vai trò; quyền thật do server kiểm tra |
| Gõ thẳng địa chỉ `/quan-tri/nguoi-dung` | Bị đưa về trang chính | API quản trị trả 403 với nông dân |

## 2. Nông dân gửi báo cáo (UC02, BR01, ARCH02)

Chuẩn bị sẵn 1–2 ảnh sâu bệnh thật (ảnh chụp hoặc tải trên mạng) trong máy.

1. Gửi báo cáo → điền thông tin, khu vực **Xã Thới Thạnh**, **không chọn ảnh** → Gửi: báo "Báo cáo phải có ít nhất 1 ảnh", dữ liệu đã nhập vẫn giữ (TC02).
2. Nhập vĩ độ `95` → ô vĩ độ viền đỏ (TC03).
3. Bấm "Lấy vị trí hiện tại" (trình duyệt hỏi quyền vị trí) hoặc nhập `10.0652`, `105.5871`; chọn ảnh → Gửi: về Báo cáo của tôi, báo cáo ở trạng thái Chờ xác minh (TC01).
4. Chỉ ra: ảnh lưu thành file, CSDL chỉ lưu đường dẫn; ảnh tải qua API có kiểm tra quyền.

## 3. Nông dân gửi lại báo cáo bị bác bỏ (UC03, BR04)

1. Thông báo (số đỏ trên nút) → "Báo cáo bị bác bỏ: Có nhiều bướm nhỏ màu vàng bay trong ruộng" → Mở báo cáo: thấy lý do (ảnh chỉ có con bướm, chưa thấy lá bị hại).
2. Bấm **Gửi lại** → thêm ảnh lá lúa bị cuốn → Gửi lại: về Chờ xác minh, số lần gửi lại 1/1.
3. Nói: nếu bị bác bỏ lần nữa thì hệ thống tự đóng hồ sơ (TC10, BR04).

## 4. Chuyên gia xác minh (UC05–UC08, BR02–BR06, BR08)

1. Đăng nhập `cg.thanhtam` → Xác minh báo cáo: dòng **đỏ** "Lúa vàng lá từng chòm, chưa rõ nguyên nhân" là báo cáo quá 48 giờ (BR02); chức năng nền chạy mỗi 60 giây đã gắn cờ và báo cho Quản trị viên.
2. Mở **"Rầy nâu bám dày gốc lúa, có chòm cháy rầy"**: xem ảnh lớn, ảnh nhỏ, lịch sử, Xem trên bản đồ.
3. Bấm **Duyệt** khi chưa chọn loại → báo "Vui lòng chọn loại sâu bệnh" (TC05).
4. (Để demo TC08) Mở cùng báo cáo này ở **thêm một tab** nữa của cửa sổ chuyên gia, để đó.
5. Ở tab đầu, chọn **Rầy nâu** (hiện triệu chứng, biện pháp) → nhập khuyến nghị → **Duyệt**:
   - Ô "Nguy cơ dịch" hiện mức và xác suất của mô hình AI (BR08, chạy sau khi Commit).
   - Đây là ca Rầy nâu thứ 5 ở Thới Thạnh trong 7 ngày nên **cảnh báo dịch BR06 được tạo ngay** (TC09). Mở Thống kê và cảnh báo để xem dòng cảnh báo, nguồn "Ngưỡng (BR06)".
6. Sang tab thứ hai (vẫn hiện báo cáo ở trạng thái cũ) → chọn loại → **Duyệt** → "Báo cáo đã được xử lý." (TC08: câu UPDATE có điều kiện `MaTrangThai = 'CHO_XAC_MINH'`, không ai ghi đè kết quả của người trước).
7. Mở **"Lúa vàng lá từng chòm, chưa rõ nguyên nhân"** → ô Loại sâu bệnh không có bệnh phù hợp → bấm **Đề nghị thêm loại mới** → nhập "Bệnh vàng lùn" (thử nhập "rầy nâu" trước: báo đã có trong danh mục) → Gửi đề nghị. Lịch sử xử lý có thêm dòng đề nghị; báo cáo vẫn Chờ xác minh. Sang cửa sổ quản trị: Thông báo "Đề nghị thêm loại sâu bệnh" → Mở danh mục sâu bệnh → thêm "Bệnh vàng lùn". Quay lại cửa sổ chuyên gia: có thông báo danh mục có loại mới, mở lại báo cáo, chọn Bệnh vàng lùn → Duyệt.
8. Mở **"Lá lúa có vết hình thoi, tâm xám, viền nâu"** → Từ chối với lý do trống → bị chặn (TC06). Hoặc duyệt là **Bệnh đạo ôn**.
9. Tab "Đã xác minh (chờ xử lý)" → chọn báo cáo vừa duyệt → **Đã xử lý** (UC10).
10. Quay lại cửa sổ nông dân: có thông báo kết quả xác minh và **cảnh báo dịch hại** cho khu vực.

## 5. Quản trị viên (UC11, UC13–UC16, UC19)

1. Đăng nhập `qt.minhkhoa` → Thông báo "Báo cáo quá hạn" → Mở báo cáo (trang Tra cứu báo cáo).
2. Thống kê và cảnh báo: bảng theo trạng thái, biểu đồ số ca theo tuần, cảnh báo trong kỳ.
3. Quản trị → Cấu hình: đặt `NGUONG_NGUY_CO` = 150 → Lưu → báo không hợp lệ (TC16).
4. Quản trị → Mô hình AI: đã có 1 phiên bản đang dùng (huấn luyện lúc nạp dữ liệu mẫu). Bấm **Huấn luyện lại** (vài giây): ra phiên bản mới với Accuracy, F1, AUC, chưa dùng ngay. Chọn bản mới → Kích hoạt: chỉ được khi F1 không thấp hơn bản đang dùng (TC15, ARCH05). Nói thêm: dữ liệu chỉ có một nhãn thì không huấn luyện (TC14).
5. Người dùng: bấm Khóa chính `qt.minhkhoa` → "Không thể khóa chính tài khoản của mình"; chọn một tài khoản → Đặt lại mật khẩu.
6. Khu vực và phân công: chọn Xã Thới Thạnh → thấy chuyên gia Võ Thanh Tâm, thử phân công trùng → báo đã được giao.

## 6. Nếu được hỏi về kiến trúc, kiểm thử

- Mở `backend/src/main/java/vn/agr05`: 4 gói `api → bll → dal → entity`; SQL chỉ có trong `dal`, luôn dùng tham số.
- `KienTrucTest` (ArchUnit) tự kiểm tra luật phụ thuộc một chiều; `docs/ket-qua-kiem-thu.md` có bảng TC01–TC16 với kết quả thật.
- Transaction: `XacMinhService.xacMinh` mở một `TransactionTemplate` gồm đổi trạng thái, lịch sử, thông báo, BR06; AI chạy sau Commit, lỗi thì trả null.
- Chạy `cd backend && mvn verify` trước mặt giảng viên nếu cần (khoảng 30 giây, 70 test không cần CSDL).
- Kiểm thử đầu-cuối qua API trên CSDL thật: `python cong-cu/kiem_thu_api.py` (47 tình huống). Script dùng hết tình huống demo nên chạy xong phải làm bước 0.4.

## Lỗi hay gặp

| Hiện tượng | Cách xử lý |
|---|---|
| Trang trắng hoặc "Không kết nối được máy chủ" | Chưa chạy `chay.bat`, hoặc SQL Server chưa bật |
| "Port 8081 was already in use" | Đang chạy bản khác (mvn spring-boot:run) → tắt bản đó |
| Duyệt ca Rầy nâu nhưng không có cảnh báo | Tình huống đã dùng rồi → làm bước 0.4 |
| Lấy vị trí không được | Trình duyệt chưa cho phép vị trí; nhập tọa độ bằng tay |
