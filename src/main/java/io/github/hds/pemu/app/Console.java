package io.github.hds.pemu.app;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class Console {

    public static final @NotNull ConsoleElement POutput = new ConsoleElement(new JTextArea());
    public static final @NotNull ConsoleElement Debug = new ConsoleElement(new JTextArea());

    public static class ConsoleElement {

        public final @NotNull JTextArea ELEMENT;

        public ConsoleElement(JTextArea textArea) {
            textArea.setEditable(false);
            textArea.setFont(new Font("Consolas", Font.PLAIN, 12));

            DefaultCaret caret = (DefaultCaret) textArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            ELEMENT = textArea;
        }

        public void clear() {
            ELEMENT.setText("");
        }

        public void print(String... strings) {
            for (String string : strings)
                ELEMENT.append(string);
        }

        public void print(char... characters) {
            for (char character : characters)
                if (character != '\0')
                    ELEMENT.append(String.valueOf(character));
        }

        public void print(int... integers) {
            for (int integer : integers)
                ELEMENT.append(String.valueOf(integer));
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

    }

}
