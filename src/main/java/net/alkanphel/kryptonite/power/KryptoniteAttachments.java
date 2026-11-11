package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.ability.ModifyKnockbackAbility;
import net.alkanphel.kryptonite.util.player.LastLocation;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.power.ability.AbilityInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class KryptoniteAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Kryptonite.MOD_ID);

    public record PendingKnockbackModifier(LivingEntity attacker, AbilityInstance<ModifyKnockbackAbility> instance) {}
    public static final Supplier<AttachmentType<List<PendingKnockbackModifier>>> PENDING_KNOCKBACK_MODIFIER = ATTACHMENT_TYPES.register("pending_knockback_modifier", () -> AttachmentType.builder((Supplier<List<PendingKnockbackModifier>>) ArrayList::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<LastLocation>>> LAST_LOCATION = ATTACHMENT_TYPES.register("last_location", () -> AttachmentType.builder(Optional::<LastLocation>empty)
            .serialize(LastLocation.CODEC.codec().optionalFieldOf("location"))
            .sync(ByteBufCodecs.optional(LastLocation.STREAM_CODEC))
            .build());

    public static final class Addon {
        private Addon() {}
        public static final Identifier KEEP_INVENTORY = Kryptonite.id("keep_inventory");
        public static final Identifier IMMEDIATE_RESPAWN = Kryptonite.id("immediate_respawn");
    }

}