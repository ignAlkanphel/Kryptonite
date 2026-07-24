package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public class SwitchHotbarSlotAction extends Action { // TODO Keep this in?

    public static final MapCodec<SwitchHotbarSlotAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.fieldOf("slot").forGetter(ab -> ab.slot)
    ).apply(instance, SwitchHotbarSlotAction::new));

    public final Value slot;

    public SwitchHotbarSlotAction(Value slot) {
        this.slot = slot;
    }

    @Override
    public boolean run(DataContext context) {
        if (!(context.getEntity() instanceof Player player)) return false;

        int slot = Math.clamp(this.slot.getAsInt(context), 0, Inventory.SELECTION_SIZE - 1);
        player.getInventory().setSelectedSlot(slot);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetHeldSlotPacket(slot));
            serverPlayer.containerMenu.broadcastChanges();
        }

        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.SWITCH_HOTBAR_SLOT.get();
    }

    public static class Serializer extends ActionSerializer<SwitchHotbarSlotAction> {

        @Override
        public MapCodec<SwitchHotbarSlotAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, SwitchHotbarSlotAction> builder, HolderLookup.Provider provider) {
            builder.setName("Switch Hotbar Slot")
                    .setDescription("Switches the player's selected hotbar slot.")
                    .add("slot", KryptoniteSettingType.intValueRange(0, 8), "The hotbar slot to force a switch to.")
                    .addExampleObject(new SwitchHotbarSlotAction(new StaticValue(0)));
        }
    }

}