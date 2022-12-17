package dev.fastmc.texture.main;

import com.google.common.base.Joiner;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = ModelLoaderRegistry.class, remap = false)
public abstract class MixinModelLoaderRegistry {
    @Mutable
    @Shadow
    @Final
    private static Map<ResourceLocation, IModel> cache;

    @Mutable
    @Shadow
    @Final
    private static Map<ResourceLocation, ResourceLocation> aliases;

    @Shadow
    public static @NotNull ResourceLocation getActualLocation(ResourceLocation location) {
        throw new AssertionError();
    }

    @Shadow
    @Final
    private static Set<ICustomModelLoader> loaders;

    @Shadow
    public static IModel getModelOrMissing(ResourceLocation location) {
        return null;
    }

    @Shadow
    public static IModel getMissingModel() {
        return null;
    }

    @Mutable
    @Shadow
    @Final
    private static Set<ResourceLocation> textures;

    private static ICustomModelLoader VARIANT_LOADER;
    private static ICustomModelLoader VANILLA_LOADER;
    private static final ThreadLocal<ArrayDeque<ResourceLocation>> loadingModelsOverride = ThreadLocal.withInitial(
        ArrayDeque::new);


    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void Inject$clinit$RETURN(CallbackInfo ci) {
        cache = new ConcurrentHashMap<>();
        aliases = new ConcurrentHashMap<>();
        textures = Collections.newSetFromMap(new ConcurrentHashMap<>());

        try {
            VARIANT_LOADER = (ICustomModelLoader) Class.forName(
                "net.minecraftforge.client.model.ModelLoader$VariantLoader").getField("INSTANCE").get(null);
            VANILLA_LOADER = (ICustomModelLoader) Class.forName(
                "net.minecraftforge.client.model.ModelLoader$VanillaLoader").getField("INSTANCE").get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @author Luna
     * @reason Parallel loading
     */
    @SuppressWarnings({ "ThrowFromFinallyBlock", "ResultOfMethodCallIgnored" })
    @Overwrite
    public static IModel getModel(ResourceLocation location) throws Exception {
        IModel model;

        IModel cached = cache.get(location);
        if (cached != null) return cached;

        ArrayDeque<ResourceLocation> loadingModels = loadingModelsOverride.get();
        for (ResourceLocation loading : loadingModels) {
            if (location.getClass() == loading.getClass() && location.equals(loading)) {
                throw new ModelLoaderRegistry.LoaderException("circular model dependencies, stack: [" + Joiner.on(", ").join(
                    loadingModels) + "]");
            }
        }
        loadingModels.addLast(location);

        try {
            ResourceLocation aliased = aliases.get(location);
            if (aliased != null) return getModel(aliased);

            ResourceLocation actual = getActualLocation(location);
            ICustomModelLoader accepted = null;
            for (ICustomModelLoader loader : loaders) {
                try {
                    if (loader.accepts(actual)) {
                        if (accepted != null) {
                            throw new ModelLoaderRegistry.LoaderException(String.format(
                                "2 loaders (%s and %s) want to load the same model %s",
                                accepted,
                                loader,
                                location
                            ));
                        }
                        accepted = loader;
                    }
                } catch (Exception e) {
                    throw new ModelLoaderRegistry.LoaderException(String.format(
                        "Exception checking if model %s can be loaded with loader %s, skipping",
                        location,
                        loader
                    ), e);
                }
            }

            // no custom loaders found, try vanilla ones
            if (accepted == null) {
                if (VARIANT_LOADER.accepts(actual)) {
                    accepted = VARIANT_LOADER;
                } else if (VANILLA_LOADER.accepts(actual)) {
                    accepted = VANILLA_LOADER;
                }
            }

            if (accepted == null) {
                throw new ModelLoaderRegistry.LoaderException("no suitable loader found for the model " + location + ", skipping");
            }
            try {
                model = accepted.loadModel(actual);
            } catch (Exception e) {
                throw new ModelLoaderRegistry.LoaderException(String.format(
                    "Exception loading model %s with loader %s, skipping",
                    location,
                    accepted
                ), e);
            }
            if (model == getMissingModel()) {
                throw new ModelLoaderRegistry.LoaderException(String.format(
                    "Loader %s returned missing model while loading model %s",
                    accepted,
                    location
                ));
            }
            if (model == null) {
                throw new ModelLoaderRegistry.LoaderException(String.format(
                    "Loader %s returned null while loading model %s",
                    accepted,
                    location
                ));
            }
            textures.addAll(model.getTextures());
        } finally {
            ResourceLocation popLoc = loadingModels.removeLast();
            if (popLoc != location) {
                throw new IllegalStateException("Corrupted loading model stack: " + popLoc + " != " + location);
            }
        }
        cache.put(location, model);
        for (ResourceLocation dep : model.getDependencies()) {
            getModelOrMissing(dep);
        }
        return model;
    }
}
