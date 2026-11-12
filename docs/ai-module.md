# Mô-đun AI dự báo nguy cơ dịch

### 5.2.5 Mô-đun AI dự báo nguy cơ dịch
a) Bài toán

Sau mỗi lần chuyên gia duyệt một báo cáo, hệ thống cần trả lời: trong 7 ngày tới, loại sâu bệnh này có bùng phát ở khu vực này không. Đây là bài toán phân loại nhị phân. Đầu ra là xác suất bùng phát, được xếp thành mức Thấp, Trung bình hoặc Cao theo tham số NGUONG_NGUY_CO.

Bùng phát được định nghĩa giống BR06 để hai cách cảnh báo dùng chung một thước đo: số báo cáo đã xác minh cùng loại, cùng khu vực trong 7 ngày tới đạt NGUONG_CANH_BAO.

b) Dữ liệu và nhãn

Dữ liệu chỉ lấy từ báo cáo Đã xác minh và Đã xử lý (BR08). Báo cáo đang chờ hoặc bị bác bỏ không được dùng, vì chưa chắc là thật. Mỗi mẫu ứng với một báo cáo đã duyệt. Nhãn bằng 1 nếu trong 7 ngày sau ngày duyệt có đủ NGUONG_CANH_BAO ca đã xác minh cùng loại, cùng khu vực; ngược lại bằng 0. Mẫu chưa đủ 7 ngày quan sát thì bỏ ra vì chưa biết nhãn.

c) Đặc trưng

Các đặc trưng đều tính được từ CSDL hiện có, không cần dữ liệu ngoài:

| Đặc trưng | Kiểu | Cách tính |
|---|---|---|
| soCa7, soCa14, soCa30 | Số | Số ca đã xác minh cùng loại, cùng khu vực trong 7, 14, 30 ngày trước ngày duyệt. |
| xuHuong | Số | soCa7 trừ số ca của 7 ngày trước đó. |
| mucDoTrungBinh | Số | Mức độ trung bình của các ca trong 14 ngày. |
| thang | Mã hóa one-hot | Tháng trong năm, phản ánh mùa vụ. |
| maLoai, maKhuVuc | Mã hóa one-hot | Loại sâu bệnh và khu vực. |

d) Thuật toán, huấn luyện và đánh giá

Mô hình dùng thuật toán GradientTreeBoost (cây quyết định tăng cường) cho phân loại nhị phân của thư viện Smile (Java). Thuật toán xử lý được đặc trưng dạng số lẫn dạng mã hóa one-hot và không cần chuẩn hóa dữ liệu. Có thể thay bằng hồi quy logistic (LogisticRegression của Smile) nếu muốn mô hình dễ giải thích hơn.

Dữ liệu được chia theo thời gian: 80% mẫu cũ hơn để huấn luyện, 20% mẫu mới hơn để kiểm tra. Không chia ngẫu nhiên, vì như vậy mô hình sẽ được học từ dữ liệu tương lai. Chỉ số đánh giá gồm Accuracy, Precision, Recall, F1 và AUC. F1 được dùng để so sánh hai phiên bản, vì số mẫu bùng phát thường ít hơn nhiều so với mẫu không bùng phát, nên Accuracy dễ cao giả.

Báo cáo này chỉ trình bày thiết kế. Giá trị cụ thể của các chỉ số chỉ có sau khi huấn luyện trên dữ liệu thật và được lưu vào bảng MoHinhAI, nên không ghi số liệu ở đây.

e) Tích hợp vào hệ thống

AIService là lớp duy nhất gọi thư viện Smile. Khi khởi động, service nạp file mô hình của phiên bản đang dùng vào bộ nhớ. Việc dự báo chạy sau khi transaction duyệt đã Commit; lỗi ở bước này chỉ ghi nhật ký và hệ thống quay về BR06. Mỗi dự báo lưu MaMoHinh để truy lại phiên bản đã dùng. Quản trị viên huấn luyện lại qua UC19; phiên bản mới chỉ được bật khi F1 không thấp hơn bản đang dùng.

f) Giới hạn

Lúc mới triển khai chưa có dữ liệu nên chưa huấn luyện được; trong giai đoạn này hệ thống chỉ dùng BR06. Mô hình không dùng dữ liệu thời tiết hay giai đoạn sinh trưởng, nên chỉ dự báo theo xu hướng số ca. Dự báo chỉ để tham khảo; AI không đổi trạng thái báo cáo và không thay kết luận của chuyên gia.
