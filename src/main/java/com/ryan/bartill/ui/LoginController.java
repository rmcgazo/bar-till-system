package com.ryan.bartill.ui;

import com.ryan.bartill.dao.StaffDao;
import com.ryan.bartill.model.Staff;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoginController {

    @FXML private ImageView bgImage;
    @FXML private Label pinDotsLabel;
    @FXML private Label errorLabel;

    private final StaffDao staffDao = new StaffDao();
    private final StringBuilder pin = new StringBuilder();

    @FXML
    private void initialize() {
        // load background
        var stream = getClass().getResourceAsStream("/images/login_screen.png");
        if (stream != null) {
            bgImage.setImage(new javafx.scene.image.Image(stream));
        }

        // IMPORTANT: wait until Scene exists before binding
        bgImage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                bgImage.fitWidthProperty().bind(newScene.widthProperty());
                bgImage.fitHeightProperty().bind(newScene.heightProperty());
            }
        });

        renderPin();
    }

    @FXML
    private void onDigit(javafx.event.ActionEvent evt) {
        errorLabel.setText("");

        if (pin.length() >= 2) return;
        Button b = (Button) evt.getSource();
        String digit = b.getText().trim();
        if (digit.length() != 1) return;

        pin.append(digit);
        renderPin();

        // auto-enter when 2 digits entered
        if (pin.length() == 2) {
            onEnter();
        }
    }

    @FXML
    private void onBackspace() {
        errorLabel.setText("");
        if (pin.length() > 0) pin.deleteCharAt(pin.length() - 1);
        renderPin();
    }

    @FXML
    private void onClear() {
        errorLabel.setText("");
        pin.setLength(0);
        renderPin();
    }

    @FXML
    private void onEnter() {
        errorLabel.setText("");

        if (pin.length() != 2) {
            errorLabel.setText("Enter 2-digit PIN");
            return;
        }

        String pinStr = pin.toString();

        try {
            // You need this method in StaffDao (see below)
            Staff staff = staffDao.findByPin(pinStr);

            boolean ok = staff != null && staff.isActive();

            staffDao.logLoginAttempt(staff == null ? null : staff.getId(), "PIN:" + pinStr, ok);

            if (!ok) {
                errorLabel.setText("Invalid PIN");
                pin.setLength(0);
                renderPin();
                return;
            }

            com.ryan.bartill.app.Session.setCurrentUser(staff);

            javafx.stage.Stage stage = (javafx.stage.Stage) pinDotsLabel.getScene().getWindow();
            com.ryan.bartill.app.SceneSwitcher.switchTo(stage, "/fxml/main.fxml", "Bar Till System");

        } catch (Exception e) {
            errorLabel.setText("Database error");
            e.printStackTrace();
        }
    }

    private void renderPin() {
        // show 2 dots max, but only filled for digits entered
        String s = "";
        if (pin.length() == 0) s = "○○";
        if (pin.length() == 1) s = "●○";
        if (pin.length() == 2) s = "●●";
        pinDotsLabel.setText(s);
    }
}