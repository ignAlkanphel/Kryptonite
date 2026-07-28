package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.ref.WeakReference;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements EntityLinkedType {

    @Unique
    private final ThreadLocal<WeakReference<Entity>> kryptonite$currentEntity = new ThreadLocal<>();

    @Override
    public Entity kryptonite$getEntity() {
        final WeakReference<Entity> reference = kryptonite$currentEntity.get();
        if (reference != null) return reference.get();
        return null;
    }

    @Override
    public void kryptonite$setEntity(Entity entity) {
        this.kryptonite$currentEntity.set(new WeakReference<>(entity));
    }

}