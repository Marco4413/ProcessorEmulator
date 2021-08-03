package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class HTMLTableBuilder {

    private final ArrayList<String> ELEMENTS;
    private final int COLS;

    public HTMLTableBuilder(int cols) {
        ELEMENTS = new ArrayList<>();
        COLS = cols;
    }

    public @NotNull HTMLTableBuilder putElement(@Nullable Object item) {
        if (item == null) ELEMENTS.add("null");
        else ELEMENTS.add(StringUtils.escapeHTML(item.toString()));
        return this;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public @NotNull String toString(boolean putHTMLTag) {
        StringBuilder builder = new StringBuilder();
        if (putHTMLTag) builder.append("<html>");
        builder.append("<table>");

        // This variable contains whether or not the last row was closed
        boolean lastClosed = false;
        for (int i = 0; i < ELEMENTS.size(); i++) {
            String item = ELEMENTS.get(i);

            // If we're on the first column then add a row
            if (i % COLS == 0) builder.append("<tr>");
            builder.append("<td>").append(item).append("</td>");

            // If the next column is outside the row close the current row
            lastClosed = (i + 1) % COLS == 0;
            if (lastClosed) builder.append("</tr>");
        }

        // If the last row wasn't closed and we had more than 0 elements, close the last row
        if (!lastClosed && ELEMENTS.size() > 0)  builder.append("</tr>");
        builder.append("</table>");
        if (putHTMLTag) builder.append("</html>");
        return builder.toString();
    }
}
