package ru.netology.nmedia.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.BuildConfig

interface PostApiService {

    @GET("posts/latest")
    suspend fun getLatest(
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>


    @GET("posts/{id}")
    suspend fun getById(
        @Path("id") id: Long
    ): Response<Post>


    @POST("posts")
    suspend fun save(
        @Body post: Post
    ): Response<Post>


    @DELETE("posts/{id}")
    suspend fun removeById(
        @Path("id") id: Long
    ): Response<Unit>


    @POST("posts/{id}/likes")
    suspend fun likeById(
        @Path("id") id: Long
    ): Response<Post>


    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(
        @Path("id") id: Long
    ): Response<Post>


    @FormUrlEncoded
    @POST("${BuildConfig.BASE_URL}api/users/authentication")
    suspend fun signIn(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<Token>


    @FormUrlEncoded
    @POST("${BuildConfig.BASE_URL}api/users/registration")
    suspend fun signUp(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String
    ): Response<Token>


    @POST("${BuildConfig.BASE_URL}api/users/push-tokens")
    suspend fun sendPushToken(
        @Body pushToken: PushToken
    ): Response<Unit>
}
