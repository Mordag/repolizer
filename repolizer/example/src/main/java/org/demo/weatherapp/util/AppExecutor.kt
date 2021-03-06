package org.demo.weatherapp.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppExecutor @Inject constructor() {

    val workerThread: Executor = Executors.newSingleThreadExecutor()
    val workerThreadPool: Executor = Executors.newCachedThreadPool()

    val mainThread: Executor = MainThreadExecutor()

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}