package dev.fastmc.texture

import dev.fastmc.texture.accessor.AccessorTextureMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.renderer.texture.Stitcher
import net.minecraft.client.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.ProgressManager

interface IPatchedTextureMap {
    fun loadTextureParallel(
        resourceManager: IResourceManager,
        stitcher: Stitcher,
        bar: ProgressManager.ProgressBar,
        mip: Int
    ): Int {
        val accessor = this as AccessorTextureMap

        return runBlocking {
            val channel = Channel<String>(8)

            launch {
                for (e in channel) {
                    bar.step(e)
                }
            }

            val map = Object2ObjectOpenHashMap(accessor.mapRegisteredSprites)
            val iterator = map.object2ObjectEntrySet().fastIterator()
            var count = Int.MAX_VALUE

            coroutineScope {
                while (iterator.hasNext()) {
                    val (key, value) = iterator.next()
                    launch(Dispatchers.Default) {
                        val location = ResourceLocation(key)
                        count = accessor.callLoadTexture(stitcher, resourceManager, location, value, bar, count, mip)
                        channel.send(location.toString())
                    }
                }
            }

            channel.close()

            count
        }
    }
}