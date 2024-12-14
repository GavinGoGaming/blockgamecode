/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class ResourcePackManager {
    private final Set<ResourcePackProvider> providers;
    private Map<String, ResourcePackProfile> profiles = ImmutableMap.of();
    private List<ResourcePackProfile> enabled = ImmutableList.of();

    public ResourcePackManager(ResourcePackProvider ... providers) {
        this.providers = ImmutableSet.copyOf(providers);
    }

    public static String listPacks(Collection<ResourcePackProfile> profiles) {
        return profiles.stream().map(profile -> profile.getId() + (profile.getCompatibility().isCompatible() ? "" : " (incompatible)")).collect(Collectors.joining(", "));
    }

    public void scanPacks() {
        List list = this.enabled.stream().map(ResourcePackProfile::getId).collect(ImmutableList.toImmutableList());
        this.profiles = this.providePackProfiles();
        this.enabled = this.buildEnabledProfiles(list);
    }

    private Map<String, ResourcePackProfile> providePackProfiles() {
        TreeMap map = Maps.newTreeMap();
        for (ResourcePackProvider lv : this.providers) {
            lv.register(profile -> map.put(profile.getId(), profile));
        }
        return ImmutableMap.copyOf(map);
    }

    public void setEnabledProfiles(Collection<String> enabled) {
        this.enabled = this.buildEnabledProfiles(enabled);
    }

    public boolean enable(String profile) {
        ResourcePackProfile lv = this.profiles.get(profile);
        if (lv != null && !this.enabled.contains(lv)) {
            ArrayList<ResourcePackProfile> list = Lists.newArrayList(this.enabled);
            list.add(lv);
            this.enabled = list;
            return true;
        }
        return false;
    }

    public boolean disable(String profile) {
        ResourcePackProfile lv = this.profiles.get(profile);
        if (lv != null && this.enabled.contains(lv)) {
            ArrayList<ResourcePackProfile> list = Lists.newArrayList(this.enabled);
            list.remove(lv);
            this.enabled = list;
            return true;
        }
        return false;
    }

    private List<ResourcePackProfile> buildEnabledProfiles(Collection<String> enabledNames) {
        List list = this.streamProfilesById(enabledNames).collect(Util.toArrayList());
        for (ResourcePackProfile lv : this.profiles.values()) {
            if (!lv.isRequired() || list.contains(lv)) continue;
            lv.getInitialPosition().insert(list, lv, ResourcePackProfile::getPosition, false);
        }
        return ImmutableList.copyOf(list);
    }

    private Stream<ResourcePackProfile> streamProfilesById(Collection<String> ids) {
        return ids.stream().map(this.profiles::get).filter(Objects::nonNull);
    }

    public Collection<String> getIds() {
        return this.profiles.keySet();
    }

    public Collection<ResourcePackProfile> getProfiles() {
        return this.profiles.values();
    }

    public Collection<String> getEnabledIds() {
        return this.enabled.stream().map(ResourcePackProfile::getId).collect(ImmutableSet.toImmutableSet());
    }

    public FeatureSet getRequestedFeatures() {
        return this.getEnabledProfiles().stream().map(ResourcePackProfile::getRequestedFeatures).reduce(FeatureSet::combine).orElse(FeatureSet.empty());
    }

    public Collection<ResourcePackProfile> getEnabledProfiles() {
        return this.enabled;
    }

    @Nullable
    public ResourcePackProfile getProfile(String id) {
        return this.profiles.get(id);
    }

    public boolean hasProfile(String id) {
        return this.profiles.containsKey(id);
    }

    public List<ResourcePack> createResourcePacks() {
        return this.enabled.stream().map(ResourcePackProfile::createResourcePack).collect(ImmutableList.toImmutableList());
    }
}

