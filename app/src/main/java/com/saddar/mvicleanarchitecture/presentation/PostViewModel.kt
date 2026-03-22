package com.saddar.mvicleanarchitecture.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saddar.mvicleanarchitecture.domain.usecase.GetPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase
) : ViewModel() {

    // ── StateFlow: UI observes this for rendering ──────────────────────
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // ── Channel/SharedFlow: one-shot effects (toasts, navigation) ─────
    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<UiEffect> = _uiEffect.receiveAsFlow()

    // Entry point — UI sends intents here
    fun handleIntent(intent: UiIntent) {
        when (intent) {
            is UiIntent.LoadPosts -> loadPosts()
            is UiIntent.Refresh   -> loadPosts()
            is UiIntent.SelectPost -> {
                viewModelScope.launch {
                    _uiEffect.send(UiEffect.ShowToast("Tapped: ${intent.post.title}"))
                }
            }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch {
            getPostsUseCase()
                .onStart {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { posts ->
                            _uiState.update {
                                it.copy(isLoading = false, posts = posts)
                            }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(isLoading = false, error = error.message)
                            }
                            _uiEffect.send(UiEffect.ShowToast("Failed: ${error.message}"))
                        }
                    )
                }
        }
    }

    init { handleIntent(UiIntent.LoadPosts) } // auto-load on creation
}