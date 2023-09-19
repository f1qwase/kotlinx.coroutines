/*
 * Copyright 2016-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines

import org.junit.Test
import org.openjdk.jol.info.*
import kotlin.test.*


class MemoryFootprintTest : TestBase(true) {

    @Test
    fun testJobLayout() {
        assertLayout(jobWithChildren(0).javaClass, 24)
    }

    @Test
    fun testJobSize() {
        assertTotalSize(jobWithChildren(1), 112)
        assertTotalSize(jobWithChildren(2), 192) // + 80
        assertTotalSize(jobWithChildren(3), 248) // + 56
        assertTotalSize(jobWithChildren(4), 304) // + 56
    }

    private fun jobWithChildren(numberOfChildren: Int): Job {
        val result = Job()
        repeat(numberOfChildren) {
            Job(result)
        }
        return result
    }

    @Test
    fun testCancellableContinuationFootprint() = assertLayout(CancellableContinuationImpl::class.java, 48)

    private fun assertLayout(clz: Class<*>, expectedSize: Int) {
        val size = ClassLayout.parseClass(clz).instanceSize()
//        println(ClassLayout.parseClass(clz).toPrintable())
        assertEquals(expectedSize.toLong(), size)
    }

    private fun assertTotalSize(instance: Job, expectedSize: Int) {
        val size = GraphLayout.parseInstance(instance).totalSize()
        assertEquals(expectedSize.toLong(), size)
    }
}
