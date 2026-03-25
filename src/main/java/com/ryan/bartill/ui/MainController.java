package com.ryan.bartill.ui;

import com.ryan.bartill.app.SceneSwitcher;
import com.ryan.bartill.app.Session;
import com.ryan.bartill.dao.CategoryDao;
import com.ryan.bartill.dao.ProductDao;
import com.ryan.bartill.dao.StaffDao;
import com.ryan.bartill.model.Category;
import com.ryan.bartill.model.Product;
import com.ryan.bartill.model.Role;
import com.ryan.bartill.model.SaleHistoryRow;
import com.ryan.bartill.model.Staff;
import com.ryan.bartill.util.Db;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    // ----------------------------
    // CONFIG: your POS grid size
    // ----------------------------
    private static final int GRID_COLS = 7;
    private static final int GRID_ROWS = 6;
    private static final int TILE_COUNT = GRID_COLS * GRID_ROWS;

    private static final double BTN_W = 95;
    private static final double BTN_H = 95;

    // Top bar
    @FXML private Label userLabel;
    @FXML private Button inventoryButton;

    @FXML private Tab tabsTab;
    @FXML private GridPane tabsGrid;

    // Tabs
    @FXML private TabPane tabPane;
    @FXML private Tab reportsTab;
    @FXML private Tab stockTab;
    @FXML private Tab staffTab;
    @FXML private Tab quickLayoutTab;
    @FXML private Tab salesHistoryTab;

    // Staff tab
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private Label staffMessageLabel;
    @FXML private ListView<String> staffListView;
    @FXML private TextField newPinField;

    // POS controls
    @FXML private TextField searchField;
    @FXML private TextField qtyField;
    @FXML private ListView<String> basketListView;
    @FXML private Label totalExLabel;
    @FXML private Label vatLabel;
    @FXML private Label totalIncLabel;
    @FXML private TextField cashField;
    @FXML private Label changeLabel;
    @FXML private Label posMessageLabel;
    @FXML private Label posSearchMessageLabel;

    // POS quick buttons grid
    @FXML private GridPane quickGrid;

    // New fast payment UI
    @FXML private Button sendButton;
    @FXML private VBox paymentOverlay;
    @FXML private HBox paymentChoiceBox;
    @FXML private VBox cashPaymentBox;
    @FXML private Label paymentTotalLabel;
    @FXML private Label cashEnteredLabel;
    @FXML private Label bigChangeLabel;

    // Quick Layout editor grid
    @FXML private GridPane layoutGrid;
    @FXML private Label layoutSelectedLabel;
    @FXML private ComboBox<String> layoutTypeCombo;
    @FXML private ComboBox<Product> layoutProductCombo;
    @FXML private ComboBox<Category> layoutCategoryCombo;
    @FXML private TextField layoutLabelField;
    @FXML private TextField layoutColorField;
    @FXML private Label layoutMsgLabel;

    // Reports
    @FXML private DatePicker eodDatePicker;
    @FXML private TextArea eodOutput;
    @FXML private DatePicker vatFromDatePicker;
    @FXML private DatePicker vatToDatePicker;
    @FXML private TextArea vatOutput;

    // Stock tab controls
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colBarcode;
    @FXML private TableColumn<Product, String> colPrice;
    @FXML private TableColumn<Product, String> colCostPrice;
    @FXML private TableColumn<Product, Integer> colVat;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String> colActive;
    @FXML private TableColumn<Product, String> colCategory;

    @FXML private Label stockSelectedLabel;
    @FXML private TextField adjustQtyField;
    @FXML private Label stockMessageLabel;

    @FXML private TextField newProdCostField;
    @FXML private TextField newProdNameField;
    @FXML private TextField newProdBarcodeField;
    @FXML private TextField newProdPriceField;
    @FXML private TextField newProdVatField;
    @FXML private TextField newProdStockField;

    @FXML private TableView<com.ryan.bartill.model.StockMovement> movementTable;
    @FXML private TableColumn<com.ryan.bartill.model.StockMovement, String> colMovTime;
    @FXML private TableColumn<com.ryan.bartill.model.StockMovement, String> colMovUser;
    @FXML private TableColumn<com.ryan.bartill.model.StockMovement, String> colMovReason;
    @FXML private TableColumn<com.ryan.bartill.model.StockMovement, Integer> colMovQty;

    // Category controls
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextField newCategoryNameField;
    @FXML private TextField newCategoryColorField;

    // Sales history tab
    @FXML private TableView<SaleHistoryRow> salesHistoryTable;
    @FXML private TableColumn<SaleHistoryRow, Number> colSaleId;
    @FXML private TableColumn<SaleHistoryRow, String> colSaleTime;
    @FXML private TableColumn<SaleHistoryRow, String> colSaleStaff;
    @FXML private TableColumn<SaleHistoryRow, String> colSalePayment;
    @FXML private TableColumn<SaleHistoryRow, String> colSaleTotal;

    @FXML private Label salesHistorySelectedLabel;
    @FXML private ListView<String> salesHistoryItemsList;

    // DAOs/services
    private final StaffDao staffDao = new StaffDao();
    private final ProductDao productDao = new ProductDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final com.ryan.bartill.dao.QuickButtonDao quickDao = new com.ryan.bartill.dao.QuickButtonDao();
    private final com.ryan.bartill.service.SaleService saleService = new com.ryan.bartill.service.SaleService();

    // Quick pages / category filter
    private int quickPage = 1;
    private Integer currentCategoryFilter = null;

    // Layout editor state
    private int layoutPage = 1;
    private Integer selectedLayoutPos = null;

    // Cash keypad state
    private String cashInput = "";

    // Basket model
    private static class BasketLine {
        final int productId;
        final String name;
        final int qty;
        final int unitPriceExCents;
        final int vatRate;

        BasketLine(int productId, String name, int qty, int unitPriceExCents, int vatRate) {
            this.productId = productId;
            this.name = name;
            this.qty = qty;
            this.unitPriceExCents = unitPriceExCents;
            this.vatRate = vatRate;
        }
    }

    private final List<BasketLine> basket = new ArrayList<>();

    // ----------------------------
    // INIT
    // ----------------------------
    @FXML
    private void initialize() {
        Staff user = Session.getCurrentUser();
        if (user == null) return;

        userLabel.setText("Logged in as: " + user.getUsername() + " (" + user.getRole() + ")");

        boolean isManager = user.getRole() == Role.MANAGER;

        if (!isManager && inventoryButton != null) {
            inventoryButton.setVisible(false);
            inventoryButton.setManaged(false);
        }

        if (!isManager) {
            if (tabPane != null) {
                tabPane.getTabs().remove(reportsTab);
                tabPane.getTabs().remove(stockTab);
                tabPane.getTabs().remove(staffTab);
                tabPane.getTabs().remove(quickLayoutTab);
                tabPane.getTabs().remove(salesHistoryTab);
            }
        } else {
            if (roleCombo != null) {
                roleCombo.getItems().setAll(Role.MANAGER, Role.BARTENDER, Role.WAITRESS);
                roleCombo.getSelectionModel().select(Role.BARTENDER);
                refreshStaffList();
            }

            if (quickLayoutTab != null && layoutTypeCombo != null) {
                layoutTypeCombo.getItems().setAll("PRODUCT", "CATEGORY");
                layoutTypeCombo.getSelectionModel().select("PRODUCT");

                layoutTypeCombo.valueProperty().addListener((obs, o, n) -> {
                    boolean isProd = "PRODUCT".equals(n);
                    if (layoutProductCombo != null) layoutProductCombo.setDisable(!isProd);
                    if (layoutCategoryCombo != null) layoutCategoryCombo.setDisable(isProd);
                });

                refreshLayoutDropdowns();
                loadLayoutPage(1);
            }

            if (productTable != null) {
                setupStockTable();
                setupMovementTable();
                refreshStockTable();
                refreshCategoryCombos();
            }

            if (salesHistoryTable != null) {
                setupSalesHistoryTable();
                refreshSalesHistoryTable();
            }
        }

        if (qtyField != null) qtyField.setText("1");
        refreshBasketUI();

        if (quickGrid != null) {
            buildEmptyGridPlaceholders(quickGrid);
            loadQuickButtons();
        }

        if (paymentOverlay != null) {
            paymentOverlay.setVisible(false);
            paymentOverlay.setManaged(false);
        }
        if (paymentChoiceBox != null) {
            paymentChoiceBox.setVisible(false);
            paymentChoiceBox.setManaged(false);
        }
        if (cashPaymentBox != null) {
            cashPaymentBox.setVisible(false);
            cashPaymentBox.setManaged(false);
        }

        if (eodDatePicker != null) eodDatePicker.setValue(java.time.LocalDate.now());
        if (vatFromDatePicker != null) vatFromDatePicker.setValue(java.time.LocalDate.now());
        if (vatToDatePicker != null) vatToDatePicker.setValue(java.time.LocalDate.now());

        if (tabsGrid != null) {
            buildTabsGrid();
        }
    }

    private void refreshLayoutDropdowns() {
        try {
            if (layoutProductCombo != null) {
                layoutProductCombo.getItems().setAll(productDao.listAllActive());
            }
            if (layoutCategoryCombo != null) {
                layoutCategoryCombo.getItems().setAll(categoryDao.listAll());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (layoutMsgLabel != null) layoutMsgLabel.setText("Could not load products/categories");
        }
    }

    private void refreshCategoryCombos() {
        try {
            var cats = categoryDao.listAll();
            if (categoryCombo != null) categoryCombo.getItems().setAll(cats);
            if (layoutCategoryCombo != null) layoutCategoryCombo.getItems().setAll(cats);
        } catch (Exception e) {
            e.printStackTrace();
            if (stockMessageLabel != null) stockMessageLabel.setText("Could not load categories");
        }
    }

    // ----------------------------
    // LOGOUT
    // ----------------------------
    @FXML
    private void onLogoutClicked() {
        try {
            Session.clear();
            Stage stage = (Stage) tabPane.getScene().getWindow();
            SceneSwitcher.switchTo(stage, "/fxml/login.fxml", "Bar Till System - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onOpenInventorySystem() {
        try {
            Staff currentUser = Session.getCurrentUser();
            if (currentUser == null || currentUser.getRole() != Role.MANAGER) {
                showAlert("Access denied", "Only managers can access the inventory system.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/InventoryDashboard.fxml")
            );

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Stock & Inventory System");
            stage.setScene(new Scene(root, 1400, 850));
            stage.show();

            stage.setFullScreenExitHint("");
            stage.setFullScreenExitKeyCombination(null);
            stage.setMaximized(true);
            stage.setFullScreen(true);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Stock & Inventory System.");
        }
    }

    private void setupMovementTable() {

        if (movementTable == null) return;

        colMovTime.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getCreatedAt()));

        colMovUser.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getStaffUsername()));

        colMovReason.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getReason()));

        colMovQty.setCellValueFactory(d ->
                new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getQtyChange()));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ----------------------------
    // STAFF
    // ----------------------------
    @FXML
    private void onCreateUserClicked() {
        String username = newUsernameField.getText() == null ? "" : newUsernameField.getText().trim();
        String password = newPasswordField.getText() == null ? "" : newPasswordField.getText();
        String pin = newPinField.getText() == null ? "" : newPinField.getText().trim();
        Role role = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty() || pin.isEmpty() || role == null) {
            staffMessageLabel.setText("Fill username, password, PIN, role");
            return;
        }

        try {
            staffDao.createStaff(username, password, role, pin);
            staffMessageLabel.setText("Created: " + username + " (" + role + ")");
            newUsernameField.clear();
            newPasswordField.clear();
            newPinField.clear();
            roleCombo.getSelectionModel().select(Role.BARTENDER);
            refreshStaffList();
        } catch (Exception e) {
            staffMessageLabel.setText("Could not create user (username taken? PIN must be 2 digits)");
            e.printStackTrace();
        }
    }

    private void refreshStaffList() {
        if (staffListView == null) return;

        staffListView.getItems().clear();
        try (Connection conn = Db.getConnection();
             var ps = conn.prepareStatement("SELECT username, role, active FROM staff ORDER BY username");
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                String u = rs.getString("username");
                String r = rs.getString("role");
                int active = rs.getInt("active");
                staffListView.getItems().add(u + " - " + r + (active == 1 ? "" : " (inactive)"));
            }
        } catch (Exception e) {
            staffListView.getItems().add("Error loading staff list");
            e.printStackTrace();
        }
    }

    // ----------------------------
    // POS: quick pages / back
    // ----------------------------
    @FXML private void onPage1() { quickPage = 1; currentCategoryFilter = null; loadQuickButtons(); }
    @FXML private void onPage2() { quickPage = 2; currentCategoryFilter = null; loadQuickButtons(); }
    @FXML private void onQuickBack() { currentCategoryFilter = null; loadQuickButtons(); }

    // ----------------------------
    // QUICK LAYOUT: pages
    // ----------------------------
    @FXML private void onLoadQuickPage1() { loadLayoutPage(1); }
    @FXML private void onLoadQuickPage2() { loadLayoutPage(2); }

    // ----------------------------
    // QUICK BUTTON GRID: POS
    // ----------------------------
    private void loadQuickButtons() {
        if (quickGrid == null) return;

        try {
            quickGrid.getChildren().clear();
            buildEmptyGridPlaceholders(quickGrid);

            if (currentCategoryFilter != null) {
                var products = productDao.listByCategory(currentCategoryFilter);

                int pos = 0;
                for (var p : products) {
                    if (pos >= TILE_COUNT) break;

                    Button b = makeTileButton(p.getName(), "#4B7BEC");
                    b.setOnAction(e -> quickAddProduct(p.getId()));

                    placeAt(quickGrid, b, pos);
                    pos++;
                }

                if (posMessageLabel != null) posMessageLabel.setText("Category view (Back to return)");
                return;
            }

            Map<Integer, com.ryan.bartill.model.QuickButton> map = new HashMap<>();
            for (var qb : quickDao.listByPage(quickPage)) {
                map.put(qb.position, qb);
            }

            for (int pos = 0; pos < TILE_COUNT; pos++) {
                var qb = map.get(pos);
                if (qb == null) continue;

                String label = resolveQuickLabel(qb);
                String color = resolveQuickColor(qb);

                Button b = makeTileButton(label, color);

                if ("PRODUCT".equals(qb.type) && qb.productId != null) {
                    int pid = qb.productId;
                    b.setOnAction(e -> quickAddProduct(pid));
                } else if ("CATEGORY".equals(qb.type) && qb.categoryId != null) {
                    int cid = qb.categoryId;
                    b.setOnAction(e -> {
                        currentCategoryFilter = cid;
                        loadQuickButtons();
                    });
                } else {
                    b.setDisable(true);
                }

                placeAt(quickGrid, b, pos);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (posMessageLabel != null) posMessageLabel.setText("Error loading quick buttons");
        }
    }

    private String resolveQuickLabel(com.ryan.bartill.model.QuickButton qb) throws Exception {
        if (qb.label != null && !qb.label.isBlank()) return qb.label;

        if ("PRODUCT".equals(qb.type) && qb.productId != null) {
            var p = productDao.findById(qb.productId);
            return (p != null) ? p.getName() : "(missing product)";
        }

        if ("CATEGORY".equals(qb.type) && qb.categoryId != null) {
            for (var c : categoryDao.listAll()) {
                if (c.getId() == qb.categoryId) return c.getName();
            }
            return "(missing category)";
        }

        return "(empty)";
    }

    private String resolveQuickColor(com.ryan.bartill.model.QuickButton qb) {
        if (qb.colorHex != null && !qb.colorHex.isBlank()) return qb.colorHex;

        if ("CATEGORY".equals(qb.type) && qb.categoryId != null) {
            try {
                for (var c : categoryDao.listAll()) {
                    if (c.getId() == qb.categoryId) {
                        if (c.getColorHex() != null && !c.getColorHex().isBlank()) return c.getColorHex();
                    }
                }
            } catch (Exception ignored) {}
        }
        return "#4B7BEC";
    }

    private void quickAddProduct(int productId) {
        try {
            if (posMessageLabel != null) posMessageLabel.setText("");

            Product p = productDao.findById(productId);
            if (p == null) {
                if (posMessageLabel != null) posMessageLabel.setText("Button product missing");
                return;
            }

            if (!p.isActive()) {
                if (posMessageLabel != null) posMessageLabel.setText("Product unavailable");
                return;
            }

            basket.add(new BasketLine(
                    p.getId(),
                    p.getName(),
                    1,
                    p.getPriceExCents(),
                    p.getVatRate()
            ));
            refreshBasketUI();

        } catch (Exception e) {
            e.printStackTrace();
            if (posMessageLabel != null) posMessageLabel.setText("Error adding product");
        }
    }

    // ----------------------------
    // QUICK LAYOUT GRID: editor
    // ----------------------------
    private void loadLayoutPage(int page) {
        layoutPage = page;
        selectedLayoutPos = null;

        if (layoutMsgLabel != null) layoutMsgLabel.setText("");
        if (layoutSelectedLabel != null) layoutSelectedLabel.setText("Click a tile");

        if (layoutGrid == null) return;

        try {
            layoutGrid.getChildren().clear();
            buildEmptyGridPlaceholders(layoutGrid);

            Map<Integer, com.ryan.bartill.model.QuickButton> map = new HashMap<>();
            for (var qb : quickDao.listByPage(page)) {
                map.put(qb.position, qb);
            }

            for (int pos = 0; pos < TILE_COUNT; pos++) {
                var qb = map.get(pos);

                String label = "(empty)";
                String color = "#555555";

                if (qb != null) {
                    label = safeResolveLabelForEditor(qb);
                    color = (qb.colorHex == null || qb.colorHex.isBlank()) ? "#555555" : qb.colorHex;
                }

                Button tile = makeTileButton(label, color);
                final int selectedPos = pos;
                tile.setOnAction(e -> selectLayoutTile(selectedPos, qb));

                placeAt(layoutGrid, tile, pos);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (layoutMsgLabel != null) layoutMsgLabel.setText("Error loading layout");
        }
    }

    private String safeResolveLabelForEditor(com.ryan.bartill.model.QuickButton qb) {
        try {
            if (qb.label != null && !qb.label.isBlank()) return qb.label;

            if ("PRODUCT".equals(qb.type) && qb.productId != null) {
                var p = productDao.findById(qb.productId);
                return (p != null) ? p.getName() : "(missing product)";
            }

            if ("CATEGORY".equals(qb.type) && qb.categoryId != null) {
                for (var c : categoryDao.listAll()) {
                    if (c.getId() == qb.categoryId) return c.getName();
                }
                return "(missing category)";
            }
        } catch (Exception ignored) {}
        return "(empty)";
    }

    private void selectLayoutTile(int pos, com.ryan.bartill.model.QuickButton qb) {
        selectedLayoutPos = pos;

        if (layoutMsgLabel != null) layoutMsgLabel.setText("");
        if (layoutSelectedLabel != null) {
            int col = pos % GRID_COLS;
            int row = pos / GRID_COLS;
            layoutSelectedLabel.setText("Selected: (" + col + "," + row + ")  pos=" + pos + "  page=" + layoutPage);
        }

        if (qb == null) {
            if (layoutTypeCombo != null) layoutTypeCombo.getSelectionModel().select("PRODUCT");
            if (layoutProductCombo != null) layoutProductCombo.getSelectionModel().clearSelection();
            if (layoutCategoryCombo != null) layoutCategoryCombo.getSelectionModel().clearSelection();
            if (layoutLabelField != null) layoutLabelField.clear();
            if (layoutColorField != null) layoutColorField.clear();
            return;
        }

        if (layoutTypeCombo != null) layoutTypeCombo.getSelectionModel().select(qb.type);

        if ("PRODUCT".equals(qb.type)) {
            if (layoutProductCombo != null) {
                layoutProductCombo.getSelectionModel().clearSelection();
                if (qb.productId != null) {
                    for (var p : layoutProductCombo.getItems()) {
                        if (p.getId() == qb.productId) {
                            layoutProductCombo.getSelectionModel().select(p);
                            break;
                        }
                    }
                }
            }
            if (layoutCategoryCombo != null) layoutCategoryCombo.getSelectionModel().clearSelection();
        } else {
            if (layoutCategoryCombo != null) {
                layoutCategoryCombo.getSelectionModel().clearSelection();
                if (qb.categoryId != null) {
                    for (var c : layoutCategoryCombo.getItems()) {
                        if (c.getId() == qb.categoryId) {
                            layoutCategoryCombo.getSelectionModel().select(c);
                            break;
                        }
                    }
                }
            }
            if (layoutProductCombo != null) layoutProductCombo.getSelectionModel().clearSelection();
        }

        if (layoutLabelField != null) layoutLabelField.setText(qb.label == null ? "" : qb.label);
        if (layoutColorField != null) layoutColorField.setText(qb.colorHex == null ? "" : qb.colorHex);
    }

    @FXML
    private void onSaveTileClicked() {
        if (selectedLayoutPos == null) {
            if (layoutMsgLabel != null) layoutMsgLabel.setText("Click a tile first");
            return;
        }

        String type = layoutTypeCombo == null ? "PRODUCT" : layoutTypeCombo.getValue();
        Integer productId = null;
        Integer categoryId = null;

        if ("PRODUCT".equals(type)) {
            Product p = (layoutProductCombo == null) ? null : layoutProductCombo.getValue();
            if (p == null) {
                if (layoutMsgLabel != null) layoutMsgLabel.setText("Pick a product");
                return;
            }
            productId = p.getId();
        } else {
            Category c = (layoutCategoryCombo == null) ? null : layoutCategoryCombo.getValue();
            if (c == null) {
                if (layoutMsgLabel != null) layoutMsgLabel.setText("Pick a category");
                return;
            }
            categoryId = c.getId();
        }

        String label = layoutLabelField == null ? "" : layoutLabelField.getText();
        String color = layoutColorField == null ? "" : layoutColorField.getText();

        try {
            quickDao.upsertButton(layoutPage, selectedLayoutPos, type, productId, categoryId, label, color);
            if (layoutMsgLabel != null) layoutMsgLabel.setText("Saved");
            loadLayoutPage(layoutPage);

            if (quickPage == layoutPage && currentCategoryFilter == null) loadQuickButtons();

        } catch (Exception e) {
            e.printStackTrace();
            if (layoutMsgLabel != null) layoutMsgLabel.setText("Save failed");
        }
    }

    @FXML
    private void onClearTileClicked() {
        if (selectedLayoutPos == null) {
            if (layoutMsgLabel != null) layoutMsgLabel.setText("Click a tile first");
            return;
        }

        try {
            quickDao.deleteButton(layoutPage, selectedLayoutPos);
            if (layoutMsgLabel != null) layoutMsgLabel.setText("Cleared");
            loadLayoutPage(layoutPage);

            if (quickPage == layoutPage && currentCategoryFilter == null) loadQuickButtons();

        } catch (Exception e) {
            e.printStackTrace();
            if (layoutMsgLabel != null) layoutMsgLabel.setText("Clear failed");
        }
    }

    // ----------------------------
    // POS: basket
    // ----------------------------
    @FXML
    private void onAddClicked() {
        if (posMessageLabel != null) posMessageLabel.setText("");

        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        String qtyText = qtyField.getText() == null ? "" : qtyField.getText().trim();

        if (query.isEmpty()) {
            if (posMessageLabel != null) posMessageLabel.setText("Enter a product name or barcode");
            return;
        }

        int qty;
        try {
            qty = qtyText.isEmpty() ? 1 : Integer.parseInt(qtyText);
            if (qty <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            if (posMessageLabel != null) posMessageLabel.setText("Qty must be a positive whole number");
            return;
        }

        try {
            Product p = productDao.findByNameOrBarcode(query);
            if (p == null) {
                if (posMessageLabel != null) posMessageLabel.setText("Product not found");
                return;
            }

            if (!p.isActive()) {
                if (posMessageLabel != null) posMessageLabel.setText("Product unavailable");
                return;
            }

            basket.add(new BasketLine(
                    p.getId(),
                    p.getName(),
                    qty,
                    p.getPriceExCents(),
                    p.getVatRate()
            ));
            refreshBasketUI();

            searchField.clear();
            qtyField.setText("1");
            searchField.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
            if (posMessageLabel != null) posMessageLabel.setText("Error adding product");
        }
    }

    @FXML
    private void onRemoveSelectedClicked() {
        if (basketListView == null) return;
        int idx = basketListView.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= basket.size()) return;

        basket.remove(idx);
        refreshBasketUI();
    }

    @FXML
    private void onClearBasketClicked() {
        basket.clear();
        refreshBasketUI();

        if (changeLabel != null) changeLabel.setText("Change: €0.00");
        if (cashField != null) cashField.clear();
        if (posMessageLabel != null) posMessageLabel.setText("");

        hidePaymentOverlay();
    }

    // ----------------------------
    // FAST PAYMENT FLOW
    // ----------------------------
    @FXML
    private void onSendClicked() {
        if (posMessageLabel != null) posMessageLabel.setText("");
        showPaymentOverlay();
    }

    @FXML
    private void onCancelPaymentClicked() {
        hidePaymentOverlay();
    }

    @FXML
    private void onCardPaymentClicked() {
        int totalInc = calcTotalIncCents();
        if (totalInc <= 0) {
            if (posMessageLabel != null) posMessageLabel.setText("Basket is empty");
            return;
        }

        try {
            var staff = Session.getCurrentUser();
            if (staff == null) {
                if (posMessageLabel != null) posMessageLabel.setText("Not logged in");
                return;
            }

            List<com.ryan.bartill.service.SaleService.SaleLine> lines = buildSaleLines();
            var receipt = saleService.completeCardSale(staff.getId(), lines);

            ReceiptController.lastReceipt = receipt;
            basket.clear();
            refreshBasketUI();
            hidePaymentOverlay();

            Stage stage = (Stage) tabPane.getScene().getWindow();
            SceneSwitcher.switchTo(stage, "/fxml/receipt.fxml", "Receipt");

        } catch (Exception e) {
            e.printStackTrace();
            if (posMessageLabel != null) posMessageLabel.setText("Could not complete card sale");
        }
    }

    @FXML
    private void onCashPaymentClicked() {
        showCashKeypad();
    }

    @FXML
    private void onCashKeyClicked(ActionEvent event) {
        Button b = (Button) event.getSource();
        String value = b.getText();

        if (!value.matches("\\d")) return;
        if (cashInput.length() >= 6) return;

        if (cashInput.equals("0")) {
            cashInput = value;
        } else {
            cashInput += value;
        }

        updateCashDisplay();
    }

    @FXML
    private void onCashClearClicked() {
        cashInput = "";
        updateCashDisplay();
    }

    @FXML
    private void onCashBackspaceClicked() {
        if (!cashInput.isEmpty()) {
            cashInput = cashInput.substring(0, cashInput.length() - 1);
        }
        updateCashDisplay();
    }

    @FXML
    private void onCashSendClicked() {
        int totalInc = calcTotalIncCents();
        if (totalInc <= 0) {
            if (posMessageLabel != null) posMessageLabel.setText("Basket is empty");
            return;
        }

        int cashCents;
        try {
            cashCents = cashInput.isEmpty() ? 0 : Integer.parseInt(cashInput);
        } catch (Exception e) {
            if (posMessageLabel != null) posMessageLabel.setText("Invalid cash amount");
            return;
        }

        if (cashCents < totalInc) {
            if (posMessageLabel != null) posMessageLabel.setText("Not enough cash");
            return;
        }

        int change = cashCents - totalInc;

        try {
            var staff = Session.getCurrentUser();
            if (staff == null) {
                if (posMessageLabel != null) posMessageLabel.setText("Not logged in");
                return;
            }

            List<com.ryan.bartill.service.SaleService.SaleLine> lines = buildSaleLines();
            var receipt = saleService.completeCashSale(staff.getId(), lines, cashCents, change);

            ReceiptController.lastReceipt = receipt;
            basket.clear();
            refreshBasketUI();
            hidePaymentOverlay();

            Stage stage = (Stage) tabPane.getScene().getWindow();
            SceneSwitcher.switchTo(stage, "/fxml/receipt.fxml", "Receipt");

        } catch (Exception e) {
            e.printStackTrace();
            if (posMessageLabel != null) posMessageLabel.setText("Could not complete cash sale");
        }
    }

    private void showPaymentOverlay() {
        int totalInc = calcTotalIncCents();
        if (totalInc <= 0) {
            if (posMessageLabel != null) posMessageLabel.setText("Basket is empty");
            return;
        }

        cashInput = "";

        if (paymentTotalLabel != null) paymentTotalLabel.setText("Total: " + centsToEuro(totalInc));
        if (cashEnteredLabel != null) cashEnteredLabel.setText("Cash: €0.00");
        if (bigChangeLabel != null) bigChangeLabel.setText("Change: €0.00");

        if (paymentOverlay != null) {
            paymentOverlay.setVisible(true);
            paymentOverlay.setManaged(true);
        }
        if (paymentChoiceBox != null) {
            paymentChoiceBox.setVisible(true);
            paymentChoiceBox.setManaged(true);
        }
        if (cashPaymentBox != null) {
            cashPaymentBox.setVisible(false);
            cashPaymentBox.setManaged(false);
        }
    }

    private void showCashKeypad() {
        cashInput = "";
        updateCashDisplay();

        if (paymentChoiceBox != null) {
            paymentChoiceBox.setVisible(false);
            paymentChoiceBox.setManaged(false);
        }
        if (cashPaymentBox != null) {
            cashPaymentBox.setVisible(true);
            cashPaymentBox.setManaged(true);
        }
    }

    private void hidePaymentOverlay() {
        if (paymentOverlay != null) {
            paymentOverlay.setVisible(false);
            paymentOverlay.setManaged(false);
        }
        if (paymentChoiceBox != null) {
            paymentChoiceBox.setVisible(false);
            paymentChoiceBox.setManaged(false);
        }
        if (cashPaymentBox != null) {
            cashPaymentBox.setVisible(false);
            cashPaymentBox.setManaged(false);
        }

        cashInput = "";
    }

    private void updateCashDisplay() {
        int cashCents = 0;

        if (!cashInput.isEmpty()) {
            try {
                cashCents = Integer.parseInt(cashInput);
            } catch (Exception ignored) {}
        }

        int totalInc = calcTotalIncCents();
        int change = Math.max(0, cashCents - totalInc);

        if (cashEnteredLabel != null) cashEnteredLabel.setText("Cash: " + centsToEuro(cashCents));
        if (bigChangeLabel != null) bigChangeLabel.setText("Change: " + centsToEuro(change));
    }

    private List<com.ryan.bartill.service.SaleService.SaleLine> buildSaleLines() {
        List<com.ryan.bartill.service.SaleService.SaleLine> lines = new ArrayList<>();

        for (BasketLine b : basket) {
            lines.add(new com.ryan.bartill.service.SaleService.SaleLine(
                    b.productId,
                    b.name,
                    b.qty,
                    b.unitPriceExCents,
                    b.vatRate
            ));
        }

        return lines;
    }

    private void refreshBasketUI() {
        if (basketListView != null) {
            basketListView.getItems().clear();
            for (BasketLine b : basket) {
                int lineEx = b.qty * b.unitPriceExCents;
                basketListView.getItems().add(b.qty + " x " + b.name + "  |  " + centsToEuro(lineEx) + " ex VAT");
            }
        }

        int ex = calcTotalExCents();
        int vat = calcTotalVatCents();
        int inc = ex + vat;

        if (totalExLabel != null) totalExLabel.setText(centsToEuro(ex));
        if (vatLabel != null) vatLabel.setText(centsToEuro(vat));
        if (totalIncLabel != null) totalIncLabel.setText(centsToEuro(inc));

        if (sendButton != null) {
            sendButton.setDisable(basket.isEmpty());
        }
    }

    private int calcTotalExCents() {
        int sum = 0;
        for (BasketLine b : basket) sum += b.qty * b.unitPriceExCents;
        return sum;
    }

    private int calcTotalVatCents() {
        int sum = 0;
        for (BasketLine b : basket) {
            int lineEx = b.qty * b.unitPriceExCents;
            sum += (lineEx * b.vatRate) / 100;
        }
        return sum;
    }

    private int calcTotalIncCents() {
        return calcTotalExCents() + calcTotalVatCents();
    }

    // ----------------------------
    // REPORTS
    // ----------------------------
    @FXML
    private void onRunEodClicked() {
        if (eodOutput == null) return;

        var d = eodDatePicker.getValue();
        if (d == null) {
            eodOutput.setText("Pick a date.");
            return;
        }
        String day = d.toString();

        String sqlTotals = """
            SELECT
              COUNT(*) AS sale_count,
              COALESCE(SUM(total_ex_cents), 0) AS ex_total,
              COALESCE(SUM(total_vat_cents), 0) AS vat_total,
              COALESCE(SUM(total_inc_cents), 0) AS inc_total
            FROM sales
            WHERE date(created_at) = date(?)
        """;

        String sqlTopItems = """
            SELECT
              name_snapshot,
              SUM(qty) AS qty_sold,
              COALESCE(SUM(line_inc_cents), 0) AS inc_total
            FROM sale_items si
            JOIN sales s ON s.id = si.sale_id
            WHERE date(s.created_at) = date(?)
            GROUP BY name_snapshot
            ORDER BY qty_sold DESC
            LIMIT 10
        """;

        try (var conn = Db.getConnection()) {
            StringBuilder out = new StringBuilder();
            out.append("End of Day: ").append(day).append("\n\n");

            try (var ps = conn.prepareStatement(sqlTotals)) {
                ps.setString(1, day);
                try (var rs = ps.executeQuery()) {
                    rs.next();
                    out.append("Sales count: ").append(rs.getInt("sale_count")).append("\n");
                    out.append("Total ex VAT: ").append(centsToEuro(rs.getInt("ex_total"))).append("\n");
                    out.append("Total VAT:    ").append(centsToEuro(rs.getInt("vat_total"))).append("\n");
                    out.append("Total inc VAT:").append(centsToEuro(rs.getInt("inc_total"))).append("\n\n");
                }
            }

            out.append("Top items:\n");
            try (var ps = conn.prepareStatement(sqlTopItems)) {
                ps.setString(1, day);
                try (var rs = ps.executeQuery()) {
                    int i = 1;
                    while (rs.next()) {
                        out.append(i++).append(". ")
                                .append(rs.getString("name_snapshot"))
                                .append(" — qty ").append(rs.getInt("qty_sold"))
                                .append(" — ").append(centsToEuro(rs.getInt("inc_total")))
                                .append("\n");
                    }
                    if (i == 1) out.append("(no items)\n");
                }
            }

            eodOutput.setText(out.toString());
        } catch (Exception e) {
            e.printStackTrace();
            eodOutput.setText("Error running report.");
        }
    }

    @FXML
    private void onRunVatClicked() {
        if (vatOutput == null) return;

        var from = vatFromDatePicker.getValue();
        var to = vatToDatePicker.getValue();
        if (from == null || to == null) {
            vatOutput.setText("Pick from/to dates.");
            return;
        }
        if (to.isBefore(from)) {
            vatOutput.setText("'To' must be same or after 'From'.");
            return;
        }

        String sql = """
            SELECT
              si.vat_rate AS vat_rate,
              COALESCE(SUM(si.line_ex_cents), 0)  AS ex_total,
              COALESCE(SUM(si.line_vat_cents), 0) AS vat_total,
              COALESCE(SUM(si.line_inc_cents), 0) AS inc_total
            FROM sale_items si
            JOIN sales s ON s.id = si.sale_id
            WHERE date(s.created_at) BETWEEN date(?) AND date(?)
            GROUP BY si.vat_rate
            ORDER BY si.vat_rate
        """;

        try (var conn = Db.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, from.toString());
            ps.setString(2, to.toString());

            StringBuilder out = new StringBuilder();
            out.append("VAT Report: ").append(from).append(" to ").append(to).append("\n\n");

            int grandEx = 0, grandVat = 0, grandInc = 0;

            try (var rs = ps.executeQuery()) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    int rate = rs.getInt("vat_rate");
                    int ex = rs.getInt("ex_total");
                    int vat = rs.getInt("vat_total");
                    int inc = rs.getInt("inc_total");
                    grandEx += ex;
                    grandVat += vat;
                    grandInc += inc;

                    out.append("Rate ").append(rate).append("%")
                            .append(" | ex ").append(centsToEuro(ex))
                            .append(" | VAT ").append(centsToEuro(vat))
                            .append(" | inc ").append(centsToEuro(inc))
                            .append("\n");
                }
                if (!any) out.append("(no sales in range)\n");
            }

            out.append("\nGrand totals:\n");
            out.append("ex  ").append(centsToEuro(grandEx)).append("\n");
            out.append("VAT ").append(centsToEuro(grandVat)).append("\n");
            out.append("inc ").append(centsToEuro(grandInc)).append("\n");

            vatOutput.setText(out.toString());
        } catch (Exception e) {
            e.printStackTrace();
            vatOutput.setText("Error running VAT report.");
        }
    }

    // ----------------------------
    // STOCK TAB
    // ----------------------------
    private void setupStockTable() {
        if (colName != null) {
            colName.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));
        }

        if (colBarcode != null) {
            colBarcode.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().getBarcode() == null ? "" : d.getValue().getBarcode()
                    ));
        }

        if (colPrice != null) {
            colPrice.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            centsToEuro(d.getValue().getPriceExCents())
                    ));
        }

        if (colCostPrice != null) {
            colCostPrice.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            centsToEuro(d.getValue().getCostPriceCents())
                    ));
        }

        if (colVat != null) {
            colVat.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getVatRate()));
        }

        if (colStock != null) {
            colStock.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getStockQty()));
        }

        if (colActive != null) {
            colActive.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().isActive() ? "Yes" : "No"
                    ));
        }

        if (colCategory != null) {
            colCategory.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().getCategoryName() == null ? "" : d.getValue().getCategoryName()
                    ));
        }

        if (productTable != null) {
            productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV == null) {
                    if (movementTable != null) movementTable.getItems().clear();
                    if (stockSelectedLabel != null) stockSelectedLabel.setText("Select a product");
                } else {
                    refreshMovementTable(newV.getId());
                    if (stockSelectedLabel != null) {
                        stockSelectedLabel.setText("Selected: " + newV.getName());
                    }
                }
                if (stockMessageLabel != null) stockMessageLabel.setText("");
            });
        }
    }
    private void refreshMovementTable(int productId) {
        if (movementTable == null) return;
        try {
            var moves = productDao.listMovementsForProduct(productId, 50);
            movementTable.getItems().setAll(moves);
        } catch (Exception e) {
            e.printStackTrace();
            movementTable.getItems().clear();
            movementTable.getItems().add(new com.ryan.bartill.model.StockMovement("Error", "", "", 0));
        }
    }

    private void refreshStockTable() {
        try {
            var list = productDao.listAllActive();
            productTable.getItems().setAll(list);
            if (stockMessageLabel != null) stockMessageLabel.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            if (stockMessageLabel != null) stockMessageLabel.setText("Error loading stock");
        }
    }

    @FXML
    private void onRefreshStockClicked() {
        refreshStockTable();
    }

    @FXML
    private void onRestockClicked() {
        if (stockMessageLabel != null) stockMessageLabel.setText("");

        var selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (stockMessageLabel != null) stockMessageLabel.setText("Select a product first");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(adjustQtyField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            if (stockMessageLabel != null) stockMessageLabel.setText("Enter a positive whole number qty");
            return;
        }

        try {
            productDao.addStock(selected.getId(), qty, Session.getCurrentUser().getId());
            adjustQtyField.clear();
            refreshStockTable();
        } catch (Exception e) {
            e.printStackTrace();
            if (stockMessageLabel != null) stockMessageLabel.setText("Could not restock");
        }
    }

    @FXML
    private void onWasteClicked() {
        if (stockMessageLabel != null) stockMessageLabel.setText("");

        var selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (stockMessageLabel != null) stockMessageLabel.setText("Select a product first");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(adjustQtyField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            if (stockMessageLabel != null) stockMessageLabel.setText("Enter a positive whole number qty");
            return;
        }

        try {
            productDao.removeStock(selected.getId(), qty, Session.getCurrentUser().getId());
            adjustQtyField.clear();
            refreshStockTable();
        } catch (Exception e) {
            e.printStackTrace();
            if (stockMessageLabel != null) stockMessageLabel.setText("Could not remove stock (not enough?)");
        }
    }

    @FXML
    private void onCreateProductClicked() {
        if (stockMessageLabel != null) stockMessageLabel.setText("");

        String name = newProdNameField.getText() == null ? "" : newProdNameField.getText().trim();
        String barcode = newProdBarcodeField.getText() == null ? "" : newProdBarcodeField.getText().trim();
        String priceText = newProdPriceField.getText() == null ? "" : newProdPriceField.getText().trim();
        String vatText = newProdVatField.getText() == null ? "" : newProdVatField.getText().trim();
        String costText = newProdCostField.getText() == null ? "" : newProdCostField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty() || costText.isEmpty() || vatText.isEmpty()) {
            if (stockMessageLabel != null) stockMessageLabel.setText("Fill name, price, cost, VAT");
            return;
        }

        int priceCents, vatRate, costPriceCents;
        try {
            priceCents = parseMoneyToCents(priceText);
            costPriceCents = parseMoneyToCents(costText);
            vatRate = Integer.parseInt(vatText);


            if (priceCents < 0 || costPriceCents < 0 || vatRate < 0 || vatRate > 100) {
                throw new NumberFormatException();
            }
        } catch (Exception e) {
            if (stockMessageLabel != null) {
                stockMessageLabel.setText("Bad values (price/cost like 5.50, VAT 0-100");
            }
            return;
        }

        try {
            productDao.createProduct(name, barcode, priceCents, costPriceCents, vatRate);

            newProdNameField.clear();
            newProdBarcodeField.clear();
            newProdPriceField.clear();
            newProdCostField.clear();
            newProdVatField.clear();


            refreshStockTable();
            refreshLayoutDropdowns();
            if (stockMessageLabel != null) stockMessageLabel.setText("Product created");
        } catch (Exception e) {
            e.printStackTrace();
            if (stockMessageLabel != null) stockMessageLabel.setText("Could not create product (barcode taken?)");
        }
    }

    // ----------------------------
    // CATEGORY: assign + create
    // ----------------------------
    @FXML
    private void onSetCategoryClicked() {
        if (stockMessageLabel != null) stockMessageLabel.setText("");

        var product = (productTable == null) ? null : productTable.getSelectionModel().getSelectedItem();
        var category = (categoryCombo == null) ? null : categoryCombo.getValue();

        if (product == null || category == null) {
            if (stockMessageLabel != null) stockMessageLabel.setText("Select product and category");
            return;
        }

        try {
            productDao.setCategory(product.getId(), category.getId());
            refreshStockTable();
            if (stockMessageLabel != null) stockMessageLabel.setText("Category set");
        } catch (Exception e) {
            e.printStackTrace();
            if (stockMessageLabel != null) stockMessageLabel.setText("Error setting category");
        }
    }

    @FXML
    private void onCreateCategoryClicked() {
        if (stockMessageLabel != null) stockMessageLabel.setText("");

        String name = newCategoryNameField.getText() == null ? "" : newCategoryNameField.getText().trim();
        String color = newCategoryColorField.getText() == null ? "" : newCategoryColorField.getText().trim();

        if (name.isEmpty()) {
            if (stockMessageLabel != null) stockMessageLabel.setText("Enter a category name");
            return;
        }

        try {
            categoryDao.create(name, color);

            newCategoryNameField.clear();
            newCategoryColorField.clear();

            refreshCategoryCombos();
            refreshLayoutDropdowns();

            if (stockMessageLabel != null) stockMessageLabel.setText("Category created: " + name);
        } catch (Exception e) {
            e.printStackTrace();
            if (stockMessageLabel != null) stockMessageLabel.setText("Could not create category (name already exists?)");
        }
    }

    @FXML
    private void onRefreshCategoriesClicked() {
        refreshCategoryCombos();
        refreshLayoutDropdowns();
        if (stockMessageLabel != null) stockMessageLabel.setText("Categories refreshed");
    }

    // ----------------------------
    // IMPORT CSV
    // ----------------------------
    @FXML
    private void onImportProductsClicked() {
        if (stockMessageLabel != null) stockMessageLabel.setText("");

        var chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Select products CSV");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        var file = chooser.showOpenDialog(tabPane.getScene().getWindow());
        if (file == null) return;

        try {
            int count = productDao.importFromCsv(file.toPath());
            refreshStockTable();
            refreshLayoutDropdowns();
            if (stockMessageLabel != null) stockMessageLabel.setText("Imported " + count + " products");
        } catch (Exception e) {
            e.printStackTrace();
            if (stockMessageLabel != null) stockMessageLabel.setText("Import failed (check CSV format)");
        }
    }

    // ----------------------------
    // GRID HELPERS
    // ----------------------------
    private void buildEmptyGridPlaceholders(GridPane grid) {
        for (int pos = 0; pos < TILE_COUNT; pos++) {
            Button ghost = new Button("");
            ghost.setDisable(true);
            ghost.setOpacity(0);
            ghost.setPrefSize(BTN_W, BTN_H);
            placeAt(grid, ghost, pos);
        }
    }

    private void placeAt(GridPane grid, Button b, int pos) {
        int col = pos % GRID_COLS;
        int row = pos / GRID_COLS;
        GridPane.setColumnIndex(b, col);
        GridPane.setRowIndex(b, row);
        grid.getChildren().add(b);
    }

    private Button makeTileButton(String text, String colorHex) {
        Button b = new Button(text == null ? "" : text);
        b.setPrefSize(BTN_W, BTN_H);
        b.setMinSize(BTN_W, BTN_H);
        b.setMaxSize(BTN_W, BTN_H);

        String c = (colorHex == null || colorHex.isBlank()) ? "#4B7BEC" : colorHex.trim();
        b.setStyle(
                "-fx-background-color: " + c + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 10;" +
                        "-fx-alignment: center;" +
                        "-fx-wrap-text: true;"
        );

        b.setTextOverrun(OverrunStyle.CLIP);
        b.setWrapText(true);

        return b;
    }

    // ----------------------------
    // MONEY helpers
    // ----------------------------
    private static String centsToEuro(int cents) {
        int abs = Math.abs(cents);
        int euros = abs / 100;
        int rem = abs % 100;
        String s = "€" + euros + "." + (rem < 10 ? "0" : "") + rem;
        return cents < 0 ? "-" + s : s;
    }

    private static int parseMoneyToCents(String text) {
        String t = text.trim();
        if (t.isEmpty()) throw new IllegalArgumentException();

        if (!t.contains(".")) {
            int euros = Integer.parseInt(t);
            return euros * 100;
        }

        String[] parts = t.split("\\.");
        if (parts.length != 2) throw new IllegalArgumentException();

        int euros = Integer.parseInt(parts[0]);
        String centsPart = parts[1];

        if (centsPart.length() == 1) centsPart = centsPart + "0";
        if (centsPart.length() != 2) throw new IllegalArgumentException();

        int cents = Integer.parseInt(centsPart);
        return euros * 100 + cents;
    }

    private void setupSalesHistoryTable() {
        if (colSaleId != null) {
            colSaleId.setCellValueFactory(d ->
                    new SimpleLongProperty(d.getValue().getSaleId()));
        }

        if (colSaleTime != null) {
            colSaleTime.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getCreatedAt()));
        }

        if (colSaleStaff != null) {
            colSaleStaff.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getStaffUsername()));
        }

        if (colSalePayment != null) {
            colSalePayment.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getPaymentMethod()));
        }

        if (colSaleTotal != null) {
            colSaleTotal.setCellValueFactory(d ->
                    new SimpleStringProperty(centsToEuro(d.getValue().getTotalIncCents())));
        }

        if (salesHistoryTable != null) {
            salesHistoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV == null) {
                    if (salesHistorySelectedLabel != null) {
                        salesHistorySelectedLabel.setText("Select a sale");
                    }
                    if (salesHistoryItemsList != null) {
                        salesHistoryItemsList.getItems().clear();
                    }
                } else {
                    loadSaleHistoryItems(newV.getSaleId());
                }
            });
        }
    }

    private void refreshSalesHistoryTable() {
        if (salesHistoryTable == null) return;

        String sql = """
        SELECT
            s.id,
            s.created_at,
            st.username,
            s.payment_method,
            s.total_inc_cents
        FROM sales s
        JOIN staff st ON st.id = s.staff_id
        ORDER BY s.id DESC
        LIMIT 300
    """;

        try (Connection conn = Db.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            List<SaleHistoryRow> rows = new ArrayList<>();

            while (rs.next()) {
                rows.add(new SaleHistoryRow(
                        rs.getLong("id"),
                        rs.getString("created_at"),
                        rs.getString("username"),
                        rs.getString("payment_method"),
                        rs.getInt("total_inc_cents")
                ));
            }

            salesHistoryTable.getItems().setAll(rows);

            if (salesHistorySelectedLabel != null) {
                salesHistorySelectedLabel.setText("Select a sale");
            }

            if (salesHistoryItemsList != null) {
                salesHistoryItemsList.getItems().clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
            salesHistoryTable.getItems().clear();

            if (salesHistorySelectedLabel != null) {
                salesHistorySelectedLabel.setText("Could not load sales");
            }

            if (salesHistoryItemsList != null) {
                salesHistoryItemsList.getItems().clear();
            }
        }
    }

    private void loadSaleHistoryItems(long saleId) {
        if (salesHistoryItemsList == null) return;

        String sql = """
        SELECT
            name_snapshot,
            qty,
            line_inc_cents
        FROM sale_items
        WHERE sale_id = ?
        ORDER BY id
    """;

        try (Connection conn = Db.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setLong(1, saleId);

            try (var rs = ps.executeQuery()) {
                salesHistoryItemsList.getItems().clear();

                while (rs.next()) {
                    String name = rs.getString("name_snapshot");
                    int qty = rs.getInt("qty");
                    int lineInc = rs.getInt("line_inc_cents");

                    salesHistoryItemsList.getItems().add(
                            qty + " x " + name + "  =  " + centsToEuro(lineInc)
                    );
                }
            }

            if (salesHistorySelectedLabel != null) {
                salesHistorySelectedLabel.setText("Sale #" + saleId);
            }

        } catch (Exception e) {
            e.printStackTrace();

            salesHistoryItemsList.getItems().clear();
            salesHistoryItemsList.getItems().add("Could not load items");

            if (salesHistorySelectedLabel != null) {
                salesHistorySelectedLabel.setText("Sale #" + saleId);
            }
        }
    }



    @FXML
    private void onRefreshSalesHistoryClicked() {
        refreshSalesHistoryTable();
    }

    private void buildTabsGrid() {
        tabsGrid.getChildren().clear();

        int cols = 5;
        int totalTabs = 50;

        for (int i = 1; i <= totalTabs; i++) {
            Button b = new Button("Tab " + i);
            b.setPrefSize(120, 80);
            b.setMinSize(120, 80);
            b.setMaxSize(120, 80);

            b.setStyle(
                    "-fx-background-color: #444444;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 18;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 10;"
            );

            int col = (i - 1) % cols;
            int row = (i - 1) / cols;

            tabsGrid.add(b, col, row);
        }
    }
}