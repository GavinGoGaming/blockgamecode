/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.item;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.TranslucentBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MatrixUtil;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ItemRenderer
implements SynchronousResourceReloader {
    public static final Identifier ENTITY_ENCHANTMENT_GLINT = Identifier.ofVanilla("textures/misc/enchanted_glint_entity.png");
    public static final Identifier ITEM_ENCHANTMENT_GLINT = Identifier.ofVanilla("textures/misc/enchanted_glint_item.png");
    private static final Set<Item> WITHOUT_MODELS = Sets.newHashSet(Items.AIR);
    public static final int field_32937 = 8;
    public static final int field_32938 = 8;
    public static final int field_32934 = 200;
    public static final float COMPASS_WITH_GLINT_GUI_MODEL_MULTIPLIER = 0.5f;
    public static final float COMPASS_WITH_GLINT_FIRST_PERSON_MODEL_MULTIPLIER = 0.75f;
    public static final float field_41120 = 0.0078125f;
    private static final ModelIdentifier TRIDENT = ModelIdentifier.ofInventoryVariant(Identifier.ofVanilla("trident"));
    public static final ModelIdentifier TRIDENT_IN_HAND = ModelIdentifier.ofInventoryVariant(Identifier.ofVanilla("trident_in_hand"));
    private static final ModelIdentifier SPYGLASS = ModelIdentifier.ofInventoryVariant(Identifier.ofVanilla("spyglass"));
    public static final ModelIdentifier SPYGLASS_IN_HAND = ModelIdentifier.ofInventoryVariant(Identifier.ofVanilla("spyglass_in_hand"));
    private final MinecraftClient client;
    private final ItemModels models;
    private final TextureManager textureManager;
    private final ItemColors colors;
    private final BuiltinModelItemRenderer builtinModelItemRenderer;

    public ItemRenderer(MinecraftClient client, TextureManager manager, BakedModelManager bakery, ItemColors colors, BuiltinModelItemRenderer builtinModelItemRenderer) {
        this.client = client;
        this.textureManager = manager;
        this.models = new ItemModels(bakery);
        this.builtinModelItemRenderer = builtinModelItemRenderer;
        for (Item lv : Registries.ITEM) {
            if (WITHOUT_MODELS.contains(lv)) continue;
            this.models.putModel(lv, ModelIdentifier.ofInventoryVariant(Registries.ITEM.getId(lv)));
        }
        this.colors = colors;
    }

    public ItemModels getModels() {
        return this.models;
    }

    private void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices) {
        Random lv = Random.create();
        long l = 42L;
        for (Direction lv2 : Direction.values()) {
            lv.setSeed(42L);
            this.renderBakedItemQuads(matrices, vertices, model.getQuads(null, lv2, lv), stack, light, overlay);
        }
        lv.setSeed(42L);
        this.renderBakedItemQuads(matrices, vertices, model.getQuads(null, null, lv), stack, light, overlay);
    }

    public void renderItem(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
        boolean bl2;
        if (stack.isEmpty()) {
            return;
        }
        matrices.push();
        boolean bl = bl2 = renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED;
        if (bl2) {
            if (stack.isOf(Items.TRIDENT)) {
                model = this.models.getModelManager().getModel(TRIDENT);
            } else if (stack.isOf(Items.SPYGLASS)) {
                model = this.models.getModelManager().getModel(SPYGLASS);
            }
        }
        model.getTransformation().getTransformation(renderMode).apply(leftHanded, matrices);
        matrices.translate(-0.5f, -0.5f, -0.5f);
        if (model.isBuiltin() || stack.isOf(Items.TRIDENT) && !bl2) {
            this.builtinModelItemRenderer.render(stack, renderMode, matrices, vertexConsumers, light, overlay);
        } else {
            VertexConsumer lv5;
            BlockItem lv;
            Block lv2;
            Item item;
            boolean bl3 = renderMode != ModelTransformationMode.GUI && !renderMode.isFirstPerson() && (item = stack.getItem()) instanceof BlockItem ? !((lv2 = (lv = (BlockItem)item).getBlock()) instanceof TranslucentBlock) && !(lv2 instanceof StainedGlassPaneBlock) : true;
            RenderLayer lv3 = RenderLayers.getItemLayer(stack, bl3);
            if (ItemRenderer.usesDynamicDisplay(stack) && stack.hasGlint()) {
                MatrixStack.Entry lv4 = matrices.peek().copy();
                if (renderMode == ModelTransformationMode.GUI) {
                    MatrixUtil.scale(lv4.getPositionMatrix(), 0.5f);
                } else if (renderMode.isFirstPerson()) {
                    MatrixUtil.scale(lv4.getPositionMatrix(), 0.75f);
                }
                lv5 = ItemRenderer.getDynamicDisplayGlintConsumer(vertexConsumers, lv3, lv4);
            } else {
                lv5 = bl3 ? ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, lv3, true, stack.hasGlint()) : ItemRenderer.getItemGlintConsumer(vertexConsumers, lv3, true, stack.hasGlint());
            }
            this.renderBakedItemModel(model, stack, light, overlay, matrices, lv5);
        }
        matrices.pop();
    }

    private static boolean usesDynamicDisplay(ItemStack stack) {
        return stack.isIn(ItemTags.COMPASSES) || stack.isOf(Items.CLOCK);
    }

    public static VertexConsumer getArmorGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, boolean glint) {
        if (glint) {
            return VertexConsumers.union(provider.getBuffer(RenderLayer.getArmorEntityGlint()), provider.getBuffer(layer));
        }
        return provider.getBuffer(layer);
    }

    public static VertexConsumer getDynamicDisplayGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, MatrixStack.Entry entry) {
        return VertexConsumers.union((VertexConsumer)new OverlayVertexConsumer(provider.getBuffer(RenderLayer.getGlint()), entry, 0.0078125f), provider.getBuffer(layer));
    }

    public static VertexConsumer getItemGlintConsumer(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint) {
        if (glint) {
            if (MinecraftClient.isFabulousGraphicsOrBetter() && layer == TexturedRenderLayers.getItemEntityTranslucentCull()) {
                return VertexConsumers.union(vertexConsumers.getBuffer(RenderLayer.getGlintTranslucent()), vertexConsumers.getBuffer(layer));
            }
            return VertexConsumers.union(vertexConsumers.getBuffer(solid ? RenderLayer.getGlint() : RenderLayer.getEntityGlint()), vertexConsumers.getBuffer(layer));
        }
        return vertexConsumers.getBuffer(layer);
    }

    public static VertexConsumer getDirectItemGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, boolean solid, boolean glint) {
        if (glint) {
            return VertexConsumers.union(provider.getBuffer(solid ? RenderLayer.getGlint() : RenderLayer.getDirectEntityGlint()), provider.getBuffer(layer));
        }
        return provider.getBuffer(layer);
    }

    private void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, ItemStack stack, int light, int overlay) {
        boolean bl = !stack.isEmpty();
        MatrixStack.Entry lv = matrices.peek();
        for (BakedQuad lv2 : quads) {
            int k = Colors.WHITE;
            if (bl && lv2.hasColor()) {
                k = this.colors.getColor(stack, lv2.getColorIndex());
            }
            float f = (float)ColorHelper.Argb.getAlpha(k) / 255.0f;
            float g = (float)ColorHelper.Argb.getRed(k) / 255.0f;
            float h = (float)ColorHelper.Argb.getGreen(k) / 255.0f;
            float l = (float)ColorHelper.Argb.getBlue(k) / 255.0f;
            vertices.quad(lv, lv2, g, h, l, f, light, overlay);
        }
    }

    public BakedModel getModel(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, int seed) {
        BakedModel lv = stack.isOf(Items.TRIDENT) ? this.models.getModelManager().getModel(TRIDENT_IN_HAND) : (stack.isOf(Items.SPYGLASS) ? this.models.getModelManager().getModel(SPYGLASS_IN_HAND) : this.models.getModel(stack));
        ClientWorld lv2 = world instanceof ClientWorld ? (ClientWorld)world : null;
        BakedModel lv3 = lv.getOverrides().apply(lv, stack, lv2, entity, seed);
        return lv3 == null ? this.models.getModelManager().getMissingModel() : lv3;
    }

    public void renderItem(ItemStack stack, ModelTransformationMode transformationType, int light, int overlay, MatrixStack matrices, VertexConsumerProvider vertexConsumers, @Nullable World world, int seed) {
        this.renderItem(null, stack, transformationType, false, matrices, vertexConsumers, world, light, overlay, seed);
    }

    public void renderItem(@Nullable LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, @Nullable World world, int light, int overlay, int seed) {
        if (item.isEmpty()) {
            return;
        }
        BakedModel lv = this.getModel(item, world, entity, seed);
        this.renderItem(item, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, lv);
    }

    @Override
    public void reload(ResourceManager manager) {
        this.models.reloadModels();
    }
}

