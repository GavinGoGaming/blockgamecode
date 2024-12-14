/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.state.StateManager;

@Environment(value=EnvType.CLIENT)
public class OrMultipartModelSelector
implements MultipartModelSelector {
    public static final String KEY = "OR";
    private final Iterable<? extends MultipartModelSelector> selectors;

    public OrMultipartModelSelector(Iterable<? extends MultipartModelSelector> selectors) {
        this.selectors = selectors;
    }

    @Override
    public Predicate<BlockState> getPredicate(StateManager<Block, BlockState> arg) {
        List list = Streams.stream(this.selectors).map(selector -> selector.getPredicate(arg)).collect(Collectors.toList());
        return state -> list.stream().anyMatch(predicate -> predicate.test(state));
    }
}

