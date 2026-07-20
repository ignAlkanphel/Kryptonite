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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public record IsEffectPossibleCondition(HolderSet<MobEffect> mobEffect) implements Condition {

    public static final MapCodec<IsEffectPossibleCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("effects").forGetter(IsEffectPossibleCondition::mobEffect)
    ).apply(instance, IsEffectPossibleCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, IsEffectPossibleCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.MOB_EFFECT), IsEffectPossibleCondition::mobEffect,
            IsEffectPossibleCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();
        if (entity == null) return false;

        for (Holder<MobEffect> effectHolder : mobEffect.stream().toList()) {
            MobEffectInstance dummyInstance = new MobEffectInstance(effectHolder, 1, 0);

            MobEffectEvent.Applicable event = new MobEffectEvent.Applicable(entity, dummyInstance, null);
            NeoForge.EVENT_BUS.post(event);

            if (event.getApplicationResult()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ConditionSerializer<IsEffectPossibleCondition> getSerializer() {
        return KryptoniteConditionSerializers.IS_EFFECT_POSSIBLE.get();
    }

    public static class Serializer extends ConditionSerializer<IsEffectPossibleCondition> {

        @Override
        public MapCodec<IsEffectPossibleCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, IsEffectPossibleCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is Effect Possible")
                    .setDescription("Checks if the entity is NOT immune to the specified effects.")
                    .add("effects", TYPE_MOB_EFFECT_TYPE_HOLDER_SET, "IDs or tags of the effects")
                    .addExampleObject(new IsEffectPossibleCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("slowness"))))))
                    .addExampleObject(new IsEffectPossibleCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("slowness"))), provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("mining_fatigue"))))));
        }
    }

}