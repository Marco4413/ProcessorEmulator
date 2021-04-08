package io.github.hds.pemu.app;

import io.github.hds.pemu.Main;
import io.github.hds.pemu.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.function.Function;

public class Console {

    public static final @NotNull ConsoleComponent POutput = new ConsoleComponent();
    public static final @NotNull ConsoleComponent Debug = new ConsoleComponent();
    public static final @NotNull ConsoleContextualMenu CTX_MENU = new ConsoleContextualMenu();

    private static class ConsoleContextualMenu extends JPopupMenu implements ITranslatable {
        private final TJMenuItem SAVE;
        private final TJMenuItem CLEAR;
        private final TJMenuItem RESET_FONT_SIZE;

        private final ImageIcon ICON_CLEAR;
        private final ImageIcon ICON_RESET_ZOOM;

        private @NotNull String localeSaveErrorPanelTitle = "";
        private @NotNull String localeSaveErrorPanelMsg = "";

        protected ConsoleContextualMenu() {
            super();

            TranslationManager.addTranslationListener(this);

            Function<TJMenuItem, Boolean> enableCondition = i -> getInvoker() instanceof ConsoleComponent && ((ConsoleComponent) getInvoker()).getText().length() > 0;
            SAVE = new TJMenuItem(enableCondition);
            SAVE.setIcon(GFileDialog.ICON_SAVE);
            SAVE.addActionListener(this::saveConsole);
            add(SAVE);

            ICON_CLEAR = IconUtils.importIcon("/assets/clear.png", Application.MENU_ITEM_ICON_SIZE);

            CLEAR = new TJMenuItem(enableCondition);
            CLEAR.setIcon(ICON_CLEAR);
            CLEAR.addActionListener(this::clearConsole);
            add(CLEAR);

            ICON_RESET_ZOOM = IconUtils.importIcon("/assets/reset_zoom.png", Application.MENU_ITEM_ICON_SIZE);

            RESET_FONT_SIZE = new TJMenuItem(i -> getInvoker() instanceof ConsoleComponent && ((ConsoleComponent) getInvoker()).getFontSize() != ConsoleComponent.DEFAULT_FONT_SIZE);
            RESET_FONT_SIZE.setIcon(ICON_RESET_ZOOM);
            RESET_FONT_SIZE.addActionListener(this::resetZoom);
            add(RESET_FONT_SIZE);
        }

        @Override
        public void updateTranslations(@NotNull Translation translation) {
            translation.translateComponent("consoleContextualMenu.save", SAVE);
            translation.translateComponent("consoleContextualMenu.clear", CLEAR);
            translation.translateComponent("consoleContextualMenu.resetZoom", RESET_FONT_SIZE);

            localeSaveErrorPanelTitle = translation.getOrDefault("consoleContextualMenu.saveErrorPanelTitle");
            localeSaveErrorPanelMsg = translation.getOrDefault("consoleContextualMenu.saveErrorPanelMsg");
        }

        public void saveConsole(ActionEvent e) {
            if (!(getInvoker() instanceof  ConsoleComponent)) return;
            GFileDialog gFileDialog = GFileDialog.getInstance();
            if (gFileDialog.showSaveDialog(this, GFileDialog.getTextFileFilter()) == JFileChooser.APPROVE_OPTION) {
                File file = gFileDialog.getSelectedFile();
                try {
                    PrintWriter writer = new PrintWriter(file, "UTF-8");
                    writer.print(((ConsoleComponent) getInvoker()).getText());
                    writer.close();
                } catch (Exception err) {
                    JOptionPane.showMessageDialog(
                            this, StringUtils.format(localeSaveErrorPanelMsg, file.getName(), err.getMessage()),
                            StringUtils.format(localeSaveErrorPanelTitle, gFileDialog.getDialogTitle()), JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }

        public void clearConsole(ActionEvent e) {
            if (!(getInvoker() instanceof  ConsoleComponent)) return;
            ((ConsoleComponent) getInvoker()).clear();
        }

        public void resetZoom(ActionEvent e) {
            if (!(getInvoker() instanceof  ConsoleComponent)) return;
            ((ConsoleComponent) getInvoker()).resetFontSize();
        }
    }

    public static class ConsoleComponent extends JTextArea {

        public static final int DEFAULT_FONT_SIZE = 12;

        public ConsoleComponent() {
            super();

            setEditable(false);
            setFont(new Font("Consolas", Font.PLAIN, DEFAULT_FONT_SIZE));

            DefaultCaret caret = (DefaultCaret) getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3)
                        CTX_MENU.show(ConsoleComponent.this, e.getX(), e.getY());
                }
            });

            addMouseWheelListener(new MouseAdapter() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (e.getModifiers() == KeyEvent.CTRL_MASK)
                        setFontSize(getFontSize() - e.getWheelRotation());
                }
            });
        }

        public int getFontSize() {
            return getFont().getSize();
        }

        public void setFontSize(int size) {
            Font oldFont = getFont();
            setFont(new Font(oldFont.getFamily(), oldFont.getStyle(), Math.max(size, 0)));
        }

        public void resetFontSize() {
            setFontSize(DEFAULT_FONT_SIZE);
        }

        public synchronized void clear() {
            setText("");
        }

        public synchronized void print(String... strings) {
            for (String string : strings)
                append(string);
        }

        public synchronized void print(char... characters) {
            for (char character : characters)
                if (character != '\0')
                    append(String.valueOf(character));
        }

        public synchronized void print(int... integers) {
            for (int integer : integers)
                append(String.valueOf(integer));
        }

        public synchronized void println(String... strings) {
            print(strings);
            print('\n');
        }

        public synchronized void println(char... characters) {
            print(characters);
            print('\n');
        }

        public synchronized void println(int... integers) {
            print(integers);
            print('\n');
        }

        public synchronized void printStackTrace(@NotNull Exception err) {
            println(StringUtils.stackTraceAsString(err));
        }

        public synchronized void printStackTrace(@NotNull Exception err, boolean printBacktraceForKnownExceptions) {
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
