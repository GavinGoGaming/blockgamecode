/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.world;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.BackupPromptScreen;
import net.minecraft.client.gui.screen.world.OptimizeWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.nbt.NbtCrashException;
import net.minecraft.nbt.NbtException;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.PathUtil;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class EditWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text ENTER_NAME_TEXT = Text.translatable("selectWorld.enterName").formatted(Formatting.GRAY);
    private static final Text RESET_ICON_TEXT = Text.translatable("selectWorld.edit.resetIcon");
    private static final Text OPEN_FOLDER_TEXT = Text.translatable("selectWorld.edit.openFolder");
    private static final Text BACKUP_TEXT = Text.translatable("selectWorld.edit.backup");
    private static final Text BACKUP_FOLDER_TEXT = Text.translatable("selectWorld.edit.backupFolder");
    private static final Text OPTIMIZE_TEXT = Text.translatable("selectWorld.edit.optimize");
    private static final Text CONFIRM_TITLE_TEXT = Text.translatable("optimizeWorld.confirm.title");
    private static final Text CONFIRM_DESCRIPTION_TEXT = Text.translatable("optimizeWorld.confirm.description");
    private static final Text SAVE_TEXT = Text.translatable("selectWorld.edit.save");
    private static final int field_46893 = 200;
    private static final int field_46894 = 4;
    private static final int field_46895 = 98;
    private final DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical().spacing(5);
    private final BooleanConsumer callback;
    private final LevelStorage.Session storageSession;
    private final TextFieldWidget nameFieldWidget;

    public static EditWorldScreen create(MinecraftClient client, LevelStorage.Session session, BooleanConsumer callback) throws IOException {
        LevelSummary lv = session.getLevelSummary(session.readLevelProperties());
        return new EditWorldScreen(client, session, lv.getDisplayName(), callback);
    }

    private EditWorldScreen(MinecraftClient client, LevelStorage.Session session, String levelName, BooleanConsumer callback) {
        super(Text.translatable("selectWorld.edit.title"));
        this.callback = callback;
        this.storageSession = session;
        TextRenderer lv = client.textRenderer;
        this.layout.add(new EmptyWidget(200, 20));
        this.layout.add(new TextWidget(ENTER_NAME_TEXT, lv));
        this.nameFieldWidget = this.layout.add(new TextFieldWidget(lv, 200, 20, ENTER_NAME_TEXT));
        this.nameFieldWidget.setText(levelName);
        DirectionalLayoutWidget lv2 = DirectionalLayoutWidget.horizontal().spacing(4);
        ButtonWidget lv3 = lv2.add(ButtonWidget.builder(SAVE_TEXT, button -> this.commit(this.nameFieldWidget.getText())).width(98).build());
        lv2.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close()).width(98).build());
        this.nameFieldWidget.setChangedListener(name -> {
            arg.active = !StringHelper.isBlank(name);
        });
        this.layout.add(ButtonWidget.builder((Text)EditWorldScreen.RESET_ICON_TEXT, (ButtonWidget.PressAction)(ButtonWidget.PressAction)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/widget/ButtonWidget;)V, method_54608(net.minecraft.world.level.storage.LevelStorage$Session net.minecraft.client.gui.widget.ButtonWidget ), (Lnet/minecraft/client/gui/widget/ButtonWidget;)V)((LevelStorage.Session)session)).width((int)200).build()).active = session.getIconFile().filter(path -> Files.isRegularFile(path, new LinkOption[0])).isPresent();
        this.layout.add(ButtonWidget.builder(OPEN_FOLDER_TEXT, button -> Util.getOperatingSystem().open(session.getDirectory(WorldSavePath.ROOT))).width(200).build());
        this.layout.add(ButtonWidget.builder(BACKUP_TEXT, button -> {
            boolean bl = EditWorldScreen.backupLevel(session);
            this.callback.accept(!bl);
        }).width(200).build());
        this.layout.add(ButtonWidget.builder(BACKUP_FOLDER_TEXT, button -> {
            LevelStorage lv = client.getLevelStorage();
            Path path = lv.getBackupsDirectory();
            try {
                PathUtil.createDirectories(path);
            } catch (IOException iOException) {
                throw new RuntimeException(iOException);
            }
            Util.getOperatingSystem().open(path);
        }).width(200).build());
        this.layout.add(ButtonWidget.builder(OPTIMIZE_TEXT, button -> client.setScreen(new BackupPromptScreen(() -> client.setScreen(this), (backup, eraseCache) -> {
            if (backup) {
                EditWorldScreen.backupLevel(session);
            }
            client.setScreen(OptimizeWorldScreen.create(client, this.callback, client.getDataFixer(), session, eraseCache));
        }, CONFIRM_TITLE_TEXT, CONFIRM_DESCRIPTION_TEXT, true))).width(200).build());
        this.layout.add(new EmptyWidget(200, 20));
        this.layout.add(lv2);
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.nameFieldWidget);
    }

    @Override
    protected void init() {
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(this.layout, this.getNavigationFocus());
    }

    @Override
    public void close() {
        this.callback.accept(false);
    }

    private void commit(String levelName) {
        try {
            this.storageSession.save(levelName);
        } catch (IOException | NbtCrashException | NbtException exception) {
            LOGGER.error("Failed to access world '{}'", (Object)this.storageSession.getDirectoryName(), (Object)exception);
            SystemToast.addWorldAccessFailureToast(this.client, this.storageSession.getDirectoryName());
        }
        this.callback.accept(true);
    }

    public static boolean backupLevel(LevelStorage.Session storageSession) {
        long l = 0L;
        IOException iOException = null;
        try {
            l = storageSession.createBackup();
        } catch (IOException iOException2) {
            iOException = iOException2;
        }
        if (iOException != null) {
            MutableText lv = Text.translatable("selectWorld.edit.backupFailed");
            MutableText lv2 = Text.literal(iOException.getMessage());
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP, lv, lv2));
            return false;
        }
        MutableText lv = Text.translatable("selectWorld.edit.backupCreated", storageSession.getDirectoryName());
        MutableText lv2 = Text.translatable("selectWorld.edit.backupSize", MathHelper.ceil((double)l / 1048576.0));
        MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP, lv, lv2));
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    private static /* synthetic */ void method_54608(LevelStorage.Session arg, ButtonWidget arg2) {
        arg.getIconFile().ifPresent(path -> FileUtils.deleteQuietly(path.toFile()));
        arg2.active = false;
    }
}

