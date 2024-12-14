/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.world;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.WorldScreenOptionGrid;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(value=EnvType.CLIENT)
public class ExperimentsScreen
extends Screen {
    private static final Text TITLE = Text.translatable("selectWorld.experiments");
    private static final Text INFO_TEXT = Text.translatable("selectWorld.experiments.info").formatted(Formatting.RED);
    private static final int INFO_WIDTH = 310;
    private final ThreePartsLayoutWidget experimentToggleList = new ThreePartsLayoutWidget(this);
    private final Screen parent;
    private final ResourcePackManager resourcePackManager;
    private final Consumer<ResourcePackManager> applier;
    private final Object2BooleanMap<ResourcePackProfile> experiments = new Object2BooleanLinkedOpenHashMap<ResourcePackProfile>();

    public ExperimentsScreen(Screen parent, ResourcePackManager resourcePackManager, Consumer<ResourcePackManager> applier) {
        super(TITLE);
        this.parent = parent;
        this.resourcePackManager = resourcePackManager;
        this.applier = applier;
        for (ResourcePackProfile lv : resourcePackManager.getProfiles()) {
            if (lv.getSource() != ResourcePackSource.FEATURE) continue;
            this.experiments.put(lv, resourcePackManager.getEnabledProfiles().contains(lv));
        }
    }

    @Override
    protected void init() {
        this.experimentToggleList.addHeader(TITLE, this.textRenderer);
        DirectionalLayoutWidget lv = this.experimentToggleList.addBody(DirectionalLayoutWidget.vertical());
        lv.add(new MultilineTextWidget(INFO_TEXT, this.textRenderer).setMaxWidth(310), positioner -> positioner.marginBottom(15));
        WorldScreenOptionGrid.Builder lv2 = WorldScreenOptionGrid.builder(310).withTooltipBox(2, true).setRowSpacing(4);
        this.experiments.forEach((pack, enabled2) -> lv2.add(ExperimentsScreen.getDataPackName(pack), () -> this.experiments.getBoolean(pack), enabled -> this.experiments.put((ResourcePackProfile)pack, (boolean)enabled)).tooltip(pack.getDescription()));
        lv2.build(lv::add);
        DirectionalLayoutWidget lv3 = this.experimentToggleList.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        lv3.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.applyAndClose()).build());
        lv3.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close()).build());
        this.experimentToggleList.forEachChild(widget -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(widget);
        });
        this.initTabNavigation();
    }

    private static Text getDataPackName(ResourcePackProfile packProfile) {
        String string = "dataPack." + packProfile.getId() + ".name";
        return I18n.hasTranslation(string) ? Text.translatable(string) : packProfile.getDisplayName();
    }

    @Override
    protected void initTabNavigation() {
        this.experimentToggleList.refreshPositions();
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(super.getNarratedTitle(), INFO_TEXT);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private void applyAndClose() {
        ArrayList<ResourcePackProfile> list = new ArrayList<ResourcePackProfile>(this.resourcePackManager.getEnabledProfiles());
        ArrayList list2 = new ArrayList();
        this.experiments.forEach((pack, enabled) -> {
            list.remove(pack);
            if (enabled.booleanValue()) {
                list2.add(pack);
            }
        });
        list.addAll(Lists.reverse(list2));
        this.resourcePackManager.setEnabledProfiles(list.stream().map(ResourcePackProfile::getId).toList());
        this.applier.accept(this.resourcePackManager);
    }
}

