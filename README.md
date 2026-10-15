# Car Tour Guide VN — Offline-first AI Tour Guide (MVP)

Hướng dẫn viên аудио tiếng Việt cho chuyến xe miền Nam. Offline-first, chi phí định kỳ bắt buộc = 0.

## Trạng thái build (trung thực)
- ✅ Source MVP hoàn chỉnh trong thư mục này.
- ❌ **Chưa build được tại máy này**: máy chỉ có JRE 8, không có JDK 17 / Android SDK / Gradle.
  Không claim build pass, test pass, hay Android Auto đã verify.
- Để build: cài JDK 17 + Android Studio (SDK 34) rồi chạy lệnh dưới.

## Build & cài (máy có SDK)
```powershell
# 1) Mở Android Studio -> Open -> thư mục này (D:\tín\CAR)
# 2) Để Studio tải Gradle 8.7 + AGP 8.5.2 + deps (cần mạng lần đầu)
# 3) Chạy unit test:
.\gradlew.bat :app:testDebugUnitTest
# 4) Cài lên máy:
.\gradlew.bat :app:installDebug
```

## Dùng thử không cần lái xe (simulated GPS)
`JourneyStateManager.simFixes` nhận Flow<GeoFix> giả lập.
`SimRoute.fixes()` mô phỏng tuyến đường qua Dinh Độc Lập có nhiễu ở biên 1000m.
Test `TriggerEngineTest.simJourney_*` chạy toàn bộ kịch bản này trên JVM.

## Cấu trúc
- `app/src/main/java/com/cartourguide/`
  - `core/` — GeoUtils, TriggerEngine, StoryRanker, PlaybackQueue (thuần Kotlin, test được)
  - `data/` — Room + SeedData (8 điểm miền Nam, có nguồn)
  - `location/` — Fused Location wrapper (không gọi mạng theo mỗi GPS)
  - `journey/` — JourneyStateManager + Foreground Service
  - `audio/` — TtsSpeaker (giọng máy) + PlaybackManager (audio focus)
  - `content/` — RegionContentManager (tải vùng offline từ assets)
  - `auto/` — TourMediaService (Media3 → Android Auto media)
  - `ui/` — Compose: Chuyến đi / Vùng / Cài đặt (tiếng Việt)
  - `ai/` — interface AI optional (mặc định NoOp, không tốn phí)
- `app/src/main/assets/regions/*.json` — 5 vùng tải offline
- `app/src/test/` — 9 files test JVM (GeoUtils, TriggerEngine, StoryRanker, PlaybackQueue,
  ProcessRecreation, Provenance, RegionValidator, VoiceCheck, AiFallback)

## Quy tắc sản phẩm
- Bán kính mặc định 1000m, chỉnh trong Cài đặt (không sửa code).
- Im lặng khi không có chuyện đủ hay (không spam quán xá).
- Không tự chuyển sang dịch vụ trả phí. AI/TTS online là optional, tắt mặc định.
- Google Maps vẫn là app dẫn đường; app này chỉ kể chuyện qua loa xe.

Xem thêm: `ARCHITECTURE.md`, `ASSUMPTIONS.md`, `AUTO_ASSESSMENT.md`.
