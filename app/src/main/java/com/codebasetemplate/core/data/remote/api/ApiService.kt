package com.codebasetemplate.core.data.remote.api

import com.codebasetemplate.shared.frame.data.remote.dto.FrameItemsDto
import retrofit2.http.GET
import retrofit2.http.Query

private const val VERSION_NAME = "2"
interface ApiService {

    @GET("api/photo/item/adv/")
    suspend fun getFrameItems(
        @Query("application_id") appId: String = "com.kunkunnapps.photocollage",
        @Query("category_id") categoryId: String,
        @Query("limit") limit: Int = 200,
        @Query("version") version: String = VERSION_NAME,
        @Query("offset") offset: Int
    ): FrameItemsDto
}