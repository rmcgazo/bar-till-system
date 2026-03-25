package com.ryan.bartill.ui;

import com.ryan.bartill.app.SceneSwitcher;
import com.ryan.bartill.service.SaleService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class ReceiptController {

    public static SaleService.Receipt lastReceipt;

    @FXML private Label metaLabel;
    @FXML private ListView<String> linesListView;

    @FXML private Label paymentMethodLabel;
    @FXML private Label totalExLabel;
    @FXML private Label vatLabel;
    @FXML private Label totalIncLabel;
    @FXML private Label cashLabel;
    @FXML private Label changeLabel;

    @FXML
    private void initialize() {
        SaleService.Receipt r = lastReceipt;
        if (r == null) {
            metaLabel.setText("No receipt");
            return;
        }

        metaLabel.setText("Sale #" + r.saleId + "  |  " + r.createdAt);

        linesListView.getItems().clear();
        for (SaleService.ReceiptLine line : r.lines) {
            linesListView.getItems().add(
                    line.qty + " x " + line.name + "  =  " + centsToEuro(line.lineIncCents)
            );
        }

        paymentMethodLabel.setText(r.paymentMethod);
        totalExLabel.setText(centsToEuro(r.totalExCents));
        vatLabel.setText(centsToEuro(r.totalVatCents));
        totalIncLabel.setText(centsToEuro(r.totalIncCents));
        cashLabel.setText(centsToEuro(r.cashReceivedCents));
        changeLabel.setText(centsToEuro(r.changeGivenCents));
    }

    @FXML
    private void onBackClicked() {
        try {
            Stage stage = (Stage) metaLabel.getScene().getWindow();
            SceneSwitcher.switchTo(stage, "/fxml/main.fxml", "Bar Till System");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String centsToEuro(int cents) {
        int abs = Math.abs(cents);
        int euros = abs / 100;
        int rem = abs % 100;
        String s = "€" + euros + "." + (rem < 10 ? "0" : "") + rem;
        return cents < 0 ? "-" + s : s;
    }
}