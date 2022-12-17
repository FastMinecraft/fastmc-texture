package dev.fastmc.texture.accessor;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(value = ModelBakery.class, remap = false)
public interface AccessorModelBakery {
    @Accessor("LOGGER")
    static Logger getLogger() {throw new UnsupportedOperationException();}

    @Invoker
    ResourceLocation callGetItemLocation(String location);

    @Invoker
    List<String> callGetVariantNames(Item stack);

    @Accessor
    Map<ModelBlockDefinition, Collection<ModelResourceLocation>> getMultipartVariantMap();

    @Invoker
    void callLoadBlock(BlockStateMapper blockstatemapper, Block block, final ResourceLocation resourcelocation);
}
