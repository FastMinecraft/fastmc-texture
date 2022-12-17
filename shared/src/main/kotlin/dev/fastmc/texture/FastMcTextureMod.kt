package dev.fastmc.texture

import dev.fastmc.common.ParallelUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object FastMcTextureMod {
    @JvmStatic
    fun init() {
        Thread {
            runBlocking {
                println("Initializing FastMcTextureMod")
                repeat(ParallelUtils.CPU_THREADS * 2) {
                    launch(Dispatchers.Default) {
                        @Suppress("BlockingMethodInNonBlockingContext")
                        Thread.sleep(5L)
                    }
                }
            }
        }.start()
    }
}