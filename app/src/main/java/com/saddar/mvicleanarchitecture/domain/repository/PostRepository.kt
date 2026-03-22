package com.saddar.mvicleanarchitecture.domain.repository

import com.saddar.mvicleanarchitecture.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPosts(): Flow<Result<List<Post>>>
}