package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.FloatDataAttachmentValue;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class GlowingAbility extends Ability {

    public static final MapCodec<GlowingAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Mode.CODEC.optionalFieldOf("mode", Mode.SELF).forGetter(ab -> ab.mode),
            Condition.CODEC.optionalFieldOf("entity_conditions").forGetter(ab -> ab.entityConditions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(ab -> ab.biEntityConditions),
            Value.CODEC.optionalFieldOf("use_teams", new StaticValue(true)).forGetter(ab -> ab.useTeams),
            KryptoniteCodecs.RGBValue.CODEC.optionalFieldOf("color", KryptoniteCodecs.RGBValue.WHITE).forGetter(ab -> ab.color),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, GlowingAbility::new));

    public final Mode mode;
    public final Optional<Condition> entityConditions;
    public final List<BiCondition> biEntityConditions;
    public final Value useTeams;
    public final KryptoniteCodecs.RGBValue color;

    public GlowingAbility(Mode mode, Optional<Condition> entityConditions, List<BiCondition> biEntityConditions, Value useTeams, KryptoniteCodecs.RGBValue color, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.mode = mode;
        this.entityConditions = entityConditions;
        this.biEntityConditions = biEntityConditions;
        this.useTeams = useTeams;
        this.color = color;
    }

    public boolean doesApply(Entity holder, Entity other) {
        if (entityConditions.isPresent() && !entityConditions.get().test(DataContext.forEntity(other))) {
            return false;
        }

        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, holder, other)) {
            return false;
        }

        return true;
    }

    public static boolean shouldActorGlow(LivingEntity viewer, LivingEntity holder) {
        return AbilityUtil.getEnabledInstances(holder, KryptoniteAbilitySerializers.GLOWING.get())
                .stream()
                .filter(instance -> instance.getAbility().mode == Mode.SELF)
                .anyMatch(instance -> instance.getAbility().doesApply(holder, viewer));
    }

    public static boolean shouldTargetGlow(LivingEntity viewer, Entity target) {
        return AbilityUtil.getEnabledInstances(viewer, KryptoniteAbilitySerializers.GLOWING.get())
                .stream()
                .filter(instance -> instance.getAbility().mode == Mode.OTHER)
                .anyMatch(instance -> instance.getAbility().doesApply(viewer, target));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.GLOWING.get();
    }

    public static class Serializer extends AbilitySerializer<GlowingAbility> {

        @Override
        public MapCodec<GlowingAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, GlowingAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Glowing")
                    .setDescription("Makes entities glow like the \"minecraft:glowing\" effect. Use \"self\" mode to make the ability holder glow for others, \"target\" mode to make other entities glow for the ability holder.")
                    .addOptional("mode", TYPE_STRING, "\"self\" means the ability holder glows for others & \"target\" means the ability holder sees others glow.", Mode.SELF)
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "If specified, filters which entities see the glow for \"self\" & which entities glow for \"other\".")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, the bi conditions filter. In the context of this field, the \"actor\" is the entity that has this ability & \"target\" is the other entity.")
                    .addOptional("use_teams", TYPE_VALUE, "If true, the team color overrides the glow color when applicable.", true)
                    .addOptional("color", KryptoniteDocumented.TYPE_RGB_VALUE, "The RGB glow color to use.", KryptoniteCodecs.RGBValue.WHITE)
                    .addExampleObject(new GlowingAbility(Mode.SELF, Optional.empty(), List.of(), new StaticValue(true), new KryptoniteCodecs.RGBValue(new StaticValue(0.85), new StaticValue(1.0), new StaticValue(0.1)), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new GlowingAbility(Mode.OTHER, Optional.empty(), List.of(), new StaticValue(false), new KryptoniteCodecs.RGBValue(new FloatDataAttachmentValue(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("test", "glow_red")), 1.0F, ""), new FloatDataAttachmentValue(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("test", "glow_green")), 1.0F, ""), new FloatDataAttachmentValue(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("test", "glow_blue")), 1.0F, "")), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum Mode implements StringRepresentable {
        SELF, OTHER;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

}