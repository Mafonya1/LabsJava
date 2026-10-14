//обработка событий

package com.warehouse.warehouselab1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


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
    }

// Включает кнопку «Изменить» только для редактируемых партий
        @FXML
        private void updateEditButton() {
            Batch selected = batchTable.getSelectionModel().getSelectedItem();

            if (selected instanceof Editable) {
                btnEdit.setDisable(false);
            } else {
                btnEdit.setDisable(true); // ничего не выбрано или выбрана архивная партия
            }
        }

    //КНОПКА «ЗАГРУЗИТЬ ИЗ CSV»
    @FXML
    private void handelReadCSV() {
        try {
            ReadRes.LoadResult result = FileService.readerCSV(FILE_NAME);
            data.setAll(result.batches()); //полностью заменяет все старые элементы в ObservableList на новые за одну единственную транзакцию
            updateEditButton();

            String message = "Загружено записей: " + data.size();
            if (result.skipped() > 0) {
                showDialog("Предупреждение", message + "\nПропущено битых строк: " + result.skipped());
            } else if (result.batches().isEmpty()) {
                showDialog("Файл пуст", "В файле " + FILE_NAME + " нет записей.");
            } else {
                showDialog("Успех", message);
            }
        } catch (IOException e) {
            showErrorDialog("Ошибка чтения файла",
                    "Не удалось прочитать " + FILE_NAME + ": " + e.getMessage());
        }
    }

    @FXML
    private void handelSaveCSV(){
        try {
           FileService.saveCSV(FILE_NAME, data);
           String message = "Сохранено записей: " + data.size();
           showDialog("Успех", message);
        } catch (IOException e) {
            showErrorDialog("Ошибка сохранения файла",
                    "Не удалось прочитать " + FILE_NAME + ": " + e.getMessage());
        }
    }

    //КНОПКА «ДОБАВИТЬ»
    //КНОПКА «ДОБАВИТЬ»
    @FXML
    private void handleAdd() {
        TextField fArticle = new TextField();
        TextField fName = new TextField();
        TextField fQt = new TextField();
        TextField fCell = new TextField();
        TextField fDate = new TextField();
        fDate.setPromptText("дд.мм.гггг");
        TextField fCountry = new TextField();
        TextField fCode = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Артикул:"), 0, 0);   grid.add(fArticle, 1, 0);
        grid.add(new Label("Название:"), 0, 1);  grid.add(fName, 1, 1);
        grid.add(new Label("Количество:"), 0, 2); grid.add(fQt, 1, 2);
        grid.add(new Label("Ячейка:"), 0, 3);    grid.add(fCell, 1, 3);
        grid.add(new Label("Дата завоза:"), 0, 4); grid.add(fDate, 1, 4);
        grid.add(new Label("Страна:"), 0, 5);    grid.add(fCountry, 1, 5);
        grid.add(new Label("Таможенный код:"), 0, 6); grid.add(fCode, 1, 6);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Новая партия");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return; // пользователь нажал «Отмена» или закрыл окно
        }

        // Проверка введённых данных
        try {
            int article = Integer.parseInt(fArticle.getText().trim());
            int qt = Integer.parseInt(fQt.getText().trim());
            int cell = Integer.parseInt(fCell.getText().trim());
            String name = fName.getText().trim();
            String date = fDate.getText().trim();
            String country = fCountry.getText().trim();
            String code = fCode.getText().trim();

            if (name.isEmpty() || country.isEmpty() || code.isEmpty()) {
                showErrorDialog("Ошибка ввода", "Название, страна и код не должны быть пустыми.");
                return;
            }
            if (name.contains(";") || country.contains(";") || code.contains(";")) {
                showErrorDialog("Ошибка ввода", "Символ ';' использовать нельзя (он разделитель в CSV).");
                return;
            }
            if (qt < 0 || article < 0 || cell < 0) {
                showErrorDialog("Ошибка ввода", "Числа не могут быть отрицательными.");
                return;
            }
            if (!date.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
                showErrorDialog("Ошибка ввода", "Дата должна быть в формате дд.мм.гггг.");
                return;
            }
            for (Batch b : data) {
                if (b.getArticle() == article) {
                    showErrorDialog("Ошибка ввода", "Партия с артикулом " + article + " уже есть.");
                    return;
                }
            }

            data.add(new ImportedBatch(article, name, qt, cell, date, country, code));
        } catch (NumberFormatException e) {
            showErrorDialog("Ошибка ввода", "Артикул, количество и ячейка должны быть целыми числами.");
        }
    }

    //КНОПКА «ИЗМЕНИТЬ»
    @FXML
    private void handleEdit() {
        Batch selected = batchTable.getSelectionModel().getSelectedItem();

        // защита на случай, если кнопка всё же нажата для архивной партии
        if (!(selected instanceof Editable)) {
            showErrorDialog("Ошибка", "Эту партию нельзя редактировать.");
            return;
        }
        ImportedBatch batch = (ImportedBatch) selected;

        // поля заранее заполнены текущими значениями
        TextField fCountry = new TextField(batch.getCountry());
        TextField fCode = new TextField(batch.getCode());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Страна:"), 0, 0);          grid.add(fCountry, 1, 0);
        grid.add(new Label("Таможенный код:"), 0, 1);  grid.add(fCode, 1, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Редактирование партии " + batch.getArticle());
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            batch.update(fCountry.getText().trim(), fCode.getText().trim());
            batchTable.refresh();
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