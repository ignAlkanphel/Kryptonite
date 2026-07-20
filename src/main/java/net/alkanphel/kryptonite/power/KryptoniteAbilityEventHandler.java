package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.ability.*;
import net.alkanphel.kryptonite.util.apoli.BlockUsagePhase;
import net.alkanphel.kryptonite.util.apoli.SavedBlockPosition;
import net.alkanphel.kryptonite.util.apoli.ability.Prioritized;
import net.alkanphel.kryptonite.util.apoli.ability.PriorityPhase;
import net.alkanphel.kryptonite.util.apoli.access.BlockBreakDirectionHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@EventBusSubscriber(modid = Kryptonite.MOD_ID)
public class KryptoniteAbilityEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH) // Modify Damage Dealt ability
    public static void onLivingIncomingDamageDealt(LivingIncomingDamageEvent e) {
        DamageSource source = e.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker)) return;

        LivingEntity target = e.getEntity();
        float damage = e.getAmount();

        var instances = AbilityUtil.getEnabledInstances(attacker, KryptoniteAbilitySerializers.MODIFY_DAMAGE_DEALT.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApply(source, damage, attacker, target))
                .toList();

        if (instances.isEmpty()) return;

        float newDamage = damage;

        for (AbilityInstance<ModifyDamageDealtAbility> instance : instances) {
            newDamage = instance.getAbility().applyModifiers(newDamage, instance, attacker);
            instance.getAbility().runActions(attacker, target);
        }

        e.setAmount(newDamage);
    }

    @SubscribeEvent
    public static void onLivingIncomingDamageTaken(LivingIncomingDamageEvent e) {
        LivingEntity target = e.getEntity();
        DamageSource source = e.getSource();
        float amount = e.getAmount();

        // Prevent Damage ability
        for (AbilityInstance<PreventDamageAbility> instance : AbilityUtil.getEnabledInstances(target, KryptoniteAbilitySerializers.PREVENT_DAMAGE.get())) {
            if (PreventDamageAbility.isImmuneAgainst(instance, source, amount, target)) {
                e.setAmount(0);
                e.setCanceled(true);
                return; // return to prevent 'Modify Damage Taken'
            }
        }

        // Modify Damage Taken
        var modifyDamageTakenInstances = AbilityUtil.getEnabledInstances(target, KryptoniteAbilitySerializers.MODIFY_DAMAGE_TAKEN.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApply(source, amount, target))
                .toList();

        if (modifyDamageTakenInstances.isEmpty()) return;

        float newDamage = amount;
        Entity attacker = source.getEntity();

        for (AbilityInstance<ModifyDamageTakenAbility> instance : modifyDamageTakenInstances) {
            newDamage = instance.getAbility().applyModifiers(newDamage, instance, target);
            instance.getAbility().runActions(attacker, target);
        }

        e.setAmount(newDamage);
    }

    @SubscribeEvent // Modify Damage Taken ability (armor part)
    public static void onArmorHurt(ArmorHurtEvent e) {
        LivingEntity target = e.getEntity();

        var instances = AbilityUtil.getEnabledInstances(target, KryptoniteAbilitySerializers.MODIFY_DAMAGE_TAKEN.get())
                .stream()
                .filter(instance -> instance.getAbility().modifiesArmorDamaging())
                .toList();

        if (instances.isEmpty()) return;

        long wantDamage = instances.stream().filter(instance -> instance.getAbility().shouldDamageArmor(target)).count();
        long doNotWantDamage = instances.stream().filter(instance -> !instance.getAbility().shouldDamageArmor(target)).count();

        if (wantDamage == doNotWantDamage) return;

        if (doNotWantDamage > wantDamage) {
            e.setCanceled(true);
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
    public static void onLivingDamagePost(LivingDamageEvent.Post e) {
        LivingEntity target = e.getEntity();
        Entity attacker = e.getSource().getEntity();
        float amount = e.getInflictedDamage();

        // Action On Hit ability (attacker has the ability)
        if (attacker instanceof LivingEntity livingAttacker) {
            AbilityUtil.getEnabledInstances(livingAttacker, KryptoniteAbilitySerializers.ACTION_ON_HIT.get())
                    .stream()
                    .filter(instance -> instance.getAbility().doesApply(livingAttacker, target, e.getSource(), amount))
                    .forEach(instance -> instance.getAbility().onHit(livingAttacker, target));
        }

        // Action When Hit ability (target has the ability)
        if (attacker != null) {
            AbilityUtil.getEnabledInstances(target, KryptoniteAbilitySerializers.ACTION_WHEN_HIT.get())
                    .stream()
                    .filter(instance -> instance.getAbility().doesApply(attacker, target, e.getSource(), amount))
                    .forEach(instance -> instance.getAbility().whenHit(attacker, target));
        }

        // Action When Damage Taken ability (target has the ability, no specified attacker)
        AbilityUtil.getEnabledInstances(target, KryptoniteAbilitySerializers.ACTION_WHEN_DAMAGE_TAKEN.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApply(e.getSource(), amount))
                .forEach(instance -> instance.getAbility().whenHit(target));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCriticalHit(CriticalHitEvent e) {
        Player player = e.getEntity();
        Entity target = e.getTarget();

        // Prevent Critical Hit
        if (PreventCriticalHitAbility.shouldPreventCrit(player, target)) {
            e.setCriticalHit(false);
            e.setDamageMultiplier(1.0F);
            return; // stops actions running for 'Action On Critical Hit'
        }

        // Action On Critical Hit
        if (!(player instanceof ServerPlayer serverPlayer) || !e.isCriticalHit()) return;
        AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.ACTION_ON_CRITICAL_HIT.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApply(serverPlayer, target))
                .forEach(instance -> instance.getAbility().runActions(serverPlayer, target));
    }

    @SubscribeEvent // Modify Invulnerability Ticks ability
    public static void onLivingIncomingDamageHurtTicks(LivingIncomingDamageEvent e) {
        LivingEntity target = e.getEntity();
        Entity attacker = e.getSource().getEntity();
        float amount = e.getAmount();

        // Modify Invulnerability Ticks ability (SELF)
        AbilityUtil.getEnabledInstances(target, KryptoniteAbilitySerializers.MODIFY_INVULNERABILITY_TICKS.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApplyToSelf(e.getSource(), amount))
                .forEach(instance -> {
                    instance.getAbility().applyModifiers(e, DataContext.forAbility(target, instance));
                    instance.getAbility().runActions(attacker, target);
                });

        // Modify Invulnerability Ticks ability (OTHER)
        if (attacker instanceof LivingEntity livingAttacker) {
            AbilityUtil.getEnabledInstances(livingAttacker, KryptoniteAbilitySerializers.MODIFY_INVULNERABILITY_TICKS.get())
                    .stream()
                    .filter(instance -> instance.getAbility().doesApplyToTarget(livingAttacker, target, e.getSource(), amount))
                    .forEach(instance -> {
                        instance.getAbility().applyModifiers(e, DataContext.forAbility(livingAttacker, instance));
                        instance.getAbility().runActions(livingAttacker, target);
                    });
        }
    }

    @SubscribeEvent // Modify Knockback ability
    public static void onLivingKnockback(LivingKnockBackEvent e) {
        LivingEntity entity = e.getEntity();

        var instances = AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.MODIFY_KNOCKBACK.get());
        if (instances.isEmpty()) return;

        float strength = e.getStrength();
        double ratioX = e.getRatioX();
        double ratioZ = e.getRatioZ();

        for (AbilityInstance<ModifyKnockbackAbility> instance : instances) {
            strength = instance.getAbility().applyStrength(strength, entity, instance);
            ratioX = instance.getAbility().applyRatioX(ratioX, entity, instance);
            ratioZ = instance.getAbility().applyRatioZ(ratioZ, entity, instance);
            instance.getAbility().runActions(entity);
        }

        e.setStrength(strength);
        e.setRatioX(ratioX);
        e.setRatioZ(ratioZ);
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

    @SubscribeEvent(priority = EventPriority.LOW) // Modify Healing ability
    public static void onLivingHealModify(LivingHealEvent e) {
        LivingEntity entity = e.getEntity();
        float amount = e.getAmount();

        var instances = AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.MODIFY_HEALING.get());
        if (instances.isEmpty()) return;

        float newAmount = amount;

        for (var instance : instances) {
            newAmount = instance.getAbility().applyModifiers(newAmount, instance, entity);
        }

        e.setAmount(Math.max(0F, newAmount));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onLivingHealPrevent(LivingHealEvent e) { // Prevent Healing ability
        for (AbilityInstance<PreventHealingAbility> instance : AbilityUtil.getEnabledInstances(e.getEntity(), KryptoniteAbilitySerializers.PREVENT_HEALING.get())) {
            if (PreventHealingAbility.isFullPrevention(e.getEntity(), instance)) {
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

    @SubscribeEvent // Action On Item Use ability (instant trigger)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem e) {
        Player holder = e.getEntity();
        ItemStack stack = e.getItemStack();

        if (stack.getUseDuration(holder) == 0) {
            ActionOnItemUseAbility.run(holder, stack, EnumSet.of(ActionOnItemUseAbility.TriggerType.INSTANT), PriorityPhase.BEFORE);
        }
    }

    @SubscribeEvent // Action On Item Use ability (start trigger)
    public static void onUseItemStart(LivingEntityUseItemEvent.Start e) {
        LivingEntity holder = e.getEntity();
        ItemStack stack = e.getItem();

        ActionOnItemUseAbility.run(holder, stack, EnumSet.of(ActionOnItemUseAbility.TriggerType.START), PriorityPhase.BEFORE);

        if (!e.isCanceled() && e.getDuration() > 0) {
            ActionOnItemUseAbility.run(holder, stack, EnumSet.of(ActionOnItemUseAbility.TriggerType.START), PriorityPhase.AFTER);
        }
    }

    @SubscribeEvent // Action On Item Use ability (tick trigger)
    public static void onUseItemTick(LivingEntityUseItemEvent.Tick e) {
        LivingEntity holder = e.getEntity();
        ItemStack stack = e.getItem();

        ActionOnItemUseAbility.run(holder, stack, EnumSet.of(ActionOnItemUseAbility.TriggerType.TICK), PriorityPhase.BEFORE);

        if (!e.isCanceled() && e.getDuration() > 0) {
            ActionOnItemUseAbility.run(holder, stack, EnumSet.of(ActionOnItemUseAbility.TriggerType.TICK), PriorityPhase.AFTER);
        }
    }

    @SubscribeEvent // Action On Item Use ability (stop trigger)
    public static void onUseItemStop(LivingEntityUseItemEvent.Stop e) {
        LivingEntity holder = e.getEntity();
        ItemStack stack = e.getItem();

        ActionOnItemUseAbility.run(holder, stack, EnumSet.of(ActionOnItemUseAbility.TriggerType.STOP), PriorityPhase.BEFORE);

        if (!e.isCanceled()) {
            ActionOnItemUseAbility.run(holder, stack, EnumSet.of(ActionOnItemUseAbility.TriggerType.STOP), PriorityPhase.AFTER);
        }
    }

    @SubscribeEvent // Action On Item Use ability (finish trigger)
    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish e) {
        LivingEntity holder = e.getEntity();
        ItemStack stack = e.getItem();

        ActionOnItemUseAbility.run(holder, stack, EnumSet.of(ActionOnItemUseAbility.TriggerType.FINISH), PriorityPhase.BEFORE);
        ActionOnItemUseAbility.run(holder, stack, EnumSet.of(ActionOnItemUseAbility.TriggerType.FINISH), PriorityPhase.AFTER);
    }


    // ------------------------------------------------------------------------------------------------------------------------


    @SubscribeEvent // Modify Block Harvest ability
    public static void onPlayerHarvestCheck(PlayerEvent.HarvestCheck e) {
        LivingEntity living = e.getEntity();

        var savedBlockPosition = new SavedBlockPosition(living.level(), e.getPos(), e.getTargetBlock(), living.level().getBlockEntity(e.getPos()));
        ModifyBlockHarvestAbility.resolve(living, savedBlockPosition).ifPresent(e::setCanHarvest);
    }

    @SubscribeEvent // Prevent Block Use ability
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        Player player = e.getEntity();
        InteractionHand hand = e.getHand();
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = e.getHitVec();

        BlockUsagePhase phase = (e.getUseBlock() == TriState.FALSE) ? BlockUsagePhase.ITEM : BlockUsagePhase.BLOCK;

        Prioritized.CallInstance<PreventBlockUseAbility> instance = new Prioritized.CallInstance<>();
        instance.add(player, PreventBlockUseAbility.class, a -> a.doesPrevent(player, phase, hitResult, stack, hand));

        if (instance.isEmpty()) return;

        PreventBlockUseAbility ability = instance.getAllAbilities().getFirst();
        ability.runActions(player, hitResult, hand);

        e.setUseBlock(TriState.FALSE);
        e.setUseItem(TriState.FALSE);
        e.setCancellationResult(InteractionResult.FAIL);
        e.setCanceled(true);
    }

    @SubscribeEvent // Action On Block Use ability
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock e) {
        Player player = e.getEntity();

        InteractionHand hand = e.getHand();
        ItemStack heldStack = player.getItemInHand(hand);
        BlockHitResult hitResult = e.getHitVec();
        DataContext context = DataContext.forEntity(player);

        BlockUsagePhase usePhase = (e.getUseBlock() != TriState.FALSE) ? BlockUsagePhase.BLOCK : BlockUsagePhase.ITEM;

        // before phase
        Prioritized.CallInstance<ActionOnBlockUseAbility> beforeInstances = new Prioritized.CallInstance<>();
        beforeInstances.add(player, ActionOnBlockUseAbility.class, a -> a.shouldRun(context, usePhase, PriorityPhase.BEFORE, hand, heldStack, hitResult));

        InteractionResult beforeResult = ActionOnBlockUseAbility.interactionResultHelper(beforeInstances, context, hitResult, hand, player);
        if (beforeResult != InteractionResult.PASS) {
            e.setCancellationResult(beforeResult);
            e.setCanceled(true);
            return;
        }

        // after phase
        if (e.getUseBlock() == TriState.DEFAULT && e.getUseItem() == TriState.DEFAULT) {
            Prioritized.CallInstance<ActionOnBlockUseAbility> afterInstances = new Prioritized.CallInstance<>();
            afterInstances.add(player, ActionOnBlockUseAbility.class, a -> a.shouldRun(context, usePhase, PriorityPhase.AFTER, hand, heldStack, hitResult));

            InteractionResult afterResult = ActionOnBlockUseAbility.interactionResultHelper(afterInstances, context, hitResult, hand, player);
            if (afterResult != InteractionResult.PASS) {
                e.setCancellationResult(afterResult);
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent // Action On Block Break ability
    public static void onBreak(BreakBlockEvent e) {
        Player player = e.getPlayer();
        if (!(e.getLevel() instanceof Level level)) return;

        BlockPos pos = e.getPos();
        BlockState state = e.getState();

        SavedBlockPosition saved = new SavedBlockPosition(level, pos, state, level.getBlockEntity(pos));

        boolean harvested = state.canHarvestBlock(level, pos, player);
        Direction direction = Direction.UP; // fallback

        if (player instanceof ServerPlayer serverPlayer && serverPlayer.gameMode instanceof BlockBreakDirectionHolder holder) {
            Direction captured = holder.kryptonite$getDirection();
            if (captured != null) direction = captured;
        }

        Direction finalDirection = direction;

        AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.ACTION_ON_BLOCK_BREAK.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApply(saved, harvested))
                .forEach(instance -> instance.getAbility().runActions(player, pos, finalDirection));
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


    // ------------------------------------------------------------------------------------------------------------------------


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

    @SubscribeEvent // Prevent Mob Aggro ability
    public static void onLivingChangeTarget(LivingChangeTargetEvent e) {
        LivingEntity newTarget = e.getNewAboutToBeSetTarget();

        if (newTarget == null) return;
        if (!(e.getEntity() instanceof Mob mob)) return;

        if (PreventMobAggroAbility.shouldIgnore(newTarget, mob)) {
            e.setCanceled(true);
        }

        if (mob.getTarget() != null && PreventMobAggroAbility.shouldAggroReset(newTarget, mob)) {
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH) // Prevent Teleport ability (dimension travel)
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent e) {
        if (!(e.getEntity() instanceof LivingEntity living)) return;

        if (PreventTeleportAbility.doesPreventDimensionTravel(living, e.getDimension())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH) // Prevent Teleport ability
    public static void onTeleport(EntityTeleportEvent e) {
        if (!(e.getEntity() instanceof LivingEntity living)) return;
        PreventTeleportAbility.Source source;

        switch (e) {
            case EntityTeleportEvent.TeleportCommand _ -> source = PreventTeleportAbility.Source.COMMAND_TELEPORT;
            case EntityTeleportEvent.SpreadPlayersCommand _ -> source = PreventTeleportAbility.Source.COMMAND_SPREAD_PLAYERS;
            case EntityTeleportEvent.EnderPearl _ -> source = PreventTeleportAbility.Source.ENDER_PEARL;
            case EntityTeleportEvent.EnderEntity _ -> source = PreventTeleportAbility.Source.ENDER_ENTITY;
            case EntityTeleportEvent.ItemConsumption _ -> source = PreventTeleportAbility.Source.ITEM_CONSUMPTION;
            default -> {
                return;
            }
        }

        AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.PREVENT_TELEPORT.get())
                .stream()
                .filter(instance -> instance.getAbility().source == source)
                .filter(instance -> instance.getAbility().doesApply(living, e.getTargetLevel(), e.getTarget()))
                .forEach(instance -> {
                    instance.getAbility().runActions(living);
                    e.setCanceled(true);
                });
    }

    @SubscribeEvent // Prevent Effect ability
    public static void onEffect(MobEffectEvent.Applicable e) {
        for (AbilityInstance<PreventEffectsAbility> instance : AbilityUtil.getEnabledInstances(e.getEntity(), KryptoniteAbilitySerializers.PREVENT_EFFECTS.get())) {
            if (PreventEffectsAbility.isImmuneTo(e.getEntity(), instance, e.getEffectInstance().getEffect())) {
                e.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
                return;
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

    @SubscribeEvent // Prevent Sleeping ability
    public static void onCanSleep(CanPlayerSleepEvent e) {
        ServerPlayer player = e.getEntity();
        BlockPos bedPos = e.getPos();

        if (PreventSleepingAbility.isSleepPrevented(player, bedPos)) {
            String message = PreventSleepingAbility.getSleepMessage(player, bedPos);
            player.sendOverlayMessage(Component.literal(message));
            e.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }

    @SubscribeEvent // Prevent Sleeping ability (spawnpoint)
    public static void onSetSpawn(PlayerSetSpawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer player)) return;
        BlockPos bedPos = e.getNewSpawn() != null ? e.getNewSpawn() : player.blockPosition();

        if (PreventSleepingAbility.isSpawnPrevented(player, bedPos)) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent // Action On Jump
    public static void onLivingJump(LivingEvent.LivingJumpEvent e) {
        LivingEntity entity = e.getEntity();

        AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.ACTION_ON_JUMP.get())
                .forEach(instance -> instance.getAbility().runActions(entity));
    }

    @SubscribeEvent // Action On Mount ability
    public static void onEntityMount(EntityMountEvent e) {
        if (!(e.getEntityMounting() instanceof LivingEntity rider)) return;

        Entity vehicle = e.getEntityBeingMounted();
        boolean dismounting = e.isDismounting();

        AbilityUtil.getEnabledInstances(rider, KryptoniteAbilitySerializers.ACTION_ON_MOUNT.get())
                .stream()
                .filter(instance -> instance.getAbility().switchToDismount == dismounting)
                .filter(instance -> instance.getAbility().doesApply(rider, vehicle))
                .forEach(instance -> instance.getAbility().runActions(rider, vehicle));
    }

    @SubscribeEvent // Action On Tame ability
    public static void onAnimalTame(AnimalTameEvent e) {
        Player tamer = e.getTamer();

        AbilityUtil.getEnabledInstances(tamer, KryptoniteAbilitySerializers.ACTION_ON_TAME.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApply(tamer, e.getAnimal()))
                .forEach(instance -> instance.getAbility().runActions(tamer, e.getAnimal()));
    }

    @SubscribeEvent // Action On Wake Up ability
    public static void onPlayerWakeUp(PlayerWakeUpEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer player)) return;
        if (e.wakeImmediately() || e.updateLevel()) return;

        player.getSleepingPos().ifPresent(pos -> AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.ACTION_ON_WAKE_UP.get())
                .stream()
                .filter(instance -> instance.getAbility().doesApply(pos, player))
                .forEach(instance -> instance.getAbility().runActions(pos, Direction.DOWN, player))
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH) // Allow Enderman Stare ability
    public static void onEnderManAnger(EnderManAngerEvent e) {
        if (!(e.getPlayer() instanceof ServerPlayer player)) return;

        var enderman = e.getEntity();

        for (AbilityInstance<AllowEndermanStareAbility> instance : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.ALLOW_ENDERMAN_STARE.get())) {
            if (instance.getAbility().appliesTo(player, enderman)) {
                e.setCanceled(true);
                return;
            }
        }
    }

}