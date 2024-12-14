/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.util.JsonUtils;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Subscription
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public long startDate;
    public int daysLeft;
    public SubscriptionType type = SubscriptionType.NORMAL;

    public static Subscription parse(String json) {
        Subscription lv = new Subscription();
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
            lv.startDate = JsonUtils.getLongOr("startDate", jsonObject, 0L);
            lv.daysLeft = JsonUtils.getIntOr("daysLeft", jsonObject, 0);
            lv.type = Subscription.typeFrom(JsonUtils.getNullableStringOr("subscriptionType", jsonObject, SubscriptionType.NORMAL.name()));
        } catch (Exception exception) {
            LOGGER.error("Could not parse Subscription: {}", (Object)exception.getMessage());
        }
        return lv;
    }

    private static SubscriptionType typeFrom(String subscriptionType) {
        try {
            return SubscriptionType.valueOf(subscriptionType);
        } catch (Exception exception) {
            return SubscriptionType.NORMAL;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum SubscriptionType {
        NORMAL,
        RECURRING;

    }
}

