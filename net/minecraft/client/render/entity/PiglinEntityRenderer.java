/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PiglinEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class PiglinEntityRenderer
extends BipedEntityRenderer<MobEntity, PiglinEntityModel<MobEntity>> {
    private static final Map<EntityType<?>, Identifier> TEXTURES = ImmutableMap.of(EntityType.PIGLIN, Identifier.ofVanilla("textures/entity/piglin/piglin.png"), EntityType.ZOMBIFIED_PIGLIN, Identifier.ofVanilla("textures/entity/piglin/zombified_piglin.png"), EntityType.PIGLIN_BRUTE, Identifier.ofVanilla("textures/entity/piglin/piglin_brute.png"));
    private static final float HORIZONTAL_SCALE = 1.0019531f;

    public PiglinEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer mainLayer, EntityModelLayer innerArmorLayer, EntityModelLayer outerArmorLayer, boolean zombie) {
        super(ctx, PiglinEntityRenderer.getPiglinModel(ctx.getModelLoader(), mainLayer, zombie), 0.5f, 1.0019531f, 1.0f, 1.0019531f);
        this.addFeature(new ArmorFeatureRenderer(this, new ArmorEntityModel(ctx.getPart(innerArmorLayer)), new ArmorEntityModel(ctx.getPart(outerArmorLayer)), ctx.getModelManager()));
    }

    private static PiglinEntityModel<MobEntity> getPiglinModel(EntityModelLoader modelLoader, EntityModelLayer layer, boolean zombie) {
        PiglinEntityModel<MobEntity> lv = new PiglinEntityModel<MobEntity>(modelLoader.getModelPart(layer));
        if (zombie) {
            lv.rightEar.visible = false;
        }
        return lv;
    }

    @Override
    public Identifier getTexture(MobEntity arg) {
        Identifier lv = TEXTURES.get(arg.getType());
        if (lv == null) {
            throw new IllegalArgumentException("I don't know what texture to use for " + String.valueOf(arg.getType()));
        }
        return lv;
    }

    @Override
    protected boolean isShaking(MobEntity arg) {
        return super.isShaking(arg) || arg instanceof AbstractPiglinEntity && ((AbstractPiglinEntity)arg).shouldZombify();
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntity entity) {
        return this.isShaking((MobEntity)entity);
    }

    @Override
    public /* synthetic */ Identifier getTexture(Entity entity) {
        return this.getTexture((MobEntity)entity);
    }
}

