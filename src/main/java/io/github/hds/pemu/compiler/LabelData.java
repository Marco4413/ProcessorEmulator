package io.github.hds.pemu.compiler;

import io.github.hds.pemu.compiler.labels.ILabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LabelData <T extends ILabel> extends HashMap<String, T> {

    private HashMap<Integer, ArrayList<String>> ADDRESS_MAP = new HashMap<>();
    private HashMap<Integer, String> INSTANCES_MAP = new HashMap<>();
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

    protected LabelData(Map<? extends String, ? extends T> m) {
        super(m);
    }

    public void cacheData() {
        if (dataCached) return;
        ADDRESS_MAP   = new HashMap<>();
        INSTANCES_MAP = new HashMap<>();

        this.forEach(
                (String labelName, T label) -> {
                    int labelPointer = label.getPointer();
                    if (!ADDRESS_MAP.containsKey(labelPointer))
                        ADDRESS_MAP.put(labelPointer, new ArrayList<>());
                    ADDRESS_MAP.get(labelPointer).add(labelName);

                    Integer[] instances = label.getInstances();
                    for (Integer instance : instances) {
                        INSTANCES_MAP.put(instance, labelName);
                    }
                }
        );
        dataCached = true;
    }

    @Override
    public T put(String key, T value) {
        dataCached = false;
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends T> m) {
        dataCached = false;
        super.putAll(m);
    }

    @Override
    public T remove(Object key) {
        if (containsKey(key)) dataCached = false;
        return super.remove(key);
    }

    @Override
    public void clear() {
        dataCached = false;
        super.clear();
    }

    @Override
    public T putIfAbsent(String key, T value) {
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
    public boolean replace(String key, T oldValue, T newValue) {
        boolean replaced = super.replace(key, oldValue, newValue);
        if (replaced) dataCached = false;
        return replaced;
    }

    @Override
    public T replace(String key, T value) {
        if (containsKey(key)) dataCached = false;
        return super.replace(key, value);
    }

    @Override
    public T computeIfAbsent(String key, @NotNull Function<? super String, ? extends T> mappingFunction) {
        T newValue = super.computeIfAbsent(key, mappingFunction);
        dataCached = false;
        return newValue;
    }

    @Override
    public T computeIfPresent(String key, @NotNull BiFunction<? super String, ? super T, ? extends T> remappingFunction) {
        T newValue = super.computeIfPresent(key, remappingFunction);
        dataCached = false;
        return newValue;
    }

    @Override
    public T compute(String key, @NotNull BiFunction<? super String, ? super T, ? extends T> remappingFunction) {
        T newValue = super.compute(key, remappingFunction);
        dataCached = false;
        return newValue;
    }

    @Override
    public T merge(String key, @NotNull T value, @NotNull BiFunction<? super T, ? super T, ? extends T> remappingFunction) {
        T newValue = super.merge(key, value, remappingFunction);
        dataCached = false;
        return newValue;
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super T> action) {
        super.forEach(action);
        dataCached = false;
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super T, ? extends T> function) {
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

    @Nullable String getInstancesAtAddress(int address) {
        cacheData();
        return INSTANCES_MAP.get(address);
    }

    boolean hasInstancesAtAddress(int address) {
        cacheData();
        return INSTANCES_MAP.containsKey(address);
    }

}
