package com.warehouse;

import com.warehouse.structure.HashTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Ёмкость по умолчанию 16: ключи 1, 17, 33 дают один и тот же начальный индекс (1) - коллизия. */
class HashTableTest {

    @Test
    void put_then_get_returns_value() {
        HashTable<Integer, String> t = new HashTable<>();
        t.put(5, "five");
        assertEquals("five", t.get(5));
    }

    @Test
    void get_of_missing_key_returns_null() {
        HashTable<Integer, String> t = new HashTable<>();
        assertNull(t.get(5));
    }

    @Test
    void put_existing_key_replaces_value_and_keeps_size() {
        HashTable<Integer, String> t = new HashTable<>();
        t.put(5, "old");
        boolean isNew = t.put(5, "new");
        assertFalse(isNew);
        assertEquals("new", t.get(5));
        assertEquals(1, t.size());
    }

    @Test
    void putIfAbsent_does_not_replace_existing_value() {
        HashTable<Integer, String> t = new HashTable<>();
        t.put(5, "old");
        assertFalse(t.putIfAbsent(5, "new"));
        assertEquals("old", t.get(5));
    }

    @Test
    void colliding_keys_are_placed_in_consecutive_cells() {
        HashTable<Integer, String> t = new HashTable<>();
        t.put(1, "a");
        t.put(17, "b");
        t.put(33, "c");
        t.get(33);
        assertArrayEquals(new int[]{1, 2, 3}, t.getLastPath());
        assertEquals(3, t.getLastProbes());
    }

    @Test
    void remove_in_middle_of_chain_does_not_lose_following_keys() {
        HashTable<Integer, String> t = new HashTable<>();
        t.put(1, "a");
        t.put(17, "b");
        t.put(33, "c");
        assertTrue(t.remove(17));
        assertEquals("c", t.get(33));
        assertNull(t.get(17));
    }

    @Test
    void remove_missing_key_returns_false() {
        HashTable<Integer, String> t = new HashTable<>();
        assertFalse(t.remove(42));
    }

    @Test
    void deleted_cell_is_reused_by_next_insert() {
        HashTable<Integer, String> t = new HashTable<>();
        t.put(1, "a");
        t.put(17, "b");
        t.remove(1);
        t.put(33, "c");
        assertEquals(33, t.getKeyAt(1));
        assertEquals(0, t.getDeletedCount());
    }

    @Test
    void rehash_doubles_capacity_and_keeps_all_entries() {
        HashTable<Integer, Integer> t = new HashTable<>();
        for (int i = 0; i < 12; i++) {
            t.put(i, i * 10);
        }
        assertEquals(16, t.getCapacity());

        t.put(12, 120);                                // 13-й элемент превышает 75% от 16

        assertEquals(32, t.getCapacity());
        assertTrue(t.wasLastRehashed());
        for (int i = 0; i <= 12; i++) {
            assertEquals(i * 10, t.get(i));
        }
    }

    @Test
    void null_key_is_rejected() {
        HashTable<Integer, String> t = new HashTable<>();
        assertThrows(IllegalArgumentException.class, () -> t.put(null, "x"));
    }
}
