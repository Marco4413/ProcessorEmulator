package io.github.hds.pemu.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LabelData extends HashMap<String, Label> {

    private HashMap<Integer, ArrayList<String>> ADDRESS_MAP = new HashMap<>();
    private HashMap<Integer, String> OCCURRENCES_MAP = new HashMap<>();
    private boolean dataCached = false;

    protected LabelData(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    protected LabelData(int initialCapacity) {
        super(initialCapacity);
    }

    protected LabelData() {
        super();
    }

    protected LabelData(Map<? extends String, ? extends Label> m) {
        super(m);
    }

    public void cacheData() {
        if (dataCached) return;
        ADDRESS_MAP = new HashMap<>();
        this.forEach(
                (n, l) -> {
                    if (!ADDRESS_MAP.containsKey(l.pointer))
                        ADDRESS_MAP.put(l.pointer, new ArrayList<>());
                    ADDRESS_MAP.get(l.pointer).add(n);

                    for (Integer occurrence : l.occurrences) {
                        OCCURRENCES_MAP.put(occurrence, n);
                    }
                }
        );
        dataCached = true;
    }

    @Override
    public Label put(String key, Label value) {
        dataCached = false;
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Label> m) {
        dataCached = false;
        super.putAll(m);
    }

    @Override
    public Label remove(Object key) {
        if (containsKey(key)) dataCached = false;
        return super.remove(key);
    }

    @Override
    public void clear() {
        dataCached = false;
        super.clear();
    }

    @Override
    public Label putIfAbsent(String key, Label value) {
        if (!containsKey(key)) dataCached = false;
        return super.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        boolean removed = super.remove(key, value);
        if (removed) dataCached = false;
        return removed;
    }

    @Override
    public boolean replace(String key, Label oldValue, Label newValue) {
        boolean replaced = super.replace(key, oldValue, newValue);
        if (replaced) dataCached = false;
        return replaced;
    }

    @Override
    public Label replace(String key, Label value) {
        if (containsKey(key)) dataCached = false;
        return super.replace(key, value);
    }

    @Override
    public Label computeIfAbsent(String key, @NotNull Function<? super String, ? extends Label> mappingFunction) {
        Label newValue = super.computeIfAbsent(key, mappingFunction);
        dataCached = false;
        return newValue;
    }

    @Override
    public Label computeIfPresent(String key, @NotNull BiFunction<? super String, ? super Label, ? extends Label> remappingFunction) {
        Label newValue = super.computeIfPresent(key, remappingFunction);
        dataCached = false;
        return newValue;
    }

    @Override
    public Label compute(String key, @NotNull BiFunction<? super String, ? super Label, ? extends Label> remappingFunction) {
        Label newValue = super.compute(key, remappingFunction);
        dataCached = false;
        return newValue;
    }

    @Override
    public Label merge(String key, @NotNull Label value, @NotNull BiFunction<? super Label, ? super Label, ? extends Label> remappingFunction) {
        Label newValue = super.merge(key, value, remappingFunction);
        dataCached = false;
        return newValue;
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Label> action) {
        super.forEach(action);
        dataCached = false;
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Label, ? extends Label> function) {
        super.replaceAll(function);
        dataCached = false;
    }

    @NotNull String[] getLabelsAtAddress(int address) {
        cacheData();
        return ADDRESS_MAP.containsKey(address) ? ADDRESS_MAP.get(address).toArray(new String[0]) : new String[0];
    }

    boolean hasLabelsAtAddress(int address) {
        cacheData();
        return ADDRESS_MAP.containsKey(address);
    }

    @Nullable String getOccurrenceAtAddress(int address) {
        cacheData();
        return OCCURRENCES_MAP.get(address);
    }

    boolean hasOccurrenceAtAddress(int address) {
        cacheData();
        return OCCURRENCES_MAP.containsKey(address);
    }

}
