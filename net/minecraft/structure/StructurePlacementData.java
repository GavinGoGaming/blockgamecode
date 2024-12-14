/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class StructurePlacementData {
    private BlockMirror mirror = BlockMirror.NONE;
    private BlockRotation rotation = BlockRotation.NONE;
    private BlockPos position = BlockPos.ORIGIN;
    private boolean ignoreEntities;
    @Nullable
    private BlockBox boundingBox;
    private StructureLiquidSettings liquidSettings = StructureLiquidSettings.APPLY_WATERLOGGING;
    @Nullable
    private Random random;
    private int field_15575;
    private final List<StructureProcessor> processors = Lists.newArrayList();
    private boolean updateNeighbors;
    private boolean initializeMobs;

    public StructurePlacementData copy() {
        StructurePlacementData lv = new StructurePlacementData();
        lv.mirror = this.mirror;
        lv.rotation = this.rotation;
        lv.position = this.position;
        lv.ignoreEntities = this.ignoreEntities;
        lv.boundingBox = this.boundingBox;
        lv.liquidSettings = this.liquidSettings;
        lv.random = this.random;
        lv.field_15575 = this.field_15575;
        lv.processors.addAll(this.processors);
        lv.updateNeighbors = this.updateNeighbors;
        lv.initializeMobs = this.initializeMobs;
        return lv;
    }

    public StructurePlacementData setMirror(BlockMirror mirror) {
        this.mirror = mirror;
        return this;
    }

    public StructurePlacementData setRotation(BlockRotation rotation) {
        this.rotation = rotation;
        return this;
    }

    public StructurePlacementData setPosition(BlockPos position) {
        this.position = position;
        return this;
    }

    public StructurePlacementData setIgnoreEntities(boolean ignoreEntities) {
        this.ignoreEntities = ignoreEntities;
        return this;
    }

    public StructurePlacementData setBoundingBox(BlockBox boundingBox) {
        this.boundingBox = boundingBox;
        return this;
    }

    public StructurePlacementData setRandom(@Nullable Random random) {
        this.random = random;
        return this;
    }

    public StructurePlacementData setLiquidSettings(StructureLiquidSettings liquidSettings) {
        this.liquidSettings = liquidSettings;
        return this;
    }

    public StructurePlacementData setUpdateNeighbors(boolean updateNeighbors) {
        this.updateNeighbors = updateNeighbors;
        return this;
    }

    public StructurePlacementData clearProcessors() {
        this.processors.clear();
        return this;
    }

    public StructurePlacementData addProcessor(StructureProcessor processor) {
        this.processors.add(processor);
        return this;
    }

    public StructurePlacementData removeProcessor(StructureProcessor processor) {
        this.processors.remove(processor);
        return this;
    }

    public BlockMirror getMirror() {
        return this.mirror;
    }

    public BlockRotation getRotation() {
        return this.rotation;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public Random getRandom(@Nullable BlockPos pos) {
        if (this.random != null) {
            return this.random;
        }
        if (pos == null) {
            return Random.create(Util.getMeasuringTimeMs());
        }
        return Random.create(MathHelper.hashCode(pos));
    }

    public boolean shouldIgnoreEntities() {
        return this.ignoreEntities;
    }

    @Nullable
    public BlockBox getBoundingBox() {
        return this.boundingBox;
    }

    public boolean shouldUpdateNeighbors() {
        return this.updateNeighbors;
    }

    public List<StructureProcessor> getProcessors() {
        return this.processors;
    }

    public boolean shouldApplyWaterlogging() {
        return this.liquidSettings == StructureLiquidSettings.APPLY_WATERLOGGING;
    }

    public StructureTemplate.PalettedBlockInfoList getRandomBlockInfos(List<StructureTemplate.PalettedBlockInfoList> infoLists, @Nullable BlockPos pos) {
        int i = infoLists.size();
        if (i == 0) {
            throw new IllegalStateException("No palettes");
        }
        return infoLists.get(this.getRandom(pos).nextInt(i));
    }

    public StructurePlacementData setInitializeMobs(boolean initializeMobs) {
        this.initializeMobs = initializeMobs;
        return this;
    }

    public boolean shouldInitializeMobs() {
        return this.initializeMobs;
    }
}

