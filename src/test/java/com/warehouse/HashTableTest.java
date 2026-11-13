package com.warehouse;

import com.warehouse.structure.HashTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashTableTest {

    @Test
    @DisplayName("Вставка и поиск: put() и get() возвращают корректные значения")
    void putAndGet_works() {
        HashTable<Integer, String> table = new HashTable<>();
        table.put(1, "A");
        table.put(2, "B");
        assertEquals("A", table.get(1));
        assertEquals("B", table.get(2));
        assertNull(table.get(3));
    }

    @Test
    @DisplayName("Повторная вставка по существующему ключу перезаписывает значение")
    void updateExistingKey_overwritesValue() {
        HashTable<Integer, String> table = new HashTable<>();
        table.put(1, "A");
        table.put(1, "B");
        assertEquals("B", table.get(1));
        assertEquals(1, table.size());
    }

    @Test
    @DisplayName("Удаление создаёт tombstone; поиск других ключей продолжает работать")
    void remove_createsTombstone_getStillWorksThroughIt() {
        HashTable<Integer, String> table = new HashTable<>();
        table.put(1, "A");
        table.put(2, "B");
        table.remove(1);
        assertNull(table.get(1));
        // Ключ 2 должен остаться доступным, несмотря на дырку (tombstone) от ключа 1
        assertEquals("B", table.get(2));
        assertEquals(1, table.size());
        assertEquals(1, table.getDeletedCount());
    }

    @Test
    @DisplayName("Рехеш срабатывает при превышении load factor (0.75) и сохраняет все элементы")
    void rehash_happensWhenLoadFactorExceeded() {
        HashTable<Integer, String> table = new HashTable<>();
        int initialCapacity = table.getCapacity();
        // MAX_LOAD = 0.75, значит после 12 элементов для cap=16 должен быть рехеш
        for (int i = 0; i < 15; i++) {
            table.put(i, "val" + i);
        }
        assertTrue(table.getCapacity() > initialCapacity);
        for (int i = 0; i < 15; i++) {
            assertEquals("val" + i, table.get(i));
        }
    }

    @Test
    @DisplayName("Статистика: количество проб и операций увеличивается")
    void probesAreCounted() {
        HashTable<Integer, String> table = new HashTable<>();
        table.put(1, "A");
        table.get(1);
        assertTrue(table.getTotalProbes() > 0);
        assertEquals(2, table.getOperationCount());
    }
}