package com.cartourguide.data

import org.junit.Assert.*
import org.junit.Test

/** Provenance: every seed story must carry a source; legends labelled; coords sane. */
class ProvenanceTest {
    @Test fun everyStoryHasSourceOrLegendLabel() {
        for (s in SeedData.stories) {
            assertTrue("story trống: ${s.id}", s.textVi.isNotBlank())
            if (s.verification == "VERIFIED") {
                assertNotNull("thiếu nguồn: ${s.id}", s.sourceUrl)
                assertTrue(s.sourceUrl!!.startsWith("http"))
            }
            assertTrue(s.verification in setOf("VERIFIED", "LEGEND", "NEEDS_CHECK"))
        }
    }

    @Test fun everyPlaceHasStoryAndValidCoords() {
        val storyPlaceIds = SeedData.stories.map { it.placeId }.toSet()
        for (p in SeedData.places) {
            assertTrue("thiếu story cho ${p.id}", storyPlaceIds.contains(p.id))
            assertTrue(p.lat in -90.0..90.0 && p.lng in -180.0..180.0)
            assertTrue(p.importance in 1..5)
        }
    }

    @Test fun seedCoversSouthernVietnam() {
        val regions = SeedData.places.map { it.regionId }.toSet()
        assertTrue(regions.contains("sg-central"))
        assertTrue(regions.size >= 4)
        assertTrue(SeedData.places.size >= 6)
    }
}
