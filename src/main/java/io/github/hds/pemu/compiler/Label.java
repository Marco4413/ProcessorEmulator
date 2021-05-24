package io.github.hds.pemu.compiler;

import java.util.ArrayList;

public class Label {
    public static int NULL_PTR = -1;

    public int pointer = NULL_PTR;
    public ArrayList<Integer> occurrences = new ArrayList<>();
    public ArrayList<Integer> offsets = new ArrayList<>();
    public Label() { }

    protected Label(int pointer) {
        this.pointer = pointer;
    }

    public void addOccurrence(int at) {
        addOccurrence(at, 0);
    }

    public void addOccurrence(int at, int offset) {
        occurrences.add(at);
        offsets.add(offset);
    }
}
