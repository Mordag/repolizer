package repolizer.repository.retrofit

import android.arch.lifecycle.LiveData
import android.util.Log
import repolizer.repository.response.NetworkResponse
import repolizer.repository.response.NetworkResponseStatus
import repolizer.repository.response.RequestProvider
import repolizer.repository.util.AppExecutor
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean

class LiveDataCallAdapter internal constructor(
        private val responseType: Type,
        private val requestProvider: RequestProvider?,
        private val appExecutor: AppExecutor) : CallAdapter<String, LiveData<NetworkResponse<String>>> {

    override fun responseType(): Type {
        return responseType
    }

    override fun adapt(call: Call<String>): LiveData<NetworkResponse<String>> {
        return object : LiveData<NetworkResponse<String>>() {
            internal var started = AtomicBoolean(false)

            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    appExecutor.workerThread.execute {
                        val url = call.request().url().toString()

                        call.enqueue(object : Callback<String> {
                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                postValue(NetworkResponse(
                                        if (response.isSuccessful) response.body() else getErrorBody(response),
                                        url,
                                        response.code(),
                                        if (response.isSuccessful) NetworkResponseStatus.SUCCESS else NetworkResponseStatus.FAILED))
                                requestProvider?.removeRequest(call)
                            }

                            override fun onFailure(call: Call<String>, throwable: Throwable) {
                                postValue(NetworkResponse(null, url, 0, NetworkResponseStatus.NETWORK_ERROR))
                                requestProvider?.removeRequest(call)
                            }
                        })

                        requestProvider?.addRequest(call)
                    }
                }
            }
        }
    }

    private fun getErrorBody(response: Response<String>?): String {
        try {
            val errorBody = response?.errorBody()
            if (errorBody != null) {
                return errorBody.string()
            }
        } catch (e: IOException) {
            Log.e(LiveDataCallAdapter::class.java.name, e.message)
        }

        return ""
    }
}