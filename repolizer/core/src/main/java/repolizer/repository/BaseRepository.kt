package repolizer.repository

import repolizer.Repolizer
import repolizer.repository.network.FetchSecurityLayer
import repolizer.repository.network.NetworkFutureBuilder
import repolizer.repository.persistent.PersistentFutureBuilder
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseRepository constructor(private val repolizer: Repolizer) : FetchSecurityLayer {

    private val fetchingData = AtomicBoolean(false)

    protected fun <T> executeRefresh(futureBuilder: NetworkFutureBuilder): T {
        return futureBuilder.buildRefresh(repolizer).create()
    }

    protected fun <T> executeGet(futureBuilder: NetworkFutureBuilder, returnType: Type): T {
        val bodyType = if(returnType is ParameterizedType) {
            returnType.actualTypeArguments[0]
        } else {
            returnType
        }

        return futureBuilder.buildGet(repolizer, bodyType).create()
    }

    protected fun <T> executeCud(futureBuilder: NetworkFutureBuilder): T {
        return futureBuilder.buildCud(repolizer).create()
    }

    protected fun <T> executeStorage(builder: PersistentFutureBuilder): T {
        return builder.buildCache(repolizer).create()
    }

    override fun allowFetch(): Boolean {
        return fetchingData.compareAndSet(false, true)
    }

    override fun onFetchFinished() {
        fetchingData.set(false)
    }
}