package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.List;

public record FluidTypeBlockCondition(HolderSet<Fluid> fluid) implements BlockCondition {

    public static final MapCodec<FluidTypeBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluid_type").forGetter(FluidTypeBlockCondition::fluid)
    ).apply(instance, FluidTypeBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidTypeBlockCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.FLUID), FluidTypeBlockCondition::fluid,
            FluidTypeBlockCondition::new
    );

    @Override
    public boolean test(BlockConditionContext context) {
        Holder<Fluid> fluidHolder = context.blockState().getFluidState().typeHolder();
        return fluid.contains(fluidHolder);
    }

    @Override
    public BlockConditionSerializer<FluidTypeBlockCondition> getSerializer() {
        return BlockConditionSerializers.FLUID_TYPE.get();
    }

    public static class Serializer extends BlockConditionSerializer<FluidTypeBlockCondition> {

        @Override
        public MapCodec<FluidTypeBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, FluidTypeBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Fluid Type")
                    .setDescription("Checks if the fluid type is at the block position.")
                    .add("fluid_type", KryptoniteDocumented.TYPE_FLUID_TYPE_HOLDER_SET, "IDs or tags of the required fluid type.")
                    .addExampleObject(new FluidTypeBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.FLUID, Identifier.withDefaultNamespace("water"))))))
                    .addExampleObject(new FluidTypeBlockCondition(new OrHolderSet<>(List.of(provider.lookupOrThrow(Registries.FLUID).getOrThrow(FluidTags.WATER), HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.FLUID, Identifier.withDefaultNamespace("lava"))))))));
        }
    }

}