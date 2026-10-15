# ASSUMPTIONS & UNRESOLVED DECISIONS

## Đã chốt (theo yêu cầu)
- Nền tảng: Android phone; xe: Android Auto; dẫn đường: Google Maps.
- Bán kính 1000m mặc định, chỉnh được. Tự động kể, chọn lọc thông minh, giọng văn miền Nam.
- Ngân sách MVP: 0 phí định kỳ bắt buộc. Offline-first, tải vùng trước.
- Cá nhân, voice-oriented trong giới hạn platform.

## Giả định kỹ thuật (cần kiểm chứng trên máy có SDK)
1. Media3 1.4.1 + car-app 1.7.0 tương thích compileSdk 34 / AGP 8.5.2 — chưa build nên chưa xác nhận.
2. Tọa độ seed làm tròn ~10m từ dữ liệu bản đồ công cộng; các điểm ngoài trung tâm SG nên khảo sát thực địa lại.
3. Giọng TTS tiếng Việt có sẵn phụ thuộc máy (Google TTS + data VI). Không claim giọng miền Nam.
4. Background location + foreground service bị giới hạn bởi từng hãng máy (OEM) — cần test tắt màn hình thực tế.
5. `narrated` (Room, v2) lưu lịch sử đã kể + `TriggerEngine.snapshot/restore()` giữ cooldown
   khi process chết và khởi động lại — đã có code + test `ProcessRecreationTest`, chờ chạy trên máy có SDK.

## Chưa quyết (giữ configurable)
- AI provider: chưa chọn. So sánh sau MVP theo giá/quota, chất lượng VI, privacy, offline, điều khoản thương mại, API key.
- TTS chất lượng cao: interface TtsSpeaker thay được; chưa chọn nhà cung cấp.
- POI template trên Auto: chỉ làm khi media path đã chạy + chính đáng.
- Remote content sync: interface để trống; MVP dùng assets kèm app.

## Không bịa
- Không bịa credentials, giá dịch vụ, tính năng API, kết quả test, tương thích Auto.
- Chuyện seed có nguồn Wikipedia VN; đoạn "tương truyền" gắn nhãn LEGEND.
- Không copy kết quả Google Maps search vào DB offline (điều khoản Google).
