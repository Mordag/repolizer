package repolizer

import repolizer.adapter.*
import repolizer.adapter.factory.AdapterFactory
import repolizer.adapter.future.FutureWrapperAdapterFactory
import repolizer.repository.login.LoginManager
import repolizer.repository.provider.GlobalRepositoryProvider
import repolizer.repository.request.RequestProvider
import repolizer.repository.response.ResponseService
import repolizer.repository.util.RepositoryExecutor
import java.util.concurrent.Executor

class Repolizer private constructor(builder: Builder) {

    val baseUrl: String? = builder.baseUrl

    val requestProvider: RequestProvider<*>? = builder.requestProvider
    val loginManager: LoginManager? = builder.loginManager
    val responseService: ResponseService? = builder.responseService

    val defaultMainThread: Executor = builder.defaultMainThread
            ?: RepositoryExecutor.applicationThread
    val workerThread: Executor = builder.workerThread
            ?: RepositoryExecutor.getRepositoryDefaultThread()

    val wrapperAdapters: ArrayList<AdapterFactory<out WrapperAdapter<*>>> = builder.wrapperAdapters
    val networkAdapters: ArrayList<AdapterFactory<out NetworkAdapter>> = builder.networkAdapters
    val dataAdapters: ArrayList<AdapterFactory<out DataAdapter<*>>> = builder.dataAdapters
    val cacheAdapters: ArrayList<AdapterFactory<out CacheAdapter>> = builder.cacheAdapters
    val converterAdapters: ArrayList<AdapterFactory<out ConverterAdapter>> = builder.converterAdapters

    @Suppress("UNCHECKED_CAST")
    fun <T> getRepository(repositoryClass: Class<T>): T {
        return GlobalRepositoryProvider.getRepository(this, repositoryClass) as? T
                ?: throw IllegalStateException("Internal error: Repository is null. Make sure " +
                        "that you used the correct class for the function Repolizer.getRepository(...).")
    }

    companion object {

        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder {
        val wrapperAdapters: ArrayList<AdapterFactory<out WrapperAdapter<*>>> = ArrayList()
        val networkAdapters: ArrayList<AdapterFactory<out NetworkAdapter>> = ArrayList()
        val dataAdapters: ArrayList<AdapterFactory<out DataAdapter<*>>> = ArrayList()
        val cacheAdapters: ArrayList<AdapterFactory<out CacheAdapter>> = ArrayList()
        val converterAdapters: ArrayList<AdapterFactory<out ConverterAdapter>> = ArrayList()

        var requestProvider: RequestProvider<*>? = null
            private set

        var baseUrl: String? = null
            private set

        var loginManager: LoginManager? = null
            private set
        var responseService: ResponseService? = null
            private set

        var defaultMainThread: Executor? = null
            private set
        var workerThread: Executor? = null
            private set

        fun addWrapperAdapterFactory(factory: AdapterFactory<out WrapperAdapter<*>>): Builder {
            wrapperAdapters.add(factory)
            return this@Builder
        }

        fun addNetworkAdapterFactory(factory: AdapterFactory<out NetworkAdapter>): Builder {
            networkAdapters.add(factory)
            return this@Builder
        }

        fun addCacheAdapterFactory(factory: AdapterFactory<out CacheAdapter>): Builder {
            cacheAdapters.add(factory)
            return this@Builder
        }

        fun addDataAdapterFactory(factory: AdapterFactory<out DataAdapter<*>>): Builder {
            dataAdapters.add(factory)
            return this@Builder
        }

        fun addConverterAdapterFactory(factory: AdapterFactory<out ConverterAdapter>): Builder {
            converterAdapters.add(factory)
            return this@Builder
        }

        fun setBaseUrl(baseUrl: String): Builder {
            this@Builder.baseUrl = baseUrl
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

        fun setRequestProvider(requestProvider: RequestProvider<*>): Builder {
            this@Builder.requestProvider = requestProvider
            return this@Builder
        }

        fun setDefaultMainThread(defaultMainThread: Executor): Builder {
            this@Builder.defaultMainThread = defaultMainThread
            return this@Builder
        }

        fun setWorkerThread(executor: Executor): Builder {
            this@Builder.workerThread = executor
            return this@Builder
        }

        fun setWorkerThread(threadName: String): Builder {
            RepositoryExecutor.addRepositoryThread(threadName)
            this@Builder.workerThread = RepositoryExecutor.getRepositoryThread(threadName)
            return this@Builder
        }

        fun build(): Repolizer {
            wrapperAdapters.add(FutureWrapperAdapterFactory())
            return Repolizer(this@Builder)
        }
    }
}