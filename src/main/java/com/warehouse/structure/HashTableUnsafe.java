package com.warehouse.structure;

public class HashTableUnsafe<K, V> {

    public enum SlotState { FREE, OCCUPIED, DELETED }

    private static final int START_CAPACITY = 16;
    private static final double MAX_LOAD = 0.75;

    private Object[] keys;
    private Object[] values;
    private SlotState[] states;
    private int capacity;
    private int size;
    private int deleted;

    private int[] lastPath = new int[0];
    private int lastPathLength;
    private int lastResultIndex = -1;
    private boolean lastRehashed;

    // статистика за всё время
    private long totalProbes;
    private long operationCount;

    public HashTableUnsafe() {
        allocate(START_CAPACITY);
    }


    public void put(K key, V value) {
        checkKey(key);

        int index = find(key);
        if (index >= 0) {
            values[index] = value;
            finishOperation(index, false);
            return;
        }

        boolean rehashed = false;
        if (size + deleted + 1 > MAX_LOAD * capacity) {
            rehash();
            rehashed = true;
            find(key);
        }

        int slot = freeSlotFor(key);
        if (states[slot] == SlotState.DELETED) {
            deleted--;
        }
        keys[slot] = key;
        values[slot] = value;
        states[slot] = SlotState.OCCUPIED;
        size++;
        finishOperation(slot, rehashed);
    }
    @SuppressWarnings("unchecked")
    public V get(K key) {
        checkKey(key);
        int index = find(key);
        finishOperation(index, false);
        if (index >= 0) {
            return (V) values[index];
        }
        return null;
    }

    public void remove(K key) {
        checkKey(key);
        int index = find(key);
        if (index >= 0) {
            keys[index] = null;
            values[index] = null;
            states[index] = SlotState.DELETED;
            size--;
            deleted++;
        }
        finishOperation(index, false);
    }

    private int find(Object key) {
        lastPathLength = 0;
        int i = indexFor(key);
        for (int step = 0; step < capacity; step++) {
            lastPath[lastPathLength] = i;
            lastPathLength++;
            if (states[i] == SlotState.FREE) {
                return -1;
            }
            if (states[i] == SlotState.OCCUPIED && keys[i].equals(key)) {
                return i;
            }
            i = (i + 1) % capacity;
        }
        return -1;
    }
    private int freeSlotFor(Object key) {
        int i = indexFor(key);
        while (states[i] == SlotState.OCCUPIED) {
            i = (i + 1) % capacity;
        }
        return i;
    }

    private int indexFor(Object key) {
        return Math.abs(key.hashCode() % capacity);     // abs, чтобы номер не был отрицательным
    }

    private void rehash() {
        Object[] oldKeys = keys;
        Object[] oldValues = values;
        SlotState[] oldStates = states;
        int oldCapacity = capacity;

        allocate(capacity * 2);
        deleted = 0;
        for (int i = 0; i < oldCapacity; i++) {
            if (oldStates[i] == SlotState.OCCUPIED) {
                int slot = freeSlotFor(oldKeys[i]);
                keys[slot] = oldKeys[i];
                values[slot] = oldValues[i];
                states[slot] = SlotState.OCCUPIED;
            }
        }
    }

    private void allocate(int newCapacity) {
        capacity = newCapacity;
        keys = new Object[newCapacity];
        values = new Object[newCapacity];
        states = new SlotState[newCapacity];
        for (int i = 0; i < newCapacity; i++) {
            states[i] = SlotState.FREE;
        }
        lastPath = new int[newCapacity];
    }

    private void finishOperation(int resultIndex, boolean rehashed) {
        lastResultIndex = resultIndex;
        lastRehashed = rehashed;
        totalProbes = totalProbes + lastPathLength;
        operationCount++;
    }

    private void checkKey(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Ключ не может быть null");
        }
    }

    public int size() { return size; }
    public int getCapacity() { return capacity; }
    public int getDeletedCount() { return deleted; }
    public SlotState getSlotState(int index) { return states[index]; }

    @SuppressWarnings("unchecked")
    public K getKeyAt(int index) { return (K) keys[index]; }

    @SuppressWarnings("unchecked")
    public V getValueAt(int index) { return (V) values[index]; }

    public int getLastResultIndex() { return lastResultIndex; }
    public boolean wasLastRehashed() { return lastRehashed; }
    public  long getTotalProbes() { return totalProbes; }
    public long getOperationCount() { return operationCount; }

    public int[] getLastPath() {
        int[] copy = new int[lastPathLength];
        for (int i = 0; i < lastPathLength; i++) {
            copy[i] = lastPath[i];
        }
        return copy;
    }
}

