/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class EntityList {
    private Int2ObjectMap<Entity> entities = new Int2ObjectLinkedOpenHashMap<Entity>();
    private Int2ObjectMap<Entity> temp = new Int2ObjectLinkedOpenHashMap<Entity>();
    @Nullable
    private Int2ObjectMap<Entity> iterating;

    private void ensureSafe() {
        if (this.iterating == this.entities) {
            this.temp.clear();
            for (Int2ObjectMap.Entry entry : Int2ObjectMaps.fastIterable(this.entities)) {
                this.temp.put(entry.getIntKey(), (Entity)entry.getValue());
            }
            Int2ObjectMap<Entity> int2ObjectMap = this.entities;
            this.entities = this.temp;
            this.temp = int2ObjectMap;
        }
    }

    public void add(Entity entity) {
        this.ensureSafe();
        this.entities.put(entity.getId(), entity);
    }

    public void remove(Entity entity) {
        this.ensureSafe();
        this.entities.remove(entity.getId());
    }

    public boolean has(Entity entity) {
        return this.entities.containsKey(entity.getId());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void forEach(Consumer<Entity> action) {
        if (this.iterating != null) {
            throw new UnsupportedOperationException("Only one concurrent iteration supported");
        }
        this.iterating = this.entities;
        try {
            for (Entity lv : this.entities.values()) {
                action.accept(lv);
            }
        } finally {
            this.iterating = null;
        }
    }
}

