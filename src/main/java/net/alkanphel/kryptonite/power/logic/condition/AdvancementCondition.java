package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.mixin.client.ClientAdvancementsAccessor;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.Utils;

import java.util.List;
import java.util.Map;

public record AdvancementCondition(List<Identifier> advancements) implements Condition {

    public static final MapCodec<AdvancementCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.compactListCodec(Identifier.CODEC).fieldOf("advancement").forGetter(AdvancementCondition::advancements)
    ).apply(instance, AdvancementCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancementCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(Utils::newList, Identifier.STREAM_CODEC), AdvancementCondition::advancements,
            AdvancementCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        if (!(context.getEntity() instanceof Player player)) return false;

        if (player instanceof ServerPlayer serverPlayer) {
            var server = serverPlayer.level().getServer();

            for (Identifier advancement : advancements) {
                AdvancementHolder advancementHolder = server.getAdvancements().get(advancement);

                if (advancementHolder == null) {
                    Kryptonite.LOGGER.warn("Advancement \"{}\" did not exist, but was referenced in an \"advancement\" condition!", advancement);
                    return false;
                }

                if (!serverPlayer.getAdvancements().getOrStartProgress(advancementHolder).isDone()) {
                    return false;
                }
            }

            return true;
        }

        if (player instanceof LocalPlayer localPlayer) {
            ClientAdvancements clientAdvancements = localPlayer.connection.getAdvancements();

            for (Identifier advancement : advancements) {
                AdvancementHolder advancementHolder = clientAdvancements.get(advancement);
                if (advancementHolder == null) return false;

                Map<AdvancementHolder, AdvancementProgress> progresses = ((ClientAdvancementsAccessor) clientAdvancements).getProgress();
                AdvancementProgress progress = progresses.get(advancementHolder);

                if (progress == null || !progress.isDone()) return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public ConditionSerializer<AdvancementCondition> getSerializer() {
        return KryptoniteConditionSerializers.ADVANCEMENT.get();
    }

    public static class Serializer extends ConditionSerializer<AdvancementCondition> {

        @Override
        public MapCodec<AdvancementCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, AdvancementCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Advancement")
                    .setDescription("Checks if the player has completed a specific advancement.")
                    .add("advancement", SettingType.listOrPrimitive(TYPE_IDENTIFIER), "The namespace and ID(s) of the advancement(s) to check. May be a single identifier or a list. All listed advancements must be completed.")
                    .addExampleObject(new AdvancementCondition(List.of(Identifier.withDefaultNamespace("story/mine_stone"))))
                    .addExampleObject(new AdvancementCondition(List.of(Identifier.withDefaultNamespace("story/mine_stone"), Identifier.withDefaultNamespace("story/smelt_iron"))));
        }
    }

}