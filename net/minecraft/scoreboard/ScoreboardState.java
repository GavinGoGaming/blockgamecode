/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.NumberFormatTypes;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;
import org.slf4j.Logger;

public class ScoreboardState
extends PersistentState {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String SCOREBOARD_KEY = "scoreboard";
    private final Scoreboard scoreboard;

    public ScoreboardState(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public ScoreboardState readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        this.readObjectivesNbt(nbt.getList("Objectives", NbtElement.COMPOUND_TYPE), registries);
        this.scoreboard.readNbt(nbt.getList("PlayerScores", NbtElement.COMPOUND_TYPE), registries);
        if (nbt.contains("DisplaySlots", NbtElement.COMPOUND_TYPE)) {
            this.readDisplaySlotsNbt(nbt.getCompound("DisplaySlots"));
        }
        if (nbt.contains("Teams", NbtElement.LIST_TYPE)) {
            this.readTeamsNbt(nbt.getList("Teams", NbtElement.COMPOUND_TYPE), registries);
        }
        return this;
    }

    private void readTeamsNbt(NbtList nbt, RegistryWrapper.WrapperLookup registries) {
        for (int i = 0; i < nbt.size(); ++i) {
            AbstractTeam.CollisionRule lv6;
            AbstractTeam.VisibilityRule lv5;
            MutableText lv4;
            NbtCompound lv = nbt.getCompound(i);
            String string = lv.getString("Name");
            Team lv2 = this.scoreboard.addTeam(string);
            MutableText lv3 = Text.Serialization.fromJson(lv.getString("DisplayName"), registries);
            if (lv3 != null) {
                lv2.setDisplayName(lv3);
            }
            if (lv.contains("TeamColor", NbtElement.STRING_TYPE)) {
                lv2.setColor(Formatting.byName(lv.getString("TeamColor")));
            }
            if (lv.contains("AllowFriendlyFire", NbtElement.NUMBER_TYPE)) {
                lv2.setFriendlyFireAllowed(lv.getBoolean("AllowFriendlyFire"));
            }
            if (lv.contains("SeeFriendlyInvisibles", NbtElement.NUMBER_TYPE)) {
                lv2.setShowFriendlyInvisibles(lv.getBoolean("SeeFriendlyInvisibles"));
            }
            if (lv.contains("MemberNamePrefix", NbtElement.STRING_TYPE) && (lv4 = Text.Serialization.fromJson(lv.getString("MemberNamePrefix"), registries)) != null) {
                lv2.setPrefix(lv4);
            }
            if (lv.contains("MemberNameSuffix", NbtElement.STRING_TYPE) && (lv4 = Text.Serialization.fromJson(lv.getString("MemberNameSuffix"), registries)) != null) {
                lv2.setSuffix(lv4);
            }
            if (lv.contains("NameTagVisibility", NbtElement.STRING_TYPE) && (lv5 = AbstractTeam.VisibilityRule.getRule(lv.getString("NameTagVisibility"))) != null) {
                lv2.setNameTagVisibilityRule(lv5);
            }
            if (lv.contains("DeathMessageVisibility", NbtElement.STRING_TYPE) && (lv5 = AbstractTeam.VisibilityRule.getRule(lv.getString("DeathMessageVisibility"))) != null) {
                lv2.setDeathMessageVisibilityRule(lv5);
            }
            if (lv.contains("CollisionRule", NbtElement.STRING_TYPE) && (lv6 = AbstractTeam.CollisionRule.getRule(lv.getString("CollisionRule"))) != null) {
                lv2.setCollisionRule(lv6);
            }
            this.readTeamPlayersNbt(lv2, lv.getList("Players", NbtElement.STRING_TYPE));
        }
    }

    private void readTeamPlayersNbt(Team team, NbtList nbt) {
        for (int i = 0; i < nbt.size(); ++i) {
            this.scoreboard.addScoreHolderToTeam(nbt.getString(i), team);
        }
    }

    private void readDisplaySlotsNbt(NbtCompound nbt) {
        for (String string : nbt.getKeys()) {
            ScoreboardDisplaySlot lv = ScoreboardDisplaySlot.CODEC.byId(string);
            if (lv == null) continue;
            String string2 = nbt.getString(string);
            ScoreboardObjective lv2 = this.scoreboard.getNullableObjective(string2);
            this.scoreboard.setObjectiveSlot(lv, lv2);
        }
    }

    private void readObjectivesNbt(NbtList nbt, RegistryWrapper.WrapperLookup registries) {
        for (int i = 0; i < nbt.size(); ++i) {
            NbtCompound lv = nbt.getCompound(i);
            String string = lv.getString("CriteriaName");
            ScoreboardCriterion lv2 = ScoreboardCriterion.getOrCreateStatCriterion(string).orElseGet(() -> {
                LOGGER.warn("Unknown scoreboard criteria {}, replacing with {}", (Object)string, (Object)ScoreboardCriterion.DUMMY.getName());
                return ScoreboardCriterion.DUMMY;
            });
            String string2 = lv.getString("Name");
            MutableText lv3 = Text.Serialization.fromJson(lv.getString("DisplayName"), registries);
            ScoreboardCriterion.RenderType lv4 = ScoreboardCriterion.RenderType.getType(lv.getString("RenderType"));
            boolean bl = lv.getBoolean("display_auto_update");
            NumberFormat lv5 = NumberFormatTypes.CODEC.parse(registries.getOps(NbtOps.INSTANCE), lv.get("format")).result().orElse(null);
            this.scoreboard.addObjective(string2, lv2, lv3, lv4, bl, lv5);
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.put("Objectives", this.objectivesToNbt(registryLookup));
        nbt.put("PlayerScores", this.scoreboard.toNbt(registryLookup));
        nbt.put("Teams", this.teamsToNbt(registryLookup));
        this.writeDisplaySlotsNbt(nbt);
        return nbt;
    }

    private NbtList teamsToNbt(RegistryWrapper.WrapperLookup registries) {
        NbtList lv = new NbtList();
        Collection<Team> collection = this.scoreboard.getTeams();
        for (Team lv2 : collection) {
            NbtCompound lv3 = new NbtCompound();
            lv3.putString("Name", lv2.getName());
            lv3.putString("DisplayName", Text.Serialization.toJsonString(lv2.getDisplayName(), registries));
            if (lv2.getColor().getColorIndex() >= 0) {
                lv3.putString("TeamColor", lv2.getColor().getName());
            }
            lv3.putBoolean("AllowFriendlyFire", lv2.isFriendlyFireAllowed());
            lv3.putBoolean("SeeFriendlyInvisibles", lv2.shouldShowFriendlyInvisibles());
            lv3.putString("MemberNamePrefix", Text.Serialization.toJsonString(lv2.getPrefix(), registries));
            lv3.putString("MemberNameSuffix", Text.Serialization.toJsonString(lv2.getSuffix(), registries));
            lv3.putString("NameTagVisibility", lv2.getNameTagVisibilityRule().name);
            lv3.putString("DeathMessageVisibility", lv2.getDeathMessageVisibilityRule().name);
            lv3.putString("CollisionRule", lv2.getCollisionRule().name);
            NbtList lv4 = new NbtList();
            for (String string : lv2.getPlayerList()) {
                lv4.add(NbtString.of(string));
            }
            lv3.put("Players", lv4);
            lv.add(lv3);
        }
        return lv;
    }

    private void writeDisplaySlotsNbt(NbtCompound nbt) {
        NbtCompound lv = new NbtCompound();
        for (ScoreboardDisplaySlot lv2 : ScoreboardDisplaySlot.values()) {
            ScoreboardObjective lv3 = this.scoreboard.getObjectiveForSlot(lv2);
            if (lv3 == null) continue;
            lv.putString(lv2.asString(), lv3.getName());
        }
        if (!lv.isEmpty()) {
            nbt.put("DisplaySlots", lv);
        }
    }

    private NbtList objectivesToNbt(RegistryWrapper.WrapperLookup registries) {
        NbtList lv = new NbtList();
        Collection<ScoreboardObjective> collection = this.scoreboard.getObjectives();
        for (ScoreboardObjective lv2 : collection) {
            NbtCompound lv3 = new NbtCompound();
            lv3.putString("Name", lv2.getName());
            lv3.putString("CriteriaName", lv2.getCriterion().getName());
            lv3.putString("DisplayName", Text.Serialization.toJsonString(lv2.getDisplayName(), registries));
            lv3.putString("RenderType", lv2.getRenderType().getName());
            lv3.putBoolean("display_auto_update", lv2.shouldDisplayAutoUpdate());
            NumberFormat lv4 = lv2.getNumberFormat();
            if (lv4 != null) {
                NumberFormatTypes.CODEC.encodeStart(registries.getOps(NbtOps.INSTANCE), lv4).ifSuccess(arg2 -> lv3.put("format", (NbtElement)arg2));
            }
            lv.add(lv3);
        }
        return lv;
    }
}

