package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.repository.response.NetworkResponse
import repolizer.repository.util.CacheState

@Suppress("UNCHECKED_CAST")
class NetworkGetFuture<Body>
constructor(private val repolizer: Repolizer,
            private val futureRequest: NetworkFutureRequest) : NetworkFuture<Body>(repolizer, futureRequest) {

    private lateinit var cacheState: CacheState

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters,
                futureRequest.typeToken.type, futureRequest.repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return if (wrapperAdapter.canHaveDataConnection() && dataAdapter?.canHaveActiveConnections() == true) {
            wrapperAdapter.establishDataConnection(this, futureRequest, dataAdapter)
                    ?: throw IllegalStateException("If you want to use an active data connection, " +
                            "you need to implement the method establishDataConnection() of your " +
                            "WrapperAdapter and establishConnection() function inside your DataAdapter.")
        } else wrapperAdapter.execute(this, futureRequest)
                ?: throw IllegalStateException("It seems like that your WrapperAdapter does not" +
                        "have the method execute() implemented.")
    }

    override fun onDetermineExecutionType(): ExecutionType {
        this.cacheState = cacheAdapter?.get(futureRequest, futureRequest.fullUrl,
                futureRequest.freshCacheTime, futureRequest.maxCacheTime)
                ?: CacheState.NEEDS_NO_REFRESH

        val cacheData = dataAdapter?.get(futureRequest)
        val needsFetch = cacheState == CacheState.NEEDS_SOFT_REFRESH ||
                cacheState == CacheState.NEEDS_HARD_REFRESH ||
                cacheState == CacheState.NO_CACHE

        return if ((futureRequest.url.isNotEmpty() || futureRequest.ignoreEmptyUrl)
                && (cacheData == null || needsFetch)
                && futureRequest.allowFetch
                && (futureRequest.allowMultipleRequestsAtSameTime || futureRequest.fetchSecurityLayer.allowFetch())) {
            ExecutionType.USE_NETWORK
        } else ExecutionType.USE_STORAGE
    }

    override fun onExecute(executionType: ExecutionType): Body? {
        return when (executionType) {
            ExecutionType.USE_NETWORK -> fetchFromNetwork()
            ExecutionType.USE_STORAGE -> fetchCacheData()
            ExecutionType.DO_NOTHING -> null
        }
    }

    override fun onFinished(result: Body?) {
        super.onFinished(result)
        futureRequest.fetchSecurityLayer.onFetchFinished()
    }

    private fun fetchFromNetwork(): Body? {
        val response: NetworkResponse? = networkAdapter?.execute(futureRequest, requestProvider)

        return if (response?.isSuccessful() == true && response.body != null) {
            val convertedBody = if (response.body is String && futureRequest.bodyType != String::class.java)
                convertResponseData(response.body) else response.body as Body?

            if (convertedBody != null && saveData) saveNetworkResponse(response, convertedBody)
            else convertedBody
        } else {
            handleRequestError(response)
            null
        }
    }

    private fun saveNetworkResponse(response: NetworkResponse, data: Body): Body? {
        val saveSuccessful = dataAdapter?.insert(futureRequest, data) ?: false

        return if (saveSuccessful) {
            val cacheSuccessful = if (cacheAdapter != null) {
                val cacheKey = cacheAdapter.getCacheKeyForNetwork(futureRequest, response)
                cacheAdapter.save(futureRequest, cacheKey)
            } else true

            if (cacheSuccessful) {
                repolizer.defaultMainThread.execute {
                    responseService?.handleSuccess(futureRequest)
                }
                dataAdapter?.get(futureRequest)
            } else {
                repolizer.defaultMainThread.execute {
                    responseService?.handleCacheError(futureRequest)
                }
                null
            }
        } else {
            repolizer.defaultMainThread.execute {
                responseService?.handleDataError(futureRequest)
            }
            null
        }
    }

    private fun handleRequestError(response: NetworkResponse?) {
        repolizer.defaultMainThread.execute {
            responseService?.handleRequestError(futureRequest, response)
        }

        if (futureRequest.isDeletingCacheIfTooOld && cacheState == CacheState.NEEDS_HARD_REFRESH) {
            dataAdapter?.delete(futureRequest)

            if (cacheAdapter != null) {
                val cacheKey = cacheAdapter.getCacheKeyForNetwork(futureRequest, response)
                cacheAdapter.delete(futureRequest, cacheKey)
            }
        }
    }

    private fun fetchCacheData(): Body? {
        return dataAdapter?.get(futureRequest)
    }

    private fun convertResponseData(bodyData: String): Body? {
        val data: Body? = converterAdapter?.convertStringToData(
                futureRequest.repositoryClass, bodyData, futureRequest.bodyType)
        if (data == null) responseService?.handleDataError(futureRequest)
        return data
    }
}
