package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.threetag.palladium.power.ability.DamageImmunityAbility;

public record DamageImmunityCondition(HolderSet<DamageType> damageTypes) implements Condition {

    public static final MapCodec<DamageImmunityCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).fieldOf("damage_type").forGetter(DamageImmunityCondition::damageTypes)
    ).apply(instance, DamageImmunityCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DamageImmunityCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.DAMAGE_TYPE), DamageImmunityCondition::damageTypes,
            DamageImmunityCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();
        if (entity == null) return false;

        for (Holder<DamageType> holder : damageTypes.stream().toList()) {
            DamageSource dummySource = new DamageSource(holder);

            boolean vanillaInvulnerable = (
                    entity.isInvulnerable()
                            && !dummySource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                            && !dummySource.isCreativePlayer())
                            || (dummySource.is(DamageTypeTags.IS_FIRE) && entity.fireImmune())
                            || (dummySource.is(DamageTypeTags.IS_FALL) && entity.is(EntityTypeTags.FALL_DAMAGE_IMMUNE)
            );

            if (vanillaInvulnerable) return true;

            if (entity.level() instanceof ServerLevel serverLevel && EnchantmentHelper.isImmuneToDamage(serverLevel, entity, dummySource)) {
                return true;
            }

            for (AbilityInstance<DamageImmunityAbility> instance : AbilityUtil.getEnabledInstances(entity, AbilitySerializers.DAMAGE_IMMUNITY.get())) {
                if (DamageImmunityAbility.isImmuneAgainst(instance, dummySource)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ConditionSerializer<DamageImmunityCondition> getSerializer() {
        return KryptoniteConditionSerializers.DAMAGE_IMMUNITY.get();
    }

    public static class Serializer extends ConditionSerializer<DamageImmunityCondition> {

        @Override
        public MapCodec<DamageImmunityCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, DamageImmunityCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Damage Immunity")
                    .setDescription("Checks if the entity is immune against certain damage types.")
                    .add("damage_type", TYPE_DAMAGE_TYPE_HOLDER_SET, "IDs or tags of the damage types")
                    .addExampleObject(new DamageImmunityCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("cactus"))))))
                    .addExampleObject(new DamageImmunityCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("cactus"))), provider.holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("in_fire"))))));
        }
    }

}