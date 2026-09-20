package com.reon.music.data.remote

import com.reon.music.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReonBackendApi {

    @GET("api/v1/search")
    suspend fun search(
        @Query("q") q: String,
        @Query("filter") filter: String? = null,
    ): SearchResponse

    @GET("api/v1/search/suggestions")
    suspend fun suggestions(
        @Query("q") q: String,
    ): SuggestionsResponse

    @GET("api/v1/home")
    suspend fun home(): HomeResponse

    @GET("api/v1/albums/{id}")
    suspend fun album(@Path("id") id: String): AlbumDetails

    @GET("api/v1/artists/{id}")
    suspend fun artist(@Path("id") id: String): ArtistDetails

    @GET("api/v1/playlists/{id}")
    suspend fun playlist(@Path("id") id: String): PlaylistDetails

    @GET("api/v1/radio/{trackId}")
    suspend fun radio(@Path("trackId") trackId: String): RadioResponse

    @GET("api/v1/stream/{trackId}")
    suspend fun stream(
        @Path("trackId") trackId: String,
        @Query("quality") quality: String = "high",
    ): StreamResponse

    @GET("api/v1/player/{trackId}")
    suspend fun player(@Path("trackId") trackId: String): PlayerResponse

    @GET("api/v1/lyrics/{trackId}")
    suspend fun lyrics(@Path("trackId") trackId: String): LyricsResponse

    companion object {
        fun create(
            baseUrl: String = resolveBaseUrl(),
            apiKey: String? = resolveApiKey(),
            moshi: Moshi = defaultMoshi(),
            client: OkHttpClient = defaultClient(apiKey),
        ): ReonBackendApi {
            val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            return Retrofit.Builder()
                .baseUrl(normalized)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(ReonBackendApi::class.java)
        }

        fun defaultMoshi(): Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        fun defaultClient(apiKey: String? = resolveApiKey()): OkHttpClient {
            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            }
            val apiKeyInterceptor = Interceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                if (!apiKey.isNullOrBlank()) {
                    builder.header("X-Reon-Key", apiKey)
                }
                builder.header("Accept", "application/json")
                chain.proceed(builder.build())
            }
            return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(apiKeyInterceptor)
                .addInterceptor(logging)
                .retryOnConnectionFailure(true)
                .build()
        }

        fun resolveBaseUrl(): String {
            return try {
                val field = BuildConfig::class.java.getField("REON_API_BASE_URL")
                val value = field.get(null) as? String
                value?.takeIf { it.isNotBlank() } ?: "http://10.0.2.2:8080"
            } catch (_: Exception) {
                "http://10.0.2.2:8080"
            }
        }

        fun resolveApiKey(): String? {
            return try {
                val field = BuildConfig::class.java.getField("REON_API_KEY")
                val value = field.get(null) as? String
                value?.takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            }
        }
    }
}
