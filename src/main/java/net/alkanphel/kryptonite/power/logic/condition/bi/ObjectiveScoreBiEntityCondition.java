package net.alkanphel.kryptonite.power.logic.condition.bi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.util.NumberComparator;

public record ObjectiveScoreBiEntityCondition(String actorObjective, String targetObjective, NumberComparator comparator) implements BiCondition {

    public static final MapCodec<ObjectiveScoreBiEntityCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("actor_objective").forGetter(ObjectiveScoreBiEntityCondition::actorObjective),
            Codec.STRING.fieldOf("target_objective").forGetter(ObjectiveScoreBiEntityCondition::targetObjective),
            NumberComparator.CODEC.optionalFieldOf("comparator", NumberComparator.EQUALS).forGetter(ObjectiveScoreBiEntityCondition::comparator)
    ).apply(instance, ObjectiveScoreBiEntityCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ObjectiveScoreBiEntityCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ObjectiveScoreBiEntityCondition::actorObjective,
            ByteBufCodecs.STRING_UTF8, ObjectiveScoreBiEntityCondition::targetObjective,
            NumberComparator.STREAM_CODEC, ObjectiveScoreBiEntityCondition::comparator,
            ObjectiveScoreBiEntityCondition::new
    );

    @Override
    public boolean test(BiConditionContext context) {
        var actor = context.actor();
        var target = context.target();
        if (actor == null || target == null) return false;

        var scoreboard = actor.level().getScoreboard();
        var actorObj = scoreboard.getObjective(actorObjective);
        var targetObj = scoreboard.getObjective(targetObjective);

        if (actorObj == null || targetObj == null) return false;

        var actorScoreInfo = scoreboard.getPlayerScoreInfo(actor, actorObj);
        var targetScoreInfo = scoreboard.getPlayerScoreInfo(target, targetObj);

        if (actorScoreInfo == null || targetScoreInfo == null) return false;

        return comparator.compare(actorScoreInfo.value(), targetScoreInfo.value());
    }

    @Override
    public BiConditionSerializer<ObjectiveScoreBiEntityCondition> getSerializer() {
        return BiConditionSerializers.OBJECTIVE_SCORE.get();
    }

    public static class Serializer extends BiConditionSerializer<ObjectiveScoreBiEntityCondition> {

        @Override
        public MapCodec<ObjectiveScoreBiEntityCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, ObjectiveScoreBiEntityCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Objective Score")
                    .setDescription("Compares the scoreboard objective scores of the actor & target entities. NOT SYNCED TO THE CLIENT!")
                    .add("actor_objective", TYPE_STRING, "The scoreboard objective to get from the actor entity.")
                    .add("target_objective", TYPE_STRING, "The scoreboard objective to get from the target entity.")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator used between both scores.")
                    .addExampleObject(new ObjectiveScoreBiEntityCondition("kills", "deaths", NumberComparator.GREATER_THAN));
        }
    }

}