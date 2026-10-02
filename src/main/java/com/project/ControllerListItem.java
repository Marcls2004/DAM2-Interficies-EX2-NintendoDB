package com.project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

public class ControllerListItem {

    @FXML
    private Label title, subtitle;

    @FXML
    private Circle circle;

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setSubtitle(String subtitle) {
        this.subtitle.setText(subtitle);
    }

    public void setCircleColor(String color) {
        circle.setStyle("-fx-fill: " + color);
    }
}