/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.Nullable;

public record ApiServices(MinecraftSessionService sessionService, ServicesKeySet servicesKeySet, GameProfileRepository profileRepository, UserCache userCache) {
    private static final String USER_CACHE_FILE_NAME = "usercache.json";

    public static ApiServices create(YggdrasilAuthenticationService authenticationService, File rootDirectory) {
        MinecraftSessionService minecraftSessionService = authenticationService.createMinecraftSessionService();
        GameProfileRepository gameProfileRepository = authenticationService.createProfileRepository();
        UserCache lv = new UserCache(gameProfileRepository, new File(rootDirectory, USER_CACHE_FILE_NAME));
        return new ApiServices(minecraftSessionService, authenticationService.getServicesKeySet(), gameProfileRepository, lv);
    }

    @Nullable
    public SignatureVerifier serviceSignatureVerifier() {
        return SignatureVerifier.create(this.servicesKeySet, ServicesKeyType.PROFILE_KEY);
    }

    public boolean providesProfileKeys() {
        return !this.servicesKeySet.keys(ServicesKeyType.PROFILE_KEY).isEmpty();
    }
}

