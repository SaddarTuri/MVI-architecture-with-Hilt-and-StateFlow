package com.saddar.mvicleanarchitecture.domain.usecase

import com.saddar.mvicleanarchitecture.domain.model.Post
import com.saddar.mvicleanarchitecture.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(): Flow<Result<List<Post>>> = repository.getPosts()
}