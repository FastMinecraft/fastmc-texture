package dev.fastmc.texture

import dev.fastmc.texture.accessor.AccessorModelBakery
import dev.fastmc.texture.accessor.AccessorModelLoaderRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.block.Block
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.block.statemap.BlockStateMapper
import net.minecraft.item.Item
import net.minecraftforge.client.model.IModel
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.client.model.ModelLoaderRegistry
import net.minecraftforge.fml.common.ProgressManager


interface IPatchedModelLoader {
    val stateModels: MutableMap<ModelResourceLocation, IModel>

    fun loadItemsModelParallel() {
        val accessor = this as AccessorModelBakery
        val items = Item.REGISTRY.iterator().asSequence()
            .filter { it.registryName != null }
            .toList()

        val itemBar = ProgressManager.push("ModelLoader: items", items.size)

        runBlocking {
            val channel = Channel<String>(8)

            launch {
                for (e in channel) {
                    itemBar.step(e)
                }
            }

            coroutineScope {
                for (i in items.indices) {
                    launch(Dispatchers.Default) {
                        val item = items[i]
                        for (s in accessor.callGetVariantNames(item)) {
                            val file = accessor.callGetItemLocation(s)
                            val memory = ModelLoader.getInventoryVariant(s)
                            var model: IModel
                            try {
                                model = ModelLoaderRegistry.getModel(memory)
                            } catch (e: Exception) {
                                try {
                                    model = ModelLoaderRegistry.getModel(file)
                                    AccessorModelLoaderRegistry.callAddAlias(memory, file)
                                } catch (normalException: Exception) {
                                    model = AccessorModelLoaderRegistry.callGetMissingModel(memory, null)
                                }
                            }
                            stateModels[memory] = model
                        }
                        channel.send(item.registryName.toString())
                    }
                }
            }

            channel.close()
        }
        ProgressManager.pop(itemBar)
    }

    fun loadBlocksParallel(mapper: BlockStateMapper) {
        val accessor = this as AccessorModelBakery
        val blocks = Block.REGISTRY.iterator().asSequence()
            .filter { it.registryName != null }
            .toList()


        runBlocking {
            val channel = Channel<String>(8)

            launch {
                val blockBar = ProgressManager.push("ModelLoader: blocks", blocks.size)
                for (e in channel) {
                    blockBar.step(e)
                }
                ProgressManager.pop(blockBar)
            }

            coroutineScope {
                for (i in blocks.indices) {
                    launch(Dispatchers.Default) {
                        val block = blocks[i]
                        for (location in mapper.getBlockstateLocations(block)) {
                            accessor.callLoadBlock(mapper, block, location)
                        }
                        channel.send(block.registryName.toString())
                    }
                }
            }

            channel.close()
        }
    }
}