forgeProject {
    modPackage.set("dev.fastmc.texture")
    accessTransformer = "fastmc-texture-at.cfg"
    mixinConfig("mixins.fastmc.texture.accessor.json", "mixins.fastmc.texture.main.json")
    coreModClass.set("dev.fastmc.texture.FastMcTextureCoremod")
    devCoreModClass.set("dev.fastmc.texture.FastMcTextureDevFixCoremod")
}