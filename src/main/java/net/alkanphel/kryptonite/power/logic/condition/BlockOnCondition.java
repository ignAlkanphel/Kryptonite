package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.FrictionBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.util.NumberComparator;

import java.util.List;

public record BlockOnCondition(List<BlockCondition> blockConditions) implements Condition {

    public static final MapCodec<BlockOnCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(BlockOnCondition::blockConditions)
    ).apply(instance, BlockOnCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockOnCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BlockCondition.LIST_CODEC), BlockOnCondition::blockConditions,
            BlockOnCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null || !entity.onGround()) return false;

        return blockConditions.isEmpty() || BlockCondition.checkConditions(blockConditions, entity.level(), entity.getOnPos());
    }

    @Override
    public ConditionSerializer<BlockOnCondition> getSerializer() {
        return KryptoniteConditionSerializers.BLOCK_ON.get();
    }

    public static class Serializer extends ConditionSerializer<BlockOnCondition> {

        @Override
        public MapCodec<BlockOnCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, BlockOnCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Block On")
                    .setDescription("Checks if the entity is standing on a block.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, these conditions must be fulfilled for the block underneath the entity's feet.")
                    .addExampleObject(new BlockOnCondition(List.of()))
                    .addExampleObject(new BlockOnCondition(List.of(new FrictionBlockCondition(NumberComparator.GREATER_OR_EQUAL, new StaticValue(0.98)))));
        }
    }

}