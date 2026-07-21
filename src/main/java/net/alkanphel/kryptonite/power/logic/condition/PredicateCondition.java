package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

import java.util.Optional;

public record PredicateCondition(ResourceKey<LootItemCondition> predicate) implements Condition {

    public static final MapCodec<PredicateCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.PREDICATE).fieldOf("predicate").forGetter(PredicateCondition::predicate)
    ).apply(instance, PredicateCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PredicateCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(s -> ResourceKey.create(Registries.PREDICATE, Identifier.parse(s)), k -> k.identifier().toString()), PredicateCondition::predicate,
            PredicateCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null || !(entity.level() instanceof ServerLevel serverLevel)) return false;

        LootItemCondition lootCondition = serverLevel.getServer().reloadableRegistries()
                .lookup()
                .lookup(Registries.PREDICATE)
                .flatMap(r -> r.get(predicate))
                .map(Holder::value)
                .orElse(null);

        if (lootCondition == null) return false;

        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withOptionalParameter(LootContextParams.THIS_ENTITY, entity)
                .create(LootContextParamSets.COMMAND);

        return lootCondition.test(new LootContext.Builder(params).create(Optional.empty()));
    }

    @Override
    public ConditionSerializer<PredicateCondition> getSerializer() {
        return KryptoniteConditionSerializers.PREDICATE.get();
    }

    public static class Serializer extends ConditionSerializer<PredicateCondition> {

        @Override
        public MapCodec<PredicateCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, PredicateCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Predicate")
                    .setDescription("Checks if the entity fulfills a specified predicate. Warning that it operates on the server-side.")
                    .add("predicate", TYPE_IDENTIFIER, "The namespace and id of the predicate to test.")
                    .addExampleObject(new PredicateCondition(ResourceKey.create(Registries.PREDICATE, Identifier.parse("datapack:test"))));
        }
    }

}