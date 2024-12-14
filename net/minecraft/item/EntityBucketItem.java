/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class EntityBucketItem
extends BucketItem {
    private static final MapCodec<TropicalFishEntity.Variant> TROPICAL_FISH_VARIANT_MAP_CODEC = TropicalFishEntity.Variant.CODEC.fieldOf("BucketVariantTag");
    private final EntityType<?> entityType;
    private final SoundEvent emptyingSound;

    public EntityBucketItem(EntityType<?> type, Fluid fluid, SoundEvent emptyingSound, Item.Settings settings) {
        super(fluid, settings);
        this.entityType = type;
        this.emptyingSound = emptyingSound;
    }

    @Override
    public void onEmptied(@Nullable PlayerEntity player, World world, ItemStack stack, BlockPos pos) {
        if (world instanceof ServerWorld) {
            this.spawnEntity((ServerWorld)world, stack, pos);
            world.emitGameEvent((Entity)player, GameEvent.ENTITY_PLACE, pos);
        }
    }

    @Override
    protected void playEmptyingSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos) {
        world.playSound(player, pos, this.emptyingSound, SoundCategory.NEUTRAL, 1.0f, 1.0f);
    }

    private void spawnEntity(ServerWorld world, ItemStack stack, BlockPos pos) {
        Object lv = this.entityType.spawnFromItemStack(world, stack, null, pos, SpawnReason.BUCKET, true, false);
        if (lv instanceof Bucketable) {
            Bucketable lv2 = (Bucketable)lv;
            NbtComponent lv3 = stack.getOrDefault(DataComponentTypes.BUCKET_ENTITY_DATA, NbtComponent.DEFAULT);
            lv2.copyDataFromNbt(lv3.copyNbt());
            lv2.setFromBucket(true);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (this.entityType == EntityType.TROPICAL_FISH) {
            NbtComponent lv = stack.getOrDefault(DataComponentTypes.BUCKET_ENTITY_DATA, NbtComponent.DEFAULT);
            if (lv.isEmpty()) {
                return;
            }
            Optional<TropicalFishEntity.Variant> optional = lv.get(TROPICAL_FISH_VARIANT_MAP_CODEC).result();
            if (optional.isPresent()) {
                TropicalFishEntity.Variant lv2 = optional.get();
                Formatting[] lvs = new Formatting[]{Formatting.ITALIC, Formatting.GRAY};
                String string = "color.minecraft." + String.valueOf(lv2.baseColor());
                String string2 = "color.minecraft." + String.valueOf(lv2.patternColor());
                int i = TropicalFishEntity.COMMON_VARIANTS.indexOf(lv2);
                if (i != -1) {
                    tooltip.add(Text.translatable(TropicalFishEntity.getToolTipForVariant(i)).formatted(lvs));
                    return;
                }
                tooltip.add(lv2.variety().getText().copyContentOnly().formatted(lvs));
                MutableText lv3 = Text.translatable(string);
                if (!string.equals(string2)) {
                    lv3.append(", ").append(Text.translatable(string2));
                }
                lv3.formatted(lvs);
                tooltip.add(lv3);
            }
        }
    }
}

