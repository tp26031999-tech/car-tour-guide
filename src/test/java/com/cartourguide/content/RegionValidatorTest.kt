package com.cartourguide.content

import org.junit.Assert.*
import org.junit.Test

class RegionValidatorTest {
    private fun bundle() = RegionBundle(
        RegionMeta("r1", "Vùng 1", 1, 10),
        listOf(PlaceJson("p1", "Điểm 1", 10.77, 106.69, "di-tich", 5, "s1")),
        listOf(StoryJson("s1", "p1", "Chuyện 1", "Nội dung hay.", 60,
            "https://vi.wikipedia.org/wiki/X", "Wikipedia", "VERIFIED")),
    )

    @Test fun valid_passes() { assertNull(RegionValidator.validate(bundle())) }

    @Test fun emptyPlaces_rejected() {
        assertNotNull(RegionValidator.validate(bundle().copy(places = emptyList())))
    }

    @Test fun badCoords_rejected() {
        val bad = bundle().copy(places = listOf(PlaceJson("p1", "X", 999.0, 106.69, "c", 3, "s1")))
        assertTrue(RegionValidator.validate(bad)!!.contains("Tọa độ"))
    }

    @Test fun missingStory_rejected() {
        assertTrue(RegionValidator.validate(bundle().copy(stories = emptyList()))!!.contains("Thiếu story"))
    }

    @Test fun blankStory_rejected() {
        val bad = bundle().copy(stories = listOf(bundle().stories[0].copy(textVi = "  ")))
        assertTrue(RegionValidator.validate(bad)!!.contains("trống"))
    }

    @Test fun verifiedWithoutSource_rejected() {
        val bad = bundle().copy(stories = listOf(bundle().stories[0].copy(sourceUrl = null)))
        assertTrue(RegionValidator.validate(bad)!!.contains("nguồn"))
    }

    @Test fun storageEstimate_positive() {
        assertTrue(RegionValidator.storageKb(bundle()) >= 1)
    }
}
