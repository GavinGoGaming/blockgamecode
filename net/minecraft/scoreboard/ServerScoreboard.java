/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreResetS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

public class ServerScoreboard
extends Scoreboard {
    private final MinecraftServer server;
    private final Set<ScoreboardObjective> objectives = Sets.newHashSet();
    private final List<Runnable> updateListeners = Lists.newArrayList();

    public ServerScoreboard(MinecraftServer server) {
        this.server = server;
    }

    @Override
    protected void updateScore(ScoreHolder scoreHolder, ScoreboardObjective objective, ScoreboardScore score) {
        super.updateScore(scoreHolder, objective, score);
        if (this.objectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardScoreUpdateS2CPacket(scoreHolder.getNameForScoreboard(), objective.getName(), score.getScore(), Optional.ofNullable(score.getDisplayText()), Optional.ofNullable(score.getNumberFormat())));
        }
        this.runUpdateListeners();
    }

    @Override
    protected void resetScore(ScoreHolder scoreHolder, ScoreboardObjective objective) {
        super.resetScore(scoreHolder, objective);
        this.runUpdateListeners();
    }

    @Override
    public void onScoreHolderRemoved(ScoreHolder scoreHolder) {
        super.onScoreHolderRemoved(scoreHolder);
        this.server.getPlayerManager().sendToAll(new ScoreboardScoreResetS2CPacket(scoreHolder.getNameForScoreboard(), null));
        this.runUpdateListeners();
    }

    @Override
    public void onScoreRemoved(ScoreHolder scoreHolder, ScoreboardObjective objective) {
        super.onScoreRemoved(scoreHolder, objective);
        if (this.objectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardScoreResetS2CPacket(scoreHolder.getNameForScoreboard(), objective.getName()));
        }
        this.runUpdateListeners();
    }

    @Override
    public void setObjectiveSlot(ScoreboardDisplaySlot slot, @Nullable ScoreboardObjective objective) {
        ScoreboardObjective lv = this.getObjectiveForSlot(slot);
        super.setObjectiveSlot(slot, objective);
        if (lv != objective && lv != null) {
            if (this.getSlot(lv) > 0) {
                this.server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
            } else {
                this.removeScoreboardObjective(lv);
            }
        }
        if (objective != null) {
            if (this.objectives.contains(objective)) {
                this.server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
            } else {
                this.addScoreboardObjective(objective);
            }
        }
        this.runUpdateListeners();
    }

    @Override
    public boolean addScoreHolderToTeam(String scoreHolderName, Team team) {
        if (super.addScoreHolderToTeam(scoreHolderName, team)) {
            this.server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, scoreHolderName, TeamS2CPacket.Operation.ADD));
            this.runUpdateListeners();
            return true;
        }
        return false;
    }

    @Override
    public void removeScoreHolderFromTeam(String scoreHolderName, Team team) {
        super.removeScoreHolderFromTeam(scoreHolderName, team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, scoreHolderName, TeamS2CPacket.Operation.REMOVE));
        this.runUpdateListeners();
    }

    @Override
    public void updateObjective(ScoreboardObjective objective) {
        super.updateObjective(objective);
        this.runUpdateListeners();
    }

    @Override
    public void updateExistingObjective(ScoreboardObjective objective) {
        super.updateExistingObjective(objective);
        if (this.objectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE));
        }
        this.runUpdateListeners();
    }

    @Override
    public void updateRemovedObjective(ScoreboardObjective objective) {
        super.updateRemovedObjective(objective);
        if (this.objectives.contains(objective)) {
            this.removeScoreboardObjective(objective);
        }
        this.runUpdateListeners();
    }

    @Override
    public void updateScoreboardTeamAndPlayers(Team team) {
        super.updateScoreboardTeamAndPlayers(team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, true));
        this.runUpdateListeners();
    }

    @Override
    public void updateScoreboardTeam(Team team) {
        super.updateScoreboardTeam(team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, false));
        this.runUpdateListeners();
    }

    @Override
    public void updateRemovedTeam(Team team) {
        super.updateRemovedTeam(team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateRemovedTeam(team));
        this.runUpdateListeners();
    }

    public void addUpdateListener(Runnable listener) {
        this.updateListeners.add(listener);
    }

    protected void runUpdateListeners() {
        for (Runnable runnable : this.updateListeners) {
            runnable.run();
        }
    }

    public List<Packet<?>> createChangePackets(ScoreboardObjective objective) {
        ArrayList<Packet<?>> list = Lists.newArrayList();
        list.add(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.ADD_MODE));
        for (ScoreboardDisplaySlot lv : ScoreboardDisplaySlot.values()) {
            if (this.getObjectiveForSlot(lv) != objective) continue;
            list.add(new ScoreboardDisplayS2CPacket(lv, objective));
        }
        for (ScoreboardEntry lv2 : this.getScoreboardEntries(objective)) {
            list.add(new ScoreboardScoreUpdateS2CPacket(lv2.owner(), objective.getName(), lv2.value(), Optional.ofNullable(lv2.display()), Optional.ofNullable(lv2.numberFormatOverride())));
        }
        return list;
    }

    public void addScoreboardObjective(ScoreboardObjective objective) {
        List<Packet<?>> list = this.createChangePackets(objective);
        for (ServerPlayerEntity lv : this.server.getPlayerManager().getPlayerList()) {
            for (Packet<?> lv2 : list) {
                lv.networkHandler.sendPacket(lv2);
            }
        }
        this.objectives.add(objective);
    }

    public List<Packet<?>> createRemovePackets(ScoreboardObjective objective) {
        ArrayList<Packet<?>> list = Lists.newArrayList();
        list.add(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE));
        for (ScoreboardDisplaySlot lv : ScoreboardDisplaySlot.values()) {
            if (this.getObjectiveForSlot(lv) != objective) continue;
            list.add(new ScoreboardDisplayS2CPacket(lv, objective));
        }
        return list;
    }

    public void removeScoreboardObjective(ScoreboardObjective objective) {
        List<Packet<?>> list = this.createRemovePackets(objective);
        for (ServerPlayerEntity lv : this.server.getPlayerManager().getPlayerList()) {
            for (Packet<?> lv2 : list) {
                lv.networkHandler.sendPacket(lv2);
            }
        }
        this.objectives.remove(objective);
    }

    public int getSlot(ScoreboardObjective objective) {
        int i = 0;
        for (ScoreboardDisplaySlot lv : ScoreboardDisplaySlot.values()) {
            if (this.getObjectiveForSlot(lv) != objective) continue;
            ++i;
        }
        return i;
    }

    public PersistentState.Type<ScoreboardState> getPersistentStateType() {
        return new PersistentState.Type<ScoreboardState>(this::createState, this::stateFromNbt, DataFixTypes.SAVED_DATA_SCOREBOARD);
    }

    private ScoreboardState createState() {
        ScoreboardState lv = new ScoreboardState(this);
        this.addUpdateListener(lv::markDirty);
        return lv;
    }

    private ScoreboardState stateFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        return this.createState().readNbt(nbt, registryLookup);
    }

    public static enum UpdateMode {
        CHANGE,
        REMOVE;

    }
}

