/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.util.profiler.Profiler;

public class GoalSelector {
    private static final PrioritizedGoal REPLACEABLE_GOAL = new PrioritizedGoal(Integer.MAX_VALUE, new Goal(){

        @Override
        public boolean canStart() {
            return false;
        }
    }){

        @Override
        public boolean isRunning() {
            return false;
        }
    };
    private final Map<Goal.Control, PrioritizedGoal> goalsByControl = new EnumMap<Goal.Control, PrioritizedGoal>(Goal.Control.class);
    private final Set<PrioritizedGoal> goals = new ObjectLinkedOpenHashSet<PrioritizedGoal>();
    private final Supplier<Profiler> profiler;
    private final EnumSet<Goal.Control> disabledControls = EnumSet.noneOf(Goal.Control.class);

    public GoalSelector(Supplier<Profiler> profiler) {
        this.profiler = profiler;
    }

    public void add(int priority, Goal goal) {
        this.goals.add(new PrioritizedGoal(priority, goal));
    }

    @VisibleForTesting
    public void clear(Predicate<Goal> predicate) {
        this.goals.removeIf(goal -> predicate.test(goal.getGoal()));
    }

    public void remove(Goal goal) {
        for (PrioritizedGoal lv : this.goals) {
            if (lv.getGoal() != goal || !lv.isRunning()) continue;
            lv.stop();
        }
        this.goals.removeIf(arg2 -> arg2.getGoal() == goal);
    }

    private static boolean usesAny(PrioritizedGoal goal, EnumSet<Goal.Control> controls) {
        for (Goal.Control lv : goal.getControls()) {
            if (!controls.contains((Object)lv)) continue;
            return true;
        }
        return false;
    }

    private static boolean canReplaceAll(PrioritizedGoal goal, Map<Goal.Control, PrioritizedGoal> goalsByControl) {
        for (Goal.Control lv : goal.getControls()) {
            if (goalsByControl.getOrDefault((Object)lv, REPLACEABLE_GOAL).canBeReplacedBy(goal)) continue;
            return false;
        }
        return true;
    }

    public void tick() {
        Profiler lv = this.profiler.get();
        lv.push("goalCleanup");
        for (PrioritizedGoal lv2 : this.goals) {
            if (!lv2.isRunning() || !GoalSelector.usesAny(lv2, this.disabledControls) && lv2.shouldContinue()) continue;
            lv2.stop();
        }
        this.goalsByControl.entrySet().removeIf(entry -> !((PrioritizedGoal)entry.getValue()).isRunning());
        lv.pop();
        lv.push("goalUpdate");
        for (PrioritizedGoal lv2 : this.goals) {
            if (lv2.isRunning() || GoalSelector.usesAny(lv2, this.disabledControls) || !GoalSelector.canReplaceAll(lv2, this.goalsByControl) || !lv2.canStart()) continue;
            for (Goal.Control lv3 : lv2.getControls()) {
                PrioritizedGoal lv4 = this.goalsByControl.getOrDefault((Object)lv3, REPLACEABLE_GOAL);
                lv4.stop();
                this.goalsByControl.put(lv3, lv2);
            }
            lv2.start();
        }
        lv.pop();
        this.tickGoals(true);
    }

    public void tickGoals(boolean tickAll) {
        Profiler lv = this.profiler.get();
        lv.push("goalTick");
        for (PrioritizedGoal lv2 : this.goals) {
            if (!lv2.isRunning() || !tickAll && !lv2.shouldRunEveryTick()) continue;
            lv2.tick();
        }
        lv.pop();
    }

    public Set<PrioritizedGoal> getGoals() {
        return this.goals;
    }

    public void disableControl(Goal.Control control) {
        this.disabledControls.add(control);
    }

    public void enableControl(Goal.Control control) {
        this.disabledControls.remove((Object)control);
    }

    public void setControlEnabled(Goal.Control control, boolean enabled) {
        if (enabled) {
            this.enableControl(control);
        } else {
            this.disableControl(control);
        }
    }
}

