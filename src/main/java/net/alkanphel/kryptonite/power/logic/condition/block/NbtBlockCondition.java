package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record NbtBlockCondition(CompoundTag nbt) implements BlockCondition {

    public static final MapCodec<NbtBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CompoundTag.CODEC.optionalFieldOf("nbt", new CompoundTag()).forGetter(NbtBlockCondition::nbt)
    ).apply(instance, NbtBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, NbtBlockCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, NbtBlockCondition::nbt,
            NbtBlockCondition::new
    );

    @Override
    public boolean test(BlockConditionContext context) {
        return context.blockEntity()
                .map(be -> be.saveWithoutMetadata(context.level().registryAccess()))
                .map(beNbt -> NbtUtils.compareNbt(nbt, beNbt, true))
                .orElse(false);
    }

    @Override
    public BlockConditionSerializer<NbtBlockCondition> getSerializer() {
        return BlockConditionSerializers.NBT.get();
    }

    public static CompoundTag addExampleNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("CustomName", "Test Chest");
        tag.put("Items", new ListTag());
        return tag;
    }

    public static class Serializer extends BlockConditionSerializer<NbtBlockCondition> {

        @Override
        public MapCodec<NbtBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, NbtBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("NBT")
                    .setDescription("Checks the NBT of a block entity.")
                    .add("nbt", TYPE_NBT, "The NBT data to check for.")
                    .addExampleObject(new NbtBlockCondition(addExampleNbt()));
        }
    }

}