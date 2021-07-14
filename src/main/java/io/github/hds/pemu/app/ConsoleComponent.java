package io.github.hds.pemu.app;

import io.github.hds.pemu.utils.IClearable;
import io.github.hds.pemu.utils.IPrintable;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public final class ConsoleComponent extends JTextArea implements IPrintable, IClearable {

    public static final int DEFAULT_FONT_SIZE = 12;

    protected ConsoleComponent() {
        super();

        setEditable(false);
        setFont(new Font("Consolas", Font.PLAIN, DEFAULT_FONT_SIZE));

        DefaultCaret caret = (DefaultCaret) getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3)
                    ConsoleContextualMenu.getInstance().show(ConsoleComponent.this, e.getX(), e.getY());
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

    @Override
    public synchronized void print(String string) {
        append(string);
    }

    @Override
    public synchronized void print(char character) {
        append(String.valueOf(character));
    }

    @Override
    public synchronized void print(int integer) {
        append(String.valueOf(integer));
    }

    public synchronized void printStackTrace(@NotNull Exception err) {
        println(StringUtils.stackTraceAsString(err));
    }

}
