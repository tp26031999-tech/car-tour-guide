package com.cartourguide.audio

import org.junit.Assert.*
import org.junit.Test

/** Missing Vietnamese voice must degrade to a clear message, never a crash or fake accent claim. */
class VoiceCheckTest {
    @Test fun availableCodes_ok() {
        assertTrue(VoiceCheck.vietnameseSupported(0))
        assertTrue(VoiceCheck.vietnameseSupported(1))
        assertNull(VoiceCheck.userMessage(0))
    }

    @Test fun missingData_message() {
        assertFalse(VoiceCheck.vietnameseSupported(-1))
        assertTrue(VoiceCheck.userMessage(-1)!!.contains("Thiếu dữ liệu"))
    }

    @Test fun notSupported_message() {
        assertFalse(VoiceCheck.vietnameseSupported(-2))
        assertNotNull(VoiceCheck.userMessage(-2))
    }
}
