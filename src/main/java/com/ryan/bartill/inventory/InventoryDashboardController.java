package com.ryan.bartill.inventory;


import com.ryan.bartill.model.*;
import com.ryan.bartill.util.Db;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.ryan.bartill.model.ReportProductRow;
import com.ryan.bartill.model.ReportWastageRow;

import java.util.ArrayList;
import com.ryan.bartill.dao.ProductDao;
import com.ryan.bartill.dao.StockItemDao;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import com.ryan.bartill.dao.ProductRecipeDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class InventoryDashboardController {

    // Product stock links

    @FXML private Label reportMessageLabel;
    @FXML private Label reportWastageLossLabel;


    @FXML private Label reportStockTakeLossLabel;

    @FXML private javafx.scene.chart.LineChart<String, Number> reportRevenueChart;
    @FXML private javafx.scene.chart.BarChart<String, Number> reportHourlySalesChart;

    @FXML private ListView<String> reportInsightsListView;
    @FXML private GridPane reportProductHeatmapGrid;

    @FXML private TableView<ReportStockAlertRow> reportStockAlertsTable;
    @FXML private TableColumn<ReportStockAlertRow, String> colStockAlertItem;
    @FXML private TableColumn<ReportStockAlertRow, String> colStockAlertCurrent;
    @FXML private TableColumn<ReportStockAlertRow, String> colStockAlertDailyUse;
    @FXML private TableColumn<ReportStockAlertRow, String> colStockAlertDaysLeft;
    @FXML private TableColumn<ReportStockAlertRow, String> colStockAlertSuggestion;


    @FXML private TableView<ReportWastageRow> reportWastageTable;

    @FXML
    private TableView<ProductStockLinkRow> productLinksTable;
    @FXML
    private TableColumn<ProductStockLinkRow, String> colLinkProduct;
    @FXML
    private TableColumn<ProductStockLinkRow, String> colLinkCategory;
    @FXML
    private TableColumn<ProductStockLinkRow, String> colLinkStockItem;
    @FXML
    private TableColumn<ProductStockLinkRow, Number> colLinkQtyUsed;

    @FXML
    private ComboBox<Product> linkProductCombo;
    @FXML
    private ComboBox<StockItem> linkStockItemCombo;
    @FXML
    private TextField linkQtyUsedField;
    @FXML
    private Label productLinkMessageLabel;

    @FXML private DatePicker reportFromDate;
    @FXML private DatePicker reportToDate;

    @FXML private Label reportRevenueLabel;
    @FXML private Label reportCostLabel;
    @FXML private Label reportProfitLabel;

    @FXML private TableView<ReportProductRow> reportTopProductsTable;
    @FXML private TableColumn<ReportProductRow, String> colReportProduct;
    @FXML private TableColumn<ReportProductRow, Number> colReportQty;
    @FXML private TableColumn<ReportProductRow, String> colReportRevenue;
    @FXML private TableColumn<ReportProductRow, String> colReportProfit;

    @FXML private TableColumn<ReportWastageRow, String> colWasteItem;
    @FXML private TableColumn<ReportWastageRow, Number> colWasteQty;
    @FXML private TableColumn<ReportWastageRow, String> colWasteReason;
    @FXML private TableColumn<ReportWastageRow, String> colWasteValue;
    // Recipes
    @FXML
    private TableView<RecipeRow> recipesTable;
    @FXML
    private TableColumn<RecipeRow, String> colRecipeProduct;
    @FXML
    private TableColumn<RecipeRow, String> colRecipeStockItem;
    @FXML
    private TableColumn<RecipeRow, Number> colRecipeQtyUsed;

    @FXML
    private ComboBox<Product> recipeProductCombo;
    @FXML
    private ComboBox<StockItem> recipeStockItemCombo;
    @FXML
    private TextField recipeQtyUsedField;
    @FXML
    private Label recipeMessageLabel;

    @FXML
    private BorderPane rootPane;
    @FXML
    private TabPane inventoryTabPane;

    // Dashboard
    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label totalStockUnitsLabel;
    @FXML
    private Label totalRetailValueLabel;
    @FXML
    private Label totalCostValueLabel;
    @FXML
    private Label lowStockCountLabel;
    @FXML
    private ListView<String> lowStockListView;
    @FXML
    private ListView<String> recentActivityListView;

    // Inventory table
    @FXML
    private TableView<Product> inventoryTable;
    @FXML
    private TableColumn<Product, String> colInventoryName;
    @FXML
    private TableColumn<Product, String> colInventoryCategory;
    @FXML
    private TableColumn<Product, String> colInventoryBarcode;
    @FXML
    private TableColumn<Product, String> colInventorySellPrice;
    @FXML
    private TableColumn<Product, String> colInventoryCostPrice;
    @FXML
    private TableColumn<Product, String> colInventoryStock;
    @FXML
    private TableColumn<Product, String> colInventoryRetailValue;
    @FXML
    private TableColumn<Product, String> colInventoryCostValue;

    // Deliveries
    @FXML
    private TableView<StockItem> deliveriesTable;
    @FXML
    private TableColumn<StockItem, String> colDeliveryName;
    @FXML
    private TableColumn<StockItem, String> colDeliveryCategory;
    @FXML
    private TableColumn<StockItem, String> colDeliveryUnitName;
    @FXML
    private TableColumn<StockItem, Number> colDeliveryUnitSize;
    @FXML
    private TableColumn<StockItem, String> colDeliveryCurrentUnits;
    @FXML
    private TableColumn<StockItem, String> colDeliveryCostPrice;
    @FXML
    private Label deliverySelectedLabel;
    @FXML
    private TextField deliveryQtyField;
    @FXML
    private TextField deliveryCostField;
    @FXML
    private Label deliveryMessageLabel;

    // Stock Items
    @FXML
    private TableView<StockItem> stockItemsTable;
    @FXML
    private TableColumn<StockItem, String> colStockItemName;
    @FXML
    private TableColumn<StockItem, String> colStockItemCategory;
    @FXML
    private TableColumn<StockItem, String> colStockItemBaseUnit;
    @FXML
    private TableColumn<StockItem, String> colStockItemUnitName;
    @FXML
    private TableColumn<StockItem, Number> colStockItemUnitSize;
    @FXML
    private TableColumn<StockItem, Number> colStockItemCurrentBaseQty;
    @FXML
    private TableColumn<StockItem, String> colStockItemCurrentUnits;
    @FXML
    private TableColumn<StockItem, String> colStockItemCostPrice;
    @FXML
    private TableColumn<StockItem, String> colStockItemStockValue;

    @FXML private ComboBox<StockItem> wastageStockItemCombo;
    @FXML private TextField wastageQtyField;
    @FXML private TextField wastageReasonField;
    @FXML private Label wastageMessageLabel;


    @FXML
    private TextField stockItemNameField;
    @FXML
    private TextField stockItemCategoryField;
    @FXML
    private TextField stockItemBaseUnitField;
    @FXML
    private TextField stockItemUnitNameField;
    @FXML
    private TextField stockItemUnitSizeField;
    @FXML
    private TextField stockItemInitialUnitsField;
    @FXML
    private TextField stockItemCostPriceField;
    @FXML
    private Label stockItemMessageLabel;


    // Stock Take
    @FXML
    private TableView<StockTakeRow> stockTakeTable;
    @FXML
    private TableColumn<StockTakeRow, String> colStockTakeName;
    @FXML
    private TableColumn<StockTakeRow, String> colStockTakeCategory;
    @FXML
    private TableColumn<StockTakeRow, String> colStockTakeUnitName;
    @FXML
    private TableColumn<StockTakeRow, Number> colStockTakeUnitSize;
    @FXML
    private TableColumn<StockTakeRow, Number> colStockTakeExpectedUnits;
    @FXML
    private TableColumn<StockTakeRow, Number> colStockTakeExpectedBaseQty;
    @FXML
    private TableColumn<StockTakeRow, Number> colStockTakeActualUnits;
    @FXML
    private TableColumn<StockTakeRow, Number> colStockTakeActualBaseQty;
    @FXML
    private TableColumn<StockTakeRow, Number> colStockTakeVarianceUnits;
    @FXML
    private TableColumn<StockTakeRow, Number> colStockTakeVarianceBaseQty;
    @FXML
    private TableColumn<StockTakeRow, String> colStockTakeCostPerUnit;
    @FXML
    private TableColumn<StockTakeRow, String> colStockTakeVarianceValue;

    @FXML
    private Label stockTakeItemsCountLabel;
    @FXML
    private Label stockTakeExpectedValueLabel;
    @FXML
    private Label stockTakeActualValueLabel;
    @FXML
    private Label stockTakeVarianceValueLabel;
    @FXML
    private Label stockTakeVarianceCountLabel;
    @FXML
    private Label stockTakeSelectedItemLabel;
    @FXML
    private Label stockTakeSessionLabel;
    @FXML
    private Label stockTakeMessageLabel;

    @FXML
    private TextField stockTakeExpectedUnitsField;
    @FXML
    private TextField stockTakeActualUnitsField;
    @FXML
    private TextField stockTakeActualLooseQtyField;
    @FXML
    private TextArea stockTakeNotesArea;
    @FXML
    private TextField stockTakeSearchField;
    @FXML
    private ComboBox<String> stockTakeCategoryCombo;
    @FXML
    private CheckBox stockTakeVarianceOnlyCheckBox;
    @FXML
    private CheckBox stockTakeUncountedOnlyCheckBox;

    private final ObservableList<StockTakeRow> stockTakeMasterList = FXCollections.observableArrayList();


    private final ProductDao productDao = new ProductDao();
    private final StockItemDao stockItemDao = new StockItemDao();
    private final ProductRecipeDao productRecipeDao = new ProductRecipeDao();

    @FXML
    private void onRefreshRecipesClicked() {
        refreshRecipesTable();
        refreshRecipeCombos();
    }

    @FXML
    private void onStartNewStockTakeClicked() {
        refreshStockTakeTable();

        if (stockTakeMessageLabel != null) {
            stockTakeMessageLabel.setText("New stock take started");
        }
    }

    @FXML
    private void onRefreshStockTakeClicked() {
        refreshStockTakeTable();

        if (stockTakeMessageLabel != null) {
            stockTakeMessageLabel.setText("Stock take refreshed");
        }
    }

    @FXML
    private void onSaveStockTakeClicked() {
        if (stockTakeMessageLabel != null) {
            stockTakeMessageLabel.setText("Stock take counts saved in screen memory");
        }
    }


    @FXML
    private void onPostStockTakeAdjustmentsClicked() {
        if (stockTakeMessageLabel != null) {
            stockTakeMessageLabel.setText("");
        }

        if (stockTakeMasterList.isEmpty()) {
            if (stockTakeMessageLabel != null) {
                stockTakeMessageLabel.setText("No stock take rows to post");
            }
            return;
        }

        int changedRows = 0;
        int lossValueCents = 0;
        int gainValueCents = 0;

        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                for (StockTakeRow row : stockTakeMasterList) {
                    int expectedBaseQty = row.getExpectedBaseQty();
                    int actualBaseQty = row.getActualBaseQty();
                    int varianceBaseQty = actualBaseQty - expectedBaseQty;

                    if (varianceBaseQty == 0) {
                        continue;
                    }

                    String reason;
                    if (varianceBaseQty < 0) {
                        reason = "STOCKTAKE ADJUSTMENT - LOSS";
                        lossValueCents += Math.abs(row.getVarianceValueCents());
                    } else {
                        reason = "STOCKTAKE ADJUSTMENT - GAIN";
                        gainValueCents += row.getVarianceValueCents();
                    }

                    stockItemDao.applyStockTakeAdjustment(
                            conn,
                            row.getStockItemId(),
                            actualBaseQty,
                            reason
                    );

                    changedRows++;
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

            refreshAll();
            refreshWastageCombo();

            if (stockTakeMessageLabel != null) {
                if (changedRows == 0) {
                    stockTakeMessageLabel.setText("No stock take differences to post");
                } else {
                    stockTakeMessageLabel.setText(
                            "Posted " + changedRows
                                    + " adjustments | Loss: " + centsToEuro(lossValueCents)
                                    + " | Gain: " + centsToEuro(gainValueCents)
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (stockTakeMessageLabel != null) {
                stockTakeMessageLabel.setText("Failed to post stock take adjustments");
            }
        }
    }

    @FXML
    private void onExportStockTakeExcelClicked() {
        try {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Save Stock Take Excel");
            chooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            chooser.setInitialFileName("stock_take.xlsx");

            java.io.File file = chooser.showSaveDialog(rootPane.getScene().getWindow());
            if (file == null) return;

            try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

                org.apache.poi.ss.usermodel.DataFormat dataFormat = wb.createDataFormat();

                org.apache.poi.ss.usermodel.Font titleFont = wb.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 14);

                org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
                headerFont.setBold(true);
                headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());

                org.apache.poi.ss.usermodel.CellStyle titleStyle = wb.createCellStyle();
                titleStyle.setFont(titleFont);

                org.apache.poi.ss.usermodel.CellStyle headerStyle = wb.createCellStyle();
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
                headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
                headerStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
                headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

                org.apache.poi.ss.usermodel.CellStyle moneyStyle = wb.createCellStyle();
                moneyStyle.setDataFormat(dataFormat.getFormat("€#,##0.00"));

                org.apache.poi.ss.usermodel.CellStyle intStyle = wb.createCellStyle();
                intStyle.setDataFormat(dataFormat.getFormat("0"));

                org.apache.poi.ss.usermodel.CellStyle blueInputStyle = wb.createCellStyle();
                blueInputStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

                org.apache.poi.ss.usermodel.CellStyle redMoneyStyle = wb.createCellStyle();
                redMoneyStyle.cloneStyleFrom(moneyStyle);
                org.apache.poi.ss.usermodel.Font redFont = wb.createFont();
                redFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.RED.getIndex());
                redMoneyStyle.setFont(redFont);

                org.apache.poi.ss.usermodel.CellStyle redIntStyle = wb.createCellStyle();
                redIntStyle.cloneStyleFrom(intStyle);
                redIntStyle.setFont(redFont);

                org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Stock Take");

                int rowIndex = 0;

                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex++);
                org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
                cell.setCellValue("Bar Till System - Stock Take");
                cell.setCellStyle(titleStyle);

                rowIndex++;

                row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue("Stocktake Date");
                row.createCell(1).setCellValue(java.time.LocalDate.now().toString());
                row.createCell(3).setCellValue("Total Items");
                row.createCell(4).setCellValue(stockTakeMasterList.size());

                int totalVarianceUnits = 0;
                int totalVarianceValueCents = 0;

                for (StockTakeRow r : stockTakeMasterList) {
                    totalVarianceUnits += r.getVarianceBaseQty();
                    totalVarianceValueCents += r.getVarianceValueCents();
                }

                row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue("Variance Units");
                row.createCell(1).setCellValue(totalVarianceUnits);
                row.createCell(3).setCellValue("Variance Value");
                cell = row.createCell(4);
                cell.setCellValue(totalVarianceValueCents / 100.0);
                cell.setCellStyle(totalVarianceValueCents < 0 ? redMoneyStyle : moneyStyle);

                rowIndex++;

                row = sheet.createRow(rowIndex++);
                String[] headers = {
                        "Category", "Item", "Unit", "Pack Size", "Cost/Unit (€)",
                        "Opening Stock", "Actual Count", "Variance Units", "Variance Value (€)"
                };

                for (int i = 0; i < headers.length; i++) {
                    cell = row.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                for (StockTakeRow r : stockTakeMasterList) {
                    row = sheet.createRow(rowIndex++);

                    row.createCell(0).setCellValue(r.getCategoryName());
                    row.createCell(1).setCellValue(r.getName());
                    row.createCell(2).setCellValue(r.getStockUnitName());
                    row.createCell(3).setCellValue(r.getStockUnitSize());

                    cell = row.createCell(4);
                    cell.setCellValue(r.getCostPerUnitCents() / 100.0);
                    cell.setCellStyle(moneyStyle);

                    cell = row.createCell(5);
                    cell.setCellValue(r.getExpectedUnits());
                    cell.setCellStyle(intStyle);

                    cell = row.createCell(6);
                    cell.setCellValue(r.getActualUnits());
                    cell.setCellStyle(intStyle);

                    cell = row.createCell(7);
                    cell.setCellValue(r.getVarianceUnits());
                    cell.setCellStyle(r.getVarianceBaseQty() < 0 ? redIntStyle : intStyle);

                    cell = row.createCell(8);
                    cell.setCellValue(r.getVarianceValueCents() / 100.0);
                    cell.setCellStyle(r.getVarianceValueCents() < 0 ? redMoneyStyle : moneyStyle);
                }

                for (int i = 0; i < 9; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    wb.write(fos);
                }
            }

            if (stockTakeMessageLabel != null) {
                stockTakeMessageLabel.setText("Stock take Excel exported");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (stockTakeMessageLabel != null) {
                stockTakeMessageLabel.setText("Stock take export failed");
            }
        }
    }

    private int loadStockTakeLossTotal(Connection conn, java.time.LocalDate from, java.time.LocalDate to) throws Exception {
        int total = 0;

        String sql = """
        SELECT
            ABS(sim.qty_change_base_units) AS qty_base_units,
            si.cost_price_cents_per_stock_unit,
            si.stock_unit_size
        FROM stock_item_movements sim
        JOIN stock_items si ON si.id = sim.stock_item_id
        WHERE sim.qty_change_base_units < 0
          AND UPPER(sim.reason) LIKE 'STOCKTAKE ADJUSTMENT%'
          AND date(sim.created_at) >= ?
          AND date(sim.created_at) <= ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int qtyBaseUnits = rs.getInt("qty_base_units");
                    int costPerStockUnitCents = rs.getInt("cost_price_cents_per_stock_unit");
                    int stockUnitSize = rs.getInt("stock_unit_size");

                    if (stockUnitSize > 0) {
                        double costPerBaseUnit = (double) costPerStockUnitCents / stockUnitSize;
                        total += (int) Math.round(costPerBaseUnit * qtyBaseUnits);
                    }
                }
            }
        }

        return total;
    }

    @FXML
    private void onApplyStockTakeCountClicked() {
        if (stockTakeMessageLabel != null) {
            stockTakeMessageLabel.setText("");
        }

        StockTakeRow selected = stockTakeTable == null ? null : stockTakeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (stockTakeMessageLabel != null) {
                stockTakeMessageLabel.setText("Select a stock item first");
            }
            return;
        }


        int actualUnits;
        int looseQty = 0;

        try {
            String unitsText = stockTakeActualUnitsField == null ? "" : stockTakeActualUnitsField.getText().trim();
            if (unitsText.isEmpty()) {
                throw new NumberFormatException();
            }

            actualUnits = Integer.parseInt(unitsText);

            String looseText = stockTakeActualLooseQtyField == null ? "" : stockTakeActualLooseQtyField.getText().trim();
            if (!looseText.isEmpty()) {
                looseQty = Integer.parseInt(looseText);
            }

            if (actualUnits < 0 || looseQty < 0) {
                throw new NumberFormatException();
            }

        } catch (Exception e) {
            if (stockTakeMessageLabel != null) {
                stockTakeMessageLabel.setText("Enter valid unit counts");
            }
            return;
        }

        selected.setActualUnits(actualUnits);
        selected.setActualLooseBaseQty(looseQty);

        int actualBaseQty = (int) Math.round(actualUnits * selected.getStockUnitSize()) + looseQty;
        selected.setActualBaseQty(actualBaseQty);

        String notes = stockTakeNotesArea == null ? "" : stockTakeNotesArea.getText().trim();
        selected.setNotes(notes);

        applyStockTakeFilters();
        refreshStockTakeSummary();
        stockTakeTable.refresh();

        if (stockTakeMessageLabel != null) {
            stockTakeMessageLabel.setText("Count applied");
        }
    }

    @FXML
    private void onClearStockTakeCountClicked() {
        StockTakeRow selected = stockTakeTable == null ? null : stockTakeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (stockTakeMessageLabel != null) {
                stockTakeMessageLabel.setText("Select a stock item first");
            }
            return;
        }

        selected.resetToExpected();

        if (stockTakeActualUnitsField != null) {
            stockTakeActualUnitsField.setText(String.format("%.2f", selected.getActualUnits()));
        }
        if (stockTakeActualLooseQtyField != null) {
            stockTakeActualLooseQtyField.setText("0");
        }
        if (stockTakeNotesArea != null) {
            stockTakeNotesArea.clear();
        }

        applyStockTakeFilters();
        refreshStockTakeSummary();
        stockTakeTable.refresh();

        if (stockTakeMessageLabel != null) {
            stockTakeMessageLabel.setText("Selected count cleared");
        }
    }

    @FXML
    private void onStockTakeSearchChanged() {
        applyStockTakeFilters();
    }

    @FXML
    private void onStockTakeFilterChanged() {
        applyStockTakeFilters();
    }

    private void setupProductLinksTable() {
        if (colLinkProduct != null) {
            colLinkProduct.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getProductName()));
        }

        if (colLinkCategory != null) {
            colLinkCategory.setCellValueFactory(d ->
                    new SimpleStringProperty(
                            d.getValue().getCategoryName() == null ? "" : d.getValue().getCategoryName()
                    ));
        }

        if (colLinkStockItem != null) {
            colLinkStockItem.setCellValueFactory(d ->
                    new SimpleStringProperty(
                            d.getValue().getStockItemName() == null ? "" : d.getValue().getStockItemName()
                    ));
        }

        if (colLinkQtyUsed != null) {
            colLinkQtyUsed.setCellValueFactory(d ->
                    new SimpleObjectProperty<>(d.getValue().getQtyUsed()));
        }
    }

    private void setupStockTakeTable() {
        if (colStockTakeName != null) {
            colStockTakeName.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getName()));
        }

        if (colStockTakeCategory != null) {
            colStockTakeCategory.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getCategoryName()));
        }

        if (colStockTakeUnitName != null) {
            colStockTakeUnitName.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getStockUnitName()));
        }

        if (colStockTakeUnitSize != null) {
            colStockTakeUnitSize.setCellValueFactory(d ->
                    new SimpleIntegerProperty(d.getValue().getStockUnitSize()));
        }

        if (colStockTakeExpectedUnits != null) {
            colStockTakeExpectedUnits.setCellValueFactory(d ->
                    new SimpleObjectProperty<>(d.getValue().getExpectedUnits()));
        }

        if (colStockTakeExpectedBaseQty != null) {
            colStockTakeExpectedBaseQty.setCellValueFactory(d ->
                    new SimpleIntegerProperty(d.getValue().getExpectedBaseQty()));
        }

        if (colStockTakeActualUnits != null) {
            colStockTakeActualUnits.setCellValueFactory(d ->
                    new SimpleObjectProperty<>(d.getValue().getActualUnits()));
        }

        if (colStockTakeActualBaseQty != null) {
            colStockTakeActualBaseQty.setCellValueFactory(d ->
                    new SimpleIntegerProperty(d.getValue().getActualBaseQty()));
        }

        if (colStockTakeVarianceUnits != null) {
            colStockTakeVarianceUnits.setCellValueFactory(d ->
                    new SimpleObjectProperty<>(d.getValue().getVarianceUnits()));
        }

        if (colStockTakeVarianceBaseQty != null) {
            colStockTakeVarianceBaseQty.setCellValueFactory(d ->
                    new SimpleIntegerProperty(d.getValue().getVarianceBaseQty()));
        }

        if (colStockTakeCostPerUnit != null) {
            colStockTakeCostPerUnit.setCellValueFactory(d ->
                    new SimpleStringProperty(centsToEuro(d.getValue().getCostPerUnitCents())));
        }

        if (colStockTakeVarianceValue != null) {
            colStockTakeVarianceValue.setCellValueFactory(d ->
                    new SimpleStringProperty(centsToEuro(d.getValue().getVarianceValueCents())));
        }

        if (stockTakeTable != null) {
            stockTakeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV == null) {
                    if (stockTakeSelectedItemLabel != null) {
                        stockTakeSelectedItemLabel.setText("Select a stock item from the table");
                    }
                    if (stockTakeExpectedUnitsField != null) {
                        stockTakeExpectedUnitsField.clear();
                    }
                    if (stockTakeActualUnitsField != null) {
                        stockTakeActualUnitsField.clear();
                    }
                    if (stockTakeActualLooseQtyField != null) {
                        stockTakeActualLooseQtyField.clear();
                    }
                    if (stockTakeNotesArea != null) {
                        stockTakeNotesArea.clear();
                    }
                } else {
                    if (stockTakeSelectedItemLabel != null) {
                        stockTakeSelectedItemLabel.setText(
                                newV.getName()
                                        + " | Count in: " + newV.getStockUnitName()
                                        + " | " + formatStockTakeRowDisplay(newV)
                        );
                    }
                    if (stockTakeExpectedUnitsField != null) {
                        stockTakeExpectedUnitsField.setText(String.format("%.2f", newV.getExpectedUnits()));
                    }
                    if (stockTakeActualUnitsField != null) {
                        stockTakeActualUnitsField.setText(String.format("%.2f", newV.getActualUnits()));
                    }
                    if (stockTakeActualLooseQtyField != null) {
                        stockTakeActualLooseQtyField.setText(String.valueOf(newV.getActualLooseBaseQty()));
                    }
                    if (stockTakeNotesArea != null) {
                        stockTakeNotesArea.setText(newV.getNotes() == null ? "" : newV.getNotes());
                    }
                }
            });
        }
    }

    private void refreshStockTakeTable() {
        if (stockTakeTable == null) return;

        try {
            List<StockItem> items = stockItemDao.listAllActive();
            stockTakeMasterList.clear();

            for (StockItem s : items) {
                StockTakeRow row = new StockTakeRow(
                        s.getId(),
                        s.getName(),
                        s.getCategoryName() == null ? "" : s.getCategoryName(),
                        s.getStockUnitName(),
                        s.getStockUnitSize(),
                        s.getCurrentStockUnits(),
                        s.getCurrentQtyBaseUnits(),
                        s.getCurrentStockUnits(),
                        s.getCurrentQtyBaseUnits(),
                        0,
                        s.getCostPriceCentsPerStockUnit()
                );

                stockTakeMasterList.add(row);
            }

            applyStockTakeFilters();
            refreshStockTakeSummary();
            refreshStockTakeCategoryCombo();

            if (stockTakeSessionLabel != null) {
                stockTakeSessionLabel.setText("Live stock take session");
            }

        } catch (Exception e) {
            e.printStackTrace();
            stockTakeTable.getItems().clear();

            if (stockTakeMessageLabel != null) {
                stockTakeMessageLabel.setText("Could not load stock take");
            }
        }
    }

    private void refreshStockTakeSummary() {
        double totalExpectedUnits = 0;
        double totalActualUnits = 0;
        int totalExpectedValueCents = 0;
        int totalActualValueCents = 0;
        int varianceCount = 0;

        for (StockTakeRow row : stockTakeMasterList) {
            totalExpectedUnits += row.getExpectedUnits();
            totalActualUnits += row.getActualUnits();
            totalExpectedValueCents += row.getExpectedValueCents();
            totalActualValueCents += row.getActualValueCents();

            if (row.getVarianceBaseQty() != 0) {
                varianceCount++;
            }
        }

        if (stockTakeItemsCountLabel != null) {
            stockTakeItemsCountLabel.setText(String.valueOf(stockTakeMasterList.size()));
        }

        if (stockTakeExpectedValueLabel != null) {
            stockTakeExpectedValueLabel.setText(centsToEuro(totalExpectedValueCents));
        }

        if (stockTakeActualValueLabel != null) {
            stockTakeActualValueLabel.setText(centsToEuro(totalActualValueCents));
        }

        if (stockTakeVarianceValueLabel != null) {
            stockTakeVarianceValueLabel.setText(centsToEuro(totalActualValueCents - totalExpectedValueCents));
        }

        if (stockTakeVarianceCountLabel != null) {
            stockTakeVarianceCountLabel.setText(String.valueOf(varianceCount));
        }
    }


    private void refreshStockTakeCategoryCombo() {
        if (stockTakeCategoryCombo == null) return;

        try {
            String current = stockTakeCategoryCombo.getValue();

            ArrayList<String> categories = new ArrayList<>();
            categories.add("All");

            for (StockItem s : stockItemDao.listAllActive()) {
                String cat = s.getCategoryName() == null ? "" : s.getCategoryName().trim();
                if (!cat.isEmpty() && !categories.contains(cat)) {
                    categories.add(cat);
                }
            }

            stockTakeCategoryCombo.getItems().setAll(categories);

            if (current != null && categories.contains(current)) {
                stockTakeCategoryCombo.setValue(current);
            } else {
                stockTakeCategoryCombo.setValue("All");
            }

        } catch (Exception e) {
            e.printStackTrace();
            stockTakeCategoryCombo.getItems().setAll("All");
            stockTakeCategoryCombo.setValue("All");
        }
    }

    private void applyStockTakeFilters() {
        if (stockTakeTable == null) return;

        String search = stockTakeSearchField == null ? "" : stockTakeSearchField.getText().trim().toLowerCase();
        String category = stockTakeCategoryCombo == null || stockTakeCategoryCombo.getValue() == null
                ? "All"
                : stockTakeCategoryCombo.getValue();

        boolean varianceOnly = stockTakeVarianceOnlyCheckBox != null && stockTakeVarianceOnlyCheckBox.isSelected();
        boolean uncountedOnly = stockTakeUncountedOnlyCheckBox != null && stockTakeUncountedOnlyCheckBox.isSelected();

        ObservableList<StockTakeRow> filtered = FXCollections.observableArrayList();

        for (StockTakeRow row : stockTakeMasterList) {
            boolean matchesSearch = search.isEmpty()
                    || row.getName().toLowerCase().contains(search);

            boolean matchesCategory = category.equals("All")
                    || row.getCategoryName().equalsIgnoreCase(category);

            boolean matchesVariance = !varianceOnly || row.getVarianceBaseQty() != 0;

            boolean matchesUncounted = !uncountedOnly || !row.isCountEntered();

            if (matchesSearch && matchesCategory && matchesVariance && matchesUncounted) {
                filtered.add(row);
            }
        }

        stockTakeTable.getItems().setAll(filtered);
    }





    private void refreshProductLinksTable() {
        if (productLinksTable == null) return;

        try {
            productLinksTable.getItems().setAll(productDao.listProductStockLinks());
        } catch (Exception e) {
            e.printStackTrace();
            productLinksTable.getItems().clear();
        }
    }

    private void refreshProductLinkCombos() {
        try {
            if (linkProductCombo != null) {
                linkProductCombo.getItems().setAll(productDao.listAllActive());
            }
            if (linkStockItemCombo != null) {
                linkStockItemCombo.getItems().setAll(stockItemDao.listAllActive());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (productLinkMessageLabel != null) {
                productLinkMessageLabel.setText("Could not load products / stock items");
            }
        }
    }

    @FXML
    private void onRefreshProductLinksClicked() {
        refreshProductLinksTable();
        refreshProductLinkCombos();
    }

    @FXML
    private void onSaveProductLinkClicked() {
        if (productLinkMessageLabel != null) {
            productLinkMessageLabel.setText("");
        }

        Product product = linkProductCombo == null ? null : linkProductCombo.getValue();
        StockItem stockItem = linkStockItemCombo == null ? null : linkStockItemCombo.getValue();
        String qtyText = linkQtyUsedField == null ? "" : linkQtyUsedField.getText().trim();

        if (product == null || stockItem == null || qtyText.isEmpty()) {
            if (productLinkMessageLabel != null) {
                productLinkMessageLabel.setText("Select a product, stock item, and qty");
            }
            return;
        }

        int qtyUsed;
        try {
            qtyUsed = Integer.parseInt(qtyText);
            if (qtyUsed <= 0) {
                throw new NumberFormatException();
            }
        } catch (Exception e) {
            if (productLinkMessageLabel != null) {
                productLinkMessageLabel.setText("Qty must be a positive whole number");
            }
            return;
        }

        try {
            productDao.setDefaultStockLink(product.getId(), stockItem.getId(), qtyUsed);

            if (linkQtyUsedField != null) {
                linkQtyUsedField.clear();
            }

            refreshProductLinksTable();

            if (productLinkMessageLabel != null) {
                productLinkMessageLabel.setText("Default stock link saved");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (productLinkMessageLabel != null) {
                productLinkMessageLabel.setText("Could not save stock link");
            }
        }
    }

    @FXML
    private void onSaveRecipeClicked() {
        if (recipeMessageLabel != null) {
            recipeMessageLabel.setText("");
        }

        Product product = recipeProductCombo == null ? null : recipeProductCombo.getValue();
        StockItem stockItem = recipeStockItemCombo == null ? null : recipeStockItemCombo.getValue();
        String qtyText = recipeQtyUsedField == null ? "" : recipeQtyUsedField.getText().trim();

        if (product == null || stockItem == null || qtyText.isEmpty()) {
            if (recipeMessageLabel != null) {
                recipeMessageLabel.setText("Select product, stock item, and qty used");
            }
            return;
        }

        int qtyUsed;
        try {
            qtyUsed = Integer.parseInt(qtyText);
            if (qtyUsed <= 0) {
                throw new NumberFormatException();
            }
        } catch (Exception e) {
            if (recipeMessageLabel != null) {
                recipeMessageLabel.setText("Qty used must be a positive whole number");
            }
            return;
        }

        try {
            productRecipeDao.upsertSingleRecipe(product.getId(), stockItem.getId(), qtyUsed);

            if (recipeQtyUsedField != null) {
                recipeQtyUsedField.clear();
            }

            refreshRecipesTable();

            if (recipeMessageLabel != null) {
                recipeMessageLabel.setText("Recipe saved");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (recipeMessageLabel != null) {
                recipeMessageLabel.setText("Could not save recipe");
            }
        }
    }

    private void setupRecipesTable() {
        if (colRecipeProduct != null) {
            colRecipeProduct.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getProductName()));
        }

        if (colRecipeStockItem != null) {
            colRecipeStockItem.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getStockItemName()));
        }

        if (colRecipeQtyUsed != null) {
            colRecipeQtyUsed.setCellValueFactory(d ->
                    new SimpleIntegerProperty(d.getValue().getQtyBaseUnitsUsed()));
        }
    }

    private void refreshRecipesTable() {
        if (recipesTable == null) return;

        try {
            recipesTable.getItems().setAll(productRecipeDao.listAllRecipeRows());
        } catch (Exception e) {
            e.printStackTrace();
            recipesTable.getItems().clear();
        }
    }

    private void refreshRecipeCombos() {
        try {
            if (recipeProductCombo != null) {
                recipeProductCombo.getItems().setAll(productDao.listAllActive());
            }
            if (recipeStockItemCombo != null) {
                recipeStockItemCombo.getItems().setAll(stockItemDao.listAllActive());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (recipeMessageLabel != null) {
                recipeMessageLabel.setText("Could not load recipe dropdowns");
            }
        }
    }

    @FXML
    private void initialize() {
        setupInventoryTable();
        setupStockItemsTable();
        setupProductLinksTable();
        setupRecipesTable();
        setupDeliveriesTable();
        setupStockTakeTable();

        if (colStockAlertItem != null) {
            colStockAlertItem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getItemName()));
        }
        if (colStockAlertCurrent != null) {
            colStockAlertCurrent.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCurrentUnits()));
        }
        if (colStockAlertDailyUse != null) {
            colStockAlertDailyUse.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAvgDailyUse()));
        }
        if (colStockAlertDaysLeft != null) {
            colStockAlertDaysLeft.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDaysLeft()));
        }
        if (colStockAlertSuggestion != null) {
            colStockAlertSuggestion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSuggestion()));
        }



        if (reportFromDate != null && reportFromDate.getValue() == null) {
            reportFromDate.setValue(java.time.LocalDate.now().minusDays(6));
        }
        if (reportToDate != null && reportToDate.getValue() == null) {
            reportToDate.setValue(java.time.LocalDate.now());
        }

        if (wastageStockItemCombo != null) {
            wastageStockItemCombo.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(StockItem item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });

            wastageStockItemCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(StockItem item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
        }

        refreshAll();
        refreshProductLinkCombos();
        refreshRecipeCombos();
        refreshWastageCombo();
        refreshStockTakeCategoryCombo();

        colReportProduct.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colReportQty.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getQty()));
        colReportRevenue.setCellValueFactory(d -> new SimpleStringProperty(centsToEuro(d.getValue().getRevenueCents())));
        colReportProfit.setCellValueFactory(d -> new SimpleStringProperty(centsToEuro(d.getValue().getProfitCents())));

        colWasteItem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colWasteQty.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getQty()));
        colWasteReason.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReason()));
        colWasteValue.setCellValueFactory(d -> new SimpleStringProperty(centsToEuro(d.getValue().getValueCents())));
    }

    @FXML
    private void onCloseWindow() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onRefreshDashboardClicked() {
        refreshAll();
    }

    @FXML
    private void onRecordWastageClicked() {
        if (wastageMessageLabel != null) {
            wastageMessageLabel.setText("");
            wastageMessageLabel.setStyle("-fx-text-fill: red;");
        }

        StockItem item = wastageStockItemCombo == null ? null : wastageStockItemCombo.getValue();
        String qtyText = wastageQtyField == null ? "" : wastageQtyField.getText().trim();
        String reason = wastageReasonField == null ? "" : wastageReasonField.getText().trim();

        if (item == null || qtyText.isEmpty()) {
            if (wastageMessageLabel != null) {
                wastageMessageLabel.setText("Select item and enter quantity");
            }
            return;
        }

        double qtyStockUnits;
        try {
            qtyStockUnits = Double.parseDouble(qtyText);
        } catch (Exception e) {
            if (wastageMessageLabel != null) {
                wastageMessageLabel.setText("Invalid quantity");
            }
            return;
        }

        if (qtyStockUnits <= 0) {
            if (wastageMessageLabel != null) {
                wastageMessageLabel.setText("Quantity must be greater than 0");
            }
            return;
        }

        if (reason.isEmpty()) {
            reason = "WASTAGE";
        } else {
            reason = "WASTAGE - " + reason;
        }

        int qtyBaseUnits = (int) Math.round(qtyStockUnits * item.getStockUnitSize());

        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                stockItemDao.consumeBaseUnits(conn, item.getId(), qtyBaseUnits, null, reason);
                conn.commit();

                if (wastageQtyField != null) wastageQtyField.clear();
                if (wastageReasonField != null) wastageReasonField.clear();

                refreshAll();
                refreshWastageCombo();

                if (wastageMessageLabel != null) {
                    wastageMessageLabel.setStyle("-fx-text-fill: green;");
                    wastageMessageLabel.setText("Wastage recorded");
                }

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (wastageMessageLabel != null) {
                wastageMessageLabel.setText("Failed to record wastage");
            }
        }
    }

    @FXML
    private void onGoToInventoryTab() {
        inventoryTabPane.getSelectionModel().select(1);
    }

    @FXML
    private void onGoToDeliveriesTab() {
        inventoryTabPane.getSelectionModel().select(4);
    }

    @FXML
    private void onGoToStockTakeTab() {
        inventoryTabPane.getSelectionModel().select(5);
    }

    @FXML
    private void onRunReportClicked() {
        if (reportMessageLabel != null) {
            reportMessageLabel.setText("");
        }



        try (Connection conn = Db.getConnection()) {
            java.time.LocalDate from = reportFromDate == null || reportFromDate.getValue() == null
                    ? java.time.LocalDate.now().minusDays(6)
                    : reportFromDate.getValue();

            java.time.LocalDate to = reportToDate == null || reportToDate.getValue() == null
                    ? java.time.LocalDate.now()
                    : reportToDate.getValue();

            if (to.isBefore(from)) {
                if (reportMessageLabel != null) {
                    reportMessageLabel.setText("To date must be after From date");
                }
                return;
            }

            int stockTakeLoss = loadStockTakeLossTotal(conn, from, to);

            if (reportStockTakeLossLabel != null) {
                reportStockTakeLossLabel.setText(centsToEuro(stockTakeLoss));
            }

            int revenue = loadRevenueTotal(conn, from, to);
            int cost = loadCostTotal(conn, from, to);
            int wastageLoss = loadWastageLossTotal(conn, from, to);

            reportRevenueLabel.setText(centsToEuro(revenue));
            reportCostLabel.setText(centsToEuro(cost));
            reportProfitLabel.setText(centsToEuro(revenue - cost));
            reportWastageLossLabel.setText(centsToEuro(wastageLoss));

            if (reportTopProductsTable != null) {
                reportTopProductsTable.getItems().setAll(loadTopProducts(conn, from, to));
            }
            if (reportWastageTable != null) {
                reportWastageTable.getItems().setAll(loadWastage(conn, from, to));
            }
            if (reportInsightsListView != null) {
                reportInsightsListView.getItems().setAll(loadInsights(conn, from, to));
            }
            if (reportStockAlertsTable != null) {
                reportStockAlertsTable.getItems().setAll(loadStockAlerts(conn, from, to));
            }

            drawRevenueChart(conn, from, to);
            drawHourlySalesChart(conn, from, to);
            drawProductHeatmap(conn, from, to);

            if (reportMessageLabel != null) {
                reportMessageLabel.setText("Report loaded");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (reportMessageLabel != null) {
                reportMessageLabel.setText("Could not load reports");
            }
        }
    }

    @FXML
    private void onGoToReportsTab() {
        inventoryTabPane.getSelectionModel().select(7);
    }

    @FXML
    private void onRefreshStockItemsClicked() {
        refreshStockItemsTable();
    }

    @FXML
    private void onRefreshDeliveriesClicked() {
        refreshDeliveriesTable();
    }

    @FXML
    private void onAddDeliveryClicked() {
        if (deliveryMessageLabel != null) {
            deliveryMessageLabel.setText("");
        }

        StockItem selected = deliveriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            deliveryMessageLabel.setText("Select a stock item first");
            return;
        }

        int qtyUnits;
        int costPerUnitCents;

        try {
            qtyUnits = Integer.parseInt(deliveryQtyField.getText().trim());
            costPerUnitCents = parseMoneyToCents(deliveryCostField.getText().trim());

            if (qtyUnits <= 0 || costPerUnitCents < 0) {
                throw new NumberFormatException();
            }
        } catch (Exception e) {
            deliveryMessageLabel.setText("Enter valid quantity and cost");
            return;
        }

        try {
            stockItemDao.recordDelivery(
                    selected.getId(),
                    qtyUnits,
                    costPerUnitCents,
                    null
            );

            deliveryQtyField.clear();
            deliveryCostField.clear();

            refreshAll();
            deliveryMessageLabel.setText("Delivery recorded");

        } catch (Exception e) {
            e.printStackTrace();
            deliveryMessageLabel.setText("Could not record delivery");
        }
    }

    private void setupDeliveriesTable() {
        if (colDeliveryName != null) {
            colDeliveryName.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getName()));
        }

        if (colDeliveryCategory != null) {
            colDeliveryCategory.setCellValueFactory(d ->
                    new SimpleStringProperty(
                            d.getValue().getCategoryName() == null ? "" : d.getValue().getCategoryName()
                    ));
        }

        if (colDeliveryUnitName != null) {
            colDeliveryUnitName.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getStockUnitName()));
        }

        if (colDeliveryUnitSize != null) {
            colDeliveryUnitSize.setCellValueFactory(d ->
                    new SimpleIntegerProperty(d.getValue().getStockUnitSize()));
        }

        if (colDeliveryCurrentUnits != null) {
            colDeliveryCurrentUnits.setCellValueFactory(d ->
                    new SimpleStringProperty(String.format("%.2f", d.getValue().getCurrentStockUnits())));
        }

        if (colDeliveryCostPrice != null) {
            colDeliveryCostPrice.setCellValueFactory(d ->
                    new SimpleStringProperty(centsToEuro(d.getValue().getCostPriceCentsPerStockUnit())));
        }

        if (deliveriesTable != null) {
            deliveriesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV == null) {
                    deliverySelectedLabel.setText("Select a stock item");
                } else {
                    deliverySelectedLabel.setText(
                            "Selected: " + newV.getName()
                                    + " | " + formatStockUnitDisplay(newV)
                    );
                }
            });
        }
    }

    private void refreshDeliveriesTable() {
        if (deliveriesTable == null) return;

        try {
            deliveriesTable.getItems().setAll(stockItemDao.listAllActive());
        } catch (Exception e) {
            e.printStackTrace();
            deliveriesTable.getItems().clear();
        }
    }

    @FXML
    private void onCreateStockItemClicked() {
        if (stockItemMessageLabel != null) {
            stockItemMessageLabel.setText("");
        }

        String name = stockItemNameField.getText() == null ? "" : stockItemNameField.getText().trim();
        String category = stockItemCategoryField.getText() == null ? "" : stockItemCategoryField.getText().trim();
        String baseUnit = stockItemBaseUnitField.getText() == null ? "" : stockItemBaseUnitField.getText().trim();
        String stockUnitName = stockItemUnitNameField.getText() == null ? "" : stockItemUnitNameField.getText().trim();
        String unitSizeText = stockItemUnitSizeField.getText() == null ? "" : stockItemUnitSizeField.getText().trim();
        String initialUnitsText = stockItemInitialUnitsField.getText() == null ? "" : stockItemInitialUnitsField.getText().trim();
        String costPriceText = stockItemCostPriceField.getText() == null ? "" : stockItemCostPriceField.getText().trim();

        if (name.isEmpty() || baseUnit.isEmpty() || stockUnitName.isEmpty()
                || unitSizeText.isEmpty() || initialUnitsText.isEmpty() || costPriceText.isEmpty()) {
            if (stockItemMessageLabel != null) {
                stockItemMessageLabel.setText("Fill all required fields");
            }
            return;
        }

        int stockUnitSize;
        int initialUnits;
        int costPriceCents;

        try {
            stockUnitSize = Integer.parseInt(unitSizeText);
            initialUnits = Integer.parseInt(initialUnitsText);
            costPriceCents = parseMoneyToCents(costPriceText);

            if (stockUnitSize <= 0 || initialUnits < 0 || costPriceCents < 0) {
                throw new NumberFormatException();
            }
        } catch (Exception e) {
            if (stockItemMessageLabel != null) {
                stockItemMessageLabel.setText("Bad values (size > 0, units >= 0, cost like 14.50)");
            }
            return;
        }

        try {
            stockItemDao.createStockItem(
                    name,
                    category,
                    baseUnit,
                    stockUnitName,
                    stockUnitSize,
                    initialUnits,
                    costPriceCents,
                    1
            );

            stockItemNameField.clear();
            stockItemCategoryField.clear();
            stockItemBaseUnitField.clear();
            stockItemUnitNameField.clear();
            stockItemUnitSizeField.clear();
            stockItemInitialUnitsField.clear();
            stockItemCostPriceField.clear();

            refreshAll();

            if (stockItemMessageLabel != null) {
                stockItemMessageLabel.setText("Stock item created");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (stockItemMessageLabel != null) {
                stockItemMessageLabel.setText("Could not create stock item");
            }
        }
    }

    private void refreshAll() {
        refreshInventoryTable();
        refreshStockItemsTable();
        refreshProductLinksTable();
        refreshRecipesTable();
        refreshDeliveriesTable();
        refreshStockTakeTable();
        refreshDashboard();
    }

    private void setupInventoryTable() {
        if (colInventoryName != null) {
            colInventoryName.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getName()));
        }

        if (colInventoryCategory != null) {
            colInventoryCategory.setCellValueFactory(d ->
                    new SimpleStringProperty(
                            d.getValue().getCategoryName() == null ? "" : d.getValue().getCategoryName()
                    ));
        }

        if (colInventoryBarcode != null) {
            colInventoryBarcode.setCellValueFactory(d ->
                    new SimpleStringProperty(
                            d.getValue().getBarcode() == null ? "" : d.getValue().getBarcode()
                    ));
        }

        if (colInventorySellPrice != null) {
            colInventorySellPrice.setCellValueFactory(d ->
                    new SimpleStringProperty(centsToEuro(d.getValue().getPriceExCents())));
        }

        if (colInventoryCostPrice != null) {
            colInventoryCostPrice.setCellValueFactory(d -> {
                try {
                    int costPerSale = productRecipeDao.getCalculatedCostPerSaleCents(d.getValue().getId());
                    if (costPerSale <= 0) {
                        return new SimpleStringProperty(centsToEuro(d.getValue().getCostPriceCents()));
                    }
                    return new SimpleStringProperty(centsToEuro(costPerSale));
                } catch (Exception e) {
                    e.printStackTrace();
                    return new SimpleStringProperty(centsToEuro(d.getValue().getCostPriceCents()));
                }
            });
        }

        if (colInventoryStock != null) {
            colInventoryStock.setCellValueFactory(d -> {
                try {
                    return new SimpleStringProperty(
                            productRecipeDao.getAvailableStockDisplayForProduct(d.getValue().getId())
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    return new SimpleStringProperty("Error");
                }
            });
        }

        if (colInventoryRetailValue != null) {
            colInventoryRetailValue.setCellValueFactory(d -> {
                try {
                    int availableStock = productRecipeDao.getAvailableStockForProduct(d.getValue().getId());
                    int value = availableStock * d.getValue().getPriceExCents();
                    return new SimpleStringProperty(centsToEuro(value));
                } catch (Exception e) {
                    e.printStackTrace();
                    return new SimpleStringProperty("Error");
                }
            });
        }

        if (colInventoryCostValue != null) {
            colInventoryCostValue.setCellValueFactory(d -> {
                try {
                    int availableStock = productRecipeDao.getAvailableStockForProduct(d.getValue().getId());
                    int costPerSale = productRecipeDao.getCalculatedCostPerSaleCents(d.getValue().getId());

                    if (costPerSale <= 0) {
                        costPerSale = d.getValue().getCostPriceCents();
                    }

                    int value = availableStock * costPerSale;
                    return new SimpleStringProperty(centsToEuro(value));
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        int availableStock = productRecipeDao.getAvailableStockForProduct(d.getValue().getId());
                        int value = availableStock * d.getValue().getCostPriceCents();
                        return new SimpleStringProperty(centsToEuro(value));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return new SimpleStringProperty("€0.00");
                    }
                }
            });
        }
    }

    private void setupStockItemsTable() {
        if (colStockItemName != null) {
            colStockItemName.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getName()));
        }

        if (colStockItemCategory != null) {
            colStockItemCategory.setCellValueFactory(d ->
                    new SimpleStringProperty(
                            d.getValue().getCategoryName() == null ? "" : d.getValue().getCategoryName()
                    ));
        }

        if (colStockItemBaseUnit != null) {
            colStockItemBaseUnit.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getBaseUnit()));
        }

        if (colStockItemUnitName != null) {
            colStockItemUnitName.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getStockUnitName()));
        }

        if (colStockItemUnitSize != null) {
            colStockItemUnitSize.setCellValueFactory(d ->
                    new SimpleIntegerProperty(d.getValue().getStockUnitSize()));
        }

        if (colStockItemCurrentBaseQty != null) {
            colStockItemCurrentBaseQty.setCellValueFactory(d ->
                    new SimpleIntegerProperty(d.getValue().getCurrentQtyBaseUnits()));
        }

        if (colStockItemCurrentUnits != null) {
            colStockItemCurrentUnits.setCellValueFactory(d ->
                    new SimpleStringProperty(String.format("%.2f", d.getValue().getCurrentStockUnits())));
        }

        if (colStockItemCostPrice != null) {
            colStockItemCostPrice.setCellValueFactory(d ->
                    new SimpleStringProperty(centsToEuro(d.getValue().getCostPriceCentsPerStockUnit())));
        }

        if (colStockItemStockValue != null) {
            colStockItemStockValue.setCellValueFactory(d ->
                    new SimpleStringProperty(centsToEuro(d.getValue().getStockValueCents())));
        }
    }

    private void refreshInventoryTable() {
        if (inventoryTable == null) {
            return;
        }

        try {
            inventoryTable.getItems().setAll(productDao.listAllActive());
        } catch (Exception e) {
            e.printStackTrace();
            inventoryTable.getItems().clear();
        }
    }

    private void refreshStockItemsTable() {
        if (stockItemsTable == null) {
            return;
        }

        try {
            stockItemsTable.getItems().setAll(stockItemDao.listAllActive());
        } catch (Exception e) {
            e.printStackTrace();
            stockItemsTable.getItems().clear();
        }
    }

    private void refreshDashboard() {
        try {
            List<Product> products = productDao.listAllActive();
            List<StockItem> lowStockItems = stockItemDao.listLowStockActive();

            int totalProducts = products.size();
            int totalStockUnits = 0;
            int totalRetailValue = 0;
            int totalCostValue = 0;

            if (lowStockListView != null) {
                lowStockListView.getItems().clear();
            }

            for (Product p : products) {
                int availableStock = productRecipeDao.getAvailableStockForProduct(p.getId());

                totalStockUnits += availableStock;
                totalRetailValue += availableStock * p.getPriceExCents();
                int costPerSale;
                try {
                    costPerSale = productRecipeDao.getCalculatedCostPerSaleCents(p.getId());
                    if (costPerSale <= 0) {
                        costPerSale = p.getCostPriceCents();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    costPerSale = p.getCostPriceCents();
                }

                totalCostValue += availableStock * costPerSale;
            }

            if (totalProductsLabel != null) {
                totalProductsLabel.setText(String.valueOf(totalProducts));
            }
            if (totalStockUnitsLabel != null) {
                totalStockUnitsLabel.setText(String.valueOf(totalStockUnits));
            }
            if (totalRetailValueLabel != null) {
                totalRetailValueLabel.setText(centsToEuro(totalRetailValue));
            }
            if (totalCostValueLabel != null) {
                totalCostValueLabel.setText(centsToEuro(totalCostValue));
            }
            if (lowStockCountLabel != null) {
                lowStockCountLabel.setText(String.valueOf(lowStockItems.size()));
            }

            if (lowStockListView != null) {
                for (StockItem s : lowStockItems) {
                    lowStockListView.getItems().add(
                            s.getName()
                                    + " - "
                                    + String.format("%.2f", s.getCurrentStockUnits())
                                    + " "
                                    + s.getStockUnitName()
                                    + " left"
                    );
                }

                if (lowStockListView.getItems().isEmpty()) {
                    lowStockListView.getItems().add("No low stock items");
                }
            }

            refreshRecentActivity();

        } catch (Exception e) {
            e.printStackTrace();

            if (totalProductsLabel != null) {
                totalProductsLabel.setText("0");
            }
            if (totalStockUnitsLabel != null) {
                totalStockUnitsLabel.setText("0");
            }
            if (totalRetailValueLabel != null) {
                totalRetailValueLabel.setText("€0.00");
            }
            if (totalCostValueLabel != null) {
                totalCostValueLabel.setText("€0.00");
            }
            if (lowStockCountLabel != null) {
                lowStockCountLabel.setText("0");
            }

            if (lowStockListView != null) {
                lowStockListView.getItems().clear();
                lowStockListView.getItems().add("Could not load low stock alerts");
            }

            if (recentActivityListView != null) {
                recentActivityListView.getItems().clear();
                recentActivityListView.getItems().add("Could not load recent activity");
            }
        }
    }

    private void refreshRecentActivity() {
        if (recentActivityListView == null) return;

        recentActivityListView.getItems().clear();

        try {
            List<com.ryan.bartill.model.StockItemMovementRow> moves = stockItemDao.listRecentMovements(15);

            for (com.ryan.bartill.model.StockItemMovementRow m : moves) {
                String staff = (m.getStaffUsername() == null || m.getStaffUsername().isBlank())
                        ? "system"
                        : m.getStaffUsername();

                recentActivityListView.getItems().add(
                        m.getCreatedAt()
                                + " | "
                                + m.getStockItemName()
                                + " | "
                                + m.getReason()
                                + " | "
                                + formatMovementInStockUnits(m.getQtyChangeBaseUnits(), m.getStockUnitSize(), m.getStockUnitName())
                                + " | "
                                + staff
                );
            }

            if (recentActivityListView.getItems().isEmpty()) {
                recentActivityListView.getItems().add("No recent stock activity");
            }

        } catch (Exception e) {
            e.printStackTrace();
            recentActivityListView.getItems().clear();
            recentActivityListView.getItems().add("Could not load recent activity");
        }
    }

    private String formatMovementInStockUnits(int qtyBaseUnits, int stockUnitSize, String stockUnitName) {
        if (stockUnitSize <= 0) {
            return (qtyBaseUnits > 0 ? "+" : "") + qtyBaseUnits;
        }

        double units = (double) qtyBaseUnits / stockUnitSize;
        String sign = units > 0 ? "+" : "";
        String unitName = (stockUnitName == null || stockUnitName.isBlank()) ? "units" : stockUnitName;

        return sign + String.format("%.2f %s", units, unitName);
    }

    private String formatStockUnitDisplay(StockItem s) {
        if (s == null) return "";

        String unit = s.getStockUnitName() == null ? "unit" : s.getStockUnitName();
        String base = s.getBaseUnit() == null ? "base units" : s.getBaseUnit();

        return "1 " + unit + " = " + s.getStockUnitSize() + " " + base;
    }

    private String formatStockTakeRowDisplay(StockTakeRow r) {
        if (r == null) return "";

        String unit = r.getStockUnitName() == null ? "unit" : r.getStockUnitName();

        // you don’t have baseUnit in StockTakeRow, so just show size
        return "1 " + unit + " = " + r.getStockUnitSize() + " base units";
    }

    private int loadRevenueTotal(Connection conn, java.time.LocalDate from, java.time.LocalDate to) throws Exception {
        String sql = """
        SELECT COALESCE(SUM(total_ex_cents), 0) AS total
        FROM sales
        WHERE date(created_at) >= ?
          AND date(created_at) <= ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    private int loadCostTotal(Connection conn, java.time.LocalDate from, java.time.LocalDate to) throws Exception {
        int total = 0;

        String sql = """
        SELECT
            si.product_id,
            COALESCE(SUM(si.qty), 0) AS qty_sold
        FROM sale_items si
        JOIN sales s ON s.id = si.sale_id
        WHERE date(s.created_at) >= ?
          AND date(s.created_at) <= ?
        GROUP BY si.product_id
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    int qtySold = rs.getInt("qty_sold");
                    int costPerSale = productRecipeDao.getCalculatedCostPerSaleCents(productId);
                    total += qtySold * costPerSale;
                }
            }
        }

        return total;
    }

    private int loadWastageLossTotal(Connection conn, java.time.LocalDate from, java.time.LocalDate to) throws Exception {
        int total = 0;

        String sql = """
        SELECT
            ABS(sim.qty_change_base_units) AS qty_base_units,
            si.cost_price_cents_per_stock_unit,
            si.stock_unit_size
        FROM stock_item_movements sim
        JOIN stock_items si ON si.id = sim.stock_item_id
        WHERE sim.qty_change_base_units < 0
          AND date(sim.created_at) >= ?
          AND date(sim.created_at) <= ?
          AND UPPER(sim.reason) NOT LIKE 'SALE%'
          AND UPPER(sim.reason) NOT LIKE 'DELIVERY%'
          AND UPPER(sim.reason) NOT LIKE '%REVERSAL%'
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int qtyBaseUnits = rs.getInt("qty_base_units");
                    int costPerStockUnitCents = rs.getInt("cost_price_cents_per_stock_unit");
                    int stockUnitSize = rs.getInt("stock_unit_size");

                    if (stockUnitSize > 0) {
                        double costPerBaseUnit = (double) costPerStockUnitCents / stockUnitSize;
                        total += (int) Math.round(costPerBaseUnit * qtyBaseUnits);
                    }
                }
            }
        }

        return total;
    }

    private List<ReportProductRow> loadTopProducts(Connection conn, java.time.LocalDate from, java.time.LocalDate to) throws Exception {
        List<ReportProductRow> out = new ArrayList<>();

        String sql = """
        SELECT
            p.id AS product_id,
            p.name AS product_name,
            COALESCE(SUM(si.qty), 0) AS qty_sold,
            COALESCE(SUM(si.line_ex_cents), 0) AS revenue_cents
        FROM sale_items si
        JOIN products p ON p.id = si.product_id
        JOIN sales s ON s.id = si.sale_id
        WHERE date(s.created_at) >= ?
          AND date(s.created_at) <= ?
        GROUP BY p.id, p.name
        ORDER BY qty_sold DESC, revenue_cents DESC
        LIMIT 20
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    String productName = rs.getString("product_name");
                    int qtySold = rs.getInt("qty_sold");
                    int revenueCents = rs.getInt("revenue_cents");
                    int costPerSale = productRecipeDao.getCalculatedCostPerSaleCents(productId);
                    int profitCents = revenueCents - (qtySold * costPerSale);

                    out.add(new ReportProductRow(productName, qtySold, revenueCents, profitCents));
                }
            }
        }

        return out;
    }

    private List<ReportWastageRow> loadWastage(Connection conn, java.time.LocalDate from, java.time.LocalDate to) throws Exception {
        List<ReportWastageRow> out = new ArrayList<>();

        String sql = """
        SELECT
            si.name AS stock_item_name,
            ABS(sim.qty_change_base_units) AS qty_base_units,
            sim.reason,
            si.cost_price_cents_per_stock_unit,
            si.stock_unit_size
        FROM stock_item_movements sim
        JOIN stock_items si ON si.id = sim.stock_item_id
        WHERE sim.qty_change_base_units < 0
          AND date(sim.created_at) >= ?
          AND date(sim.created_at) <= ?
          AND UPPER(sim.reason) NOT LIKE 'SALE%'
          AND UPPER(sim.reason) NOT LIKE 'DELIVERY%'
          AND UPPER(sim.reason) NOT LIKE '%REVERSAL%'
        ORDER BY sim.id DESC
        LIMIT 100
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String itemName = rs.getString("stock_item_name");
                    int qtyBaseUnits = rs.getInt("qty_base_units");
                    String reason = rs.getString("reason");
                    int costPerStockUnitCents = rs.getInt("cost_price_cents_per_stock_unit");
                    int stockUnitSize = rs.getInt("stock_unit_size");

                    int valueCents = 0;
                    if (stockUnitSize > 0) {
                        double costPerBaseUnit = (double) costPerStockUnitCents / stockUnitSize;
                        valueCents = (int) Math.round(costPerBaseUnit * qtyBaseUnits);
                    }

                    out.add(new ReportWastageRow(itemName, qtyBaseUnits, reason, valueCents));
                }
            }
        }

        return out;
    }


    private void drawRevenueChart(Connection conn, java.time.LocalDate from, java.time.LocalDate to) throws Exception {
        if (reportRevenueChart == null) return;

        reportRevenueChart.getData().clear();

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Revenue");

        String sql = """
        SELECT date(created_at) AS sale_day,
               COALESCE(SUM(total_ex_cents), 0) AS revenue_cents
        FROM sales
        WHERE date(created_at) >= ?
          AND date(created_at) <= ?
        GROUP BY date(created_at)
        ORDER BY date(created_at)
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String day = rs.getString("sale_day");
                    double revenueEuro = rs.getInt("revenue_cents") / 100.0;
                    series.getData().add(new javafx.scene.chart.XYChart.Data<>(day, revenueEuro));
                }
            }
        }

        reportRevenueChart.getData().add(series);
    }

    private void drawHourlySalesChart(Connection conn, java.time.LocalDate from, java.time.LocalDate to) throws Exception {
        if (reportHourlySalesChart == null) return;

        reportHourlySalesChart.getData().clear();

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Hourly Sales");

        String sql = """
        SELECT strftime('%H', created_at) AS hour_num,
               COALESCE(SUM(total_ex_cents), 0) AS revenue_cents
        FROM sales
        WHERE date(created_at) >= ?
          AND date(created_at) <= ?
        GROUP BY strftime('%H', created_at)
        ORDER BY strftime('%H', created_at)
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String hour = rs.getString("hour_num");
                    double revenueEuro = rs.getInt("revenue_cents") / 100.0;
                    series.getData().add(new javafx.scene.chart.XYChart.Data<>(hour + ":00", revenueEuro));
                }
            }
        }

        reportHourlySalesChart.getData().add(series);
    }


    private void drawProductHeatmap(Connection conn, java.time.LocalDate from, java.time.LocalDate to) throws Exception {
        if (reportProductHeatmapGrid == null) return;

        reportProductHeatmapGrid.getChildren().clear();

        List<ReportProductRow> top = loadTopProducts(conn, from, to);
        int maxQty = 1;
        for (ReportProductRow r : top) {
            if (r.getQty() > maxQty) maxQty = r.getQty();
        }

        int col = 0;
        int row = 0;

        for (ReportProductRow r : top) {
            double intensity = (double) r.getQty() / maxQty;
            int shade = 255 - (int) Math.round(intensity * 120);

            Label cell = new Label(r.getName() + "\nQty: " + r.getQty());
            cell.setMinSize(140, 70);
            cell.setWrapText(true);
            cell.setStyle(
                    "-fx-background-color: rgb(" + shade + "," + shade + ",255);" +
                            "-fx-border-color: #cccccc;" +
                            "-fx-padding: 8;" +
                            "-fx-font-weight: bold;"
            );

            reportProductHeatmapGrid.add(cell, col, row);

            col++;
            if (col >= 4) {
                col = 0;
                row++;
            }
        }
    }


    private List<String> loadInsights(Connection conn, java.time.LocalDate from, java.time.LocalDate to) throws Exception {
        List<String> out = new ArrayList<>();

        int revenue = loadRevenueTotal(conn, from, to);
        int wastageLoss = loadWastageLossTotal(conn, from, to);

        out.add("You are losing " + centsToEuro(wastageLoss) + " from wastage in this period.");

        List<ReportProductRow> topProducts = loadTopProducts(conn, from, to);
        if (!topProducts.isEmpty() && revenue > 0) {
            int running = 0;
            int productCount = 0;

            for (ReportProductRow row : topProducts) {
                running += row.getRevenueCents();
                productCount++;
                if (running >= revenue * 0.8) {
                    out.add("Top " + productCount + " products generate about 80% of revenue.");
                    break;
                }
            }

            ReportProductRow best = topProducts.get(0);
            out.add("Best seller: " + best.getName() + " (" + best.getQty() + " sold).");
        }

        if (topProducts.size() >= 2) {
            ReportProductRow worst = topProducts.get(topProducts.size() - 1);
            out.add("Lowest performer in the top list: " + worst.getName() + ".");
        }

        if (out.isEmpty()) {
            out.add("No sales data for this period.");
        }

        return out;
    }


    private List<ReportStockAlertRow> loadStockAlerts(Connection conn, java.time.LocalDate from, java.time.LocalDate to) throws Exception {
        List<ReportStockAlertRow> out = new ArrayList<>();

        long daysInRange = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
        if (daysInRange <= 0) daysInRange = 1;

        for (StockItem item : stockItemDao.listAllActive()) {
            int usedBaseUnits = getBaseUnitsUsedForStockItem(conn, item.getId(), from, to);
            double avgDailyUse = usedBaseUnits / (double) daysInRange;

            if (avgDailyUse <= 0) continue;

            double daysLeft = item.getCurrentQtyBaseUnits() / avgDailyUse;
            double currentUnits = item.getCurrentStockUnits();
            double dailyUnits = avgDailyUse / item.getStockUnitSize();

            String suggestion = "";
            if (daysLeft <= 3) {
                suggestion = "Reorder urgently";
            } else if (daysLeft <= 7) {
                suggestion = "Reorder soon";
            } else {
                suggestion = "Stock level okay";
            }

            out.add(new ReportStockAlertRow(
                    item.getName(),
                    String.format("%.2f %s", currentUnits, item.getStockUnitName()),
                    String.format("%.2f %s/day", dailyUnits, item.getStockUnitName()),
                    String.format("%.1f days", daysLeft),
                    suggestion
            ));
        }

        out.sort((a, b) -> {
            double da = parseDaysLeft(a.getDaysLeft());
            double db = parseDaysLeft(b.getDaysLeft());
            return Double.compare(da, db);
        });

        return out;
    }

    private double parseDaysLeft(String text) {
        try {
            return Double.parseDouble(text.replace(" days", "").trim());
        } catch (Exception e) {
            return 999999;
        }
    }

    private int getBaseUnitsUsedForStockItem(Connection conn, int stockItemId, java.time.LocalDate from, java.time.LocalDate to) throws Exception {
        int total = 0;

        String recipeSql = """
        SELECT COALESCE(SUM(si.qty * pri.qty_base_units_used), 0) AS total_used
        FROM sale_items si
        JOIN sales s ON s.id = si.sale_id
        JOIN product_recipe_items pri ON pri.product_id = si.product_id
        WHERE pri.stock_item_id = ?
          AND date(s.created_at) >= ?
          AND date(s.created_at) <= ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(recipeSql)) {
            ps.setInt(1, stockItemId);
            ps.setString(2, from.toString());
            ps.setString(3, to.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total += rs.getInt("total_used");
                }
            }
        }

        String defaultSql = """
        SELECT COALESCE(SUM(si.qty * p.default_qty_used), 0) AS total_used
        FROM sale_items si
        JOIN sales s ON s.id = si.sale_id
        JOIN products p ON p.id = si.product_id
        WHERE p.default_stock_item_id = ?
          AND NOT EXISTS (
              SELECT 1
              FROM product_recipe_items pri
              WHERE pri.product_id = p.id
          )
          AND date(s.created_at) >= ?
          AND date(s.created_at) <= ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(defaultSql)) {
            ps.setInt(1, stockItemId);
            ps.setString(2, from.toString());
            ps.setString(3, to.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total += rs.getInt("total_used");
                }
            }
        }

        return total;
    }


    @FXML
    private void onExportReportsCsvClicked() {
        try {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Save Reports Excel");
            chooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            chooser.setInitialFileName("reports.xlsx");

            java.io.File file = chooser.showSaveDialog(rootPane.getScene().getWindow());
            if (file == null) return;

            try (Connection conn = Db.getConnection();
                 java.io.PrintWriter out = new java.io.PrintWriter(file)) {

                java.time.LocalDate from = reportFromDate.getValue();
                java.time.LocalDate to = reportToDate.getValue();

                out.println("TYPE,NAME,QTY,REVENUE,PROFIT,REASON,VALUE");

                for (ReportProductRow row : loadTopProducts(conn, from, to)) {
                    out.println("PRODUCT,\"" + row.getName() + "\"," + row.getQty() + "," + row.getRevenueCents() + "," + row.getProfitCents() + ",,");
                }

                for (ReportWastageRow row : loadWastage(conn, from, to)) {
                    out.println("WASTAGE,\"" + row.getName() + "\"," + row.getQty() + ",,,\"" + row.getReason() + "\"," + row.getValueCents());
                }
            }

            if (reportMessageLabel != null) {
                reportMessageLabel.setText("CSV exported");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (reportMessageLabel != null) {
                reportMessageLabel.setText("CSV export failed");
            }
        }
    }

    @FXML
    private void onExportReportsExcelClicked() {
        try {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Save Reports Excel");
            chooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            chooser.setInitialFileName("reports.xlsx");

            java.io.File file = chooser.showSaveDialog(rootPane.getScene().getWindow());
            if (file == null) return;

            java.time.LocalDate from = reportFromDate == null || reportFromDate.getValue() == null
                    ? java.time.LocalDate.now().minusDays(6)
                    : reportFromDate.getValue();

            java.time.LocalDate to = reportToDate == null || reportToDate.getValue() == null
                    ? java.time.LocalDate.now()
                    : reportToDate.getValue();

            try (Connection conn = Db.getConnection();
                 org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

                // ---------- Styles ----------
                org.apache.poi.ss.usermodel.DataFormat dataFormat = wb.createDataFormat();

                org.apache.poi.ss.usermodel.Font titleFont = wb.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 16);

                org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
                headerFont.setBold(true);

                org.apache.poi.ss.usermodel.CellStyle titleStyle = wb.createCellStyle();
                titleStyle.setFont(titleFont);

                org.apache.poi.ss.usermodel.CellStyle headerStyle = wb.createCellStyle();
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
                headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

                org.apache.poi.ss.usermodel.CellStyle moneyStyle = wb.createCellStyle();
                moneyStyle.setDataFormat(dataFormat.getFormat("€#,##0.00"));

                org.apache.poi.ss.usermodel.CellStyle intStyle = wb.createCellStyle();
                intStyle.setDataFormat(dataFormat.getFormat("0"));

                org.apache.poi.ss.usermodel.CellStyle textWrapStyle = wb.createCellStyle();
                textWrapStyle.setWrapText(true);

                // ---------- Load report data ----------
                int revenue = loadRevenueTotal(conn, from, to);
                int cost = loadCostTotal(conn, from, to);
                int profit = revenue - cost;
                int wastageLoss = loadWastageLossTotal(conn, from, to);

                List<ReportProductRow> topProducts = loadTopProducts(conn, from, to);
                List<ReportWastageRow> wastageRows = loadWastage(conn, from, to);
                List<String> insights = loadInsights(conn, from, to);
                List<ReportStockAlertRow> stockAlerts = loadStockAlerts(conn, from, to);

                // ---------- Summary sheet ----------
                org.apache.poi.ss.usermodel.Sheet summarySheet = wb.createSheet("Summary");
                int rowIndex = 0;

                org.apache.poi.ss.usermodel.Row row = summarySheet.createRow(rowIndex++);
                org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
                cell.setCellValue("Bar Till Reports");
                cell.setCellStyle(titleStyle);

                rowIndex++;

                row = summarySheet.createRow(rowIndex++);
                row.createCell(0).setCellValue("From");
                row.createCell(1).setCellValue(from.toString());

                row = summarySheet.createRow(rowIndex++);
                row.createCell(0).setCellValue("To");
                row.createCell(1).setCellValue(to.toString());

                rowIndex++;

                row = summarySheet.createRow(rowIndex++);
                row.createCell(0).setCellValue("Revenue");
                cell = row.createCell(1);
                cell.setCellValue(revenue / 100.0);
                cell.setCellStyle(moneyStyle);

                row = summarySheet.createRow(rowIndex++);
                row.createCell(0).setCellValue("Cost");
                cell = row.createCell(1);
                cell.setCellValue(cost / 100.0);
                cell.setCellStyle(moneyStyle);

                row = summarySheet.createRow(rowIndex++);
                row.createCell(0).setCellValue("Profit");
                cell = row.createCell(1);
                cell.setCellValue(profit / 100.0);
                cell.setCellStyle(moneyStyle);

                row = summarySheet.createRow(rowIndex++);
                row.createCell(0).setCellValue("Wastage Loss");
                cell = row.createCell(1);
                cell.setCellValue(wastageLoss / 100.0);
                cell.setCellStyle(moneyStyle);

                rowIndex += 2;

                row = summarySheet.createRow(rowIndex++);
                cell = row.createCell(0);
                cell.setCellValue("Insights");
                cell.setCellStyle(headerStyle);

                for (String insight : insights) {
                    row = summarySheet.createRow(rowIndex++);
                    cell = row.createCell(0);
                    cell.setCellValue(insight);
                    cell.setCellStyle(textWrapStyle);
                }

                // ---------- Top Products sheet ----------
                org.apache.poi.ss.usermodel.Sheet productsSheet = wb.createSheet("Top Products");
                rowIndex = 0;

                row = productsSheet.createRow(rowIndex++);
                cell = row.createCell(0);
                cell.setCellValue("Top Products");
                cell.setCellStyle(titleStyle);

                rowIndex++;

                row = productsSheet.createRow(rowIndex++);
                String[] productHeaders = {"Product", "Qty Sold", "Revenue (€)", "Profit (€)"};
                for (int i = 0; i < productHeaders.length; i++) {
                    cell = row.createCell(i);
                    cell.setCellValue(productHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }

                for (ReportProductRow p : topProducts) {
                    row = productsSheet.createRow(rowIndex++);

                    row.createCell(0).setCellValue(p.getName());

                    cell = row.createCell(1);
                    cell.setCellValue(p.getQty());
                    cell.setCellStyle(intStyle);

                    cell = row.createCell(2);
                    cell.setCellValue(p.getRevenueCents() / 100.0);
                    cell.setCellStyle(moneyStyle);

                    cell = row.createCell(3);
                    cell.setCellValue(p.getProfitCents() / 100.0);
                    cell.setCellStyle(moneyStyle);
                }

                // ---------- Wastage sheet ----------
                org.apache.poi.ss.usermodel.Sheet wastageSheet = wb.createSheet("Wastage");
                rowIndex = 0;

                row = wastageSheet.createRow(rowIndex++);
                cell = row.createCell(0);
                cell.setCellValue("Wastage Report");
                cell.setCellStyle(titleStyle);

                rowIndex++;

                row = wastageSheet.createRow(rowIndex++);
                String[] wasteHeaders = {"Item", "Qty", "Reason", "Value (€)"};
                for (int i = 0; i < wasteHeaders.length; i++) {
                    cell = row.createCell(i);
                    cell.setCellValue(wasteHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }

                for (ReportWastageRow w : wastageRows) {
                    row = wastageSheet.createRow(rowIndex++);

                    row.createCell(0).setCellValue(w.getName());

                    cell = row.createCell(1);
                    cell.setCellValue(w.getQty());
                    cell.setCellStyle(intStyle);

                    cell = row.createCell(2);
                    cell.setCellValue(w.getReason());
                    cell.setCellStyle(textWrapStyle);

                    cell = row.createCell(3);
                    cell.setCellValue(w.getValueCents() / 100.0);
                    cell.setCellStyle(moneyStyle);
                }

                // ---------- Stock Alerts sheet ----------
                org.apache.poi.ss.usermodel.Sheet stockSheet = wb.createSheet("Stock Alerts");
                rowIndex = 0;

                row = stockSheet.createRow(rowIndex++);
                cell = row.createCell(0);
                cell.setCellValue("Stock Intelligence");
                cell.setCellStyle(titleStyle);

                rowIndex++;

                row = stockSheet.createRow(rowIndex++);
                String[] stockHeaders = {"Stock Item", "Current Units", "Avg Daily Use", "Days Left", "Suggestion"};
                for (int i = 0; i < stockHeaders.length; i++) {
                    cell = row.createCell(i);
                    cell.setCellValue(stockHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }

                for (ReportStockAlertRow s : stockAlerts) {
                    row = stockSheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(s.getItemName());
                    row.createCell(1).setCellValue(s.getCurrentUnits());
                    row.createCell(2).setCellValue(s.getAvgDailyUse());
                    row.createCell(3).setCellValue(s.getDaysLeft());
                    row.createCell(4).setCellValue(s.getSuggestion());
                }

                // ---------- Autosize ----------
                for (int i = 0; i < 2; i++) summarySheet.autoSizeColumn(i);
                for (int i = 0; i < 4; i++) productsSheet.autoSizeColumn(i);
                for (int i = 0; i < 4; i++) wastageSheet.autoSizeColumn(i);
                for (int i = 0; i < 5; i++) stockSheet.autoSizeColumn(i);

                // Slightly widen the insights column
                summarySheet.setColumnWidth(0, 12000);

                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    wb.write(fos);
                }
            }

            if (reportMessageLabel != null) {
                reportMessageLabel.setText("Excel exported");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (reportMessageLabel != null) {
                reportMessageLabel.setText("Excel export failed");
            }
        }
    }

    private String centsToEuro(int cents) {
        int abs = Math.abs(cents);
        int euros = abs / 100;
        int rem = abs % 100;
        String value = "€" + euros + "." + (rem < 10 ? "0" : "") + rem;
        return cents < 0 ? "-" + value : value;

    }

    private List<ReportProductRow> loadTopProducts(Connection conn) throws Exception {
        List<ReportProductRow> out = new ArrayList<>();

        String sql = """
        SELECT
            p.id AS product_id,
            p.name AS product_name,
            COALESCE(SUM(si.qty), 0) AS qty_sold,
            COALESCE(SUM(si.line_ex_cents), 0) AS revenue_cents
        FROM sale_items si
        JOIN products p ON p.id = si.product_id
        JOIN sales s ON s.id = si.sale_id
        GROUP BY p.id, p.name
        ORDER BY qty_sold DESC, revenue_cents DESC
        LIMIT 20
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                int qtySold = rs.getInt("qty_sold");
                int revenueCents = rs.getInt("revenue_cents");

                int costPerSale = 0;
                try {
                    costPerSale = productRecipeDao.getCalculatedCostPerSaleCents(productId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                int profitCents = revenueCents - (qtySold * costPerSale);

                out.add(new ReportProductRow(
                        productName,
                        qtySold,
                        revenueCents,
                        profitCents
                ));
            }
        }

        return out;
    }

    private List<ReportWastageRow> loadWastage(Connection conn) throws Exception {
        List<ReportWastageRow> out = new ArrayList<>();

        String sql = """
        SELECT
            si.name AS stock_item_name,
            ABS(sim.qty_change_base_units) AS qty_base_units,
            sim.reason,
            si.cost_price_cents_per_stock_unit,
            si.stock_unit_size
        FROM stock_item_movements sim
        JOIN stock_items si ON si.id = sim.stock_item_id
        WHERE sim.qty_change_base_units < 0
          AND sim.reason LIKE 'WASTAGE%'
        ORDER BY sim.id DESC
        LIMIT 50
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String itemName = rs.getString("stock_item_name");
                int qtyBaseUnits = rs.getInt("qty_base_units");
                String reason = rs.getString("reason");
                int costPerStockUnitCents = rs.getInt("cost_price_cents_per_stock_unit");
                int stockUnitSize = rs.getInt("stock_unit_size");

                int valueCents = 0;
                if (stockUnitSize > 0) {
                    double costPerBaseUnit = (double) costPerStockUnitCents / stockUnitSize;
                    valueCents = (int) Math.round(costPerBaseUnit * qtyBaseUnits);
                }

                out.add(new ReportWastageRow(
                        itemName,
                        qtyBaseUnits,
                        reason,
                        valueCents
                ));
            }
        }

        return out;
    }

    private int getTotalSoldForProduct(Connection conn, int productId) throws Exception {
        String sql = """
        SELECT COALESCE(SUM(qty), 0) AS total_sold
        FROM sale_items
        WHERE product_id = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_sold");
                }
            }
        }

        return 0;
    }

    private int parseMoneyToCents(String text) {
        String t = text.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException();
        }

        if (!t.contains(".")) {
            return Integer.parseInt(t) * 100;
        }

        String[] parts = t.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException();
        }

        int euros = Integer.parseInt(parts[0]);
        String centsPart = parts[1];

        if (centsPart.length() == 1) {
            centsPart = centsPart + "0";
        }
        if (centsPart.length() != 2) {
            throw new IllegalArgumentException();
        }

        int cents = Integer.parseInt(centsPart);
        return euros * 100 + cents;
    }

    @FXML
    private void onImportStockItemsCsvClicked() {
        try {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Select Stock Items CSV");

            chooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            var file = chooser.showOpenDialog(rootPane.getScene().getWindow());

            if (file == null) return;

            int imported = stockItemDao.importFromCsv(file.toPath());

            stockItemMessageLabel.setText("Imported " + imported + " stock items");

            refreshStockItemsTable();
            refreshDashboard();

        } catch (Exception e) {
            e.printStackTrace();
            stockItemMessageLabel.setText("Import failed");
        }
    }

    @FXML
    private void onImportRecipesCsvClicked() {
        try {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Select Recipes CSV");
            chooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            var file = chooser.showOpenDialog(rootPane.getScene().getWindow());
            if (file == null) return;

            int imported = productRecipeDao.importRecipesFromCsv(file.toPath());

            if (recipeMessageLabel != null) {
                recipeMessageLabel.setText("Imported " + imported + " recipe rows");
            }

            refreshRecipesTable();

        } catch (Exception e) {
            e.printStackTrace();
            if (recipeMessageLabel != null) {
                recipeMessageLabel.setText("Recipe import failed");
            }
        }
    }

    private Integer findProductIdByName(Connection conn, String productName) throws Exception {
        String sql = "SELECT id FROM products WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                return null;
            }
        }
    }

    private Integer findStockItemIdByName(Connection conn, String stockItemName) throws Exception {
        String sql = "SELECT id FROM stock_items WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stockItemName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                return null;
            }
        }
    }

    @FXML
    private void onImportDefaultStockLinksCsvClicked() {
        try {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Select Product Stock Links CSV");
            chooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            var file = chooser.showOpenDialog(rootPane.getScene().getWindow());
            if (file == null) return;

            int imported = productDao.importDefaultStockLinksFromCsv(file.toPath());

            if (productLinkMessageLabel != null) {
                productLinkMessageLabel.setText("Imported " + imported + " product stock links");
            }

            refreshAll();

        } catch (Exception e) {
            e.printStackTrace();
            if (productLinkMessageLabel != null) {
                productLinkMessageLabel.setText("Import failed: " + e.getMessage());
            }
        }
    }

    private void refreshWastageCombo() {
        if (wastageStockItemCombo == null) return;

        try {
            wastageStockItemCombo.getItems().setAll(stockItemDao.listAllActive());
        } catch (Exception e) {
            e.printStackTrace();
            if (wastageMessageLabel != null) {
                wastageMessageLabel.setText("Could not load stock items");
            }
        }
    }

}