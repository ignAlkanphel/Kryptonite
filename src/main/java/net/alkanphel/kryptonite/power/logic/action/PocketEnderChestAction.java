package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.logic.value.holder.BooleanValueHolder;

public class PocketEnderChestAction extends Action {

    private static final Component CONTAINER_TITLE = Component.translatable("container.enderchest");

    public static final MapCodec<PocketEnderChestAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BooleanValueHolder.CODEC.optionalFieldOf("increment_stat", BooleanValueHolder.TRUE).forGetter(a -> a.incrementStat),
            BooleanValueHolder.CODEC.optionalFieldOf("anger_piglins", BooleanValueHolder.TRUE).forGetter(a -> a.angerPiglins)
    ).apply(instance, PocketEnderChestAction::new));

    public final BooleanValueHolder incrementStat, angerPiglins;

    public PocketEnderChestAction(BooleanValueHolder incrementStat, BooleanValueHolder angerPiglins) {
        this.incrementStat = incrementStat;
        this.angerPiglins = angerPiglins;
    }

    @Override
    protected boolean execute(DataContext context) {
        Entity entity = context.getEntity();
        if (!(entity instanceof Player player)) return false;

        PlayerEnderChestContainer enderChest = player.getEnderChestInventory();

        player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> ChestMenu.threeRows(id, inventory, enderChest),
                CONTAINER_TITLE
        ));

        if (incrementStat.get(context)) {
            player.awardStat(Stats.OPEN_ENDERCHEST, 1);
        }

        if (angerPiglins.get(context) && player.level() instanceof ServerLevel serverLevel) {
            PiglinAi.angerNearbyPiglins(serverLevel, player, true);
        }

        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.POCKET_ENDER_CHEST.get();
    }

    public static class Serializer extends ActionSerializer<PocketEnderChestAction> {

        @Override
        public MapCodec<PocketEnderChestAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, PocketEnderChestAction> builder, HolderLookup.Provider provider) {
            builder.setName("Pocket Ender Chest")
                    .setDescription("Allows the player to open an instance of the Ender Chest GUI at their location.")
                    .addOptional("increment_stat", TYPE_BOOLEAN_VALUE, "If true, the \"Ender Chests Opened\" stat will be incremented.", true)
                    .addOptional("anger_piglins", TYPE_BOOLEAN_VALUE, "If true, nearby Piglins will be angered. This is default game behaviour and also happens opening Shulker Boxes.", true)
                    .addExampleObject(new PocketEnderChestAction(BooleanValueHolder.TRUE, BooleanValueHolder.FALSE));
        }
    }

}