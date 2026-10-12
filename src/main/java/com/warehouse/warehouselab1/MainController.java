//обработка событий

package com.warehouse.warehouselab1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;

import static com.warehouse.warehouselab1.FileService.counter;

public class MainController{

    public static final String FILE_NAME = "batches.csv";

    @FXML private TableView<Batch> batchTable;//табличка
    @FXML private TableColumn<Batch, Integer> colArticle;
    @FXML private TableColumn<Batch, String> colName;
    @FXML private TableColumn<Batch, Integer> colQt;
    @FXML private TableColumn<Batch, Integer> colCell;
    @FXML private TableColumn<Batch, String> colDate;
    @FXML private TableColumn<Batch, String> colStatus;
    @FXML private TableColumn<Batch, String> colCountry;
    @FXML private TableColumn<Batch, String> colCode;

    @FXML private TextField countryInput;
    @FXML private TextField codeInput;

    @FXML private Button btnEdit;

    private final ObservableList<Batch> data = FXCollections.observableArrayList(); //создаем список, в котором будем хранить обьекты типа Batch

    @FXML
    public void initialize() {
        colArticle.setCellValueFactory(new PropertyValueFactory<>("article")); //привязка столбца к определенному свойству данных
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colQt.setCellValueFactory(new PropertyValueFactory<>("qt"));
        colCell.setCellValueFactory(new PropertyValueFactory<>("cell"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCountry.setCellValueFactory(new PropertyValueFactory<>("country"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));


        // Связываем список данных с таблицей
        batchTable.setItems(data);

        btnEdit.setDisable(true);
        // Слушаем, какую строку выбрал пользователь
        batchTable.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Если у выбранной партии статус "Доступен" — делаем кнопку активной
                if (newSelection.getStatus().equalsIgnoreCase("Доступен")) {
                    btnEdit.setDisable(false);
                } else {
                    btnEdit.setDisable(true); // Для архива кнопка не активна
                }
            } else {
                // Если в таблице вообще ничего не выбрано, кнопка тоже серая
                btnEdit.setDisable(true);
            }
        });

    }

    //КНОПКА «ЗАГРУЗИТЬ ИЗ CSV»
    @FXML
    private void handelReadCSV() {
        try {
            List<Batch> parsedList = FileService.readerCSV(FILE_NAME);
            data.clear();
            if (!parsedList.isEmpty()) {
                data.addAll(parsedList);
                if (counter > 0) {
                    String message = "Было пропущено " + counter + " битых строк. \n";
                    showDialog("Предупреждение: ", message);
                }
                showDialog("Успех", "Данные из CSV успешно загружены!");
            }
        } catch (IOException e) {
            showErrorDialog("Ошибка чтения файла", "Не удалось прочитать " + FILE_NAME + ": " + e.getMessage());
        }
    }


    @FXML
    private void handelSaveCSV(){
            try{
                FileService.saveCSV(FILE_NAME, data);
                showDialog("Успех", "Сохранено записей: " + data.size());
            } catch (IOException e) {
                showErrorDialog("Ошибка сохранения", e.getMessage());
            }
        }


    //КНОПКА «ДОБАВИТЬ»
    @FXML
    private void handleAdd() {
        ImportedBatch newBatch = new ImportedBatch(777, "Новый Импорт", 10, 202, "08.10.2026", "Китай", "CN-99");

        data.add(newBatch);
        showDialog("Добавление", "Добавлена новая редактируемая импортная партия!");
    }

    //КНОПКА «ИЗМЕНИТЬ»
    @FXML
    private void handleEdit() {
        // Получаем выделенный Batch из таблицы
        Batch selected = batchTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showErrorDialog("Ошибка изменения", "Пожалуйста, сначала выделите строку в таблице!");
            return;
        }
        if (selected instanceof Editable) {

            String newCountry = countryInput.getText().trim();
            String newCode = codeInput.getText().trim(); //использует метод для дальнейшей удобной проверки на пустоту в строке

            Editable editable = (Editable) selected;

            editable.update(
                    newCountry.isEmpty() ? "Не указана" : newCountry,
                    newCode.isEmpty() ? "0000" : newCode
            );

            batchTable.refresh();
            countryInput.clear();
            codeInput.clear();

            showDialog("Редактирование", "Специфичные поля импортной партии (Страна и Код) успешно изменены!");

        }else {
            // Read-Only
            showErrorDialog("Доступ запрещен", "Выбранная архивная партия защищена от изменений!");
        }
    }

    // Вспомогательный метод для вывода простых информационных окон
    private void showDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Вспомогательный метод для вывода ошибок
    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Внимание!");
        alert.setContentText(content);
        alert.showAndWait();
    }
}