package repolizer.repository.future

import repolizer.repository.network.ExecutionType

abstract class Future<Body> {

    abstract fun <Wrapper> create(): Wrapper

    abstract fun execute(): Body?

    protected abstract fun onExecute(executionType: ExecutionType): Body?

    protected abstract fun onDetermineExecutionType(): ExecutionType

    protected open fun onCreate() {

    }

    protected open fun onStart() {

    }

    protected open fun onFinished() {

    }
}