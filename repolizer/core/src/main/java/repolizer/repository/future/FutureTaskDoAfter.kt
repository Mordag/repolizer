package repolizer.repository.future

import java.util.concurrent.Executor

interface FutureTaskDoAfter {
    fun doAfter(runnable: () -> Unit): FutureTaskDoAfter
    fun withAfterWorkThread(executor: Executor): FutureTaskDoAfter
    fun execute()
}