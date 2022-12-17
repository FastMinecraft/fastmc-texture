package dev.fastmc.texture

import dev.fastmc.texture.accessor.AccessorSoundHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.audio.SoundList
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Tuple
import net.minecraftforge.fml.common.ProgressManager

interface IPatchedSoundHandler {
    fun loadSoundsParallel(resources: List<Tuple<ResourceLocation, SoundList>>) {
        val accessor = this as AccessorSoundHandler
        runBlocking {
            val channel = Channel<String>(8)

            launch {
                val resourcesBar = ProgressManager.push("Loading sounds", resources.size)
                for (e in channel) {
                    resourcesBar.step(e)
                }
                ProgressManager.pop(resourcesBar)
            }

            coroutineScope {
                for (entry in resources) {
                    launch(Dispatchers.Default) {
                        channel.send(entry.first.toString())
                        try {
                            accessor.callLoadSoundResource(entry.first, entry.second)
                        } catch (e: RuntimeException) {
                            AccessorSoundHandler.getLogger().warn("Invalid sounds.json", e)
                        }
                    }
                }
            }

            channel.close()
        }
    }
}