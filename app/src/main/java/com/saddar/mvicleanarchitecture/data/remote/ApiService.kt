package com.saddar.mvicleanarchitecture.data.remote

import com.saddar.mvicleanarchitecture.data.remote.dto.PostDto
import retrofit2.http.GET

interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<PostDto>
}