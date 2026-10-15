package com.cartourguide.ai

import com.cartourguide.model.Story

/**
 * Optional online AI extension. NOT used by the MVP core path.
 * Implementations must be opt-in, key-guarded, and fall back to local stories.
 */
interface AiStoryProvider {
    val id: String
    suspend fun generateStory(placeId: String, placeName: String): Result<Story>
    fun requiresApiKey(): Boolean
}

object NoOpAiProvider : AiStoryProvider {
    override val id = "none"
    override suspend fun generateStory(placeId: String, placeName: String) =
        Result.failure(UnsupportedOperationException("AI disabled: using offline stories only."))
    override fun requiresApiKey() = false
}
