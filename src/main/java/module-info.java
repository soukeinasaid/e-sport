module com.victorygrid {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;
    requires java.logging;
    requires java.desktop;
    
    exports com.victorygrid;
    exports com.victorygrid.model;
    exports com.victorygrid.service;
    exports com.victorygrid.controller;
    
    opens com.victorygrid to javafx.fxml;
    opens com.victorygrid.model to javafx.fxml;
    opens com.victorygrid.service to javafx.fxml;
    opens com.victorygrid.controller to javafx.fxml;
}
