package io.github.hds.pemu.app;

import io.github.hds.pemu.utils.IClearable;
import io.github.hds.pemu.utils.IConsole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Objects;

public final class ConsoleComponent extends JTextArea implements IConsole, IClearable {

    public static final int DEFAULT_FONT_SIZE = 12;
    private static final int MARGIN = 3;

    private final Writer WRITER = new Writer() {
        @Override
        public synchronized void write(char[] cbuf, int off, int len) {
            ConsoleComponent.this.print(
                    String.copyValueOf(cbuf, off, len)
            );
        }

        @Override
        public void flush() { }

        @Override
        public void close() { }
    };

    private final PrintStream PRINT_STREAM = new PrintStream(
            new OutputStream() {
                @Override
                public synchronized void write(int b) {
                    ConsoleComponent.this.print((char) b);
                }
            }
    );

    protected ConsoleComponent() {
        super();

        setEditable(false);
        setFont(new Font("Consolas", Font.PLAIN, DEFAULT_FONT_SIZE));
        setMargin(new Insets(MARGIN, MARGIN, MARGIN, MARGIN));

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

    public synchronized void setFontSize(int size) {
        Font oldFont = getFont();
        setFont(new Font(oldFont.getFamily(), oldFont.getStyle(), Math.max(size, 0)));
    }

    public synchronized void resetFontSize() {
        setFontSize(DEFAULT_FONT_SIZE);
    }

    public synchronized void clear() {
        setText("");
    }

    @Override
    public synchronized void print(@Nullable String string) {
        append(Objects.toString(string));
    }

    @Override
    public synchronized void print(boolean bool) {
        append(String.valueOf(bool));
    }

    @Override
    public synchronized void print(char character) {
        append(String.valueOf(character));
    }

    @Override
    public synchronized void print(int number) {
        append(String.valueOf(number));
    }

    @Override
    public synchronized void print(long number) {
        append(String.valueOf(number));
    }

    @Override
    public synchronized void print(float number) {
        append(String.valueOf(number));
    }

    @Override
    public synchronized void print(double number) {
        append(String.valueOf(number));
    }

    @Override
    public synchronized void print(@Nullable Object object) {
        append(Objects.toString(object));
    }

    @Override
    public @NotNull Writer getWriter() {
        return WRITER;
    }

    @Override
    public @NotNull PrintStream getPrintStream() {
        return PRINT_STREAM;
    }

}
