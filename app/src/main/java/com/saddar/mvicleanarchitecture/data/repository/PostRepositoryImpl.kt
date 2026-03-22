package com.saddar.mvicleanarchitecture.data.repository

import com.saddar.mvicleanarchitecture.data.remote.ApiService
import com.saddar.mvicleanarchitecture.domain.model.Post
import com.saddar.mvicleanarchitecture.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val api: ApiService
) : PostRepository {

    override fun getPosts(): Flow<Result<List<Post>>> = flow {
        try {
            val posts = api.getPosts().map { dto ->
                Post(id = dto.id, title = dto.title, body = dto.body)
            }
            emit(Result.success(posts))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}