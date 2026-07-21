package net.alkanphel.kryptonite.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.util.apoli.InventoryUtil;
import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedItemStack;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Kryptonite.MOD_ID)
public class LinkedStackTestCommand {

    @SubscribeEvent
    static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("test_entity_linked_stack").executes(LinkedStackTestCommand::run));
    }

    private static int run(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        Entity self = source.getEntity();

        if (!(self instanceof LivingEntity living)) {
            source.sendFailure(Component.literal("Must be run by a living entity."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("--- Linked stack test for " + living.getName().getString() + " ---"), false);

        InventoryUtil.forEachStack(living, stack -> {
            Entity holder = ((EntityLinkedItemStack) (Object) stack).kryptonite$getEntity();
            String result = holder == null
                    ? "NULL (not linked!)"
                    : (holder == living ? "OK -> " + holder.getName().getString() : "WRONG -> " + holder.getName().getString());

            source.sendSuccess(() -> Component.literal(stack.getItem().toString() + " (x" + stack.getCount() + "): " + result), false);
        });

        return 1;
    }

}