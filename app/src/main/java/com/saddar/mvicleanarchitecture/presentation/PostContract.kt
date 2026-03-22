package com.saddar.mvicleanarchitecture.presentation

import com.saddar.mvicleanarchitecture.domain.model.Post

// PostContract.kt

// 1. UiState — everything the UI needs to render
data class UiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null
)

// 2. UiIntent — all actions the user can trigger
sealed class UiIntent {
    object LoadPosts : UiIntent()
    object Refresh : UiIntent()
    data class SelectPost(val post: Post) : UiIntent()
}

// 3. UiEffect — one-time events (navigation, toast, etc.)
sealed class UiEffect {
    data class ShowToast(val message: String) : UiEffect()
    data class NavigateToDetail(val postId: Int) : UiEffect()
}