/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.multiplayer;

import com.mojang.logging.LogUtils;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.network.LanServerQueryManager;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class MultiplayerScreen
extends Screen {
    public static final int field_41849 = 308;
    public static final int field_41850 = 100;
    public static final int field_41851 = 74;
    public static final int field_41852 = 64;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MultiplayerServerListPinger serverListPinger = new MultiplayerServerListPinger();
    private final Screen parent;
    protected MultiplayerServerListWidget serverListWidget;
    private ServerList serverList;
    private ButtonWidget buttonEdit;
    private ButtonWidget buttonJoin;
    private ButtonWidget buttonDelete;
    private ServerInfo selectedEntry;
    private LanServerQueryManager.LanServerEntryList lanServers;
    @Nullable
    private LanServerQueryManager.LanServerDetector lanServerDetector;
    private boolean initialized;

    public MultiplayerScreen(Screen parent) {
        super(Text.translatable("multiplayer.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (this.initialized) {
            this.serverListWidget.setDimensionsAndPosition(this.width, this.height - 64 - 32, 0, 32);
        } else {
            this.initialized = true;
            this.serverList = new ServerList(this.client);
            this.serverList.loadFile();
            this.lanServers = new LanServerQueryManager.LanServerEntryList();
            try {
                this.lanServerDetector = new LanServerQueryManager.LanServerDetector(this.lanServers);
                this.lanServerDetector.start();
            } catch (Exception exception) {
                LOGGER.warn("Unable to start LAN server detection: {}", (Object)exception.getMessage());
            }
            this.serverListWidget = new MultiplayerServerListWidget(this, this.client, this.width, this.height - 64 - 32, 32, 36);
            this.serverListWidget.setServers(this.serverList);
        }
        this.addDrawableChild(this.serverListWidget);
        this.buttonJoin = this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.select"), button -> this.connect()).width(100).build());
        ButtonWidget lv = this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.direct"), button -> {
            this.selectedEntry = new ServerInfo(I18n.translate("selectServer.defaultName", new Object[0]), "", ServerInfo.ServerType.OTHER);
            this.client.setScreen(new DirectConnectScreen(this, this::directConnect, this.selectedEntry));
        }).width(100).build());
        ButtonWidget lv2 = this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.add"), button -> {
            this.selectedEntry = new ServerInfo(I18n.translate("selectServer.defaultName", new Object[0]), "", ServerInfo.ServerType.OTHER);
            this.client.setScreen(new AddServerScreen(this, this::addEntry, this.selectedEntry));
        }).width(100).build());
        this.buttonEdit = this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.edit"), button -> {
            MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
            if (lv instanceof MultiplayerServerListWidget.ServerEntry) {
                ServerInfo lv2 = ((MultiplayerServerListWidget.ServerEntry)lv).getServer();
                this.selectedEntry = new ServerInfo(lv2.name, lv2.address, ServerInfo.ServerType.OTHER);
                this.selectedEntry.copyWithSettingsFrom(lv2);
                this.client.setScreen(new AddServerScreen(this, this::editEntry, this.selectedEntry));
            }
        }).width(74).build());
        this.buttonDelete = this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.delete"), button -> {
            String string;
            MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
            if (lv instanceof MultiplayerServerListWidget.ServerEntry && (string = ((MultiplayerServerListWidget.ServerEntry)lv).getServer().name) != null) {
                MutableText lv2 = Text.translatable("selectServer.deleteQuestion");
                MutableText lv3 = Text.translatable("selectServer.deleteWarning", string);
                MutableText lv4 = Text.translatable("selectServer.deleteButton");
                Text lv5 = ScreenTexts.CANCEL;
                this.client.setScreen(new ConfirmScreen(this::removeEntry, lv2, lv3, lv4, lv5));
            }
        }).width(74).build());
        ButtonWidget lv3 = this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.refresh"), button -> this.refresh()).width(74).build());
        ButtonWidget lv4 = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).width(74).build());
        DirectionalLayoutWidget lv5 = DirectionalLayoutWidget.vertical();
        AxisGridWidget lv6 = lv5.add(new AxisGridWidget(308, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
        lv6.add(this.buttonJoin);
        lv6.add(lv);
        lv6.add(lv2);
        lv5.add(EmptyWidget.ofHeight(4));
        AxisGridWidget lv7 = lv5.add(new AxisGridWidget(308, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
        lv7.add(this.buttonEdit);
        lv7.add(this.buttonDelete);
        lv7.add(lv3);
        lv7.add(lv4);
        lv5.refreshPositions();
        SimplePositioningWidget.setPos(lv5, 0, this.height - 64, this.width, 64);
        this.updateButtonActivationStates();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void tick() {
        super.tick();
        List<LanServerInfo> list = this.lanServers.getEntriesIfUpdated();
        if (list != null) {
            this.serverListWidget.setLanServers(list);
        }
        this.serverListPinger.tick();
    }

    @Override
    public void removed() {
        if (this.lanServerDetector != null) {
            this.lanServerDetector.interrupt();
            this.lanServerDetector = null;
        }
        this.serverListPinger.cancel();
        this.serverListWidget.onRemoved();
    }

    private void refresh() {
        this.client.setScreen(new MultiplayerScreen(this.parent));
    }

    private void removeEntry(boolean confirmedAction) {
        MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
        if (confirmedAction && lv instanceof MultiplayerServerListWidget.ServerEntry) {
            this.serverList.remove(((MultiplayerServerListWidget.ServerEntry)lv).getServer());
            this.serverList.saveFile();
            this.serverListWidget.setSelected((MultiplayerServerListWidget.Entry)null);
            this.serverListWidget.setServers(this.serverList);
        }
        this.client.setScreen(this);
    }

    private void editEntry(boolean confirmedAction) {
        MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
        if (confirmedAction && lv instanceof MultiplayerServerListWidget.ServerEntry) {
            ServerInfo lv2 = ((MultiplayerServerListWidget.ServerEntry)lv).getServer();
            lv2.name = this.selectedEntry.name;
            lv2.address = this.selectedEntry.address;
            lv2.copyWithSettingsFrom(this.selectedEntry);
            this.serverList.saveFile();
            this.serverListWidget.setServers(this.serverList);
        }
        this.client.setScreen(this);
    }

    private void addEntry(boolean confirmedAction) {
        if (confirmedAction) {
            ServerInfo lv = this.serverList.tryUnhide(this.selectedEntry.address);
            if (lv != null) {
                lv.copyFrom(this.selectedEntry);
                this.serverList.saveFile();
            } else {
                this.serverList.add(this.selectedEntry, false);
                this.serverList.saveFile();
            }
            this.serverListWidget.setSelected((MultiplayerServerListWidget.Entry)null);
            this.serverListWidget.setServers(this.serverList);
        }
        this.client.setScreen(this);
    }

    private void directConnect(boolean confirmedAction) {
        if (confirmedAction) {
            ServerInfo lv = this.serverList.get(this.selectedEntry.address);
            if (lv == null) {
                this.serverList.add(this.selectedEntry, true);
                this.serverList.saveFile();
                this.connect(this.selectedEntry);
            } else {
                this.connect(lv);
            }
        } else {
            this.client.setScreen(this);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_F5) {
            this.refresh();
            return true;
        }
        if (this.serverListWidget.getSelectedOrNull() != null) {
            if (KeyCodes.isToggle(keyCode)) {
                this.connect();
                return true;
            }
            return this.serverListWidget.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    public void connect() {
        MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
        if (lv instanceof MultiplayerServerListWidget.ServerEntry) {
            this.connect(((MultiplayerServerListWidget.ServerEntry)lv).getServer());
        } else if (lv instanceof MultiplayerServerListWidget.LanServerEntry) {
            LanServerInfo lv2 = ((MultiplayerServerListWidget.LanServerEntry)lv).getLanServerEntry();
            this.connect(new ServerInfo(lv2.getMotd(), lv2.getAddressPort(), ServerInfo.ServerType.LAN));
        }
    }

    private void connect(ServerInfo entry) {
        ConnectScreen.connect(this, this.client, ServerAddress.parse(entry.address), entry, false, null);
    }

    public void select(MultiplayerServerListWidget.Entry entry) {
        this.serverListWidget.setSelected(entry);
        this.updateButtonActivationStates();
    }

    protected void updateButtonActivationStates() {
        this.buttonJoin.active = false;
        this.buttonEdit.active = false;
        this.buttonDelete.active = false;
        MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
        if (lv != null && !(lv instanceof MultiplayerServerListWidget.ScanningEntry)) {
            this.buttonJoin.active = true;
            if (lv instanceof MultiplayerServerListWidget.ServerEntry) {
                this.buttonEdit.active = true;
                this.buttonDelete.active = true;
            }
        }
    }

    public MultiplayerServerListPinger getServerListPinger() {
        return this.serverListPinger;
    }

    public ServerList getServerList() {
        return this.serverList;
    }
}

