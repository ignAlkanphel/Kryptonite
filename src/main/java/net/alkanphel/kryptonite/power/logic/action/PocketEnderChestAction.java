package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.Codec;
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

public class PocketEnderChestAction extends Action {

    private static final Component CONTAINER_TITLE = Component.translatable("container.enderchest");

    public static final MapCodec<PocketEnderChestAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("increment_stat", true).forGetter(a -> a.incrementStat),
            Value.CODEC.optionalFieldOf("anger_piglins", true).forGetter(a -> a.angerPiglins)
    ).apply(instance, PocketEnderChestAction::new));

    public final Value incrementStat, angerPiglins;

    public PocketEnderChestAction(Value incrementStat, Value angerPiglins) {
        this.incrementStat = incrementStat;
        this.angerPiglins = angerPiglins;
    }

    @Override
    public boolean run(DataContext context) {
        Entity entity = context.getEntity();
        if (!(entity instanceof Player player)) return false;

        PlayerEnderChestContainer enderChest = player.getEnderChestInventory();

        player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> ChestMenu.threeRows(id, inventory, enderChest),
                CONTAINER_TITLE
        ));

        if (incrementStat.getAsBoolean(context)) {
            player.awardStat(Stats.OPEN_ENDERCHEST, 1);
        }

        if (angerPiglins.getAsBoolean(context) && player.level() instanceof ServerLevel serverLevel) {
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
                    .addOptional("increment_stat", TYPE_VALUE, "If the 'Ender Chests Opened' stat should be incremented.", true)
                    .addOptional("anger_piglins", TYPE_VALUE, "If nearby Piglins should be angered. This is vanilla behaviour and also happens opening Shulker Boxes.", true)
                    .addExampleObject(new PocketEnderChestAction(new StaticValue(true), new StaticValue(false)));
        }
    }

}