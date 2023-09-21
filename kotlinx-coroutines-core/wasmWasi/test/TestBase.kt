/*
 * Copyright 2016-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines

public actual val isStressTest: Boolean = false
public actual val stressTestMultiplier: Int = 1
public actual val stressTestMultiplierSqrt: Int = 1

public actual val isNative = false

@Suppress("ACTUAL_WITHOUT_EXPECT")
public actual typealias TestResult = Unit

public actual open class TestBase actual constructor() {
    public actual val isBoundByJsTestTimeout = false
    private var actionIndex = 0
    private var finished = false
    private var error: Throwable? = null

    /**
     * Throws [IllegalStateException] like `error` in stdlib, but also ensures that the test will not
     * complete successfully even if this exception is consumed somewhere in the test.
     */
    public actual fun error(message: Any, cause: Throwable?): Nothing {
        val exception = IllegalStateException(message.toString(), cause)
        if (error == null) error = exception
        throw exception
    }

    private fun printError(message: String, cause: Throwable) {
        if (error == null) error = cause
        println("$message: $cause")
    }

    /**
     * Asserts that this invocation is `index`-th in the execution sequence (counting from one).
     */
    public actual fun expect(index: Int) {
        val wasIndex = ++actionIndex
        check(index == wasIndex) { "Expecting action index $index but it is actually $wasIndex" }
    }

    /**
     * Asserts that this line is never executed.
     */
    public actual fun expectUnreached() {
        kotlin.error("Should not be reached")
    }

    /**
     * Asserts that this it the last action in the test. It must be invoked by any test that used [expect].
     */
    public actual fun finish(index: Int) {
        expect(index)
        check(!finished) { "Should call 'finish(...)' at most once" }
        finished = true
    }

    /**
     * Asserts that [finish] was invoked
     */
    public actual fun ensureFinished() {
        require(finished) { "finish(...) should be caller prior to this check" }
    }

    public actual fun reset() {
        check(actionIndex == 0 || finished) { "Expecting that 'finish(...)' was invoked, but it was not" }
        actionIndex = 0
        finished = false
    }

    @Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
    public actual fun runTest(
        expected: ((Throwable) -> Boolean)? = null,
        unhandled: List<(Throwable) -> Boolean> = emptyList(),
        block: suspend CoroutineScope.() -> Unit
    ): TestResult {
        var exCount = 0
        var ex: Throwable? = null
        try {
            runBlocking(block = block, context = CoroutineExceptionHandler { _, e ->
                if (e is CancellationException) return@CoroutineExceptionHandler // are ignored
                exCount++
                when {
                    exCount > unhandled.size ->
                        printError("Too many unhandled exceptions $exCount, expected ${unhandled.size}, got: $e", e)
                    !unhandled[exCount - 1](e) ->
                        printError("Unhandled exception was unexpected: $e", e)
                }
            })
        } catch (e: Throwable) {
            ex = e
            if (expected != null) {
                if (!expected(e))
                    error("Unexpected exception: $e", e)
            } else
                throw e
        } finally {
            if (ex == null && expected != null) kotlin.error("Exception was expected but none produced")
        }
        if (exCount < unhandled.size)
            kotlin.error("Too few unhandled exceptions $exCount, expected ${unhandled.size}")
    }
}

public actual val isJavaAndWindows: Boolean get() = false
