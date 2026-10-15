# ARCHITECTURE

```
[GPS Fused] -> LocationMonitor -> JourneyStateManager --+
                                                        v
Room (places/stories) -> TriggerEngine (1000m+hysteresis) -> StoryRanker (score) -> PlaybackManager
                                                        ^                              |
DataStore (prefs) --------------------------------------+              +---------------+--+
                                                                                          v
                                            TtsSpeaker (device VI voice) <-> AudioFocus <-> MediaSession
                                                                                          |
RegionContentManager (assets/regions/*.json -> Room) ------- offline readiness             v
                                                                              TourMediaService (Auto media)
```

- Offline-first: mọi quyết định trigger/rank/phát đều local. Không backend, không key trong repo.
- Module biên rõ: location / ranking / journey / content / audio / voice / auto / ai-optional / privacy-config.
- Luồng GPS: Fused BALANCED_POWER, min 20m / 5s; math local; không gọi API mạng theo tọa độ.
- Audio: 1 queue, không overlap; focus loss (nav prompt, call) -> pause; resume thủ công hoặc sau prompt ngắn.
- Voice: lệnh VN ("Tạm dừng/Tiếp tục/Kể thêm/Bỏ qua/Điểm tiếp theo") ánh xạ sang media controls + custom command MORE_INFO/NEXT_PLACE. Không hotword liên tục.
- Auto: media category (MediaLibraryService). Không tự vẽ overlay lên Google Maps.
- AI optional: interface AiStoryProvider, default NoOp. Bật sau MVP, có giới hạn chi phí + fallback local.
```

