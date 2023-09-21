/*
 * Copyright 2016-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines

import coroutines.registerCoroutineAsyncEvent
import kotlinx.coroutines.internal.*
import kotlin.coroutines.*

public actual abstract class W3CWindow

internal actual fun w3cSetTimeout(window: W3CWindow, handler: () -> Unit, timeout: Int): Int =
    error("Not intended to call in WASI")

internal actual fun w3cSetTimeout(handler: () -> Unit, timeout: Int): Int =
    error("Not intended to call in WASI")

internal actual fun w3cClearTimeout(handle: Int): Unit =
    error("Not intended to call in WASI")

internal actual fun w3cClearTimeout(window: W3CWindow, handle: Int): Unit =
    error("Not intended to call in WASI")

internal actual class ScheduledMessageQueue actual constructor(dispatcher: SetTimeoutBasedDispatcher) : MessageQueue() {
    actual override fun schedule(): Unit = error("Not intended to call in WASI")
    actual override fun reschedule(): Unit = error("Not intended to call in WASI")
    internal actual fun setTimeout(timeout: Int): Unit = error("Not intended to call in WASI")
}

internal actual class WindowMessageQueue actual constructor(window: W3CWindow) : MessageQueue() {
    actual override fun schedule(): Unit = error("Not intended to call in WASI")
    actual override fun reschedule(): Unit = error("Not intended to call in WASI")
}

internal object WasiDispatcher: CoroutineDispatcher(), Delay {
    override fun limitedParallelism(parallelism: Int): CoroutineDispatcher {
        parallelism.checkParallelism()
        return this
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        registerCoroutineAsyncEvent(0) { block.run() }
    }

    override fun invokeOnTimeout(timeMillis: Long, block: Runnable, context: CoroutineContext): DisposableHandle {
        val event = registerCoroutineAsyncEvent(delayToNanos(timeMillis)) { block.run() }
        return DisposableHandle { event.cancel() }
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val event = registerCoroutineAsyncEvent(delayToNanos(timeMillis)) {
            with(continuation) { resumeUndispatched(Unit) }
        }
        continuation.invokeOnCancellation(handler = { event.cancel() })
    }
}