package com.cartourguide.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cartourguide.TourApp
import com.cartourguide.content.DownloadState
import com.cartourguide.journey.JourneyService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val permReq = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as TourApp
        setContent {
            MaterialTheme {
                var tab by remember { mutableStateOf(0) }
                val prefs by app.prefs.flow.collectAsStateWithLifecycle(initialValue = null)
                val status by app.journeyManager.status.collectAsStateWithLifecycle()
                val dl by app.regionManager.state.collectAsStateWithLifecycle()
                val voice by app.ttsSpeaker.state.collectAsStateWithLifecycle()
                val scope = rememberCoroutineScope()
                val tabs = listOf("Chuyến đi", "Vùng", "Đã kể", "Cài đặt", "Riêng tư")

                Scaffold(bottomBar = {
                    NavigationBar {
                        tabs.forEachIndexed { i, t ->
                            NavigationBarItem(selected = tab == i, onClick = { tab = i }, label = { Text(t) }, icon = {})
                        }
                    }
                }) { pad ->
                    Column(Modifier.padding(pad).padding(16.dp)) {
                        Text("Car Tour Guide VN — bạn đồng hành miền Nam", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        when (tab) {
                            0 -> JourneyTab(
                                running = status.running,
                                nearby = status.nearbyCount,
                                lastStory = status.lastStoryTitle,
                                onStart = { ensurePerms(); startService(Intent(this@MainActivity, JourneyService::class.java)) },
                                onStop = { stopService(Intent(this@MainActivity, JourneyService::class.java)); app.journeyManager.stop() },
                                onPause = { app.playbackManager.pause() },
                                onResume = { app.playbackManager.resume() },
                                onSkip = { app.playbackManager.skip() },
                                onPreviewVoice = { scope.launch { app.ttsSpeaker.init(); app.ttsSpeaker.preview(rate = prefs?.speechRate ?: 1.0f) } },
                            )
                            1 -> RegionsTab(
                                dl = dl,
                                onDownload = { id -> scope.launch { app.regionManager.download(id); app.prefs.addRegion(id) } },
                                onDelete = { id -> scope.launch { app.regionManager.delete(id); app.prefs.removeRegion(id) } },
                            )
                            2 -> RecentTab(titles = status.recentTitles)
                            3 -> SettingsTab(
                                radius = prefs?.radiusMeters ?: 1000,
                                rate = prefs?.speechRate ?: 1.0f,
                                minInterval = prefs?.minIntervalMin ?: 3,
                                maxPerHour = prefs?.maxPerHour ?: 8,
                                engines = app.ttsSpeaker.listEngines(),
                                viOk = voice.vietnameseSupported,
                                viError = voice.error,
                                onRadius = { scope.launch { app.prefs.setRadius(it) } },
                                onRate = { scope.launch { app.prefs.setSpeechRate(it) } },
                                onInterval = { scope.launch { app.prefs.setMinIntervalMin(it) } },
                                onMax = { scope.launch { app.prefs.setMaxPerHour(it) } },
                            )
                            4 -> PrivacyTab()
                        }
                    }
                }
            }
        }
    }

    private fun ensurePerms() {
        val need = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
            .toTypedArray()
        if (need.isNotEmpty()) permReq.launch(need)
    }
}

@Composable
fun JourneyTab(running: Boolean, nearby: Int, lastStory: String?, onStart: () -> Unit,
    onStop: () -> Unit, onPause: () -> Unit, onResume: () -> Unit, onSkip: () -> Unit, onPreviewVoice: () -> Unit) {
    Text(if (running) "Đang dẫn đường… gần bạn: $nearby điểm" else "Đang đậu. Hãy tải vùng + thử giọng trước khi đi.")
    Text("Chuyện vừa kể: ${lastStory ?: "—"}")
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!running) Button(onClick = onStart) { Text("Bắt đầu") }
        else Button(onClick = onStop) { Text("Kết thúc") }
        OutlinedButton(onClick = onPreviewVoice) { Text("Thử giọng") }
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onPause) { Text("Tạm dừng") }
        OutlinedButton(onClick = onResume) { Text("Tiếp tục") }
        OutlinedButton(onClick = onSkip) { Text("Bỏ qua") }
    }
    Spacer(Modifier.height(8.dp))
    Text("Lệnh thoại (trên Auto qua nút media): “Tạm dừng / Tiếp tục / Kể thêm / Bỏ qua / Điểm tiếp theo”.",
        style = MaterialTheme.typography.bodySmall)
}

@Composable
fun RegionsTab(dl: DownloadState, onDownload: (String) -> Unit, onDelete: (String) -> Unit) {
    val regions = listOf("sg-central" to "Sài Gòn trung tâm", "sg-cu-chi" to "Củ Chi",
        "sg-cangio" to "Cần Giờ", "mekong-mytho" to "Mỹ Tho – Mỹ Thuận", "mekong-cantho" to "Cần Thơ")
    Text("Tải vùng để nghe offline (dữ liệu kèm sẵn trong app, không tốn phí).")
    when (dl) {
        is DownloadState.Progress -> LinearProgressIndicator(progress = dl.pct / 100f)
        is DownloadState.Error -> Text("Lỗi vùng ${dl.regionId}: ${dl.msg}", color = MaterialTheme.colorScheme.error)
        is DownloadState.Done -> Text("Đã tải xong vùng ${dl.regionId}.")
        else -> {}
    }
    regions.forEach { (id, name) ->
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = { onDownload(id) }) { Text("Tải") }
                OutlinedButton(onClick = { onDelete(id) }) { Text("Xóa") }
            }
        }
    }
}

@Composable
fun RecentTab(titles: List<String>) {
    Text("Chuyện đã kể chuyến này (${titles.size}):")
    if (titles.isEmpty()) Text("Chưa có. Bắt đầu chuyến đi để nghe.", style = MaterialTheme.typography.bodySmall)
    titles.reversed().forEach { Text("• $it") }
}

@Composable
fun SettingsTab(radius: Int, rate: Float, minInterval: Int, maxPerHour: Int,
    engines: List<String>, viOk: Boolean, viError: String?,
    onRadius: (Int) -> Unit, onRate: (Float) -> Unit, onInterval: (Int) -> Unit, onMax: (Int) -> Unit) {
    Text("Bán kính phát hiện: $radius m (mặc định 1000)")
    Slider(value = radius.toFloat(), onValueChange = { onRadius(it.toInt()) }, valueRange = 200f..5000f)
    Text("Nghỉ tối thiểu giữa 2 chuyện: $minInterval phút")
    Slider(value = minInterval.toFloat(), onValueChange = { onInterval(it.toInt()) }, valueRange = 1f..30f)
    Text("Tối đa: $maxPerHour chuyện/giờ")
    Slider(value = maxPerHour.toFloat(), onValueChange = { onMax(it.toInt()) }, valueRange = 1f..20f)
    Text("Tốc độ giọng: ${"%.2f".format(rate)}x")
    Slider(value = rate, onValueChange = onRate, valueRange = 0.5f..2.0f)
    Spacer(Modifier.height(8.dp))
    Text("Tiếng Việt: ${if (viOk) "OK" else viError ?: "chưa kiểm tra — bấm Thử giọng lúc đang đậu"}",
        style = MaterialTheme.typography.bodySmall)
    Text("Engine TTS trên máy: ${engines.ifEmpty { listOf("chưa rõ") }.joinToString()}",
        style = MaterialTheme.typography.bodySmall)
    Text("App không tự nhận giọng là miền Nam nếu chưa kiểm chứng.",
        style = MaterialTheme.typography.bodySmall)
}

@Composable
fun PrivacyTab() {
    Text("Quyền & riêng tư", style = MaterialTheme.typography.titleSmall)
    Text("• Vị trí chỉ dùng khi chuyến đi đang chạy, tính toán ngay trên máy, không tải tọa độ lên mạng. " +
        "• Không tài khoản, không đăng nhập. Lịch sử đã kể lưu trong máy, xóa app là mất. " +
        "• Muốn dừng: bấm Kết thúc — service tắt, GPS ngừng.",
        style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(8.dp))
    Text("Xử lý sự cố", style = MaterialTheme.typography.titleSmall)
    Text("• Không nghe tiếng: bấm Thử giọng lúc đậu; cài Google TTS + gói tiếng Việt; kiểm tra Bluetooth xe. " +
        "• GPS yếu trong hầm/nhà cao tầng: app tự bỏ qua tọa độ kém chính xác. " +
        "• Hao pin: vị trí chạy nền tốn pin hơn; cắm sạc khi đi xa. " +
        "• Màn xe không hiện app: mở app dạng media trên Android Auto, Google Maps vẫn dẫn đường riêng.",
        style = MaterialTheme.typography.bodySmall)
}
