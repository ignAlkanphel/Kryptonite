package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.threetag.palladium.component.PalladiumDataComponents.Abilities;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.PowerInstance;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.enabling.EnablingHandler;
import net.threetag.palladium.power.ability.enabling.KeyBindEnablingHandler;

public class AbilityCooldownAction extends Action {

    public static final MapCodec<AbilityCooldownAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AbilityReference.CODEC.fieldOf("ability").forGetter(a -> a.ability)
    ).apply(instance, AbilityCooldownAction::new));

    public final AbilityReference ability;

    public AbilityCooldownAction(AbilityReference ability) {
        this.ability = ability;
    }

    @Override
    public boolean run(DataContext context) {
        LivingEntity living = context.getLivingEntity();
        PowerInstance power = context.getPowerInstance();

        if (living == null) return false;

        AbilityInstance<?> instance = this.ability.getInstance(living, power);
        if (instance == null) return false;

        EnablingHandler handler = instance.getAbility().getStateManager().getEnablingHandler();
        if (!(handler instanceof KeyBindEnablingHandler keyHandler)) return false;

        Integer cooldown = ObfuscationReflectionHelper.getPrivateValue(KeyBindEnablingHandler.class, keyHandler, "cooldown");
        if (cooldown == null || cooldown <= 0) return false;

        instance.set(Abilities.COOLDOWN.get(), cooldown);
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.ABILITY_COOLDOWN.get();
    }

    public static class Serializer extends ActionSerializer<AbilityCooldownAction> {

        @Override
        public MapCodec<AbilityCooldownAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, AbilityCooldownAction> builder, HolderLookup.Provider provider) {
            builder.setName("Ability Cooldown")
                    .setDescription("Triggers the cooldown of the specified ability if it has one.")
                    .add("ability", TYPE_ABILITY_REFERENCE, "Ability to trigger its cooldown on.")
                    .addExampleObject(new AbilityCooldownAction(new AbilityReference(null, "ability_key")))
                    .addExampleObject(new AbilityCooldownAction(new AbilityReference(Identifier.fromNamespaceAndPath("example", "power"), "ability_key")));
        }
    }

}