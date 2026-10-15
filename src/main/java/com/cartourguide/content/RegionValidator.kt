package com.cartourguide.content

/**
 * Pure bundle validation (no Android deps) so corrupt-content handling is unit-testable.
 * Returns null when OK, else a Vietnamese error message for the UI.
 */
object RegionValidator {
    fun validate(b: RegionBundle): String? {
        if (b.places.isEmpty()) return "Dữ liệu rỗng (file hỏng?)."
        for (p in b.places) {
            if (p.lat !in -90.0..90.0 || p.lng !in -180.0..180.0) return "Tọa độ sai: ${p.id}."
            if (b.stories.none { it.placeId == p.id }) return "Thiếu story cho ${p.id}."
        }
        for (s in b.stories) {
            if (s.textVi.isBlank()) return "Story trống: ${s.id}."
            if (s.sourceUrl.isNullOrBlank() && s.verification == "VERIFIED") return "Thiếu nguồn: ${s.id}."
        }
        return null
    }

    /** Real on-disk footprint estimate (KB) for the storage setting. */
    fun storageKb(b: RegionBundle): Int {
        var bytes = 0
        for (s in b.stories) bytes += s.textVi.toByteArray().size + (s.title.toByteArray().size)
        for (p in b.places) bytes += p.name.toByteArray().size + 64
        return (bytes / 1024).coerceAtLeast(1)
    }
}
