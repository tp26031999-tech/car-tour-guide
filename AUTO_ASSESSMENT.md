# ANDROID AUTO COMPATIBILITY ASSESSMENT (chưa verify trên xe/thiết bị)

Theo tài liệu Android for Cars (tính đến 2026):
- App kể chuyện audio độc lập với dẫn đường → **media app** là đường tích hợp đúng ban đầu.
- Triển khai: `TourMediaService : MediaLibraryService` + `automotive_app_desc.xml` (`<uses name="media"/>`).
- Trên xe: hiển thị như app nhạc, điều khiển Play/Pause/Skip + nút tùy biến (Kể thêm / Điểm tiếp theo qua custom command).
- Google Maps vẫn chạy dẫn đường song song; audio focus đảm bảo không lấn prompt rẽ.
- KHÔNG hỗ trợ: tự hiện màn hình app lên xe khi đang chạy Maps, vẽ overlay lên bản đồ, đọc route đang active của Maps, voice hotword tự do, nhận diện lệnh tiếng Việt free-form offline.
- Distraction/quality: thao tác phức tạp (tải vùng, thử giọng, cài đặt) chỉ làm lúc đậu — đúng UI phone đã tách tab.
- Test chính thức: Desktop Head Unit (DHU) + `automotive` test trong Android Studio; cần kiểm tra lại khi có SDK.

Kết luận: media path là lựa chọn hợp lý, ít rủi ro kiểm duyệt nhất. POI/navigation template là dự án riêng, chưa làm.
