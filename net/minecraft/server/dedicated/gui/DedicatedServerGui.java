/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.gui;

import com.google.common.collect.Lists;
import com.mojang.logging.LogQueues;
import com.mojang.logging.LogUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.gui.PlayerListGui;
import net.minecraft.server.dedicated.gui.PlayerStatsGui;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.slf4j.Logger;

public class DedicatedServerGui
extends JComponent {
    private static final Font FONT_MONOSPACE = new Font("Monospaced", 0, 12);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TITLE = "Minecraft server";
    private static final String SHUTTING_DOWN_TITLE = "Minecraft server - shutting down!";
    private final MinecraftDedicatedServer server;
    private Thread consoleUpdateThread;
    private final Collection<Runnable> stopTasks = Lists.newArrayList();
    final AtomicBoolean stopped = new AtomicBoolean();

    public static DedicatedServerGui create(final MinecraftDedicatedServer server) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            // empty catch block
        }
        final JFrame jFrame = new JFrame(TITLE);
        final DedicatedServerGui lv = new DedicatedServerGui(server);
        jFrame.setDefaultCloseOperation(2);
        jFrame.add(lv);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
        jFrame.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent event) {
                if (!lv.stopped.getAndSet(true)) {
                    jFrame.setTitle(DedicatedServerGui.SHUTTING_DOWN_TITLE);
                    server.stop(true);
                    lv.runStopTasks();
                }
            }
        });
        lv.addStopTask(jFrame::dispose);
        lv.start();
        return lv;
    }

    private DedicatedServerGui(MinecraftDedicatedServer server) {
        this.server = server;
        this.setPreferredSize(new Dimension(854, 480));
        this.setLayout(new BorderLayout());
        try {
            this.add((Component)this.createLogPanel(), "Center");
            this.add((Component)this.createStatsPanel(), "West");
        } catch (Exception exception) {
            LOGGER.error("Couldn't build server GUI", exception);
        }
    }

    public void addStopTask(Runnable task) {
        this.stopTasks.add(task);
    }

    private JComponent createStatsPanel() {
        JPanel jPanel = new JPanel(new BorderLayout());
        PlayerStatsGui lv = new PlayerStatsGui(this.server);
        this.stopTasks.add(lv::stop);
        jPanel.add((Component)lv, "North");
        jPanel.add((Component)this.createPlaysPanel(), "Center");
        jPanel.setBorder(new TitledBorder(new EtchedBorder(), "Stats"));
        return jPanel;
    }

    private JComponent createPlaysPanel() {
        PlayerListGui jList = new PlayerListGui(this.server);
        JScrollPane jScrollPane = new JScrollPane(jList, 22, 30);
        jScrollPane.setBorder(new TitledBorder(new EtchedBorder(), "Players"));
        return jScrollPane;
    }

    private JComponent createLogPanel() {
        JPanel jPanel = new JPanel(new BorderLayout());
        JTextArea jTextArea = new JTextArea();
        JScrollPane jScrollPane = new JScrollPane(jTextArea, 22, 30);
        jTextArea.setEditable(false);
        jTextArea.setFont(FONT_MONOSPACE);
        JTextField jTextField = new JTextField();
        jTextField.addActionListener(event -> {
            String string = jTextField.getText().trim();
            if (!string.isEmpty()) {
                this.server.enqueueCommand(string, this.server.getCommandSource());
            }
            jTextField.setText("");
        });
        jTextArea.addFocusListener(new FocusAdapter(this){

            @Override
            public void focusGained(FocusEvent event) {
            }
        });
        jPanel.add((Component)jScrollPane, "Center");
        jPanel.add((Component)jTextField, "South");
        jPanel.setBorder(new TitledBorder(new EtchedBorder(), "Log and chat"));
        this.consoleUpdateThread = new Thread(() -> {
            String string;
            while ((string = LogQueues.getNextLogEvent("ServerGuiConsole")) != null) {
                this.appendToConsole(jTextArea, jScrollPane, string);
            }
        });
        this.consoleUpdateThread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
        this.consoleUpdateThread.setDaemon(true);
        return jPanel;
    }

    public void start() {
        this.consoleUpdateThread.start();
    }

    public void stop() {
        if (!this.stopped.getAndSet(true)) {
            this.runStopTasks();
        }
    }

    void runStopTasks() {
        this.stopTasks.forEach(Runnable::run);
    }

    public void appendToConsole(JTextArea textArea, JScrollPane scrollPane, String message) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> this.appendToConsole(textArea, scrollPane, message));
            return;
        }
        Document document = textArea.getDocument();
        JScrollBar jScrollBar = scrollPane.getVerticalScrollBar();
        boolean bl = false;
        if (scrollPane.getViewport().getView() == textArea) {
            bl = (double)jScrollBar.getValue() + jScrollBar.getSize().getHeight() + (double)(FONT_MONOSPACE.getSize() * 4) > (double)jScrollBar.getMaximum();
        }
        try {
            document.insertString(document.getLength(), message, null);
        } catch (BadLocationException badLocationException) {
            // empty catch block
        }
        if (bl) {
            jScrollBar.setValue(Integer.MAX_VALUE);
        }
    }
}

