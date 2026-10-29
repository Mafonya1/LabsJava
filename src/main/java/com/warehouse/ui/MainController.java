package com.warehouse.ui;

import com.warehouse.io.LoadError;
import com.warehouse.io.LoadResult;
import com.warehouse.io.serialization.Serializer;
import com.warehouse.io.serialization.Serializers;
import com.warehouse.model.ArchivedBatch;
import com.warehouse.model.Batch;
import com.warehouse.model.Editable;
import com.warehouse.model.ImportedBatch;
import com.warehouse.service.BatchService;
import com.warehouse.ui.task.ExportTask;
import com.warehouse.ui.task.GenerateTask;
import com.warehouse.ui.task.ImportTask;
import com.warehouse.ui.task.LoadCsvTask;
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
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final int MAX_GENERATE = 200_000;

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
    @FXML private Button btnImport;   // лаб. 3
    @FXML private Button btnExport;   // лаб. 3

    // --- лаб. 2 ---
    @FXML private TextField tfSearch;
    @FXML private TextField tfCount;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblStatus;
    @FXML private ScrollPane visualPane;

    private final HashTableView hashView = new HashTableView();

    private final BatchService service = new BatchService();
    private final ObservableList<Batch> rows = FXCollections.observableArrayList();

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

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            boolean sel = nw != null;
            btnDelete.setDisable(!sel);
            btnEdit.setDisable(!(nw instanceof Editable));
        });

        visualPane.setContent(hashView);
        hashView.render(service.getTable(), "таблица пуста");

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

    /** Перечитывает таблицу из сервиса и перерисовывает визуализацию. */
    private void refresh(String operation) {
        rows.setAll(service.findAll());
        hashView.render(service.getTable(), operation);
    }

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
                refresh("put(" + updated.getArticle() + ") — изменение");
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

    /** Поиск по артикулу: подсвечивает путь проб в таблице и выделяет строку. */
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
        hashView.render(service.getTable(), "get(" + article + ")");

        if (found == null) {
            lblStatus.setText("Артикул " + article + " не найден");
            tableView.getSelectionModel().clearSelection();
        } else {
            lblStatus.setText("Найдено: " + found.getName());
            tableView.getSelectionModel().select(found);
            tableView.scrollTo(found);
        }
    }

    // ===================== фоновые операции =====================

    @FXML
    private void handleLoad() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Загрузить CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showOpenDialog(window());
        if (f == null) {
            return;
        }

        Path file = f.toPath();
        LoadCsvTask task = new LoadCsvTask(file);
        task.setOnSucceeded(e -> {                       // выполняется в потоке JavaFX
            LoadResult r = task.getValue();
            service.addLoaded(r.loaded());
            refresh("загрузка " + file.getFileName());
            finishBackground("Загружено записей: " + r.loaded().size());
            if (r.hasErrors()) showErrors(r);
        });
        task.setOnFailed(e -> failBackground("Ошибка загрузки", task));
        startBackground(task, "Загрузка файла...");
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

        final int requested = count;
        GenerateTask task = new GenerateTask(requested);
        task.setOnSucceeded(e -> {
            int added = service.addAll(task.getValue());
            refresh("генерация: добавлено " + added);
            finishBackground("Сгенерировано: " + added + " из " + requested);
        });
        task.setOnFailed(e -> failBackground("Ошибка генерации", task));
        startBackground(task, "Генерация...");
    }

    // ===================== лаб. 3: экспорт / импорт (JSON и бинарный формат) =====================

    @FXML
    private void handleExport() {
        FileChooser fc = serializationChooser("Экспорт данных");
        File f = fc.showSaveDialog(window());
        if (f == null) {
            return;
        }

        Path path = withExtension(f, fc);
        Serializer serializer;
        try {
            serializer = Serializers.forFile(path);       // выбор формата по расширению
        } catch (IllegalArgumentException e) {
            error("Экспорт", e.getMessage());
            return;
        }

        Batch[] snapshot = service.findAll();             // снимок данных, снятый в потоке JavaFX
        ExportTask task = new ExportTask(serializer, snapshot, path);
        task.setOnSucceeded(e -> finishBackground(
                "Экспортировано записей: " + snapshot.length + " → " + path.getFileName()));
        task.setOnFailed(e -> failBackground("Ошибка экспорта", task));
        startBackground(task, "Экспорт...");
    }

    @FXML
    private void handleImport() {
        FileChooser fc = serializationChooser("Импорт данных");
        File f = fc.showOpenDialog(window());
        if (f == null) {
            return;
        }

        Path path = f.toPath();
        Serializer serializer;
        try {
            serializer = Serializers.forFile(path);
        } catch (IllegalArgumentException e) {
            error("Импорт", e.getMessage());
            return;
        }

        ImportTask task = new ImportTask(serializer, path);
        task.setOnSucceeded(e -> {                         // поток JavaFX
            Batch[] loaded = task.getValue();
            service.clear();                               // очищаем ТОЛЬКО после успешного чтения файла
            int added = service.addAll(loaded);
            refresh("импорт " + path.getFileName());       // обновляются и таблица, и визуализация
            finishBackground("Импортировано: " + added + " из " + loaded.length);
        });
        task.setOnFailed(e -> failBackground("Ошибка импорта (файл повреждён или неверный формат)", task));
        startBackground(task, "Импорт...");
    }

    private FileChooser serializationChooser(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON", "*.json"),
                new FileChooser.ExtensionFilter("Бинарный (Serializable)", "*.bin"));
        return fc;
    }

    /** Если пользователь не набрал расширение - берём его из выбранного фильтра. */
    private Path withExtension(File file, FileChooser chooser) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".json") || name.endsWith(".bin")) {
            return file.toPath();
        }
        FileChooser.ExtensionFilter filter = chooser.getSelectedExtensionFilter();
        String extension = (filter != null && filter.getExtensions().contains("*.bin")) ? ".bin" : ".json";
        return file.toPath().resolveSibling(file.getName() + extension);
    }

    private void startBackground(Task<?> task, String statusText) {
        setBusy(true);
        lblStatus.setText(statusText);
        progressBar.progressProperty().bind(task.progressProperty());   // полоска следует за задачей
        Thread thread = new Thread(task);
        thread.setDaemon(true);                                         // не мешает закрытию программы
        thread.start();
    }

    private void finishBackground(String statusText) {
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        lblStatus.setText(statusText);
        setBusy(false);
    }

    private void failBackground(String title, Task<?> task) {
        finishBackground("Ошибка");
        Throwable ex = task.getException();
        error(title, ex == null ? "Неизвестная ошибка" : String.valueOf(ex.getMessage()));
    }

    private void setBusy(boolean busy) {
        btnLoad.setDisable(busy);
        btnGenerate.setDisable(busy);
        btnImport.setDisable(busy);
        btnExport.setDisable(busy);
    }

    // ===================== остальное без изменений =====================

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
