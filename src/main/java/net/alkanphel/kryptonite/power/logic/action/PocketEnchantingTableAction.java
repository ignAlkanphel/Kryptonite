package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class PocketEnchantingTableAction extends Action {

    private static final Component CONTAINER_TITLE = Component.translatable("container.repair");

    public static final MapCodec<PocketEnchantingTableAction> CODEC = MapCodec.unit(new PocketEnchantingTableAction());
    public static final StreamCodec<RegistryFriendlyByteBuf, PocketEnchantingTableAction> STREAM_CODEC = StreamCodec.unit(new PocketEnchantingTableAction());

    public PocketEnchantingTableAction() {}

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (!(entity instanceof Player player)) return false;

        var level = player.level();
        if (level.isClientSide()) return false;
        if (player.containerMenu instanceof PocketEnchantingTableMenu) return false;

        var blockPos = player.blockPosition();
        player.openMenu(menuProvider(level, blockPos));

        return true;
    }

    private MenuProvider menuProvider(Level level, BlockPos pos) {
        return new SimpleMenuProvider(PocketEnchantingTableMenu::new, CONTAINER_TITLE);
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.POCKET_ENCHANTING_TABLE.get();
    }

    public static class Serializer extends ActionSerializer<PocketEnchantingTableAction> {

        @Override
        public MapCodec<PocketEnchantingTableAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, PocketEnchantingTableAction> builder, HolderLookup.Provider provider) {
            builder.setName("Pocket Enchanting Table")
                    .setDescription("Allows the player to open an instance of the Enchanting Table GUI at their location.")
                    .addExampleObject(new PocketEnchantingTableAction());
        }
    }

    public static class PocketEnchantingTableMenu extends EnchantmentMenu {
        public PocketEnchantingTableMenu(int id, Inventory playerInventory, Player player) {
            super(id, playerInventory, ContainerLevelAccess.create(player.level(), player.blockPosition()));
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

}