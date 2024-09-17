package com.example.rgb2grayscaleconverterapp

// RetrofitClient.kt
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import okhttp3.OkHttpClient

object RetrofitClient {
    private const val BASE_URL = "https://3ow99rdgna.execute-api.ap-south-1.amazonaws.com/Dev/" // Replace with your API URL

    private val client = OkHttpClient.Builder().build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }
}
