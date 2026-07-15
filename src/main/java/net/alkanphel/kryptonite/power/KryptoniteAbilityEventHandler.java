package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.ability.PreventDeathAbility;
import net.alkanphel.kryptonite.power.ability.PreventTotemUseAbility;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.threetag.palladium.power.ability.AbilityUtil;

@EventBusSubscriber(modid = Kryptonite.MOD_ID)
public class KryptoniteAbilityEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDeath(LivingDeathEvent e) {
        LivingEntity target = e.getEntity();
        DamageSource source = e.getSource();
        float damage = target.getMaxHealth();

        // Prevent Death ability
        if (PreventDeathAbility.doesPrevent(target, source, damage)) {
            target.setHealth(1.0F);
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingUseTotem(LivingUseTotemEvent e) {
        LivingEntity entity = e.getEntity();
        DamageSource source = e.getSource();
        float damage = entity.getMaxHealth();
        InteractionHand hand = e.getHandHolding();

        // Prevent Totem Use ability
        if (PreventTotemUseAbility.doesPrevent(entity, source, damage, hand)) {
            e.setCanceled(true);
            return; // stops actions running for 'Action On Totem Use'
        }

        // Action On Totem Use ability
        AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.ACTION_ON_TOTEM_USE.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApply(source, damage, hand))
                .forEach(instance -> instance.getAbility().runActions(entity));
    }

}