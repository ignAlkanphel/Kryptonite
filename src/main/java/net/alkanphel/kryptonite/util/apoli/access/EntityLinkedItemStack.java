package net.alkanphel.kryptonite.util.apoli.access;

import net.minecraft.world.entity.Entity;

public interface EntityLinkedItemStack {
    Entity kryptonite$getEntity();

    Entity kryptonite$getEntity(boolean prioritiseVanillaHolder);

    void kryptonite$setEntity(Entity entity);
}