package net.alkanphel.kryptonite.power.compat.curios;

import net.alkanphel.kryptonite.power.KryptoniteAttachments;
import net.alkanphel.kryptonite.util.AttachmentUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.event.DropRulesEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class KryptoniteCuriosCompatImpl extends KryptoniteCuriosCompat {

    private static final List<KeepRule> KEEP_RULES = new ArrayList<>();
    private record KeepRule(LivingEntity entity, List<String> slots, Predicate<ItemStack> predicate) {}

    public static void register() {
        KryptoniteCuriosCompat.INSTANCE = new KryptoniteCuriosCompatImpl();
        NeoForge.EVENT_BUS.addListener(DropRulesEvent.class, KryptoniteCuriosCompatImpl::onPreventDropping);
        NeoForge.EVENT_BUS.addListener(DropRulesEvent.class, KryptoniteCuriosCompatImpl::keepInventoryAttachment);
    }

    @Override
    public void preventDropping(LivingEntity entity, List<String> slots, Predicate<ItemStack> predicate) {
        KEEP_RULES.add(new KeepRule(entity, slots, predicate));
    }

    private static void onPreventDropping(DropRulesEvent event) {
        KEEP_RULES.removeIf(rule -> rule.entity().isRemoved());

        for (KeepRule rule : KEEP_RULES) {
            if (rule.entity() != event.getEntity()) continue;

            for (String slot : rule.slots()) {
                event.getCurioHandler().getStacksHandler(slot).ifPresent(stacksHandler -> {
                    var stacks = stacksHandler.getStacks();

                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stored = stacks.getStackInSlot(i);

                        if (stored.isEmpty() || !rule.predicate().test(stored)) {
                            continue;
                        }

                        event.addOverride(stack -> ItemStack.matches(stack, stored), DropRule.ALWAYS_KEEP);
                    }
                });
            }
        }
    }

    private static void keepInventoryAttachment(DropRulesEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!AttachmentUtil.getBoolean(player, KryptoniteAttachments.Addon.KEEP_INVENTORY)) return;

        event.getCurioHandler().getCurios().forEach((id, stacksHandler) -> {
            var stacks = stacksHandler.getStacks();

            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack stored = stacks.getStackInSlot(i);

                if (!stored.isEmpty()) {
                    event.addOverride(stack -> ItemStack.matches(stack, stored), DropRule.ALWAYS_KEEP);
                }
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------------------------

    @Override
    public List<String> getSlots(Level level) {
        return CuriosSlotTypes.getSlotTypes(level.isClientSide()).keySet().stream().toList();
    }

    @Override
    public List<ItemStack> getFromSlot(LivingEntity entity, String slot) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.getStacksHandler(slot))
                .map(stacksHandler -> {
                    var stacks = stacksHandler.getStacks();
                    List<ItemStack> result = new ArrayList<>();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        result.add(stacks.getStackInSlot(i));
                    }
                    return result;
                })
                .orElse(Collections.emptyList());
    }

    @Override
    public ItemStack getFromSlot(LivingEntity entity, String slot, int index) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.getStacksHandler(slot))
                .map(stacksHandler -> stacksHandler.getStacks().getStackInSlot(index))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public void setInSlot(LivingEntity entity, String slot, int index, ItemStack stack) {
        CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.getStacksHandler(slot))
                .ifPresent(stacksHandler -> stacksHandler.getStacks().setStackInSlot(index, stack));
    }

    @Override
    public int getSlotSize(LivingEntity entity, String slot) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.getStacksHandler(slot))
                .map(stacksHandler -> stacksHandler.getStacks().getSlots())
                .orElse(0);
    }

    @Override
    public void clearSlot(LivingEntity entity, String slot) {
        CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.getStacksHandler(slot))
                .ifPresent(stacksHandler -> {
                    var stacks = stacksHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        stacks.setStackInSlot(i, ItemStack.EMPTY);
                    }
                });
    }

}