package dev.fastmc.texture.main;

import com.google.common.collect.Sets;
import dev.fastmc.texture.IPatchedModelLoader;
import dev.fastmc.texture.accessor.AccessorModelBakery;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Mixin(value = ModelLoader.class, remap = false)
public abstract class MixinModelLoader extends ModelBakery implements IPatchedModelLoader {
    @Mutable
    @Shadow
    @Final
    private Map<ModelResourceLocation, IModel> stateModels;

    @Mutable
    @Shadow
    @Final
    private Map<ModelBlockDefinition, IModel> multipartModels;

    @Mutable
    @Shadow
    @Final
    private Map<ModelResourceLocation, ModelBlockDefinition> multipartDefinitions;

    @Mutable
    @Shadow
    @Final
    private Set<ModelResourceLocation> missingVariants;

    @Mutable
    @Shadow
    @Final
    private Map<ResourceLocation, Exception> loadingExceptions;

    public MixinModelLoader(
        IResourceManager p_i46085_1_,
        TextureMap p_i46085_2_,
        BlockModelShapes p_i46085_3_
    ) {
        super(p_i46085_1_, p_i46085_2_, p_i46085_3_);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void Inject$init$RETURN(CallbackInfo ci) {
        stateModels = new ConcurrentHashMap<>();
        multipartDefinitions = new ConcurrentHashMap<>();
        multipartModels = new ConcurrentHashMap<>();
        missingVariants = Collections.newSetFromMap(new ConcurrentHashMap<>());
        loadingExceptions = new ConcurrentHashMap<>();
    }

    /**
     * @author Luna
     * @reason Parallel loading
     */
    @Overwrite
    protected void loadItemModels() {
        registerVariantNames();
        loadItemsModelParallel();
    }

    /**
     * @author Luna
     * @reason Parallel loading
     */
    @Overwrite
    protected void loadBlocks() {
        loadBlocksParallel(this.blockModelShapes.getBlockStateMapper());
    }

    @Override
    protected void loadBlock(
        BlockStateMapper mapper,
        @NotNull Block block,
        @NotNull ResourceLocation location
    ) {
        ModelBlockDefinition modelblockdefinition = this.getModelBlockDefinition(location);
        Map<IBlockState, ModelResourceLocation> map = mapper.getVariants(block);

        if (modelblockdefinition.hasMultipartData()) {
            modelblockdefinition.getMultipartData().setStateContainer(block.getBlockState());
            Collection<ModelResourceLocation> locations = ((AccessorModelBakery) this).getMultipartVariantMap().get(
                modelblockdefinition);

            if (locations == null) {
                locations = new ObjectArrayList<>();
            }

            Collection<ModelResourceLocation> finalLocations = locations;
            Sets.newHashSet(map.values()).stream()
                .filter(location::equals)
                .collect(Collectors.toCollection(() -> finalLocations));
            registerMultipartVariant(modelblockdefinition, locations);
        }

        for (ModelResourceLocation l : map.values()) {
            if (location.equals(l)) {
                try {
                    registerVariant(modelblockdefinition, l);
                } catch (RuntimeException var12) {
                    if (!modelblockdefinition.hasMultipartData()) {
                        AccessorModelBakery.getLogger().warn(
                            "Unable to load variant: " + l.getVariant() + " from " + l,
                            var12
                        );
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    public Map<ModelResourceLocation, IModel> getStateModels() {
        return stateModels;
    }
}
