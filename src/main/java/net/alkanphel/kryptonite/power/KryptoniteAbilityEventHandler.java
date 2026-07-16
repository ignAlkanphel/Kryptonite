package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.ability.*;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = Kryptonite.MOD_ID)
public class KryptoniteAbilityEventHandler {

    @SubscribeEvent // Projectile Impact ability
    public static void onProjectileImpact(ProjectileImpactEvent e) {
        if (!(e.getRayTraceResult() instanceof EntityHitResult result)) return;
        if (!(result.getEntity() instanceof LivingEntity holder)) return;

        Projectile projectile = e.getProjectile();

        var instances = AbilityUtil.getEnabledInstances(holder, KryptoniteAbilitySerializers.PROJECTILE_IMPACT.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApply(holder, projectile))
                .toList();

        if (instances.isEmpty()) return;

        for (AbilityInstance<ProjectileImpactAbility> instance : instances) {
            instance.getAbility().runActions(holder, projectile);

            switch (instance.getAbility().impactResult) {
                case IGNORE -> e.setCanceled(true);
                case DISCARD -> {
                    e.setCanceled(true);
                    projectile.discard();
                }
                case DEFAULT -> {}
            }
        }
    }

    @SubscribeEvent // Projectile Accuracy ability
    public static void onEntityJoinLevel(EntityJoinLevelEvent e) {
        if (!(e.getEntity() instanceof Projectile projectile)) return;
        if (!(projectile.getOwner() instanceof ServerPlayer player)) return;

        var instances = AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PROJECTILE_ACCURACY.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApply(player, projectile))
                .toList();

        if (instances.isEmpty()) return;

        Vec3 delta = projectile.getDeltaMovement();
        if (delta.lengthSqr() <= 0) return;

        double speed = delta.length();
        Vec3 look = player.getLookAngle().normalize().scale(speed);
        projectile.setDeltaMovement(look);
    }


    // ------------------------------------------------------------------------------------------------------------------------


    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingIncomingDamageTaken(LivingIncomingDamageEvent e) {
        LivingEntity target = e.getEntity();
        DamageSource source = e.getSource();
        float amount = e.getAmount();

        // Prevent Damage ability
        for (AbilityInstance<PreventDamageAbility> instance : AbilityUtil.getEnabledInstances(target, KryptoniteAbilitySerializers.PREVENT_DAMAGE.get())) {
            if (PreventDamageAbility.isImmuneAgainst(instance, source, amount, target)) {
                e.setAmount(0);
                e.setCanceled(true);
                return;
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDeath(LivingDeathEvent e) {
        LivingEntity target = e.getEntity();
        DamageSource source = e.getSource();
        Entity attacker = source.getEntity();
        float damage = target.getMaxHealth();

        // Prevent Death ability
        if (PreventDeathAbility.doesPrevent(target, source, damage)) {
            target.setHealth(1.0F);
            e.setCanceled(true);
            return; // stops actions running for 'Action On Death'
        }

        // Action On Death ability
        AbilityUtil.getEnabledInstances(target, KryptoniteAbilitySerializers.ACTION_ON_DEATH.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApply(attacker, target, source, damage))
                .forEach(instance -> instance.getAbility().onDeath(attacker, target));
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onLivingHeal(LivingHealEvent e) { // Prevent Healing ability
        for (AbilityInstance<PreventHealingAbility> instance : AbilityUtil.getEnabledInstances(e.getEntity(), KryptoniteAbilitySerializers.PREVENT_HEALING.get())) {
            if (PreventHealingAbility.isFullPrevention(instance)) {
                e.setAmount(0);
                e.setCanceled(true);
                return;
            }
        }
    }


    // ------------------------------------------------------------------------------------------------------------------------


    @SubscribeEvent // Action On Item Drop ability
    public static void onItemToss(ItemTossEvent e) {
        LivingEntity living = e.getPlayer();
        if (living.level().isClientSide()) return;

        ItemStack stack = e.getEntity().getItem();
        SlotAccess slotAccess = SlotAccess.of(() -> stack, s -> e.getEntity().setItem(s));

        AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.ACTION_ON_ITEM_DROP.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApply(living, stack))
                .forEach(instance -> instance.getAbility().runActions(living, slotAccess));
    }

    @SubscribeEvent // Action On Item Swap ability
    public static void onItemSwap(LivingSwapItemsEvent.Hands e) {
        LivingEntity holder = e.getEntity();
        if (holder.level().isClientSide()) return;

        ItemStack mainStack = e.getItemSwappedToMainHand();
        ItemStack offStack = e.getItemSwappedToOffHand();

        SlotAccess mainReference = SlotAccess.of(e::getItemSwappedToMainHand, e::setItemSwappedToMainHand);
        SlotAccess offReference = SlotAccess.of(e::getItemSwappedToOffHand, e::setItemSwappedToOffHand);

        AbilityUtil.getEnabledInstances(holder, KryptoniteAbilitySerializers.ACTION_ON_ITEM_SWAP.get())
                .stream()
                .map(AbilityInstance::getAbility)
                .filter(ability -> ability.doesApply(holder, mainStack, offStack))
                .forEach(ability -> ability.runActions(holder, mainReference, offReference));
    }

    @SubscribeEvent // Action On Item Fished ability
    public static void onItemFished(ItemFishedEvent e) {
        Player player = e.getEntity();
        NonNullList<@NotNull ItemStack> drops = e.getDrops();

        for (int i = 0; i < drops.size(); i++) {
            ItemStack stack = drops.get(i);
            SlotAccess slotAccess = SlotAccess.forListElement(drops, i);

            AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.ACTION_ON_ITEM_FISHED.get())
                    .stream()
                    .map(AbilityInstance::getAbility)
                    .filter(ability -> ability.doesApply(player, stack))
                    .forEach(ability -> ability.runActions(player, slotAccess));
        }
    }


    // ------------------------------------------------------------------------------------------------------------------------


    @SubscribeEvent // Action On Jump
    public static void onLivingJump(LivingEvent.LivingJumpEvent e) {
        LivingEntity entity = e.getEntity();

        AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.ACTION_ON_JUMP.get())
                .forEach(instance -> instance.getAbility().runActions(entity));
    }

    @SubscribeEvent
    public static void onBlockFarmlandTrample(BlockEvent.FarmlandTrampleEvent e) {
        if (!(e.getEntity() instanceof LivingEntity living)) return;
        if (!(e.getLevel() instanceof Level level)) return;

        // Prevent Farmland Trample ability
        for (AbilityInstance<PreventFarmlandTrampleAbility> instance : AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.PREVENT_FARMLAND_TRAMPLE.get())) {
            if (instance.getAbility().doesPrevent(level, e.getPos())) {
                e.setCanceled(true);
                return; // stops actions running for 'Action On Farmland Trample'
            }
        }

        // Action On Farmland Trample ability
        for (AbilityInstance<ActionOnFarmlandTrampleAbility> instance : AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.ACTION_ON_FARMLAND_TRAMPLE.get())) {
            if (instance.getAbility().doesApply(level, e.getPos())) {
                instance.getAbility().runActions(living, level, e.getPos());
            }
        }
    }

    @SubscribeEvent // Prevent Game Event ability
    public static void onVanillaGameEvent(VanillaGameEvent e) {
        Entity cause = e.getCause();
        if (!(cause instanceof LivingEntity living)) return;

        var instances = AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.PREVENT_GAME_EVENT.get())
                .stream()
                .map(AbilityInstance::getAbility)
                .filter(ability -> ability.doesPrevent(living, e.getVanillaEvent()))
                .toList();

        if (instances.isEmpty()) return;

        instances.forEach(ability -> ability.runActions(living));
        e.setCanceled(true);
    }

}