/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.sound;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundContainer;
import net.minecraft.client.sound.SoundEntry;
import net.minecraft.client.sound.SoundEntryDeserializer;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstanceListener;
import net.minecraft.client.sound.SoundListenerTransform;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.minecraft.util.math.floatprovider.MultipliedFloatSupplier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SoundManager
extends SinglePreparationResourceReloader<SoundList> {
    public static final Identifier EMPTY_ID = Identifier.ofVanilla("empty");
    public static final Sound MISSING_SOUND = new Sound(EMPTY_ID, ConstantFloatProvider.create(1.0f), ConstantFloatProvider.create(1.0f), 1, Sound.RegistrationType.FILE, false, false, 16);
    public static final Identifier INTENTIONALLY_EMPTY_ID = Identifier.ofVanilla("intentionally_empty");
    public static final WeightedSoundSet INTENTIONALLY_EMPTY_SOUND_SET = new WeightedSoundSet(INTENTIONALLY_EMPTY_ID, null);
    public static final Sound INTENTIONALLY_EMPTY_SOUND = new Sound(INTENTIONALLY_EMPTY_ID, ConstantFloatProvider.create(1.0f), ConstantFloatProvider.create(1.0f), 1, Sound.RegistrationType.FILE, false, false, 16);
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String SOUNDS_JSON = "sounds.json";
    private static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(Text.class, new Text.Serializer(DynamicRegistryManager.EMPTY)).registerTypeAdapter((Type)((Object)SoundEntry.class), new SoundEntryDeserializer()).create();
    private static final TypeToken<Map<String, SoundEntry>> TYPE = new TypeToken<Map<String, SoundEntry>>(){};
    private final Map<Identifier, WeightedSoundSet> sounds = Maps.newHashMap();
    private final SoundSystem soundSystem;
    private final Map<Identifier, Resource> soundResources = new HashMap<Identifier, Resource>();

    public SoundManager(GameOptions gameOptions) {
        this.soundSystem = new SoundSystem(this, gameOptions, ResourceFactory.fromMap(this.soundResources));
    }

    @Override
    protected SoundList prepare(ResourceManager arg, Profiler arg2) {
        SoundList lv = new SoundList();
        arg2.startTick();
        arg2.push("list");
        lv.findSounds(arg);
        arg2.pop();
        for (String string : arg.getAllNamespaces()) {
            arg2.push(string);
            try {
                List<Resource> list = arg.getAllResources(Identifier.of(string, SOUNDS_JSON));
                for (Resource lv2 : list) {
                    arg2.push(lv2.getPackId());
                    try (BufferedReader reader = lv2.getReader();){
                        arg2.push("parse");
                        Map<String, SoundEntry> map = JsonHelper.deserialize(GSON, (Reader)reader, TYPE);
                        arg2.swap("register");
                        for (Map.Entry<String, SoundEntry> entry : map.entrySet()) {
                            lv.register(Identifier.of(string, entry.getKey()), entry.getValue());
                        }
                        arg2.pop();
                    } catch (RuntimeException runtimeException) {
                        LOGGER.warn("Invalid {} in resourcepack: '{}'", SOUNDS_JSON, lv2.getPackId(), runtimeException);
                    }
                    arg2.pop();
                }
            } catch (IOException iOException) {
                // empty catch block
            }
            arg2.pop();
        }
        arg2.endTick();
        return lv;
    }

    @Override
    protected void apply(SoundList arg, ResourceManager arg2, Profiler arg3) {
        arg.reload(this.sounds, this.soundResources, this.soundSystem);
        if (SharedConstants.isDevelopment) {
            for (Identifier lv : this.sounds.keySet()) {
                WeightedSoundSet lv2 = this.sounds.get(lv);
                if (Texts.hasTranslation(lv2.getSubtitle()) || !Registries.SOUND_EVENT.containsId(lv)) continue;
                LOGGER.error("Missing subtitle {} for sound event: {}", (Object)lv2.getSubtitle(), (Object)lv);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            for (Identifier lv : this.sounds.keySet()) {
                if (Registries.SOUND_EVENT.containsId(lv)) continue;
                LOGGER.debug("Not having sound event for: {}", (Object)lv);
            }
        }
        this.soundSystem.reloadSounds();
    }

    public List<String> getSoundDevices() {
        return this.soundSystem.getSoundDevices();
    }

    public SoundListenerTransform getListenerTransform() {
        return this.soundSystem.getListenerTransform();
    }

    static boolean isSoundResourcePresent(Sound sound, Identifier id, ResourceFactory resourceFactory) {
        Identifier lv = sound.getLocation();
        if (resourceFactory.getResource(lv).isEmpty()) {
            LOGGER.warn("File {} does not exist, cannot add it to event {}", (Object)lv, (Object)id);
            return false;
        }
        return true;
    }

    @Nullable
    public WeightedSoundSet get(Identifier id) {
        return this.sounds.get(id);
    }

    public Collection<Identifier> getKeys() {
        return this.sounds.keySet();
    }

    public void playNextTick(TickableSoundInstance sound) {
        this.soundSystem.playNextTick(sound);
    }

    public void play(SoundInstance sound) {
        this.soundSystem.play(sound);
    }

    public void play(SoundInstance sound, int delay) {
        this.soundSystem.play(sound, delay);
    }

    public void updateListenerPosition(Camera camera) {
        this.soundSystem.updateListenerPosition(camera);
    }

    public void pauseAll() {
        this.soundSystem.pauseAll();
    }

    public void stopAll() {
        this.soundSystem.stopAll();
    }

    public void close() {
        this.soundSystem.stop();
    }

    public void stopAbruptly() {
        this.soundSystem.stopAbruptly();
    }

    public void tick(boolean paused) {
        this.soundSystem.tick(paused);
    }

    public void resumeAll() {
        this.soundSystem.resumeAll();
    }

    public void updateSoundVolume(SoundCategory category, float volume) {
        if (category == SoundCategory.MASTER && volume <= 0.0f) {
            this.stopAll();
        }
        this.soundSystem.updateSoundVolume(category, volume);
    }

    public void stop(SoundInstance sound) {
        this.soundSystem.stop(sound);
    }

    public boolean isPlaying(SoundInstance sound) {
        return this.soundSystem.isPlaying(sound);
    }

    public void registerListener(SoundInstanceListener listener) {
        this.soundSystem.registerListener(listener);
    }

    public void unregisterListener(SoundInstanceListener listener) {
        this.soundSystem.unregisterListener(listener);
    }

    public void stopSounds(@Nullable Identifier id, @Nullable SoundCategory soundCategory) {
        this.soundSystem.stopSounds(id, soundCategory);
    }

    public String getDebugString() {
        return this.soundSystem.getDebugString();
    }

    public void reloadSounds() {
        this.soundSystem.reloadSounds();
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager manager, Profiler profiler) {
        return this.prepare(manager, profiler);
    }

    @Environment(value=EnvType.CLIENT)
    protected static class SoundList {
        final Map<Identifier, WeightedSoundSet> loadedSounds = Maps.newHashMap();
        private Map<Identifier, Resource> foundSounds = Map.of();

        protected SoundList() {
        }

        void findSounds(ResourceManager resourceManager) {
            this.foundSounds = Sound.FINDER.findResources(resourceManager);
        }

        void register(Identifier id, SoundEntry entry) {
            boolean bl;
            WeightedSoundSet lv = this.loadedSounds.get(id);
            boolean bl2 = bl = lv == null;
            if (bl || entry.canReplace()) {
                if (!bl) {
                    LOGGER.debug("Replaced sound event location {}", (Object)id);
                }
                lv = new WeightedSoundSet(id, entry.getSubtitle());
                this.loadedSounds.put(id, lv);
            }
            ResourceFactory lv2 = ResourceFactory.fromMap(this.foundSounds);
            block4: for (final Sound lv3 : entry.getSounds()) {
                final Identifier lv4 = lv3.getIdentifier();
                lv.add(switch (lv3.getRegistrationType()) {
                    case Sound.RegistrationType.FILE -> {
                        if (!SoundManager.isSoundResourcePresent(lv3, id, lv2)) continue block4;
                        yield lv3;
                    }
                    case Sound.RegistrationType.SOUND_EVENT -> new SoundContainer<Sound>(){

                        @Override
                        public int getWeight() {
                            WeightedSoundSet lv = loadedSounds.get(lv4);
                            return lv == null ? 0 : lv.getWeight();
                        }

                        @Override
                        public Sound getSound(Random arg) {
                            WeightedSoundSet lv = loadedSounds.get(lv4);
                            if (lv == null) {
                                return MISSING_SOUND;
                            }
                            Sound lv2 = lv.getSound(arg);
                            return new Sound(lv2.getIdentifier(), new MultipliedFloatSupplier(lv2.getVolume(), lv3.getVolume()), new MultipliedFloatSupplier(lv2.getPitch(), lv3.getPitch()), lv3.getWeight(), Sound.RegistrationType.FILE, lv2.isStreamed() || lv3.isStreamed(), lv2.isPreloaded(), lv2.getAttenuation());
                        }

                        @Override
                        public void preload(SoundSystem soundSystem) {
                            WeightedSoundSet lv = loadedSounds.get(lv4);
                            if (lv == null) {
                                return;
                            }
                            lv.preload(soundSystem);
                        }

                        @Override
                        public /* synthetic */ Object getSound(Random random) {
                            return this.getSound(random);
                        }
                    };
                    default -> throw new IllegalStateException("Unknown SoundEventRegistration type: " + String.valueOf((Object)lv3.getRegistrationType()));
                });
            }
        }

        public void reload(Map<Identifier, WeightedSoundSet> sounds, Map<Identifier, Resource> soundResources, SoundSystem system) {
            sounds.clear();
            soundResources.clear();
            soundResources.putAll(this.foundSounds);
            for (Map.Entry<Identifier, WeightedSoundSet> entry : this.loadedSounds.entrySet()) {
                sounds.put(entry.getKey(), entry.getValue());
                entry.getValue().preload(system);
            }
        }
    }
}

