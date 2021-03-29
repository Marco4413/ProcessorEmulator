package io.github.hds.pemu.app;

import io.github.hds.pemu.Main;
import io.github.hds.pemu.utils.IconUtils;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.function.Function;

public class Console {

    public static final @NotNull ConsoleComponent POutput = new ConsoleComponent();
    public static final @NotNull ConsoleComponent Debug = new ConsoleComponent();
    public static final @NotNull ConsoleContextualMenu CTX_MENU = new ConsoleContextualMenu();

    private static class ConsoleContextualMenu extends JPopupMenu {
        private final TJMenuItem SAVE;
        private final TJMenuItem CLEAR;

        private final ImageIcon ICON_CLEAR;

        protected ConsoleContextualMenu() {
            super();

            Function<TJMenuItem, Boolean> enableCondition = i -> getInvoker() instanceof ConsoleComponent && ((ConsoleComponent) getInvoker()).getText().length() > 0;
            SAVE = new TJMenuItem("Save", 'S', enableCondition);
            SAVE.setIcon(GFileDialog.ICON_SAVE);
            SAVE.addActionListener(this::saveConsole);
            add(SAVE);

            ICON_CLEAR = IconUtils.importIcon("/assets/clear.png", Application.MENU_ITEM_ICON_SIZE);

            CLEAR = new TJMenuItem("Clear", 'C', enableCondition);
            CLEAR.setIcon(ICON_CLEAR);
            CLEAR.addActionListener(this::clearConsole);
            add(CLEAR);
        }

        public void saveConsole(ActionEvent e) {
            if (!(getInvoker() instanceof  ConsoleComponent)) return;
            GFileDialog gFileDialog = GFileDialog.getInstance();
            if (gFileDialog.showSaveDialog(this, GFileDialog.TEXT_FILES) == JFileChooser.APPROVE_OPTION) {
                File file = gFileDialog.getSelectedFile();
                try {
                    PrintWriter writer = new PrintWriter(file, "UTF-8");
                    writer.print(((ConsoleComponent) getInvoker()).getText());
                    writer.close();
                    throw new IllegalStateException("File error");
                } catch (Exception err) {
                    JOptionPane.showMessageDialog(
                            this, file.getName() + '\n' + "Couldn't write to file:\n" + err.getMessage(),
                            "Error " + gFileDialog.getDialogTitle(), JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }

        public void clearConsole(ActionEvent e) {
            if (!(getInvoker() instanceof  ConsoleComponent)) return;
            ((ConsoleComponent) getInvoker()).clear();
        }
    }

    public static class ConsoleComponent extends JTextArea {

        public ConsoleComponent() {
            super();

            setEditable(false);
            setFont(new Font("Consolas", Font.PLAIN, 12));

            DefaultCaret caret = (DefaultCaret) getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3)
                        CTX_MENU.show(ConsoleComponent.this, e.getX(), e.getY());
                }
            });
        }

        public void clear() {
            setText("");
        }

        public void print(String... strings) {
            for (String string : strings)
                append(string);
        }

        public void print(char... characters) {
            for (char character : characters)
                if (character != '\0')
                    append(String.valueOf(character));
        }

        public void print(int... integers) {
            for (int integer : integers)
                append(String.valueOf(integer));
        }

        public void println(String... strings) {
            print(strings);
            print('\n');
        }

        public void println(char... characters) {
            print(characters);
            print('\n');
        }

        public void println(int... integers) {
            print(integers);
            print('\n');
        }

        public void printStackTrace(@NotNull Exception err) {
            println(StringUtils.stackTraceAsString(err));
        }

        public void printStackTrace(@NotNull Exception err, boolean printBacktraceForKnownExceptions) {
            if (printBacktraceForKnownExceptions)
                printStackTrace(err);
            else {
                boolean isKnown = err.getClass().getPackage().getName().startsWith(Main.class.getPackage().getName());
                if (isKnown) println(err.getMessage());
                else printStackTrace(err);
            }
        }

    }

}
