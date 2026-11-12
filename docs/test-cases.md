# Test case

Mỗi test case phải có một unit test JUnit 5 tương ứng (tên phương thức test bắt đầu bằng mã TC).

## 7.2 Kết quả đạt được
Đề tài đã hoàn thành phần phân tích và thiết kế hệ thống AGR-05:

Khảo sát hiện trạng, sơ đồ tổ chức, 4 biểu mẫu, 8 quy tắc nghiệp vụ, 19 yêu cầu chức năng.

19 use case, trong đó 5 use case có sơ đồ phân rã; đặc tả đủ 19 use case.

Biểu đồ lớp phân tích 12 lớp, 2 biểu đồ trạng thái, 10 biểu đồ tuần tự, 3 biểu đồ hoạt động.

Cơ sở dữ liệu 15 bảng với từ điển dữ liệu, ràng buộc và script T-SQL.

Kiến trúc 3 tầng, thiết kế chức năng và thiết kế mô-đun AI dự báo nguy cơ dịch.

Thiết kế giao diện 12 màn hình và bộ test case cho các chức năng cốt lõi.

Bộ test case cho các chức năng cốt lõi

Test case được thiết kế bằng phân vùng tương đương và giá trị biên (0, 1, 5, 6 ảnh; 5 MB; vĩ độ ±90), kèm kiểm thử quyền và tranh chấp đồng thời. Cột Kết quả thực tế để trống, sẽ điền khi chạy kiểm thử trên bản cài đặt.

| Mã | UC | Dữ liệu / thao tác | Kết quả mong đợi | Kết quả thực tế |
|---|---|---|---|---|
| TC01 | UC02 | Đủ trường, 3 ảnh JPG 2 MB, tọa độ (10.77, 106.70) | Lưu Chờ xác minh, chuyên gia khu vực nhận thông báo (BR01, BR07) |  |
| TC02 | UC02 | Không chọn ảnh nào | Báo phải có ít nhất 1 ảnh, không lưu (BR01) |  |
| TC03 | UC02 | Chọn 6 ảnh, hoặc 1 ảnh 7 MB, hoặc vĩ độ = 95 | Từ chối, nêu rõ lý do, giữ dữ liệu (BR01) |  |
| TC04 | UC02 | Mất kết nối CSDL khi đang ghi | Rollback, xóa file ảnh đã lưu, form giữ nội dung |  |
| TC05 | UC07 | Bấm Duyệt khi chưa chọn loại sâu bệnh | Yêu cầu chọn loại, trạng thái không đổi (BR04) |  |
| TC06 | UC08 | Bấm Từ chối với lý do để trống | Chặn thao tác, báo phải nhập lý do (BR04) |  |
| TC07 | UC07 | Chuyên gia khu vực B duyệt báo cáo khu vực A | Báo không đủ quyền, không ghi (BR05) |  |
| TC08 | UC07 | Hai chuyên gia cùng duyệt một báo cáo | Người sau nhận "Báo cáo đã được xử lý" (BR03) |  |
| TC09 | UC07 | Duyệt ca thứ 5 cùng loại, cùng khu vực trong 7 ngày | Tạo cảnh báo, thông báo cho khu vực (BR06) |  |
| TC10 | UC03 | Gửi lại báo cáo bị bác bỏ lần thứ hai | Báo hết lượt gửi lại, không lưu (BR04) |  |
| TC11 | UC18 | Duyệt báo cáo khi chưa có mô hình đang dùng | Duyệt thành công, không có dự báo, chỉ áp dụng BR06 |  |
| TC12 | UC18 | Dự báo xác suất bằng NGUONG_NGUY_CO, chưa có cảnh báo trong kỳ | Mức Cao, tạo một cảnh báo (BR08) |  |
| TC13 | UC18 | Hai lần dự báo mức Cao cùng loại, cùng khu vực trong 7 ngày | Chỉ tạo một cảnh báo, lần sau chỉ lưu dự báo |  |
| TC14 | UC19 | Huấn luyện khi dữ liệu chỉ có nhãn 0 | Báo chưa đủ dữ liệu, giữ phiên bản cũ |  |
| TC15 | UC19 | Kích hoạt bản mới có F1 thấp hơn bản đang dùng | Từ chối kích hoạt (ARCH05) |  |
| TC16 | UC16 | Đặt NGUONG_NGUY_CO = 0 hoặc 150 | Báo giá trị không hợp lệ, không lưu |  |
