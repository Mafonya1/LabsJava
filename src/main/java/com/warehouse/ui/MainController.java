package com.warehouse.ui;

import com.warehouse.io.LoadError;
import com.warehouse.io.LoadResult;
import com.warehouse.model.ArchivedBatch;
import com.warehouse.model.Batch;
import com.warehouse.model.Editable;
import com.warehouse.model.ImportedBatch;
import com.warehouse.service.BatchService;
import com.warehouse.ui.task.GenerateTask;
import com.warehouse.ui.task.LoadTask;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final int MAX_GENERATE = 200000;

    @FXML private TableView<Batch> tableView;
    @FXML private TableColumn<Batch, Integer> colArticle;
    @FXML private TableColumn<Batch, String> colName;
    @FXML private TableColumn<Batch, Integer> colQuantity;
    @FXML private TableColumn<Batch, Integer> colCell;
    @FXML private TableColumn<Batch, String> colDate;
    @FXML private TableColumn<Batch, String> colType;

    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnLoad;
    @FXML private Button btnGenerate;
    @FXML private Button btnCancel;

    @FXML private TextField tfSearch;
    @FXML private TextField tfCount;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblStatus;
    @FXML private ScrollPane visualPane;

    private final HashTableView hashView = new HashTableView();
    private final BatchService service = new BatchService();
    private final ObservableList<Batch> rows = FXCollections.observableArrayList();

    // фоновые задачи (null, пока задача не запущена)
    private LoadTask loadTask;
    private GenerateTask generateTask;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colArticle.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getArticle()).asObject());
        colName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));
        colQuantity.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getQuantity()).asObject());
        colCell.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getCell()).asObject());
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getArrivalDate()));
        colType.setCellValueFactory(cd -> {
            Batch b = cd.getValue();
            String t = b instanceof ImportedBatch ? "Импортная"
                    : b instanceof ArchivedBatch ? "Архивная" : "?";
            return new SimpleStringProperty(t);
        });

        tableView.setItems(rows);
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
        btnCancel.setDisable(true);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            boolean sel = nw != null;
            btnDelete.setDisable(!sel);
            btnEdit.setDisable(!(nw instanceof Editable));
        });

        visualPane.setContent(hashView);
        hashView.draw(service.getTable(), "таблица пуста");

        tryLoadDefaultFile();
    }

    private void tryLoadDefaultFile() {
        Path p = Path.of("batches.csv");
        if (java.nio.file.Files.exists(p)) {
            LoadResult r = service.load(p);
            refresh("загрузка batches.csv");
            if (r.hasErrors()) showErrors(r);
        }
    }

    /** Обновляет таблицу на экране и перерисовывает хеш-таблицу. Вызывается после каждой операции. */
    private void refresh(String operation) {
        rows.setAll(service.findAll());
        hashView.draw(service.getTable(), operation);
    }

    // ---------- кнопки ----------

    @FXML
    private void handleAdd() {
        Batch b = showDialog(null);
        if (b != null) {
            try {
                service.add(b);
                refresh("put(" + b.getArticle() + ")");
            } catch (Exception e) {
                error("Ошибка добавления", e.getMessage());
            }
        }
    }

    @FXML
    private void handleEdit() {
        Batch sel = tableView.getSelectionModel().getSelectedItem();
        if (!(sel instanceof Editable)) {
            error("Редактирование", "Партия read-only");
            return;
        }
        Batch updated = showDialog(sel);
        if (updated != null) {
            try {
                service.update(updated);
                refresh("put(" + updated.getArticle() + ") - изменение");
            } catch (Exception e) {
                error("Ошибка редактирования", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Batch sel = tableView.getSelectionModel().getSelectedItem();
        if (sel != null) {
            int article = sel.getArticle();
            service.delete(article);
            refresh("remove(" + article + ")");
        }
    }

    @FXML
    private void handleFind() {
        int article;
        try {
            article = Integer.parseInt(tfSearch.getText().trim());
        } catch (NumberFormatException e) {
            error("Поиск", "Артикул должен быть целым числом");
            return;
        }

        Batch found = service.find(article);
        hashView.draw(service.getTable(), "get(" + article + ")");

        if (found == null) {
            lblStatus.setText("Артикул " + article + " не найден");
            tableView.getSelectionModel().clearSelection();
        } else {
            lblStatus.setText("Найдено: " + found.getName());
            tableView.getSelectionModel().select(found);
            tableView.scrollTo(found);
        }
    }

    @FXML
    private void handleSave() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Сохранить CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showSaveDialog(window());
        if (f != null) {
            try {
                service.save(f.toPath());
            } catch (IOException e) {
                error("Ошибка сохранения", e.getMessage());
            }
        }
    }

    // ---------- фоновые задачи: загрузка и генерация ----------

    @FXML
    private void handleLoad() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Загрузить CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showOpenDialog(window());
        if (f == null) {
            return;
        }

        loadTask = new LoadTask(f.toPath());
        loadTask.setOnSucceeded(e -> loadFinished());     // когда задача закончится - вызвать loadFinished()
        loadTask.setOnFailed(e -> taskFailed(loadTask));
        loadTask.setOnCancelled(e -> taskCancelled());
        startTask(loadTask, "Загрузка файла...");
    }

    @FXML
    private void handleGenerate() {
        int count;
        try {
            count = Integer.parseInt(tfCount.getText().trim());
        } catch (NumberFormatException e) {
            error("Генерация", "Введите целое число записей");
            return;
        }
        if (count <= 0 || count > MAX_GENERATE) {
            error("Генерация", "Число записей должно быть от 1 до " + MAX_GENERATE);
            return;
        }

        generateTask = new GenerateTask(count);
        generateTask.setOnSucceeded(e -> generateFinished());
        generateTask.setOnFailed(e -> taskFailed(generateTask));
        generateTask.setOnCancelled(e -> taskCancelled());
        startTask(generateTask, "Генерация...");
    }

    @FXML
    private void handleCancel() {
        if (loadTask != null) {
            loadTask.cancel();
        }
        if (generateTask != null) {
            generateTask.cancel();
        }
    }

    private void loadFinished() {
        LoadResult result = loadTask.getValue();
        service.addAll(result.loaded());                  // кладём в таблицу уже в потоке JavaFX
        refresh("загрузка файла");
        endTask("Загружено записей: " + result.loaded().size());
        if (result.hasErrors()) {
            showErrors(result);
        }
    }

    private void generateFinished() {
        List<Batch> generated = generateTask.getValue();
        service.addAll(generated);
        refresh("генерация");
        endTask("Сгенерировано: " + generated.size() + ", в таблице: " + service.getTable().size());
    }

    private void taskFailed(Task<?> task) {
        endTask("Ошибка");
        error("Ошибка", task.getException().getMessage());
    }

    private void taskCancelled() {
        endTask("Операция отменена");
    }

    private void startTask(Task<?> task, String text) {
        btnLoad.setDisable(true);
        btnGenerate.setDisable(true);
        btnCancel.setDisable(false);
        lblStatus.setText(text);
        progressBar.progressProperty().bind(task.progressProperty());   // полоска следует за задачей

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void endTask(String text) {
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        lblStatus.setText(text);
        btnLoad.setDisable(false);
        btnGenerate.setDisable(false);
        btnCancel.setDisable(true);
        loadTask = null;
        generateTask = null;
    }

    // ---------- вспомогательное ----------

    private Batch showDialog(Batch existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.warehouse/batch-dialog.fxml"));
            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(window());
            dialog.setScene(new Scene(loader.load()));
            BatchDialogController ctrl = loader.getController();
            ctrl.setExisting(existing);
            dialog.showAndWait();
            return ctrl.getResult();
        } catch (IOException e) {
            error("Диалог", e.getMessage());
            return null;
        }
    }

    private void showErrors(LoadResult r) {
        StringBuilder sb = new StringBuilder("Пропущено строк: " + r.errors().size() + "\n");
        for (LoadError e : r.errors()) {
            sb.append("Строка ").append(e.lineNumber())
                    .append(" [").append(e.code()).append("]: ")
                    .append(e.message()).append("\n");
        }
        Alert a = new Alert(Alert.AlertType.WARNING, sb.toString(), ButtonType.OK);
        a.setHeaderText("Ошибки загрузки CSV");
        a.showAndWait();
    }

    private void error(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }

    private Stage window() {
        return (Stage) tableView.getScene().getWindow();
    }
}
