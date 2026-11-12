/* Dữ liệu danh mục để chạy thử giao diện.
   Chạy SAU AGR05.sql. Tài khoản người dùng do DbSeeder trong ứng dụng tạo (mật khẩu phải băm bằng C#).
   Nội dung triệu chứng, biện pháp chỉ để minh họa; khi triển khai thật lấy theo hướng dẫn của cơ quan bảo vệ thực vật. */
USE AGR05_QuanLySauBenh;
GO

INSERT INTO KhuVuc (TenKhuVuc, MoTa) VALUES
    (N'Xã A - Cánh đồng 1', N'Khu vực demo'),
    (N'Xã A - Cánh đồng 2', N'Khu vực demo'),
    (N'Xã B - Vùng trồng rau', N'Khu vực demo');

INSERT INTO LoaiSauBenh (TenLoai, TrieuChung, BienPhapPhongTru) VALUES
    (N'Rầy nâu', N'Rầy tập trung ở gốc lúa, lúa vàng và khô thành từng chòm.', N'Thăm đồng thường xuyên, áp dụng theo hướng dẫn của cán bộ bảo vệ thực vật địa phương.'),
    (N'Bệnh đạo ôn', N'Vết bệnh hình thoi trên lá, tâm xám, viền nâu; có thể gây thối cổ bông.', N'Không bón thừa đạm, áp dụng theo hướng dẫn của cán bộ bảo vệ thực vật địa phương.'),
    (N'Sâu cuốn lá nhỏ', N'Lá bị cuốn dọc thành ống, có vệt trắng do sâu ăn biểu bì.', N'Theo dõi mật độ sâu, áp dụng theo hướng dẫn của cán bộ bảo vệ thực vật địa phương.'),
    (N'Bệnh bạc lá', N'Vết bệnh chạy dọc mép lá, màu vàng rồi chuyển trắng xám.', N'Dùng giống chống chịu, áp dụng theo hướng dẫn của cán bộ bảo vệ thực vật địa phương.'),
    (N'Sâu đục thân', N'Dảnh lúa bị héo (dảnh héo), bông bạc khi trổ.', N'Vệ sinh đồng ruộng, áp dụng theo hướng dẫn của cán bộ bảo vệ thực vật địa phương.');
GO
