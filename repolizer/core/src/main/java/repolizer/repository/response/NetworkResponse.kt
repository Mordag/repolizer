package repolizer.repository.response

class NetworkResponse
constructor(val body: Any?,
            val url: String,
            val statusCode: Int,
            val status: NetworkResponseStatus) {

    fun isSuccessful(): Boolean {
        return status == NetworkResponseStatus.SUCCESS
    }

    fun withBody(body: Any): NetworkResponse {
        return NetworkResponse(body, url, statusCode, status)
    }
}