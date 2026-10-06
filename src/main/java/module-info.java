module com.warehouse.warehouselab1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.warehouse.warehouselab1 to javafx.fxml;
    exports com.warehouse.warehouselab1;
}