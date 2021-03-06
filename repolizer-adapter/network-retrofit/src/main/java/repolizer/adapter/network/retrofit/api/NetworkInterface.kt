package repolizer.adapter.network.retrofit.api

import okhttp3.RequestBody
import repolizer.repository.util.QueryHashMap
import retrofit2.Call
import retrofit2.http.*

interface NetworkInterface {

    @GET("{url}")
    fun get(
            @HeaderMap headerMap: Map<String, String>,
            @Path(value = "url", encoded = true) url: String,
            @QueryMap map: QueryHashMap
    ): Call<String>

    @POST("{url}")
    fun post(
            @HeaderMap headerMap: Map<String, String>,
            @Path(value = "url", encoded = true) url: String,
            @QueryMap map: QueryHashMap,
            @Body raw: RequestBody?
    ): Call<String>

    @PUT("{url}")
    fun put(
            @HeaderMap headerMap: Map<String, String>,
            @Path(value = "url", encoded = true) url: String,
            @QueryMap map: QueryHashMap,
            @Body raw: RequestBody?
    ): Call<String>

    @PATCH("{url}")
    fun patch(
            @HeaderMap headerMap: Map<String, String>,
            @Path(value = "url", encoded = true) url: String,
            @QueryMap map: QueryHashMap,
            @Body raw: RequestBody?
    ): Call<String>

    @DELETE("{url}")
    fun delete(
            @HeaderMap headerMap: Map<String, String>,
            @Path(value = "url", encoded = true) url: String,
            @QueryMap map: QueryHashMap
    ): Call<String>

    @HTTP(method = "DELETE", path = "{url}", hasBody = true)
    fun delete(
            @HeaderMap headerMap: Map<String, String>,
            @Path(value = "url", encoded = true) url: String,
            @QueryMap map: QueryHashMap,
            @Body raw: RequestBody?
    ): Call<String>
}