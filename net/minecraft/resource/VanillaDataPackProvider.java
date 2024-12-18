/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource;

import com.google.common.annotations.VisibleForTesting;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.DefaultResourcePackBuilder;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackPosition;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.VanillaResourcePackProvider;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.metadata.PackFeatureSetMetadata;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataMap;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.path.SymlinkFinder;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;

public class VanillaDataPackProvider
extends VanillaResourcePackProvider {
    private static final PackResourceMetadata METADATA = new PackResourceMetadata(Text.translatable("dataPack.vanilla.description"), SharedConstants.getGameVersion().getResourceVersion(ResourceType.SERVER_DATA), Optional.empty());
    private static final PackFeatureSetMetadata FEATURE_FLAGS = new PackFeatureSetMetadata(FeatureFlags.DEFAULT_ENABLED_FEATURES);
    private static final ResourceMetadataMap METADATA_MAP = ResourceMetadataMap.of(PackResourceMetadata.SERIALIZER, METADATA, PackFeatureSetMetadata.SERIALIZER, FEATURE_FLAGS);
    private static final ResourcePackInfo INFO = new ResourcePackInfo("vanilla", Text.translatable("dataPack.vanilla.name"), ResourcePackSource.BUILTIN, Optional.of(VANILLA_ID));
    private static final ResourcePackPosition BOTTOM_POSITION = new ResourcePackPosition(false, ResourcePackProfile.InsertionPosition.BOTTOM, false);
    private static final ResourcePackPosition TOP_POSITION = new ResourcePackPosition(false, ResourcePackProfile.InsertionPosition.TOP, false);
    private static final Identifier ID = Identifier.ofVanilla("datapacks");

    public VanillaDataPackProvider(SymlinkFinder symlinkFinder) {
        super(ResourceType.SERVER_DATA, VanillaDataPackProvider.createDefaultPack(), ID, symlinkFinder);
    }

    private static ResourcePackInfo createInfo(String id, Text title) {
        return new ResourcePackInfo(id, title, ResourcePackSource.FEATURE, Optional.of(VersionedIdentifier.createVanilla(id)));
    }

    @VisibleForTesting
    public static DefaultResourcePack createDefaultPack() {
        return new DefaultResourcePackBuilder().withMetadataMap(METADATA_MAP).withNamespaces("minecraft").runCallback().withDefaultPaths().build(INFO);
    }

    @Override
    protected Text getDisplayName(String id) {
        return Text.literal(id);
    }

    @Override
    @Nullable
    protected ResourcePackProfile createDefault(ResourcePack pack) {
        return ResourcePackProfile.create(INFO, VanillaDataPackProvider.createPackFactory(pack), ResourceType.SERVER_DATA, BOTTOM_POSITION);
    }

    @Override
    @Nullable
    protected ResourcePackProfile create(String fileName, ResourcePackProfile.PackFactory packFactory, Text displayName) {
        return ResourcePackProfile.create(VanillaDataPackProvider.createInfo(fileName, displayName), packFactory, ResourceType.SERVER_DATA, TOP_POSITION);
    }

    public static ResourcePackManager createManager(Path dataPacksPath, SymlinkFinder symlinkFinder) {
        return new ResourcePackManager(new VanillaDataPackProvider(symlinkFinder), new FileResourcePackProvider(dataPacksPath, ResourceType.SERVER_DATA, ResourcePackSource.WORLD, symlinkFinder));
    }

    public static ResourcePackManager createClientManager() {
        return new ResourcePackManager(new VanillaDataPackProvider(new SymlinkFinder(path -> true)));
    }

    public static ResourcePackManager createManager(LevelStorage.Session session) {
        return VanillaDataPackProvider.createManager(session.getDirectory(WorldSavePath.DATAPACKS), session.getLevelStorage().getSymlinkFinder());
    }
}

