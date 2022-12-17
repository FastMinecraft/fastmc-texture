import org.spongepowered.asm.gradle.plugins.MixinExtension

forgeProject {
    accessTransformer = "fastmc-texture-at.cfg"
    mixinConfig("mixins.fastmc.texture.accessor.json", "mixins.fastmc.texture.main.json")
    coreModClass.set("dev.fastmc.texture.FastMcTextureCoremod")
    devCoreModClass.set("dev.fastmc.texture.FastMcTextureDevFixCoremod")
}

configure<MixinExtension> {
    add(sourceSets["main"], "mixins.fastmc.texture.refmap.json")
}