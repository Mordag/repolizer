package repolizer.adapter.future

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.factory.AdapterFactory
import repolizer.repository.future.Future
import java.lang.reflect.Type

class FutureWrapperAdapterFactory : AdapterFactory<WrapperAdapter<*>> {

    override fun get(type: Type, repositoryClass: Class<*>, repolizer: Repolizer): WrapperAdapter<*>? {
        return if (type.javaClass != Future::class.java) null
        else FutureWrapperAdapter()
    }
}