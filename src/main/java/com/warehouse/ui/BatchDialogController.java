package com.warehouse.ui;

import com.warehouse.model.Batch;
import com.warehouse.model.ImportedBatch;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class BatchDialogController {

    @FXML private TextField tfArticle, tfName, tfQuantity, tfCell, tfDate, tfCountry, tfCustomsCode;

    private Batch result;

    public void setExisting(Batch existing) {
        if (existing != null) {
            tfArticle.setText(String.valueOf(existing.getArticle()));
            tfArticle.setDisable(true);
            tfName.setText(existing.getName());
            tfQuantity.setText(String.valueOf(existing.getQuantity()));
            tfCell.setText(String.valueOf(existing.getCell()));
            tfDate.setText(existing.getArrivalDate());
            if (existing instanceof ImportedBatch ib) {
                tfCountry.setText(ib.getCountry());
                tfCustomsCode.setText(ib.getCustomsCode());
            }
        }
    }

    public Batch getResult() { return result; }

    @FXML
    private void handleOk() {
        try {
            int article = Integer.parseInt(tfArticle.getText().trim());
            String name = tfName.getText().trim();
            int qty = Integer.parseInt(tfQuantity.getText().trim());
            int cell = Integer.parseInt(tfCell.getText().trim());
            String date = tfDate.getText().trim();
            String country = tfCountry.getText().trim();
            String code = tfCustomsCode.getText().trim();

            ImportedBatch b = new ImportedBatch(article, name, qty, cell, date, country, code);
            var errors = b.validate();
            if (!errors.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, String.join("\n", errors), javafx.scene.control.ButtonType.OK).showAndWait();
                return;
            }
            result = b;
            close();
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Проверьте числа: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        result = null;
        close();
    }

    private void close() {
        ((Stage) tfArticle.getScene().getWindow()).close();
    }
}