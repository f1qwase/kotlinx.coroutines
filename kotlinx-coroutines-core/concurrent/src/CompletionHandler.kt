/*
 * Copyright 2016-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines

import kotlinx.coroutines.internal.*

// New 'CompletionHandler` supertype is added compared to the expect declaration.
// Probably we can add it to JS and common too, to avoid the suppression/opt-in
internal actual abstract class CompletionHandlerBase actual constructor() : LockFreeLinkedListNode(), CompletionHandler {
    actual abstract override fun invoke(cause: Throwable?)
}

internal actual inline val CompletionHandlerBase.asHandler: CompletionHandler get() = this

// New 'CompletionHandler` supertype is added compared to the expect declaration.
// Probably we can add it to JS and common too, to avoid the suppression/opt-in
internal actual abstract class CancelHandlerBase actual constructor() : CompletionHandler {
    actual abstract override fun invoke(cause: Throwable?)
}

internal actual inline val CancelHandlerBase.asHandler2: CompletionHandler get() = this

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun CompletionHandler.invokeIt(cause: Throwable?) = invoke(cause)
