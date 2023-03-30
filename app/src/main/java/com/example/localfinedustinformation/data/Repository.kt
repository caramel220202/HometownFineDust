package com.example.localfinedustinformation.data

import android.util.Log
import com.example.localfinedustinformation.BuildConfig
import com.example.localfinedustinformation.data.models.airquality.MeasuredValues
import com.example.localfinedustinformation.data.models.monitoringstation.MonitoringStation
import com.example.localfinedustinformation.data.services.AirKoreaApiService
import com.example.localfinedustinformation.data.services.KakaoLocalApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object Repository {

    suspend fun getNearbyMonitoringStation(longitude: Double,latitude:Double):MonitoringStation?{
        val tmCoordinates = kakaoLocalApiService
            .getTmCoordinates(longitude,latitude)
            .body()
            ?.documents
            ?.firstOrNull()

        val tmX = tmCoordinates?.x
        val tmY = tmCoordinates?.y

        return airKoreaApiService.getNearbyMonitoringStation(tmX!!,tmY!!)
            .body()
            ?.response
            ?.body
            ?.monitoringStation
            ?.firstOrNull()
    }

    suspend fun getLatestAirQualityData(stationName:String):MeasuredValues?=
        airKoreaApiService.getRealtimeAirQualities(stationName)
            .body()
            ?.response
            ?.body
            ?.measuredValues
            ?.firstOrNull()

    private val kakaoLocalApiService:KakaoLocalApiService by lazy {

        Retrofit.Builder()
            .baseUrl(Url.KAKAO_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient())
            .build()
            .create()

    }

    private val airKoreaApiService:AirKoreaApiService by lazy {

        Retrofit.Builder()
            .baseUrl(Url.AIRKOREA_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient())
            .build()
            .create()

    }
    private fun buildHttpClient():OkHttpClient =
        OkHttpClient().newBuilder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG){
                        HttpLoggingInterceptor.Level.BODY
                    }else{
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            ).build()
}