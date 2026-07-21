package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public class PocketCraftingTableAction extends Action {

    private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");

    public static final MapCodec<PocketCraftingTableAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("increment_stat", new StaticValue(true)).forGetter(a -> a.incrementStat)
    ).apply(instance, PocketCraftingTableAction::new));

    public final Value incrementStat;

    public PocketCraftingTableAction(Value incrementStat) {
        this.incrementStat = incrementStat;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (!(entity instanceof Player player)) return false;

        var level = player.level();
        if (level.isClientSide()) return false;
        if (player.containerMenu instanceof PocketCraftingTableMenu) return false;

        var blockPos = player.blockPosition();
        player.openMenu(menuProvider(level, blockPos));

        if (incrementStat.getAsBoolean(context)) {
            player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE, 1);
        }

        return true;
    }

    private MenuProvider menuProvider(Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, playerInv, player) -> new PocketCraftingTableMenu(id, playerInv, ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE);
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.POCKET_CRAFTING_TABLE.get();
    }

    public static class Serializer extends ActionSerializer<PocketCraftingTableAction> {

        @Override
        public MapCodec<PocketCraftingTableAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, PocketCraftingTableAction> builder, HolderLookup.Provider provider) {
            builder.setName("Pocket Crafting Table")
                    .setDescription("Allows the player to open an instance of the Crafting Table GUI at their location.")
                    .addOptional("increment_stat", TYPE_VALUE, "If the 'Interactions with Crafting Table' stat should be incremented.", true)
                    .addExampleObject(new PocketCraftingTableAction(new StaticValue(true)));
        }
    }

    public static class PocketCraftingTableMenu extends CraftingMenu {
        public PocketCraftingTableMenu(int id, Inventory playerInventory, ContainerLevelAccess blockContext) {
            super(id, playerInventory, blockContext);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

}