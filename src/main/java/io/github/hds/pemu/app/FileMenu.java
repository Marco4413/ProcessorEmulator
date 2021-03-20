package io.github.hds.pemu.app;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public class FileMenu extends JMenu {

    private final Application app;
    private final JFileChooser FILE_DIALOG;

    private final JMenuItem OPEN_PROGRAM;
    private final JMenuItem QUIT;

    private final ImageIcon ICON_OPEN_PROGRAM;
    private final ImageIcon ICON_QUIT;

    protected FileMenu(@NotNull Application parentApp) {
        super("File");
        app = parentApp;

        setMnemonic('F');

        ICON_OPEN_PROGRAM = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/open_file.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        OPEN_PROGRAM = new JMenuItem("Open Program", 'O');
        OPEN_PROGRAM.setIcon(ICON_OPEN_PROGRAM);
        OPEN_PROGRAM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        OPEN_PROGRAM.addActionListener(this::openProgram);
        add(OPEN_PROGRAM);

        ICON_QUIT = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/quit.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        QUIT = new JMenuItem("Quit", 'Q');
        QUIT.setIcon(ICON_QUIT);
        QUIT.addActionListener(app::close);
        add(QUIT);

        FILE_DIALOG = new JFileChooser();
        FILE_DIALOG.setCurrentDirectory(new File("./"));
        FILE_DIALOG.setMultiSelectionEnabled(false);
        FILE_DIALOG.setFileFilter(new FileNameExtensionFilter("PEMU program", "pemu"));
    }

    private void openProgram(ActionEvent e) {
        if (FILE_DIALOG.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            app.setCurrentProgram(FILE_DIALOG.getSelectedFile());
    }

}
