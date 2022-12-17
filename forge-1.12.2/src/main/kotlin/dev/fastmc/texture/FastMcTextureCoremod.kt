package dev.fastmc.texture

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.Mixins

@IFMLLoadingPlugin.Name("FastMcTexture")
@MCVersion("1.12.2")
class FastMcTextureCoremod : IFMLLoadingPlugin {
    init {
        MixinBootstrap.init()
        Mixins.addConfigurations(
            "mixins.fastmc.texture.accessor.json",
            "mixins.fastmc.texture.main.json",
        )
    }

    override fun injectData(data: Map<String, Any>) {

    }

    override fun getASMTransformerClass(): Array<String> {
        return emptyArray()
    }

    override fun getModContainerClass(): String? {
        return null
    }

    override fun getSetupClass(): String? {
        return null
    }

    override fun getAccessTransformerClass(): String? {
        return null
    }
}
