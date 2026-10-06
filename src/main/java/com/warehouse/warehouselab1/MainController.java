package com.warehouse.warehouselab1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // Элементы интерфейса из вашего graphics.fxml (проверьте соответствие fx:id)
    @FXML private TableView<Batch> tableView;
    @FXML private TableColumn<Batch, String> colArticle;
    @FXML private TableColumn<Batch, String> colName;
    @FXML private TableColumn<Batch, Integer> colCount;
    @FXML private TableColumn<Batch, String> colCell;
    @FXML private TableColumn<Batch, String> colDate;

    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // Специальный список для динамического обновления таблицы JavaFX
    private final ObservableList<Batch> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Настраиваем привязку колонок таблицы к свойствам (полям) класса Batch
        colArticle.setCellValueFactory(new PropertyValueFactory<>("article"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        colCell.setCellValueFactory(new PropertyValueFactory<>("cell"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("arrivalDate"));

        // Подключаем наш список к таблице
        tableView.setItems(masterData);

        // 2. Изначально отключаем кнопки изменения и удаления, пока строка не выбрана
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);

        // 3. Загружаем данные из CSV файла
        loadCsvData();

        // 4. Главная фишка: Слушатель выбора строки в таблице для блокировки кнопок
        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                // Если строка не выбрана — блокируем действия
                btnEdit.setDisable(true);
                btnDelete.setDisable(true);
            } else {
                // ПРОВЕРКА НА EDITABLE (Требование МИНИМУМА)
                // Если объект реализует интерфейс Editable, разрешаем редактирование
                if (newValue instanceof Editable) {
                    btnEdit.setDisable(false);
                } else {
                    // Если это ArchivedBatch (read-only), кнопка «Изменить» тухнет (становится серой)
                    btnEdit.setDisable(true);
                }

                // Кнопку удаления можно оставить активной для любых типов
                btnDelete.setDisable(false);
            }
        });
    }

    private void loadCsvData() {
        // Читаем данные через наш сервис. Файл batches.csv должен лежать в корне проекта.
        List<Batch> loadedList = FileService.readerCSV("batches.csv");
        masterData.addAll(loadedList);
    }

    // Метод для кнопки "Добавить"
    @FXML
    private void handleAdd() {
        System.out.println("Нажата кнопка: Добавить новую импортную партию");
        // Логика вызова диалогового окна добавления
    }

    // Метод для кнопки "Изменить"
    @FXML
    private void handleEdit() {
        Batch selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null && selected instanceof Editable) {
            System.out.println("Редактируем партию: " + selected.getName());
            // Логика вызова окна редактирования полей
        }
    }

    // Метод для кнопки "Удалить"
    @FXML
    private void handleDelete() {
        Batch selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            masterData.remove(selected);
            System.out.println("Партия удалена из таблицы");
        }
    }
}
