package com.warehouse;

import com.warehouse.model.ArchivedBatch;
import com.warehouse.model.Editable;
import com.warehouse.model.ImportedBatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    @DisplayName("Валидные данные импортной партии не возвращают ошибок")
    void importedBatch_validData_returnsEmptyErrors() {
        ImportedBatch b = new ImportedBatch(1, "Болт", 10, 5, "01.01.2026", "Китай", "CN-1");
        assertTrue(b.validate().isEmpty());
    }

    @Test
    @DisplayName("Невалидные данные импортной партии возвращают список ошибок")
    void importedBatch_invalidData_returnsErrors() {
        ImportedBatch b = new ImportedBatch(-1, "", -5, 0, "", "", "");
        var errors = b.validate();
        assertTrue(errors.size() >= 5);
        assertTrue(errors.contains("Артикул должен быть > 0"));
        assertTrue(errors.contains("Название не может быть пустым"));
    }

    @Test
    @DisplayName("Архивная партия не реализует интерфейс Editable (read-only)")
    void archivedBatch_isNotEditable() {
        ArchivedBatch b = new ArchivedBatch(1, "Гайка", 10, 5, "01.01.2026");
        assertFalse(b instanceof Editable);
    }

    @Test
    @DisplayName("Импортная партия реализует интерфейс Editable")
    void importedBatch_isEditable() {
        ImportedBatch b = new ImportedBatch(1, "Болт", 10, 5, "01.01.2026", "Китай", "CN-1");
        assertTrue(b instanceof Editable);
    }
}