package com.cartourguide.ai

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/** Optional AI failure must degrade gracefully to offline stories (never fabricate). */
class AiFallbackTest {
    @Test fun noopProvider_failsCleanly() = runTest {
        val r = NoOpAiProvider.generateStory("p1", "Test")
        assertTrue(r.isFailure)
    }
}
