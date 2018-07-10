package repolizer.repository.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object AppExecutor {
    val workerThread: Executor = Executors.newCachedThreadPool()
    val mainThread: Executor = MainThreadExecutor()

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}