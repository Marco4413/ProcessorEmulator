package io.github.hds.pemu.app;

import io.github.hds.pemu.utils.IconUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class FileMenu extends JMenu {

    private final Application app;

    private final JMenuItem OPEN_PROGRAM;
    private final JMenuItem QUIT;

    private final ImageIcon ICON_QUIT;

    protected FileMenu(@NotNull Application parentApp) {
        super("File");
        app = parentApp;

        setMnemonic('F');

        OPEN_PROGRAM = new JMenuItem("Open Program", 'O');
        OPEN_PROGRAM.setIcon(GFileDialog.ICON_OPEN);
        OPEN_PROGRAM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        OPEN_PROGRAM.addActionListener(this::openProgram);
        add(OPEN_PROGRAM);

        ICON_QUIT = IconUtils.importIcon("/assets/quit.png", Application.MENU_ITEM_ICON_SIZE);;

        QUIT = new JMenuItem("Quit", 'Q');
        QUIT.setIcon(ICON_QUIT);
        QUIT.addActionListener(app::close);
        add(QUIT);
    }

    private void openProgram(ActionEvent e) {
        GFileDialog gFileDialog = GFileDialog.getInstance();
        if (gFileDialog.showOpenDialog(this, GFileDialog.PEMU_FILES) == JFileChooser.APPROVE_OPTION)
            app.setCurrentProgram(gFileDialog.getSelectedFile());
    }

}
