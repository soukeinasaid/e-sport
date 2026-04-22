package com.victorygrid.controller;

import com.victorygrid.DashboardController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class ErrorController implements DashboardController.ErrorViewController {
    
    @FXML private Text errorMessage;
    @FXML private Button okButton;
    
    @FXML
    public void initialize() {
        okButton.setOnAction(e -> {
            // Close the error dialog or navigate back
            okButton.getScene().getWindow().hide();
        });
    }
    
    @Override
    public void setError(String error) {
        if (errorMessage != null) {
            errorMessage.setText(error);
        }
    }
}
