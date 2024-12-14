/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.VillagerResourceMetadata;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

@Environment(value=EnvType.CLIENT)
public class VillagerClothingFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
extends FeatureRenderer<T, M> {
    private static final Int2ObjectMap<Identifier> LEVEL_TO_ID = Util.make(new Int2ObjectOpenHashMap(), levelToId -> {
        levelToId.put(1, Identifier.ofVanilla("stone"));
        levelToId.put(2, Identifier.ofVanilla("iron"));
        levelToId.put(3, Identifier.ofVanilla("gold"));
        levelToId.put(4, Identifier.ofVanilla("emerald"));
        levelToId.put(5, Identifier.ofVanilla("diamond"));
    });
    private final Object2ObjectMap<VillagerType, VillagerResourceMetadata.HatType> villagerTypeToHat = new Object2ObjectOpenHashMap<VillagerType, VillagerResourceMetadata.HatType>();
    private final Object2ObjectMap<VillagerProfession, VillagerResourceMetadata.HatType> professionToHat = new Object2ObjectOpenHashMap<VillagerProfession, VillagerResourceMetadata.HatType>();
    private final ResourceManager resourceManager;
    private final String entityType;

    public VillagerClothingFeatureRenderer(FeatureRendererContext<T, M> context, ResourceManager resourceManager, String entityType) {
        super(context);
        this.resourceManager = resourceManager;
        this.entityType = entityType;
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
        if (((Entity)arg3).isInvisible()) {
            return;
        }
        VillagerData lv = ((VillagerDataContainer)arg3).getVillagerData();
        VillagerType lv2 = lv.getType();
        VillagerProfession lv3 = lv.getProfession();
        VillagerResourceMetadata.HatType lv4 = this.getHatType(this.villagerTypeToHat, "type", Registries.VILLAGER_TYPE, lv2);
        VillagerResourceMetadata.HatType lv5 = this.getHatType(this.professionToHat, "profession", Registries.VILLAGER_PROFESSION, lv3);
        Object lv6 = this.getContextModel();
        ((ModelWithHat)lv6).setHatVisible(lv5 == VillagerResourceMetadata.HatType.NONE || lv5 == VillagerResourceMetadata.HatType.PARTIAL && lv4 != VillagerResourceMetadata.HatType.FULL);
        Identifier lv7 = this.findTexture("type", Registries.VILLAGER_TYPE.getId(lv2));
        VillagerClothingFeatureRenderer.renderModel(lv6, lv7, arg, arg2, i, arg3, -1);
        ((ModelWithHat)lv6).setHatVisible(true);
        if (lv3 != VillagerProfession.NONE && !((LivingEntity)arg3).isBaby()) {
            Identifier lv8 = this.findTexture("profession", Registries.VILLAGER_PROFESSION.getId(lv3));
            VillagerClothingFeatureRenderer.renderModel(lv6, lv8, arg, arg2, i, arg3, -1);
            if (lv3 != VillagerProfession.NITWIT) {
                Identifier lv9 = this.findTexture("profession_level", (Identifier)LEVEL_TO_ID.get(MathHelper.clamp(lv.getLevel(), 1, LEVEL_TO_ID.size())));
                VillagerClothingFeatureRenderer.renderModel(lv6, lv9, arg, arg2, i, arg3, -1);
            }
        }
    }

    private Identifier findTexture(String keyType, Identifier keyId) {
        return keyId.withPath(path -> "textures/entity/" + this.entityType + "/" + keyType + "/" + path + ".png");
    }

    public <K> VillagerResourceMetadata.HatType getHatType(Object2ObjectMap<K, VillagerResourceMetadata.HatType> hatLookUp, String keyType, DefaultedRegistry<K> registry, K key) {
        return hatLookUp.computeIfAbsent(key, k -> this.resourceManager.getResource(this.findTexture(keyType, registry.getId(key))).flatMap(resource -> {
            try {
                return resource.getMetadata().decode(VillagerResourceMetadata.READER).map(VillagerResourceMetadata::getHatType);
            } catch (IOException iOException) {
                return Optional.empty();
            }
        }).orElse(VillagerResourceMetadata.HatType.NONE));
    }
}

