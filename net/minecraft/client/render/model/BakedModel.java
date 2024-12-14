/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.model;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface BakedModel {
    public List<BakedQuad> getQuads(@Nullable BlockState var1, @Nullable Direction var2, Random var3);

    public boolean useAmbientOcclusion();

    public boolean hasDepth();

    public boolean isSideLit();

    public boolean isBuiltin();

    public Sprite getParticleSprite();

    public ModelTransformation getTransformation();

    public ModelOverrideList getOverrides();
}

