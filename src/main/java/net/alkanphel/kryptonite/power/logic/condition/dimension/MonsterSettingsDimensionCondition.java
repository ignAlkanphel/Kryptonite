package net.alkanphel.kryptonite.power.logic.condition.dimension;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionCondition;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.util.valueproviders.UniformInt;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;

import javax.annotation.Nullable;
import java.util.Optional;

public record MonsterSettingsDimensionCondition(@Nullable Integer monsterSpawnBlockLightLimit, @Nullable IntProvider monsterSpawnLightLevel) implements DimensionCondition {

    // int or int provider codec
    public static final Codec<IntProvider> INT_PROVIDER_CODEC = Codec.either(Codec.INT, IntProviders.CODEC).xmap(
            either -> either.map(ConstantInt::of, i -> i),
            provider -> (provider instanceof ConstantInt ci)
                    ? Either.left(ci.value())
                    : Either.right(provider)
    );

    public static final MapCodec<MonsterSettingsDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("monster_spawn_block_light_limit").forGetter(c -> Optional.ofNullable(c.monsterSpawnBlockLightLimit())),
            INT_PROVIDER_CODEC.optionalFieldOf("monster_spawn_light_level").forGetter(c -> Optional.ofNullable(c.monsterSpawnLightLevel()))
            ).apply(instance, (blockLimit, lightLevel
            ) -> new MonsterSettingsDimensionCondition(blockLimit.orElse(null), lightLevel.orElse(null))
    ));

    public static final StreamCodec<RegistryFriendlyByteBuf, MonsterSettingsDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), c -> Optional.ofNullable(c.monsterSpawnBlockLightLimit()),
            ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistriesTrusted(IntProviders.CODEC)), c -> Optional.ofNullable(c.monsterSpawnLightLevel()),
            (blockLimit, lightLevel
            ) -> new MonsterSettingsDimensionCondition(blockLimit.orElse(null), lightLevel.orElse(null))
    );

    @Override
    public boolean test(DimensionConditionContext context) {
        var type = context.dimensionType();
        if (type == null) return false;

        var settings = type.monsterSettings();

        if (monsterSpawnBlockLightLimit != null) {
            if (settings.monsterSpawnBlockLightLimit() != monsterSpawnBlockLightLimit) {
                return false;
            }
        }

        if (monsterSpawnLightLevel != null) {
            IntProvider vanilla = settings.monsterSpawnLightTest();
            if (!sameIntProvider(monsterSpawnLightLevel, vanilla)) {
                return false;
            }
        }

        return true;
    }

    private static boolean sameIntProvider(IntProvider a, IntProvider b) {
        if (a == b) return true;

        if (a instanceof ConstantInt ac && b instanceof ConstantInt bc) {
            return ac.value() == bc.value();
        }

        if (a instanceof UniformInt au && b instanceof UniformInt bu) {
            return au.minInclusive() == bu.minInclusive() && au.maxInclusive() == bu.maxInclusive();
        }

        return a.equals(b);
    }

    @Override
    public DimensionConditionSerializer<MonsterSettingsDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.MONSTER_SETTINGS.get();
    }

    public static class Serializer extends DimensionConditionSerializer<MonsterSettingsDimensionCondition> {

        @Override
        public MapCodec<MonsterSettingsDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, MonsterSettingsDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Monster Settings")
                    .setDescription("Checks the monster settings of the current dimension.)")
                    .addOptional("monster_spawn_block_light_limit", SettingType.combined(SettingType.intRange(0, 15)), "Block light limit for monsters to spawn.")
                    .addOptional("monster_spawn_light_level", SettingType.combined(SettingType.intRange(0, 15)), "Light level for monsters to spawn.")
                    .addExampleObject(new MonsterSettingsDimensionCondition(0, ConstantInt.of(7)))
                    .addExampleObject(new MonsterSettingsDimensionCondition(null, new UniformInt(0, 7)));
        }
    }

}