package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public record NbtCondition(CompoundTag nbt) implements Condition {

    public static final MapCodec<NbtCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CompoundTag.CODEC.optionalFieldOf("nbt", new CompoundTag()).forGetter(NbtCondition::nbt)
    ).apply(instance, NbtCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, NbtCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, NbtCondition::nbt,
            NbtCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        var output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        entity.saveWithoutId(output);

        return NbtUtils.compareNbt(nbt, output.buildResult(), true);
    }

    @Override
    public ConditionSerializer<NbtCondition> getSerializer() {
        return KryptoniteConditionSerializers.NBT.get();
    }

    public static class Serializer extends ConditionSerializer<NbtCondition> {

        @Override
        public MapCodec<NbtCondition> codec() {
            return CODEC;
        }

        private static CompoundTag addExampleNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean(ServerPlayer.TAG_ON_GROUND, true);
            tag.putString(ServerPlayer.TAG_DIMENSION, Level.NETHER.identifier().toString());

            CompoundTag abilities = new CompoundTag();
            abilities.putBoolean("flying", true);

            tag.put("abilities", abilities);

            return tag;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, NbtCondition> builder, HolderLookup.Provider provider) {
            builder.setName("NBT")
                    .setDescription("Checks the entity's NBT. Warning that some NBT data from the server (e.g. tags added via /tag) is not synced to the client.")
                    .add("nbt", TYPE_NBT, "Partial match NBT data to compare against the entity.")
                    .addExampleObject(new NbtCondition(addExampleNbt()));
        }
    }

}