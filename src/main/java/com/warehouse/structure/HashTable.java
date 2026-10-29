package com.warehouse.structure;

import java.util.function.BiConsumer;

/**
 * Хеш-таблица: открытая адресация + линейное пробирование (вариант 5).
 *
 * Данные лежат в трёх массивах. Ячейка с номером i - это keys[i], values[i], states[i].
 * Состояние ячейки: FREE (пустая), OCCUPIED (занята) или DELETED (там был элемент, его удалили).
 *
 * Идея: ключ "хочет" лежать в ячейке hash % capacity. Если она занята другим ключом,
 * смотрим следующую (+1, по кругу), и так пока не найдём ключ или пустую ячейку.
 * Каждая осмотренная ячейка = одна проба.
 * DELETED нужен, чтобы после удаления цепочка проб не обрывалась.
 */
public class HashTable<K, V> {

    public enum SlotState { FREE, OCCUPIED, DELETED }

    private static final int START_CAPACITY = 16;
    private static final double MAX_LOAD = 0.75;   // выше этого заполнения - рехеш

    private Object[] keys;
    private Object[] values;
    private SlotState[] states;
    private int capacity;
    private int size;
    private int deleted;

    // история последней операции ?
    private int[] lastPath = new int[0];   // номера осмотренных ячеек по порядку
    private int lastPathLength;
    private int lastResultIndex = -1;
    private boolean lastRehashed;

    // история за всё время
    private long totalProbes;//?
    private long operationCount;

    public HashTable() {
        allocate(START_CAPACITY);
    } //?

    /** Вставка или замена. true - ключ новый, false - значение заменили. */
    public boolean put(K key, V value) {
        return doPut(key, value, true);
    }

    /** Вставка только нового ключа. false - такой ключ уже есть, ничего не изменилось. */
    public boolean putIfAbsent(K key, V value) {
        return doPut(key, value, false);
    }

    @SuppressWarnings("unchecked")
    public V get(K key) {
        checkKey(key);
        int index = find(key);
        finishOperation(index, false);
        return index >= 0 ? (V) values[index] : null;
    }

    public boolean containsKey(K key) {
        checkKey(key);
        int index = find(key);
        finishOperation(index, false);
        return index >= 0;
    }

    /** Удаление. true - ключ был и удалён. */
    public boolean remove(K key) {
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
        return index >= 0;
    }

    @SuppressWarnings("unchecked")
    public void forEach(BiConsumer<K, V> action) {
        for (int i = 0; i < capacity; i++) {
            if (states[i] == SlotState.OCCUPIED) {
                action.accept((K) keys[i], (V) values[i]);
            }
        }
    }

    public void clear() {
        allocate(START_CAPACITY);
        size = 0;
        deleted = 0;
        lastPathLength = 0;
        lastResultIndex = -1;
        lastRehashed = false;
        totalProbes = 0;
        operationCount = 0;
    }

    private boolean doPut(K key, V value, boolean replaceIfExists) {
        checkKey(key);

        int index = find(key);
        if (index >= 0) {                                  // ключ уже есть
            if (replaceIfExists) {
                values[index] = value;
            }
            finishOperation(index, false);
            return false;
        }

        boolean rehashed = false;
        if (size + deleted + 1 > MAX_LOAD * capacity) {    // таблица слишком заполнена
            rehash();
            rehashed = true;
            find(key);                                     // заново, чтобы путь был по новой таблице
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
        return true;
    }

    /** Ищет ключ. Возвращает номер ячейки или -1. Попутно записывает путь проб. */
    private int find(Object key) {
        lastPathLength = 0;
        int i = indexFor(key);
        for (int step = 0; step < capacity; step++) {
            lastPath[lastPathLength++] = i;
            if (states[i] == SlotState.FREE) {
                return -1;                                 // пустая ячейка - дальше ключа нет
            }
            if (states[i] == SlotState.OCCUPIED && keys[i].equals(key)) {
                return i;
            }
            i = (i + 1) % capacity;                        // шаг вправо по кругу
        }
        return -1;
    }

    /** Первая ячейка, куда можно положить ключ (не занятая). Идём тем же путём, что и поиск. */
    private int freeSlotFor(Object key) {
        int i = indexFor(key);
        while (states[i] == SlotState.OCCUPIED) {
            i = (i + 1) % capacity;
        }
        return i;
    }

    private int indexFor(Object key) {
        return (key.hashCode() & 0x7FFFFFFF) % capacity;   // & убирает знак: индекс не бывает отрицательным
    }

    /** Увеличивает таблицу вдвое и перекладывает все живые элементы. DELETED при этом исчезают. */
    private void rehash() {
        Object[] oldKeys = keys;
        Object[] oldValues = values;
        SlotState[] oldStates = states;

        allocate(capacity * 2);
        deleted = 0;
        for (int i = 0; i < oldStates.length; i++) {
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
        totalProbes += lastPathLength;
        operationCount++;
    }

    private void checkKey(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Ключ не может быть null");
        }
    }

    // ---------- чтение состояния (для окна и тестов) ----------

    public int size() { return size; }
    public int getCapacity() { return capacity; }
    public int getDeletedCount() { return deleted; }
    public double getLoadFactor() { return (double) size / capacity; }
    public SlotState getSlotState(int index) { return states[index]; }

    @SuppressWarnings("unchecked")
    public K getKeyAt(int index) { return states[index] == SlotState.OCCUPIED ? (K) keys[index] : null; }

    @SuppressWarnings("unchecked")
    public V getValueAt(int index) { return states[index] == SlotState.OCCUPIED ? (V) values[index] : null; }

    // ---------- статистика проб ----------

    public int getLastProbes() { return lastPathLength; }
    public int getLastResultIndex() { return lastResultIndex; }
    public boolean wasLastRehashed() { return lastRehashed; }
    public long getTotalProbes() { return totalProbes; }
    public long getOperationCount() { return operationCount; }

    public double getAverageProbes() {
        return operationCount == 0 ? 0 : (double) totalProbes / operationCount;
    }

    /** Номера осмотренных ячеек в последней операции (копия). */
    public int[] getLastPath() {
        int[] copy = new int[lastPathLength];
        System.arraycopy(lastPath, 0, copy, 0, lastPathLength);
        return copy;
    }
}
