package net.alkanphel.kryptonite.power.logic.action;

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
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public class PocketAnvilAction extends Action {

    private static final Component CONTAINER_TITLE = Component.translatable("container.repair");

    public static final MapCodec<PocketAnvilAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("increment_stat", new StaticValue(true)).forGetter(a -> a.incrementStat)
    ).apply(instance, PocketAnvilAction::new));

    public final Value incrementStat;

    public PocketAnvilAction(Value incrementStat) {
        this.incrementStat = incrementStat;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (!(entity instanceof Player player)) return false;

        var level = player.level();
        if (level.isClientSide()) return false;
        if (player.containerMenu instanceof PocketAnvilMenu) return false;

        var blockPos = player.blockPosition();
        player.openMenu(menuProvider(level, blockPos));

        if (incrementStat.getAsBoolean(context)) {
            player.awardStat(Stats.INTERACT_WITH_ANVIL, 1);
        }

        return true;
    }

    private MenuProvider menuProvider(Level level, BlockPos pos) {
        return new SimpleMenuProvider(PocketAnvilMenu::new, CONTAINER_TITLE);
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.POCKET_ANVIL.get();
    }

    public static class Serializer extends ActionSerializer<PocketAnvilAction> {

        @Override
        public MapCodec<PocketAnvilAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, PocketAnvilAction> builder, HolderLookup.Provider provider) {
            builder.setName("Pocket Anvil")
                    .setDescription("Allows the player to open an instance of the Anvil GUI at their location.")
                    .addOptional("increment_stat", TYPE_VALUE, "If the 'Interactions with Anvil' stat should be incremented.", true)
                    .addExampleObject(new PocketAnvilAction(new StaticValue(true)));
        }
    }

    public static class PocketAnvilMenu extends AnvilMenu {
        public PocketAnvilMenu(int id, Inventory playerInventory, Player player) {
            super(id, playerInventory, ContainerLevelAccess.create(player.level(), player.blockPosition()));
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

}