/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.RealmsNews;
import net.minecraft.client.realms.util.RealmsPersistence;

@Environment(value=EnvType.CLIENT)
public class RealmsNewsUpdater {
    private final RealmsPersistence persistence;
    private boolean hasUnreadNews;
    private String newsLink;

    public RealmsNewsUpdater(RealmsPersistence persistence) {
        this.persistence = persistence;
        RealmsPersistence.RealmsPersistenceData lv = persistence.load();
        this.hasUnreadNews = lv.hasUnreadNews;
        this.newsLink = lv.newsLink;
    }

    public boolean hasUnreadNews() {
        return this.hasUnreadNews;
    }

    public String getNewsLink() {
        return this.newsLink;
    }

    public void updateNews(RealmsNews news) {
        RealmsPersistence.RealmsPersistenceData lv = this.checkLinkUpdated(news);
        this.hasUnreadNews = lv.hasUnreadNews;
        this.newsLink = lv.newsLink;
    }

    private RealmsPersistence.RealmsPersistenceData checkLinkUpdated(RealmsNews news) {
        boolean bl;
        RealmsPersistence.RealmsPersistenceData lv = new RealmsPersistence.RealmsPersistenceData();
        lv.newsLink = news.newsLink;
        RealmsPersistence.RealmsPersistenceData lv2 = this.persistence.load();
        boolean bl2 = bl = lv.newsLink == null || lv.newsLink.equals(lv2.newsLink);
        if (bl) {
            return lv2;
        }
        lv.hasUnreadNews = true;
        this.persistence.save(lv);
        return lv;
    }
}

