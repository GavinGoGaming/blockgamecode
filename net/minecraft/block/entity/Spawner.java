/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public interface Spawner {
    public void setEntityType(EntityType<?> var1, Random var2);

    public static void appendSpawnDataToTooltip(ItemStack stack, List<Text> tooltip, String spawnDataKey) {
        Text lv = Spawner.getSpawnedEntityText(stack, spawnDataKey);
        if (lv != null) {
            tooltip.add(lv);
        } else {
            tooltip.add(ScreenTexts.EMPTY);
            tooltip.add(Text.translatable("block.minecraft.spawner.desc1").formatted(Formatting.GRAY));
            tooltip.add(ScreenTexts.space().append(Text.translatable("block.minecraft.spawner.desc2").formatted(Formatting.BLUE)));
        }
    }

    @Nullable
    public static Text getSpawnedEntityText(ItemStack stack, String spawnDataKey) {
        NbtCompound lv = stack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT).getNbt();
        Identifier lv2 = Spawner.getSpawnedEntityId(lv, spawnDataKey);
        if (lv2 != null) {
            return Registries.ENTITY_TYPE.getOrEmpty(lv2).map(entityType -> Text.translatable(entityType.getTranslationKey()).formatted(Formatting.GRAY)).orElse(null);
        }
        return null;
    }

    @Nullable
    private static Identifier getSpawnedEntityId(NbtCompound nbt, String spawnDataKey) {
        if (nbt.contains(spawnDataKey, NbtElement.COMPOUND_TYPE)) {
            String string2 = nbt.getCompound(spawnDataKey).getCompound("entity").getString("id");
            return Identifier.tryParse(string2);
        }
        return null;
    }
}

