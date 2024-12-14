/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.resource.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.ReorderingUtil;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TranslationStorage
extends Language {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, String> translations;
    private final boolean rightToLeft;

    private TranslationStorage(Map<String, String> translations, boolean rightToLeft) {
        this.translations = translations;
        this.rightToLeft = rightToLeft;
    }

    public static TranslationStorage load(ResourceManager resourceManager, List<String> definitions, boolean rightToLeft) {
        HashMap<String, String> map = Maps.newHashMap();
        for (String string : definitions) {
            String string2 = String.format(Locale.ROOT, "lang/%s.json", string);
            for (String string3 : resourceManager.getAllNamespaces()) {
                try {
                    Identifier lv = Identifier.of(string3, string2);
                    TranslationStorage.load(string, resourceManager.getAllResources(lv), map);
                } catch (Exception exception) {
                    LOGGER.warn("Skipped language file: {}:{} ({})", string3, string2, exception.toString());
                }
            }
        }
        return new TranslationStorage(ImmutableMap.copyOf(map), rightToLeft);
    }

    private static void load(String langCode, List<Resource> resourceRefs, Map<String, String> translations) {
        for (Resource lv : resourceRefs) {
            try {
                InputStream inputStream = lv.getInputStream();
                try {
                    Language.load(inputStream, translations::put);
                } finally {
                    if (inputStream == null) continue;
                    inputStream.close();
                }
            } catch (IOException iOException) {
                LOGGER.warn("Failed to load translations for {} from pack {}", langCode, lv.getPackId(), iOException);
            }
        }
    }

    @Override
    public String get(String key, String fallback) {
        return this.translations.getOrDefault(key, fallback);
    }

    @Override
    public boolean hasTranslation(String key) {
        return this.translations.containsKey(key);
    }

    @Override
    public boolean isRightToLeft() {
        return this.rightToLeft;
    }

    @Override
    public OrderedText reorder(StringVisitable text) {
        return ReorderingUtil.reorder(text, this.rightToLeft);
    }
}

