package com.warehouse.ui;

import com.warehouse.io.LoadError;
import com.warehouse.io.LoadResult;
import com.warehouse.model.ArchivedBatch;
import com.warehouse.model.Batch;
import com.warehouse.model.Editable;
import com.warehouse.model.ImportedBatch;
import com.warehouse.service.BatchService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

        tryLoadDefaultFile();
    }

    private void tryLoadDefaultFile() {
        Path p = Path.of("batches.csv");
        if (java.nio.file.Files.exists(p)) {
            LoadResult r = service.load(p);
            refresh();
            if (r.hasErrors()) showErrors(r);
        }
    }

    private void refresh() {
        rows.setAll(service.findAll());
    }

    @FXML
    private void handleAdd() {
        Batch b = showDialog(null);
        if (b != null) {
            try {
                service.add(b);
                refresh();
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
                refresh();
            } catch (Exception e) {
                error("Ошибка редактирования", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Batch sel = tableView.getSelectionModel().getSelectedItem();
        if (sel != null) {
            service.delete(sel.getArticle());
            refresh();
        }
    }

    @FXML
    private void handleLoad() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Загрузить CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showOpenDialog(window());
        if (f != null) {
            LoadResult r = service.load(f.toPath());
            refresh();
            if (r.hasErrors()) showErrors(r);
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