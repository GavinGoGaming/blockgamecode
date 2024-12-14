/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.resource;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.VanillaDataPackProvider;

@Environment(value=EnvType.CLIENT)
public class ClientDataPackManager {
    private final ResourcePackManager packManager = VanillaDataPackProvider.createClientManager();
    private final Map<VersionedIdentifier, String> knownPacks;

    public ClientDataPackManager() {
        this.packManager.scanPacks();
        ImmutableMap.Builder builder = ImmutableMap.builder();
        this.packManager.getProfiles().forEach(resourcePackProfile -> {
            ResourcePackInfo lv = resourcePackProfile.getInfo();
            lv.knownPackInfo().ifPresent(knownPackInfo -> builder.put(knownPackInfo, lv.id()));
        });
        this.knownPacks = builder.build();
    }

    public List<VersionedIdentifier> getCommonKnownPacks(List<VersionedIdentifier> serverKnownPacks) {
        ArrayList<VersionedIdentifier> list2 = new ArrayList<VersionedIdentifier>(serverKnownPacks.size());
        ArrayList<String> list3 = new ArrayList<String>(serverKnownPacks.size());
        for (VersionedIdentifier lv : serverKnownPacks) {
            String string = this.knownPacks.get(lv);
            if (string == null) continue;
            list3.add(string);
            list2.add(lv);
        }
        this.packManager.setEnabledProfiles(list3);
        return list2;
    }

    public LifecycledResourceManager createResourceManager() {
        List<ResourcePack> list = this.packManager.createResourcePacks();
        return new LifecycledResourceManagerImpl(ResourceType.SERVER_DATA, list);
    }
}

