package net.alkanphel.kryptonite.registry;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.ability.ModifyKnockbackAbility;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.power.ability.AbilityInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class KryptoniteAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Kryptonite.MOD_ID);

    public record PendingKnockbackModifier(LivingEntity attacker, AbilityInstance<ModifyKnockbackAbility> instance) {}

    public static final Supplier<AttachmentType<List<PendingKnockbackModifier>>> PENDING_KNOCKBACK_MODIFIER =
            ATTACHMENT_TYPES.register("pending_knockback_modifier", () -> AttachmentType.builder((Supplier<List<PendingKnockbackModifier>>) ArrayList::new).build());

    public static final class Addon {
        private Addon() {}
        public static final Identifier PERSPECTIVE = Kryptonite.id("perspective");
    }

}