package com.example.localfinedustinformation.data.services

import com.example.localfinedustinformation.BuildConfig
import com.example.localfinedustinformation.BuildConfig.KAKAO_REST_API_KEY
import com.example.localfinedustinformation.data.models.tmcoordinates.TmCoordinatesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface KakaoLocalApiService {
    @Headers("Authorization: KakaoAK $KAKAO_REST_API_KEY")
    @GET("/v2/local/geo/transcoord.json?output_coord=TM")
    suspend fun getTmCoordinates(
        @Query("x") longitude:Double,
        @Query("y") latitude:Double
    ):Response<TmCoordinatesResponse>
}