package repolizer

import com.google.gson.Gson
import repolizer.adapter.ImageAdapter
import repolizer.adapter.NetworkAdapter
import repolizer.adapter.PersistentAdapter
import repolizer.adapter.WrapperAdapter
import repolizer.repository.login.LoginManager
import repolizer.repository.progress.ProgressController
import repolizer.repository.provider.GlobalRepositoryProvider
import repolizer.repository.request.RequestProvider
import repolizer.repository.response.ResponseService

class Repolizer private constructor(builder: Builder) {

    val baseUrl: String = builder.baseUrl
            ?: throw IllegalStateException("Internal error: Base url required.")
    val gson: Gson = builder.gson

    val requestProvider: RequestProvider? = builder.requestProvider
    val progressController: ProgressController? = builder.progressController
    val loginManager: LoginManager? = builder.loginManager
    val responseService: ResponseService? = builder.responseService

    @Suppress("UNCHECKED_CAST")
    fun <T> getRepository(repositoryClass: Class<*>): T {
        return GlobalRepositoryProvider.getRepository(this, repositoryClass) as? T
                ?: throw IllegalStateException("Internal error: Repository is null. Make sure " +
                        "that you used the correct class for the function Repolizer.create(Class<*>).")
    }

    companion object {

        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder {
        var wrapperAdapters: ArrayList<WrapperAdapter<*>> = ArrayList()
            private set

        var imageAdapter: ImageAdapter<*>? = null
            private set
        var networkAdapter: NetworkAdapter? = null
            private set
        var persistentAdapter: PersistentAdapter<*>? = null
            private set

        var requestProvider: RequestProvider? = null
            private set

        var baseUrl: String? = null
            private set
        var gson: Gson = Gson()
            private set

        var progressController: ProgressController? = null
            private set
        var loginManager: LoginManager? = null
            private set
        var responseService: ResponseService? = null
            private set

        fun addWrapperAdapter(wrapperAdapter: WrapperAdapter<*>): Builder {
            wrapperAdapters.add(wrapperAdapter)
            return this@Builder
        }

        fun setImageAdapter(imageAdapter: ImageAdapter<*>): Builder {
            this@Builder.imageAdapter = imageAdapter
            return this@Builder
        }

        fun setNetworkAdapter(networkAdapter: NetworkAdapter): Builder {
            this@Builder.networkAdapter = networkAdapter
            return this@Builder
        }

        fun setPersistentAdapter(persistentAdapter: PersistentAdapter<*>): Builder {
            this@Builder.persistentAdapter = persistentAdapter
            return this@Builder
        }

        fun setBaseUrl(baseUrl: String): Builder {
            this@Builder.baseUrl = baseUrl
            return this@Builder
        }

        fun setGson(gson: Gson): Builder {
            this@Builder.gson = gson
            return this@Builder
        }

        fun setProgress(progressController: ProgressController): Builder {
            this@Builder.progressController = progressController
            return this@Builder
        }

        fun setLoginManager(loginManager: LoginManager): Builder {
            this@Builder.loginManager = loginManager
            return this@Builder
        }

        fun setResponseService(responseService: ResponseService): Builder {
            this@Builder.responseService = responseService
            return this@Builder
        }

        fun setRequestProvider(requestProvider: RequestProvider): Builder {
            this@Builder.requestProvider = requestProvider
            return this@Builder
        }

        fun build(): Repolizer {
            return Repolizer(this@Builder)
        }
    }
}