package net.alkanphel.kryptonite.util.apoli;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;
import java.util.UUID;

public class MiscUtil {

    public static OptionalInt getSpaceInInventory(Player player, ItemStack stack) {
        return getSpaceInInventory(player.getInventory(), stack);
    }

    public static OptionalInt getSpaceInInventory(Inventory inventory, ItemStack stack) {
        int slot = inventory.getSlotWithRemainingSpace(stack);
        if (slot == -1) slot = inventory.getFreeSlot();

        return slot == -1 ? OptionalInt.empty() : OptionalInt.of(slot);
    }

    public static boolean hasSpaceInInventory(Player player, ItemStack stack) {
        return getSpaceInInventory(player, stack).isPresent();
    }

    public static boolean hasSpaceInInventory(Inventory playerInventory, ItemStack stack) {
        return getSpaceInInventory(playerInventory, stack).isPresent();
    }

    /**
     * Creates a stack reference that is not linked to any entity for use with item actions.
     * <p>
     * Recommended for usage when either you don't have an entity for this operation, or you don't want to set the entity's StackReference.
     *
     * @param startingStack The ItemStack that this reference will start with.
     * @return A {@linkplain SlotAccess} that contains an ItemStack.
     */
    public static SlotAccess createStackReference(ItemStack startingStack) {
        return new SlotAccess() {
            ItemStack stack = startingStack;

            @Override
            public ItemStack get() {
                return stack;
            }

            @Override
            public boolean set(ItemStack stack) {
                this.stack = stack;
                return true;
            }
        };
    }

    public static @Nullable Entity getEntityByUuid(@Nullable UUID uuid, @Nullable MinecraftServer server) {
        if (uuid == null || server == null) return null;

        Entity entity;
        for (ServerLevel serverLevel : server.getAllLevels()) {
            if ((entity = serverLevel.getEntity(uuid)) != null) {
                return entity;
            }
        }

        return null;
    }

}
