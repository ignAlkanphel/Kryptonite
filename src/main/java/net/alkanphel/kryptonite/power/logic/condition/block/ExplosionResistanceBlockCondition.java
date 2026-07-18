package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

public record ExplosionResistanceBlockCondition(NumberComparator comparator, Value compareTo) implements BlockCondition {

    public static final MapCodec<ExplosionResistanceBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(ExplosionResistanceBlockCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(ExplosionResistanceBlockCondition::compareTo)
    ).apply(instance, ExplosionResistanceBlockCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExplosionResistanceBlockCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, ExplosionResistanceBlockCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), ExplosionResistanceBlockCondition::compareTo,
            ExplosionResistanceBlockCondition::new
    );

    @Override
    public boolean test(BlockConditionContext context) {
        float resistance = context.blockState().getBlock().getExplosionResistance();
        float comparedValue = compareTo.getAsFloat(null);

        return comparator.compare(resistance, comparedValue);
    }

    @Override
    public BlockConditionSerializer<ExplosionResistanceBlockCondition> getSerializer() {
        return BlockConditionSerializers.EXPLOSION_RESISTANCE.get();
    }

    public static class Serializer extends BlockConditionSerializer<ExplosionResistanceBlockCondition> {

        @Override
        public MapCodec<ExplosionResistanceBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, ExplosionResistanceBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Explosion Resistance")
                    .setDescription("Checks the explosion resistance value of the block.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", TYPE_VALUE, "The value that is being compared against")
                    .addExampleObject(new ExplosionResistanceBlockCondition(NumberComparator.GREATER_OR_EQUAL, new StaticValue(1200)));
        }
    }

}