package com.fashionstore.controllers;

import com.fashionstore.dao.CartDAO;
import com.fashionstore.dao.ProductDAO;
import com.fashionstore.dao.ReviewDAO;
import com.fashionstore.dao.UserDAO;
import com.fashionstore.dao.VoucherDAO;
import com.fashionstore.dao.OrderDAO;
import com.fashionstore.dao.CategoryDAO;
import com.fashionstore.models.Product;
import com.fashionstore.models.Review;
import com.fashionstore.models.User;
import com.fashionstore.models.Voucher;
import com.fashionstore.models.Order;
import com.fashionstore.models.MonthlyRevenue;
import com.fashionstore.models.Category;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextArea;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.fashionstore.database.DatabaseConnection;

public class AdminController {
    private final ProductDAO productDAO = new ProductDAO();
    private final UserDAO userDAO = new UserDAO();
    private final VoucherDAO voucherDAO = new VoucherDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final CartDAO cartDAO = new CartDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    
    public void show(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("admin-root");
        
        // Top header với logo bên trái và nút Đăng xuất sát bên phải
        HBox topHeader = new HBox(15);
        topHeader.setAlignment(Pos.CENTER_LEFT);
        topHeader.setPadding(new Insets(15, 20, 15, 20));
        topHeader.getStyleClass().add("admin-top-header");
        
        Label logo = new Label("TECHSHOP ADMIN");
        logo.getStyleClass().add("admin-logo");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = new Button("Đăng xuất");
        logoutBtn.getStyleClass().addAll("admin-btn", "admin-btn-logout");
        logoutBtn.setOnAction(e -> {
            // Đăng xuất admin, quay về màn hình đăng nhập
            LoginController loginController = new LoginController();
            loginController.show(stage);
        });
        
        topHeader.getChildren().addAll(logo, spacer, logoutBtn);
        
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("admin-tabpane");
        tabPane.getTabs().addAll(
            createOverviewTab(),
            createProductsTab(),
            createOrdersTab(),
            createUsersTab(),
            createReviewsTab(),
            createVouchersTab()
        );
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        root.setTop(topHeader);
        root.setCenter(tabPane);
        
        Scene scene = new Scene(root, 1200, 800);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.setTitle("FashionStore - Admin");
        stage.show();
    }
    
    private Tab createOverviewTab() {
        List<Product> products = productDAO.getAllProducts();
        List<User> users = userDAO.getAllUsers();
        List<Order> orders = orderDAO.getAllOrders();
        BigDecimal totalRevenue = orderDAO.getTotalRevenue();
        
        Label overviewTitle = new Label("Tổng quan hệ thống");
        overviewTitle.getStyleClass().add("admin-section-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 20, 30, 20));
        
        grid.add(createStatCard("DOANH THU", formatCurrency(totalRevenue), "stat-card-blue"), 0, 0);
        grid.add(createStatCard("ĐƠN HÀNG", String.valueOf(orders.size()), "stat-card-pink"), 1, 0);
        grid.add(createStatCard("SẢN PHẨM", String.valueOf(products.size()), "stat-card-lightblue"), 2, 0);
        grid.add(createStatCard("KHÁCH HÀNG", String.valueOf(users.size()), "stat-card-green"), 3, 0);
        
        HBox chartHeader = new HBox(15);
        chartHeader.setAlignment(Pos.CENTER_LEFT);
        Label chartTitle = new Label("Biểu đồ doanh thu theo thời gian");
        chartTitle.getStyleClass().add("admin-section-title");
        
        Button reloadChartBtn = new Button("🔄 Tải lại");
        reloadChartBtn.getStyleClass().addAll("admin-btn", "admin-btn-reload");
        HBox.setHgrow(chartTitle, Priority.ALWAYS);
        chartHeader.getChildren().addAll(chartTitle, reloadChartBtn);
        
        // Tạo biểu đồ với function để có thể reload
        javafx.util.Callback<Void, BarChart<String, Number>> createChart = (v) -> {
            // Reload dữ liệu mới nhất
            List<MonthlyRevenue> latestMonthly = orderDAO.getMonthlyRevenue();
            
            // Set max cho trục Y dựa trên doanh thu lớn nhất theo tháng
            BigDecimal maxMonthlyRevenue = BigDecimal.ZERO;
            for (MonthlyRevenue row : latestMonthly) {
                if (row.getTotal().compareTo(maxMonthlyRevenue) > 0) {
                    maxMonthlyRevenue = row.getTotal();
                }
            }
            
            // Set max value cho trục Y: lấy max tháng * 1.2 để có khoảng trống
            double maxYValue = maxMonthlyRevenue.doubleValue() * 1.2;
            // Làm tròn lên đến hàng trăm nghìn
            maxYValue = Math.ceil(maxYValue / 100000) * 100000;
            // Đảm bảo tối thiểu 1 triệu
            if (maxYValue < 1000000) {
                maxYValue = 1000000;
            }
            
            // Biểu đồ doanh thu cột
            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Tháng");
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Doanh thu");
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(maxYValue);
            yAxis.setTickUnit(maxYValue / 10); // Chia thành 10 phần
            yAxis.setAutoRanging(false); // Tắt auto range để dùng giá trị cố định
            BarChart<String, Number> revenueChart = new BarChart<>(xAxis, yAxis);
            revenueChart.setLegendVisible(false);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (MonthlyRevenue row : latestMonthly) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(row.getMonth(), row.getTotal());
                series.getData().add(data);
            }
            revenueChart.getData().add(series);
            revenueChart.setCategoryGap(10);
            revenueChart.setBarGap(3);
            revenueChart.getStyleClass().add("admin-revenue-chart");
            revenueChart.setPrefHeight(400); // Tăng chiều cao biểu đồ
            
            return revenueChart;
        };
        
        // Tạo biểu đồ ban đầu
        BarChart<String, Number> revenueChart = createChart.call(null);
        
        // Container cho biểu đồ để có thể thay thế khi reload
        VBox chartContainer = new VBox();
        chartContainer.getChildren().add(revenueChart);
        
        // Reload button action
        reloadChartBtn.setOnAction(e -> {
            // Reload stats
            List<Order> latestOrders = orderDAO.getAllOrders();
            List<Product> latestProducts = productDAO.getAllProducts();
            List<User> latestUsers = userDAO.getAllUsers();
            BigDecimal latestRevenue = orderDAO.getTotalRevenue();
            
            // Update stat cards
            grid.getChildren().clear();
            grid.add(createStatCard("DOANH THU", formatCurrency(latestRevenue), "stat-card-blue"), 0, 0);
            grid.add(createStatCard("ĐƠN HÀNG", String.valueOf(latestOrders.size()), "stat-card-pink"), 1, 0);
            grid.add(createStatCard("SẢN PHẨM", String.valueOf(latestProducts.size()), "stat-card-lightblue"), 2, 0);
            grid.add(createStatCard("KHÁCH HÀNG", String.valueOf(latestUsers.size()), "stat-card-green"), 3, 0);
            
            // Reload chart
            chartContainer.getChildren().clear();
            BarChart<String, Number> newChart = createChart.call(null);
            chartContainer.getChildren().add(newChart);
        });
        
        VBox wrapper = new VBox(20);
        wrapper.setPadding(new Insets(20));
        wrapper.getChildren().addAll(overviewTitle, grid, chartHeader, chartContainer);
        
        Tab tab = new Tab("Dashboard", wrapper);
        tab.setClosable(false);
        return tab;
    }
    
    private VBox createStatCard(String title, String value, String colorClass) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(25));
        card.getStyleClass().addAll("admin-stat-card", colorClass);
        
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("admin-stat-title");
        Label valueLbl = new Label(value);
        valueLbl.getStyleClass().add("admin-stat-value");
        
        card.getChildren().addAll(titleLbl, valueLbl);
        return card;
    }
    
    private Tab createProductsTab() {
        // Tạo table trước để có thể truyền vào form
        TableView<Product> table = createProductTable();
        
        // Form thông tin sản phẩm - truyền table vào để có thể refresh
        VBox formPanel = createProductFormPanel(table);
        
        // Bảng danh sách sản phẩm
        VBox tablePanel = createProductTablePanel(table);
        
        VBox mainContent = new VBox(12);
        mainContent.setPadding(new Insets(12));
        mainContent.getChildren().addAll(formPanel, tablePanel);
        
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("admin-scroll-pane");
        
        Tab tab = new Tab("Quản lý Sản phẩm", scrollPane);
        tab.setClosable(false);
        return tab;
    }
    
    private TableView<Product> createProductTable() {
        TableView<Product> table = new TableView<>();
        table.getStyleClass().add("admin-data-table");
        table.setFixedCellSize(32); // tăng chiều cao dòng hiển thị
        
        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Product, String> nameCol = new TableColumn<>("Tên Sản Phẩm");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nameVn"));
        TableColumn<Product, String> priceCol = new TableColumn<>("Giá (VND)");
        priceCol.setCellValueFactory(cell -> new SimpleStringProperty(formatCurrency(cell.getValue().getPrice())));
        TableColumn<Product, String> originalPriceCol = new TableColumn<>("Giá gốc");
        originalPriceCol.setCellValueFactory(cell -> {
            BigDecimal op = cell.getValue().getOriginalPrice();
            return new SimpleStringProperty(op != null ? formatCurrency(op) : "");
        });
        TableColumn<Product, String> discountCol = new TableColumn<>("Giảm %");
        discountCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getDiscountPercent())));
        TableColumn<Product, String> brandCol = new TableColumn<>("Thương hiệu");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        TableColumn<Product, String> genderCol = new TableColumn<>("Giới tính");
        genderCol.setCellValueFactory(cell -> {
            String gender = cell.getValue().getGender();
            return new SimpleStringProperty(gender != null ? gender : "");
        });
        TableColumn<Product, String> productTypeCol = new TableColumn<>("Loại sản phẩm");
        productTypeCol.setCellValueFactory(cell -> {
            String productType = cell.getValue().getProductType();
            return new SimpleStringProperty(productType != null ? productType : "");
        });
        TableColumn<Product, String> stockCol = new TableColumn<>("Kho");
        stockCol.setCellValueFactory(cell -> new SimpleStringProperty("100")); // Placeholder
        TableColumn<Product, String> imageCol = new TableColumn<>("Ảnh");
        imageCol.setCellValueFactory(cell -> {
            String path = cell.getValue().getImagePath();
            return new SimpleStringProperty(path != null ? path : "");
        });
        TableColumn<Product, String> badgeCol = new TableColumn<>("Badge");
        badgeCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getBadge() != null ? cell.getValue().getBadge() : ""
        ));
        TableColumn<Product, String> sizesCol = new TableColumn<>("Size");
        sizesCol.setCellValueFactory(cell -> {
            List<String> sizes = cell.getValue().getSizes();
            return new SimpleStringProperty((sizes != null && !sizes.isEmpty()) ? String.join(", ", sizes) : "");
        });
        TableColumn<Product, String> descCol = new TableColumn<>("Mô tả");
        descCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getDescriptionVn() != null ? cell.getValue().getDescriptionVn() : ""
        ));
        
        table.getColumns().addAll(
            idCol, nameCol, priceCol, originalPriceCol, discountCol, stockCol,
            brandCol, genderCol, productTypeCol, badgeCol, sizesCol, imageCol, descCol
        );
        refreshProducts(table);
        
        return table;
    }
    
    private VBox createProductFormPanel(TableView<Product> table) {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(8));
        panel.getStyleClass().add("admin-form-panel");
        panel.setMaxHeight(280);
        
        Label title = new Label("✏️ Thông tin sản phẩm");
        title.getStyleClass().add("admin-form-title");
        
        HBox formRow = new HBox(10);
        formRow.setPadding(new Insets(3));
        
        // Left column fields
        VBox leftCol = new VBox(5);
        
        VBox nameBox = new VBox(2);
        Label nameLabel = new Label("Tên sản phẩm:");
        TextField nameField = new TextField();
        nameField.setPromptText("Nhập tên sản phẩm");
        nameField.setPrefHeight(35);
        nameField.setPrefWidth(180);
        nameBox.getChildren().addAll(nameLabel, nameField);
        
        VBox priceBox = new VBox(2);
        Label priceLabel = new Label("Giá tiền (VND):");
        TextField priceField = new TextField();
        priceField.setPromptText("Nhập giá tiền");
        priceField.setPrefHeight(35);
        priceField.setPrefWidth(180);
        priceBox.getChildren().addAll(priceLabel, priceField);
        
        VBox originalPriceBox = new VBox(2);
        Label originalPriceLabel = new Label("Giá gốc (VND):");
        TextField originalPriceField = new TextField();
        originalPriceField.setPromptText("Nhập giá gốc");
        originalPriceField.setPrefHeight(35);
        originalPriceField.setPrefWidth(180);
        originalPriceBox.getChildren().addAll(originalPriceLabel, originalPriceField);
        
        leftCol.getChildren().addAll(nameBox, priceBox, originalPriceBox);
        
        // Middle column fields
        VBox middleCol = new VBox(5);
        
        VBox brandBox = new VBox(2);
        Label brandLabel = new Label("Thương hiệu:");
        TextField brandField = new TextField();
        brandField.setPromptText("Nhập thương hiệu");
        brandField.setPrefHeight(35);
        brandField.setPrefWidth(180);
        brandBox.getChildren().addAll(brandLabel, brandField);
        
        VBox discountBox = new VBox(2);
        Label discountLabel = new Label("Giảm giá (%):");
        TextField discountField = new TextField("0");
        discountField.setPromptText("Nhập % giảm");
        discountField.setPrefHeight(35);
        discountField.setPrefWidth(180);
        discountBox.getChildren().addAll(discountLabel, discountField);
        
        VBox badgeBox = new VBox(2);
        Label badgeLabel = new Label("Badge:");
        TextField badgeField = new TextField();
        badgeField.setPromptText("Nhập badge");
        badgeField.setPrefHeight(35);
        badgeField.setPrefWidth(180);
        badgeBox.getChildren().addAll(badgeLabel, badgeField);
        
        middleCol.getChildren().addAll(brandBox, discountBox, badgeBox);
        
        // Third column
        VBox thirdCol = new VBox(5);
        
        // Ẩn trường Danh mục vì đã có Giới tính và Loại sản phẩm
        // Vẫn giữ categoryCombo để set category_id mặc định (có thể set tự động dựa trên gender và productType)
        ComboBox<Category> categoryCombo = new ComboBox<>();
        List<Category> categories = categoryDAO.getAllCategories();
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
        categoryCombo.setCellFactory(param -> new javafx.scene.control.ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNameVn());
                }
            }
        });
        if (!categories.isEmpty()) {
            categoryCombo.getSelectionModel().selectFirst();
        }
        
        VBox stockBox = new VBox(2);
        Label stockLabel = new Label("Số lượng kho:");
        TextField stockField = new TextField();
        stockField.setPromptText("Nhập số lượng");
        stockField.setPrefHeight(35);
        stockField.setPrefWidth(180);
        stockBox.getChildren().addAll(stockLabel, stockField);
        
        VBox genderBox = new VBox(2);
        Label genderLabel = new Label("Giới tính:");
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Nam", "Nữ", "Unisex", "Trẻ em");
        genderCombo.setValue("Unisex");
        genderCombo.setPrefHeight(35);
        genderCombo.setPrefWidth(180);
        genderBox.getChildren().addAll(genderLabel, genderCombo);
        
        VBox productTypeBox = new VBox(2);
        Label productTypeLabel = new Label("Loại sản phẩm:");
        ComboBox<String> productTypeCombo = new ComboBox<>();
        productTypeCombo.getItems().addAll("Áo", "Quần", "Giày", "Phụ kiện");
        productTypeCombo.setPrefHeight(35);
        productTypeCombo.setPrefWidth(180);
        productTypeBox.getChildren().addAll(productTypeLabel, productTypeCombo);
        
        // Tự động set category_id dựa trên gender và productType khi thay đổi
        javafx.util.Callback<Void, Void> updateCategory = (v) -> {
            String gender = genderCombo.getValue();
            String productType = productTypeCombo.getValue();
            
            // Logic tự động chọn category
            if (gender != null || productType != null) {
                // Tìm category phù hợp
                Category matchedCategory = null;
                for (Category cat : categories) {
                    String catName = cat.getNameVn().toLowerCase();
                    if (gender != null && gender.equals("Nam") && catName.contains("nam")) {
                        matchedCategory = cat;
                        break;
                    } else if (gender != null && gender.equals("Nữ") && catName.contains("nữ")) {
                        matchedCategory = cat;
                        break;
                    } else if (gender != null && gender.equals("Trẻ em") && catName.contains("trẻ")) {
                        matchedCategory = cat;
                        break;
                    } else if (productType != null && productType.equals("Giày") && catName.contains("giày")) {
                        matchedCategory = cat;
                        break;
                    }
                }
                if (matchedCategory != null) {
                    categoryCombo.getSelectionModel().select(matchedCategory);
                }
            }
            return null;
        };
        
        genderCombo.setOnAction(e -> updateCategory.call(null));
        productTypeCombo.setOnAction(e -> updateCategory.call(null));
        
        thirdCol.getChildren().addAll(stockBox, genderBox, productTypeBox);
        
        // Fourth column - Size selection
        VBox fourthCol = new VBox(5);
        
        Label sizeLabel = new Label("Kích cỡ:");
        
        // Size cho quần áo
        List<String> clothingSizes = new ArrayList<>();
        clothingSizes.add("XS");
        clothingSizes.add("S");
        clothingSizes.add("M");
        clothingSizes.add("L");
        clothingSizes.add("XL");
        clothingSizes.add("2XL");
        clothingSizes.add("3XL");
        
        // Size cho giày
        List<String> shoeSizes = new ArrayList<>();
        for (int i = 35; i <= 45; i++) {
            shoeSizes.add(String.valueOf(i));
        }
        
        List<CheckBox> sizeCheckBoxes = new ArrayList<>();
        VBox sizeOptions = new VBox(3);
        
        // Function to update size options based on product type
        javafx.util.Callback<Void, Void> updateSizeOptions = (v) -> {
            sizeOptions.getChildren().clear();
            sizeCheckBoxes.clear();
            
            String selectedProductType = productTypeCombo.getValue();
            List<String> sizesToShow = clothingSizes;
            
            // Check if product type is "Giày" (Shoes)
            if (selectedProductType != null && selectedProductType.equals("Giày")) {
                sizesToShow = shoeSizes;
            }
            
            HBox sizeRow1 = new HBox(5);
            HBox sizeRow2 = new HBox(5);
            HBox sizeRow3 = new HBox(5);
            
            for (int i = 0; i < sizesToShow.size(); i++) {
                CheckBox cb = new CheckBox(sizesToShow.get(i));
                sizeCheckBoxes.add(cb);
                if (i < 4) {
                    sizeRow1.getChildren().add(cb);
                } else if (i < 8) {
                    sizeRow2.getChildren().add(cb);
                } else {
                    sizeRow3.getChildren().add(cb);
                }
            }
            
            sizeOptions.getChildren().add(sizeRow1);
            if (sizeRow2.getChildren().size() > 0) {
                sizeOptions.getChildren().add(sizeRow2);
            }
            if (sizeRow3.getChildren().size() > 0) {
                sizeOptions.getChildren().add(sizeRow3);
            }
            
            return null;
        };
        
        // Update size options when product type changes
        productTypeCombo.setOnAction(e -> {
            updateCategory.call(null);
            updateSizeOptions.call(null);
        });
        
        // Initial size options
        updateSizeOptions.call(null);
        
        fourthCol.getChildren().addAll(sizeLabel, sizeOptions);
        
        // Right column - Image preview and description (declare imagePreview first)
        VBox rightCol = new VBox(5);
        
        VBox previewBox = new VBox(2);
        Label previewLabel = new Label("Xem trước ảnh:");
        ImageView imagePreview = new ImageView();
        imagePreview.setFitWidth(100);
        imagePreview.setFitHeight(100);
        imagePreview.setPreserveRatio(true);
        imagePreview.getStyleClass().add("admin-image-preview");
        previewBox.getChildren().addAll(previewLabel, imagePreview);
        
        VBox imageBox = new VBox(2);
        Label imageLabel = new Label("Tên file ảnh:");
        TextField imageField = new TextField();
        imageField.setPromptText("Tên file ảnh");
        imageField.setPrefHeight(35);
        imageField.setPrefWidth(120);
        Button chooseImageBtn = new Button("...");
        chooseImageBtn.setPrefHeight(35);
        chooseImageBtn.setPrefWidth(35);
        chooseImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                try {
                    File imagesDir = new File("src/main/resources/images");
                    if (!imagesDir.exists()) imagesDir.mkdirs();
                    
                    // Đổi tên file theo tên sản phẩm
                    String productName = nameField.getText().trim();
                    if (productName.isEmpty()) {
                        productName = "product";
                    }
                    // Loại bỏ dấu tiếng Việt và chuyển thành không dấu
                    productName = removeVietnameseAccents(productName);
                    // Loại bỏ ký tự đặc biệt và khoảng trắng, thay bằng dấu gạch dưới
                    productName = productName.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_").toLowerCase();
                    
                    // Lấy extension từ file gốc
                    String originalFileName = file.getName();
                    String extension = "";
                    int lastDot = originalFileName.lastIndexOf('.');
                    if (lastDot > 0) {
                        extension = originalFileName.substring(lastDot);
                    } else {
                        extension = ".jpg"; // Default extension
                    }
                    
                    // Tạo tên file mới
                    String newFileName = productName + extension;
                    File dest = new File(imagesDir, newFileName);
                    
                    // Nếu file đã tồn tại, thêm số vào tên
                    int counter = 1;
                    while (dest.exists()) {
                        newFileName = productName + "_" + counter + extension;
                        dest = new File(imagesDir, newFileName);
                        counter++;
                    }
                    
                    Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    imageField.setText(newFileName);
                    // Load preview
                    try {
                        Image img = new Image(dest.toURI().toURL().toExternalForm());
                        imagePreview.setImage(img);
                    } catch (Exception ex) {
                        // Ignore preview error
                    }
                } catch (IOException ex) {
                    showError("Lỗi copy ảnh: " + ex.getMessage());
                }
            }
        });
        
        // Load image preview when text changes
        imageField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                try {
                    File imgFile = new File("src/main/resources/images/" + newVal);
                    if (imgFile.exists()) {
                        Image img = new Image(imgFile.toURI().toURL().toExternalForm());
                        imagePreview.setImage(img);
                    }
                } catch (Exception ex) {
                    // Ignore preview error
                }
            }
        });
        HBox imageFieldBox = new HBox(5, imageField, chooseImageBtn);
        imageBox.getChildren().addAll(imageLabel, imageFieldBox);
        
        VBox descBox = new VBox(2);
        Label descLabel = new Label("Mô tả sản phẩm:");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Mô tả sản phẩm");
        descArea.setPrefRowCount(3);
        descArea.setPrefWidth(200);
        descArea.setWrapText(true);
        descBox.getChildren().addAll(descLabel, descArea);
        
        rightCol.getChildren().addAll(previewBox, imageBox, descBox);
        
        formRow.getChildren().addAll(leftCol, middleCol, thirdCol, fourthCol, rightCol);
        
        // Buttons
        Button addBtn = new Button("+ THÊM");
        addBtn.getStyleClass().addAll("admin-btn", "admin-btn-add");
        addBtn.setOnAction(e -> handleAddProductFromForm(table, nameField, priceField, originalPriceField, 
            brandField, discountField, badgeField, categoryCombo, imageField, descArea, sizeCheckBoxes, genderCombo, productTypeCombo));
        
        Button editBtn = new Button("✏️ ĐO SỬA");
        editBtn.getStyleClass().addAll("admin-btn", "admin-btn-edit");
        editBtn.setOnAction(e -> handleEditProductFromForm(table, nameField, priceField, originalPriceField,
            brandField, discountField, badgeField, categoryCombo, imageField, descArea, sizeCheckBoxes, genderCombo, productTypeCombo));
        
        Button deleteBtn = new Button("🗑️ XÓA");
        deleteBtn.getStyleClass().addAll("admin-btn", "admin-btn-delete");
        deleteBtn.setOnAction(e -> handleDeleteProductFromForm(table));
        
        Button newBtn = new Button("🔄 Mới");
        newBtn.getStyleClass().addAll("admin-btn", "admin-btn-new");
        newBtn.setOnAction(e -> {
            nameField.clear();
            priceField.clear();
            originalPriceField.clear();
            brandField.clear();
            discountField.setText("0");
            badgeField.clear();
            categoryCombo.getSelectionModel().selectFirst();
            imageField.clear();
            descArea.clear();
            imagePreview.setImage(null);
            genderCombo.setValue("Unisex");
            productTypeCombo.setValue(null);
            for (CheckBox cb : sizeCheckBoxes) {
                cb.setSelected(false);
            }
            table.getSelectionModel().clearSelection();
        });
        
        HBox buttons = new HBox(10, addBtn, editBtn, deleteBtn, newBtn);
        buttons.setAlignment(Pos.BOTTOM_RIGHT);
        
        panel.getChildren().addAll(title, formRow, buttons);
        
        // Load product data when row is selected
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadProductToForm(newVal, nameField, priceField, originalPriceField, brandField, 
                    discountField, badgeField, categoryCombo, imageField, descArea, imagePreview, sizeCheckBoxes, genderCombo, productTypeCombo, updateSizeOptions);
            }
        });
        
        return panel;
    }
    
    private void loadProductToForm(Product product, TextField nameField, TextField priceField, 
            TextField originalPriceField, TextField brandField, TextField discountField, 
            TextField badgeField, ComboBox<Category> categoryCombo, TextField imageField, 
            TextArea descArea, ImageView imagePreview, List<CheckBox> sizeCheckBoxes,
            ComboBox<String> genderCombo, ComboBox<String> productTypeCombo, javafx.util.Callback<Void, Void> updateSizeOptions) {
        nameField.setText(product.getNameVn() != null ? product.getNameVn() : "");
        priceField.setText(product.getPrice() != null ? product.getPrice().toPlainString() : "");
        originalPriceField.setText(product.getOriginalPrice() != null ? product.getOriginalPrice().toPlainString() : "");
        brandField.setText(product.getBrand() != null ? product.getBrand() : "");
        discountField.setText(String.valueOf(product.getDiscountPercent()));
        badgeField.setText(product.getBadge() != null ? product.getBadge() : "");
        imageField.setText(product.getImagePath() != null ? product.getImagePath() : "");
        descArea.setText(product.getDescriptionVn() != null ? product.getDescriptionVn() : "");
        
        // Select category
        Category cat = categoryDAO.getCategoryById(product.getCategoryId());
        if (cat != null) {
            categoryCombo.getSelectionModel().select(cat);
        }
        
        // Load image preview
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            try {
                File imgFile = new File("src/main/resources/images/" + product.getImagePath());
                if (imgFile.exists()) {
                    Image img = new Image(imgFile.toURI().toURL().toExternalForm());
                    imagePreview.setImage(img);
                }
            } catch (Exception ex) {
                // Ignore preview error
            }
        }
        
        // Load gender first
        if (product.getGender() != null && !product.getGender().isEmpty()) {
            genderCombo.setValue(product.getGender());
        } else {
            genderCombo.setValue("Unisex");
        }
        
        // Load product type - this will trigger updateSizeOptions
        if (product.getProductType() != null && !product.getProductType().isEmpty()) {
            productTypeCombo.setValue(product.getProductType());
        } else {
            productTypeCombo.setValue(null);
        }
        
        // Update size options based on product type
        if (updateSizeOptions != null) {
            updateSizeOptions.call(null);
        }
        
        // Load sizes AFTER size options are updated
        List<String> productSizes = product.getSizes();
        if (productSizes != null && !productSizes.isEmpty()) {
            for (CheckBox cb : sizeCheckBoxes) {
                cb.setSelected(productSizes.contains(cb.getText()));
            }
        } else {
            for (CheckBox cb : sizeCheckBoxes) {
                cb.setSelected(false);
            }
        }
    }
    
    private void handleAddProductFromForm(TableView<Product> table, TextField nameField, TextField priceField,
            TextField originalPriceField, TextField brandField, TextField discountField, TextField badgeField,
            ComboBox<Category> categoryCombo, TextField imageField, TextArea descArea, List<CheckBox> sizeCheckBoxes,
            ComboBox<String> genderCombo, ComboBox<String> productTypeCombo) {
        try {
            if (nameField.getText().isEmpty() || priceField.getText().isEmpty()) {
                showError("Vui lòng điền đầy đủ thông tin bắt buộc: Tên, Giá");
                return;
            }
            
            // Tự động set category_id nếu chưa có
            if (categoryCombo.getSelectionModel().getSelectedItem() == null) {
                if (categoryCombo.getItems().size() > 0) {
                    categoryCombo.getSelectionModel().selectFirst();
                }
            }
            
            Product product = new Product();
            product.setNameVn(nameField.getText());
            product.setName(nameField.getText());
            product.setPrice(new BigDecimal(priceField.getText()));
            if (!originalPriceField.getText().isEmpty()) {
                product.setOriginalPrice(new BigDecimal(originalPriceField.getText()));
            }
            product.setBrand(brandField.getText());
            product.setDiscountPercent(Integer.parseInt(discountField.getText()));
            product.setBadge(badgeField.getText().isEmpty() ? null : badgeField.getText());
            product.setCategoryId(categoryCombo.getSelectionModel().getSelectedItem().getId());
            product.setImagePath(imageField.getText());
            product.setDescriptionVn(descArea.getText());
            product.setDescription(descArea.getText());
            product.setGender(genderCombo.getValue());
            product.setProductType(productTypeCombo.getValue());
            
            // Get selected sizes
            List<String> selectedSizes = new ArrayList<>();
            for (CheckBox cb : sizeCheckBoxes) {
                if (cb.isSelected()) {
                    selectedSizes.add(cb.getText());
                }
            }
            // Sắp xếp size theo thứ tự từ nhỏ đến lớn trước khi lưu
            selectedSizes = ProductDAO.sortSizes(selectedSizes);
            
            int productId = productDAO.addProduct(product, new ArrayList<>(), selectedSizes);
            if (productId > 0) {
                refreshProducts(table);
                Alert success = new Alert(Alert.AlertType.INFORMATION, "Thêm sản phẩm thành công! ID: " + productId, ButtonType.OK);
                success.showAndWait();
            }
        } catch (Exception ex) {
            showError("Lỗi: " + ex.getMessage());
        }
    }
    
    private void handleEditProductFromForm(TableView<Product> table, TextField nameField, TextField priceField,
            TextField originalPriceField, TextField brandField, TextField discountField, TextField badgeField,
            ComboBox<Category> categoryCombo, TextField imageField, TextArea descArea, List<CheckBox> sizeCheckBoxes,
            ComboBox<String> genderCombo, ComboBox<String> productTypeCombo) {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn sản phẩm cần sửa");
            return;
        }
        
        try {
            String oldImagePath = selected.getImagePath();
            String oldName = selected.getNameVn();
            String newName = nameField.getText();
            
            selected.setNameVn(newName);
            selected.setName(newName);
            selected.setPrice(new BigDecimal(priceField.getText()));
            if (!originalPriceField.getText().isEmpty()) {
                selected.setOriginalPrice(new BigDecimal(originalPriceField.getText()));
            }
            selected.setBrand(brandField.getText());
            selected.setDiscountPercent(Integer.parseInt(discountField.getText()));
            selected.setBadge(badgeField.getText().isEmpty() ? null : badgeField.getText());
            selected.setCategoryId(categoryCombo.getSelectionModel().getSelectedItem().getId());
            
            // Đổi tên file ảnh nếu tên sản phẩm thay đổi và có ảnh
            String imagePath = imageField.getText();
            if (oldImagePath != null && !oldImagePath.isEmpty() && !oldName.equals(newName)) {
                try {
                    File imagesDir = new File("src/main/resources/images");
                    String oldFileName = oldImagePath;
                    // Loại bỏ prefix "images/" nếu có
                    if (oldFileName.startsWith("images/")) {
                        oldFileName = oldFileName.substring(7);
                    }
                    File oldFile = new File(imagesDir, oldFileName);
                    
                    if (oldFile.exists()) {
                        // Tạo tên file mới dựa trên tên sản phẩm mới
                        String productName = removeVietnameseAccents(newName);
                        productName = productName.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_").toLowerCase();
                        
                        // Lấy extension từ file cũ
                        String extension = "";
                        int lastDot = oldFileName.lastIndexOf('.');
                        if (lastDot > 0) {
                            extension = oldFileName.substring(lastDot);
                        } else {
                            extension = ".jpg";
                        }
                        
                        String newFileName = productName + extension;
                        File newFile = new File(imagesDir, newFileName);
                        
                        // Nếu file đã tồn tại, thêm số vào tên
                        int counter = 1;
                        while (newFile.exists() && !newFile.equals(oldFile)) {
                            newFileName = productName + "_" + counter + extension;
                            newFile = new File(imagesDir, newFileName);
                            counter++;
                        }
                        
                        // Đổi tên file
                        Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        imagePath = newFileName;
                        imageField.setText(newFileName);
                    }
                } catch (IOException ex) {
                    System.err.println("Error renaming image file: " + ex.getMessage());
                    // Tiếp tục với tên file cũ nếu không đổi được
                }
            }
            
            selected.setImagePath(imagePath);
            selected.setDescriptionVn(descArea.getText());
            selected.setDescription(descArea.getText());
            selected.setGender(genderCombo.getValue());
            selected.setProductType(productTypeCombo.getValue());
            
            productDAO.updateProduct(selected);
            
            // Update sizes
            List<String> selectedSizes = new ArrayList<>();
            for (CheckBox cb : sizeCheckBoxes) {
                if (cb.isSelected()) {
                    selectedSizes.add(cb.getText());
                }
            }
            // Sắp xếp size theo thứ tự từ nhỏ đến lớn trước khi lưu
            selectedSizes = ProductDAO.sortSizes(selectedSizes);
            // Delete old sizes and insert new ones
            try (Connection conn = DatabaseConnection.getConnection()) {
                String deleteSql = "DELETE FROM product_sizes WHERE product_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, selected.getId());
                    deleteStmt.executeUpdate();
                }
                
                if (!selectedSizes.isEmpty()) {
                    String insertSql = "INSERT INTO product_sizes (product_id, size) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        for (String size : selectedSizes) {
                            insertStmt.setInt(1, selected.getId());
                            insertStmt.setString(2, size);
                            insertStmt.addBatch();
                        }
                        insertStmt.executeBatch();
                    }
                }
            } catch (SQLException ex) {
                System.err.println("Error updating sizes: " + ex.getMessage());
            }
            
            refreshProducts(table);
            Alert success = new Alert(Alert.AlertType.INFORMATION, "Sửa sản phẩm thành công!", ButtonType.OK);
            success.showAndWait();
        } catch (Exception ex) {
            showError("Lỗi: " + ex.getMessage());
        }
    }
    
    private void handleDeleteProductFromForm(TableView<Product> table) {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn sản phẩm cần xóa");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa sản phẩm #" + selected.getId() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().filter(btn -> btn == ButtonType.OK).ifPresent(btn -> {
            productDAO.deleteProduct(selected.getId());
            refreshProducts(table);
        });
    }
    
    private VBox createProductTablePanel(TableView<Product> table) {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("admin-table-panel");
        
        // Search and filter
        HBox searchBar = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Tìm theo tên, mã sản phẩm...");
        searchField.getStyleClass().add("admin-search-field");
        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("Tất cả", "Áo", "Quần", "Giày", "Phụ kiện");
        categoryFilter.setValue("Tất cả");
        categoryFilter.getStyleClass().add("admin-filter-combo");
        Label filterLabel = new Label("Lọc danh mục:");
        searchBar.getChildren().addAll(new Label("🔍 Tìm kiếm:"), searchField, filterLabel, categoryFilter);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        // Filter logic
        javafx.util.Callback<Void, Void> applyFilters = (v) -> {
            String searchText = searchField.getText().toLowerCase();
            String filterValue = categoryFilter.getValue();
            
            List<Product> allProducts = productDAO.getAllProducts();
            List<Product> filteredProducts = new ArrayList<>();
            
            for (Product product : allProducts) {
                // Search filter
                boolean matchesSearch = searchText.isEmpty() ||
                    (product.getNameVn() != null && product.getNameVn().toLowerCase().contains(searchText)) ||
                    (product.getName() != null && product.getName().toLowerCase().contains(searchText)) ||
                    (product.getBrand() != null && product.getBrand().toLowerCase().contains(searchText)) ||
                    (String.valueOf(product.getId()).contains(searchText));
                
                // Category/Product type filter
                boolean matchesFilter = filterValue == null || filterValue.equals("Tất cả");
                if (!matchesFilter && product.getProductType() != null) {
                    matchesFilter = product.getProductType().equals(filterValue);
                }
                
                if (matchesSearch && matchesFilter) {
                    filteredProducts.add(product);
                }
            }
            
            // Update table
            ObservableList<Product> data = FXCollections.observableArrayList(filteredProducts);
            table.setItems(data);
            
            return null;
        };
        
        // Add event handlers
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters.call(null));
        categoryFilter.setOnAction(e -> applyFilters.call(null));
        
        // Table is already created and passed as parameter
        
        panel.getChildren().addAll(searchBar, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return panel;
    }
    
    private void refreshProducts(TableView<Product> table) {
        ObservableList<Product> data = FXCollections.observableArrayList(productDAO.getAllProducts());
        table.setItems(data);
    }
    
    private Tab createUsersTab() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("admin-table-panel");
        
        // Header
        Label title = new Label("👥 Quản lý Người dùng");
        title.getStyleClass().add("admin-section-title");
        
        // Action buttons
        Button lockBtn = new Button("🔒 Khóa / Mở khóa");
        lockBtn.getStyleClass().addAll("admin-btn", "admin-btn-lock");
        Button deleteBtn = new Button("🗑️ Xóa tài khoản");
        deleteBtn.getStyleClass().addAll("admin-btn", "admin-btn-delete");
        Button reloadBtn = new Button("🔄 Tải lại");
        reloadBtn.getStyleClass().addAll("admin-btn", "admin-btn-reload");
        
        HBox buttons = new HBox(10, lockBtn, deleteBtn, reloadBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        // Table
        TableView<User> table = new TableView<>();
        table.getStyleClass().add("admin-data-table");
        
        TableColumn<User, String> usernameCol = new TableColumn<>("Tên đăng nhập");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<User, String> phoneCol = new TableColumn<>("Số điện thoại");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<User, String> ordersCol = new TableColumn<>("Tổng đơn h...");
        ordersCol.setCellValueFactory(c -> {
            int userId = c.getValue().getId();
            List<Order> allOrders = orderDAO.getAllOrders();
            long count = allOrders.stream().filter(o -> o.getUserId() == userId).count();
            return new SimpleStringProperty(String.valueOf(count));
        });
        TableColumn<User, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(c -> {
            String level = c.getValue().getMembershipLevel();
            String status = "Blocked".equalsIgnoreCase(level) ? "Khóa" : "Hoạt động";
            return new SimpleStringProperty(status);
        });
        
        table.getColumns().addAll(usernameCol, emailCol, phoneCol, ordersCol, statusCol);
        refreshUsers(table);
        
        lockBtn.setOnAction(e -> handleToggleUserLock(table));
        reloadBtn.setOnAction(e -> refreshUsers(table));
        
        panel.getChildren().addAll(title, buttons, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        Tab tab = new Tab("Người dùng", panel);
        tab.setClosable(false);
        return tab;
    }
    
    private void refreshUsers(TableView<User> table) {
        ObservableList<User> data = FXCollections.observableArrayList(userDAO.getAllUsers());
        table.setItems(data);
    }
    
    private Tab createVouchersTab() {
        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        
        // Left panel - Form
        VBox formPanel = new VBox(10);
        formPanel.setPadding(new Insets(15));
        formPanel.getStyleClass().add("admin-form-panel");
        formPanel.setPrefWidth(400);
        
        Label formTitle = new Label("Tạo mã giảm giá mới");
        formTitle.getStyleClass().add("admin-form-title");
        
        TextField codeField = new TextField();
        codeField.setPromptText("Nhập mã code (VD: SALE2024)");
        TextField descField = new TextField();
        descField.setPromptText("Mô tả ngắn cho mã giảm giá");
        ComboBox<Voucher.DiscountType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(Voucher.DiscountType.values());
        typeCombo.setValue(Voucher.DiscountType.PERCENTAGE);
        TextField valueField = new TextField();
        valueField.setPromptText("Giá trị giảm (VD: 10 hoặc 50000)");
        TextField minOrderField = new TextField();
        minOrderField.setPromptText("Đơn tối thiểu (VD: 300000)");
        TextField maxDiscountField = new TextField();
        maxDiscountField.setPromptText("Giảm tối đa (tùy chọn)");
        TextField quantityField = new TextField();
        quantityField.setPromptText("Số lượng (để trống = không giới hạn)");
        DatePicker startPicker = new DatePicker();
        DatePicker expiryPicker = new DatePicker();
        ChoiceBox<String> activeChoice = new ChoiceBox<>(FXCollections.observableArrayList("Hoạt động", "Không"));
        activeChoice.setValue("Hoạt động");
        
        VBox formFields = new VBox(8);
        formFields.getChildren().addAll(
            new Label("Mã Code:"), codeField,
            new Label("Mô tả:"), descField,
            new Label("Loại giảm giá:"), typeCombo,
            new Label("Giá trị giảm:"), valueField,
            new Label("Đơn tối thiểu:"), minOrderField,
            new Label("Giảm tối đa:"), maxDiscountField,
            new Label("Số lượng voucher:"), quantityField,
            new Label("Ngày bắt đầu:"), startPicker,
            new Label("Ngày hết hạn:"), expiryPicker,
            new Label("Trạng thái:"), activeChoice
        );
        
        Button createBtn = new Button("+ Tạo Voucher");
        createBtn.getStyleClass().addAll("admin-btn", "admin-btn-primary");
        createBtn.setPrefWidth(Double.MAX_VALUE);
        
        formPanel.getChildren().addAll(formTitle, formFields, createBtn);
        
        // Wrap form panel in ScrollPane for vertical scrolling
        ScrollPane formScrollPane = new ScrollPane(formPanel);
        formScrollPane.setFitToWidth(true);
        formScrollPane.setFitToHeight(false);
        formScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScrollPane.setPrefWidth(400);
        
        // Right panel - Table
        VBox tablePanel = new VBox(15);
        tablePanel.setPadding(new Insets(20));
        tablePanel.getStyleClass().add("admin-table-panel");
        HBox.setHgrow(tablePanel, Priority.ALWAYS);
        
        HBox tableHeader = new HBox();
        Label tableTitle = new Label("Danh sách Voucher");
        tableTitle.getStyleClass().add("admin-form-title");
        Button deleteBtn = new Button("🗑️ Xóa mã chọn");
        deleteBtn.getStyleClass().addAll("admin-btn", "admin-btn-delete");
        HBox.setHgrow(tableTitle, Priority.ALWAYS);
        tableHeader.getChildren().addAll(tableTitle, deleteBtn);
        
        TableView<Voucher> table = new TableView<>();
        table.getStyleClass().add("admin-data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Voucher, String> codeCol = new TableColumn<>("Mã Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        TableColumn<Voucher, String> descCol = new TableColumn<>("Mô tả");
        descCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getDescriptionVn() != null ? cell.getValue().getDescriptionVn() : ""));
        TableColumn<Voucher, String> typeCol = new TableColumn<>("Loại");
        typeCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getDiscountType() != null ? cell.getValue().getDiscountType().name() : ""));
        TableColumn<Voucher, String> discountCol = new TableColumn<>("Giảm");
        discountCol.setCellValueFactory(cell -> {
            Voucher v = cell.getValue();
            if (v.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
                return new SimpleStringProperty(
                    v.getDiscountValue() != null ? v.getDiscountValue().stripTrailingZeros().toPlainString() + " %" : "0 %");
            }
            return new SimpleStringProperty(v.getDiscountValue() != null ? formatCurrency(v.getDiscountValue()) : "0");
        });
        TableColumn<Voucher, String> minOrderCol = new TableColumn<>("Đơn tối thiểu");
        minOrderCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getMinOrderAmount() != null ? formatCurrency(cell.getValue().getMinOrderAmount()) : "0"));
        TableColumn<Voucher, String> maxDiscountCol = new TableColumn<>("Giảm tối đa");
        maxDiscountCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getMaxDiscountAmount() != null ? formatCurrency(cell.getValue().getMaxDiscountAmount()) : ""));
        TableColumn<Voucher, String> quantityCol = new TableColumn<>("Số lượng");
        quantityCol.setCellValueFactory(cell -> {
            Integer limit = cell.getValue().getUsageLimit();
            return new SimpleStringProperty(limit != null ? limit.toString() : "0");
        });
        TableColumn<Voucher, String> startCol = new TableColumn<>("Ngày bắt đầu");
        startCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getStartDate() != null ? cell.getValue().getStartDate().toString() : ""));
        TableColumn<Voucher, String> expiryCol = new TableColumn<>("Ngày hết hạn");
        expiryCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getEndDate() != null ? cell.getValue().getEndDate().toString() : ""));
        TableColumn<Voucher, String> activeCol = new TableColumn<>("Trạng thái");
        activeCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().isActive() ? "Hoạt động" : "Không"));
        
        table.getColumns().addAll(
            codeCol, descCol, typeCol, discountCol, minOrderCol, maxDiscountCol,
            quantityCol, startCol, expiryCol, activeCol
        );
        refreshVouchers(table);
        
        createBtn.setOnAction(e -> {
            try {
                if (codeField.getText().isEmpty() || valueField.getText().isEmpty()) {
                    showError("Vui lòng nhập ít nhất Mã code và Giá trị giảm");
                    return;
                }
                
                Voucher v = new Voucher();
                v.setCode(codeField.getText().trim());
                v.setDescription(descField.getText().trim());
                v.setDescriptionVn(descField.getText().trim());
                v.setDiscountType(typeCombo.getValue() != null ? typeCombo.getValue() : Voucher.DiscountType.PERCENTAGE);
                v.setDiscountValue(new BigDecimal(valueField.getText().trim()));
                v.setMinOrderAmount(minOrderField.getText().isEmpty() ? BigDecimal.ZERO : new BigDecimal(minOrderField.getText().trim()));
                v.setMaxDiscountAmount(maxDiscountField.getText().isEmpty() ? null : new BigDecimal(maxDiscountField.getText().trim()));
                v.setUsageLimit(quantityField.getText().isEmpty() ? null : Integer.parseInt(quantityField.getText().trim()));
                v.setStartDate(startPicker.getValue());
                v.setEndDate(expiryPicker.getValue());
                v.setActive("Hoạt động".equals(activeChoice.getValue()));
                
                voucherDAO.createVoucher(v);
                refreshVouchers(table);
                
                codeField.clear();
                descField.clear();
                valueField.clear();
                minOrderField.clear();
                maxDiscountField.clear();
                quantityField.clear();
                startPicker.setValue(null);
                expiryPicker.setValue(null);
                activeChoice.setValue("Hoạt động");
            } catch (Exception ex) {
                showError("Lỗi: " + ex.getMessage());
            }
        });
        
        deleteBtn.setOnAction(e -> handleDeleteVoucher(table));
        
        // Wrap table in ScrollPane for horizontal scrolling
        ScrollPane tableScrollPane = new ScrollPane(table);
        tableScrollPane.setFitToWidth(true);
        tableScrollPane.setFitToHeight(true);
        tableScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        tableScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        tablePanel.getChildren().addAll(tableHeader, tableScrollPane);
        VBox.setVgrow(tableScrollPane, Priority.ALWAYS);
        
        mainLayout.getChildren().addAll(formScrollPane, tablePanel);
        
        Tab tab = new Tab("Voucher", mainLayout);
        tab.setClosable(false);
        return tab;
    }
    
    private void refreshVouchers(TableView<Voucher> table) {
        ObservableList<Voucher> data = FXCollections.observableArrayList(voucherDAO.getAllVouchers());
        table.setItems(data);
    }
    
    private Tab createReviewsTab() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("admin-table-panel");
        
        // Header
        Label title = new Label("⭐ Phản hồi khách hàng");
        title.getStyleClass().add("admin-section-title");
        
        // Action buttons
        Button deleteBtn = new Button("🗑️ Xóa đánh giá");
        deleteBtn.getStyleClass().addAll("admin-btn", "admin-btn-delete");
        Button reloadBtn = new Button("🔄 Tải lại");
        reloadBtn.getStyleClass().addAll("admin-btn", "admin-btn-reload");
        
        HBox buttons = new HBox(10, deleteBtn, reloadBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        // Table
        TableView<Review> table = new TableView<>();
        table.getStyleClass().add("admin-data-table");
        
        TableColumn<Review, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Review, String> userCol = new TableColumn<>("Người dùng");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        TableColumn<Review, String> productCol = new TableColumn<>("Sản phẩm");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        TableColumn<Review, String> ratingCol = new TableColumn<>("Đánh giá");
        ratingCol.setCellValueFactory(cell -> {
            int rating = cell.getValue().getRating();
            return new SimpleStringProperty(rating + "★");
        });
        TableColumn<Review, String> commentCol = new TableColumn<>("Nội dung đánh giá");
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        TableColumn<Review, String> createdCol = new TableColumn<>("Ngày đánh giá");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        createdCol.setCellValueFactory(cell -> {
            if (cell.getValue().getCreatedAt() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(cell.getValue().getCreatedAt().format(formatter));
        });
        
        table.getColumns().addAll(idCol, userCol, productCol, ratingCol, commentCol, createdCol);
        refreshReviews(table);
        
        deleteBtn.setOnAction(e -> handleDeleteReview(table));
        reloadBtn.setOnAction(e -> refreshReviews(table));
        
        panel.getChildren().addAll(title, buttons, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        Tab tab = new Tab("Đánh giá", panel);
        tab.setClosable(false);
        return tab;
    }
    
    private void refreshReviews(TableView<Review> table) {
        ObservableList<Review> data = FXCollections.observableArrayList(reviewDAO.getAllReviews());
        table.setItems(data);
    }
    
    private void refreshOrders(TableView<Order> table) {
        ObservableList<Order> data = FXCollections.observableArrayList(orderDAO.getAllOrders());
        table.setItems(data);
    }
    
    private void handleEditProduct(TableView<Product> table) {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        TextField nameField = new TextField(selected.getNameVn());
        TextField brandField = new TextField(selected.getBrand());
        TextField priceField = new TextField(selected.getPrice() != null ? selected.getPrice().toPlainString() : "");
        TextField discountField = new TextField(String.valueOf(selected.getDiscountPercent()));
        TextField badgeField = new TextField(selected.getBadge());
        
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Tên:"), nameField);
        form.addRow(1, new Label("Thương hiệu:"), brandField);
        form.addRow(2, new Label("Giá:"), priceField);
        form.addRow(3, new Label("Giảm %:"), discountField);
        form.addRow(4, new Label("Badge:"), badgeField);
        
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Sửa sản phẩm");
        dialog.getDialogPane().setContent(form);
        dialog.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().filter(btn -> btn == ButtonType.OK).ifPresent(btn -> {
            try {
                selected.setNameVn(nameField.getText());
                selected.setBrand(brandField.getText());
                selected.setPrice(new BigDecimal(priceField.getText()));
                selected.setDiscountPercent(Integer.parseInt(discountField.getText()));
                selected.setBadge(badgeField.getText());
                productDAO.updateProduct(selected);
                refreshProducts(table);
            } catch (Exception ex) {
                showError("Dữ liệu không hợp lệ: " + ex.getMessage());
            }
        });
    }
    
    private void handleDeleteProduct(TableView<Product> table) {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa sản phẩm #" + selected.getId() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().filter(btn -> btn == ButtonType.OK).ifPresent(btn -> {
            productDAO.deleteProduct(selected.getId());
            refreshProducts(table);
        });
    }
    
    private void handleAddProduct(TableView<Product> table) {
        TextField nameField = new TextField();
        TextField nameVnField = new TextField();
        TextField descField = new TextField();
        TextField descVnField = new TextField();
        TextField brandField = new TextField();
        TextField priceField = new TextField();
        TextField originalPriceField = new TextField();
        TextField discountField = new TextField("0");
        TextField imageField = new TextField();
        imageField.setEditable(false);
        Button chooseImageBtn = new Button("Chọn ảnh");
        chooseImageBtn.getStyleClass().addAll("admin-btn", "admin-btn-ghost");
        chooseImageBtn.setOnAction(ev -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn ảnh sản phẩm");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
            );
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                try {
                    // Tạo thư mục images trong resources nếu chưa có
                    File projectRoot = new File(System.getProperty("user.dir"));
                    File imagesDir = new File(projectRoot, "src/main/resources/images");
                    if (!imagesDir.exists()) {
                        imagesDir.mkdirs();
                    }
                    
                    // Copy ảnh vào thư mục images với tên file duy nhất
                    String fileName = selectedFile.getName();
                    String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                    String extension = fileName.substring(fileName.lastIndexOf('.'));
                    File destFile = new File(imagesDir, fileName);
                    
                    // Nếu file đã tồn tại, thêm số vào tên
                    int counter = 1;
                    while (destFile.exists()) {
                        String newFileName = baseName + "_" + counter + extension;
                        destFile = new File(imagesDir, newFileName);
                        counter++;
                    }
                    
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    // Lưu đường dẫn tương đối từ resources
                    imageField.setText("images/" + destFile.getName());
                } catch (IOException ex) {
                    showError("Không thể copy ảnh: " + ex.getMessage());
                }
            }
        });
        HBox imageBox = new HBox(10, imageField, chooseImageBtn);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(imageField, Priority.ALWAYS);
        TextField badgeField = new TextField();
        
        // Màu sắc - ComboBox với nhiều lựa chọn
        VBox colorsBox = new VBox(5);
        Label colorsLabel = new Label("Màu sắc:");
        List<String> availableColors = new ArrayList<>();
        availableColors.add("#1e3a5f");
        availableColors.add("#ffffff");
        availableColors.add("#e0e0e0");
        availableColors.add("#000000");
        availableColors.add("#ff0000");
        availableColors.add("#00ff00");
        availableColors.add("#0000ff");
        availableColors.add("#ffff00");
        availableColors.add("#ff00ff");
        availableColors.add("#00ffff");
        availableColors.add("#808080");
        availableColors.add("#ffa500");
        
        List<CheckBox> colorCheckBoxes = new ArrayList<>();
        for (String color : availableColors) {
            CheckBox cb = new CheckBox(color);
            colorCheckBoxes.add(cb);
        }
        VBox colorOptions = new VBox(3);
        colorOptions.getChildren().addAll(colorCheckBoxes);
        ScrollPane colorScroll = new ScrollPane(colorOptions);
        colorScroll.setPrefHeight(120);
        colorScroll.setFitToWidth(true);
        colorsBox.getChildren().addAll(colorsLabel, colorScroll);
        
        // Kích cỡ - ComboBox với nhiều lựa chọn
        VBox sizesBox = new VBox(5);
        Label sizesLabel = new Label("Kích cỡ:");
        List<String> availableSizes = new ArrayList<>();
        availableSizes.add("S");
        availableSizes.add("M");
        availableSizes.add("L");
        availableSizes.add("XL");
        availableSizes.add("2XL");
        availableSizes.add("3XL");
        availableSizes.add("XS");
        availableSizes.add("XXL");
        
        List<CheckBox> sizeCheckBoxes = new ArrayList<>();
        for (String size : availableSizes) {
            CheckBox cb = new CheckBox(size);
            sizeCheckBoxes.add(cb);
        }
        VBox sizeOptions = new VBox(3);
        sizeOptions.getChildren().addAll(sizeCheckBoxes);
        ScrollPane sizeScroll = new ScrollPane(sizeOptions);
        sizeScroll.setPrefHeight(120);
        sizeScroll.setFitToWidth(true);
        sizesBox.getChildren().addAll(sizesLabel, sizeScroll);
        
        List<Category> categories = categoryDAO.getAllCategories();
        ComboBox<Category> categoryBox = new ComboBox<>(FXCollections.observableArrayList(categories));
        categoryBox.setCellFactory(param -> new javafx.scene.control.ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNameVn());
                }
            }
        });
        if (!categories.isEmpty()) {
            categoryBox.getSelectionModel().selectFirst();
        }
        
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));
        form.addRow(0, new Label("Tên (EN):"), nameField);
        form.addRow(1, new Label("Tên (VN):"), nameVnField);
        form.addRow(2, new Label("Mô tả (EN):"), descField);
        form.addRow(3, new Label("Mô tả (VN):"), descVnField);
        form.addRow(4, new Label("Thương hiệu:"), brandField);
        form.addRow(5, new Label("Giá:"), priceField);
        form.addRow(6, new Label("Giá gốc:"), originalPriceField);
        form.addRow(7, new Label("Giảm %:"), discountField);
        form.addRow(8, new Label("Danh mục:"), categoryBox);
        form.addRow(9, new Label("Ảnh:"), imageBox);
        form.addRow(10, new Label("Badge:"), badgeField);
        form.add(colorsBox, 0, 11, 2, 1);
        form.add(sizesBox, 0, 12, 2, 1);
        
        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Thêm sản phẩm mới");
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().filter(btn -> btn == ButtonType.OK).ifPresent(btn -> {
            try {
                if (nameVnField.getText().isEmpty() || priceField.getText().isEmpty() || categoryBox.getSelectionModel().getSelectedItem() == null) {
                    showError("Vui lòng điền đầy đủ thông tin bắt buộc: Tên (VN), Giá, Danh mục");
                    return;
                }
                
                Product product = new Product();
                product.setName(nameField.getText().isEmpty() ? nameVnField.getText() : nameField.getText());
                product.setNameVn(nameVnField.getText());
                product.setDescription(descField.getText());
                product.setDescriptionVn(descVnField.getText());
                product.setBrand(brandField.getText());
                product.setPrice(new BigDecimal(priceField.getText()));
                if (!originalPriceField.getText().isEmpty()) {
                    product.setOriginalPrice(new BigDecimal(originalPriceField.getText()));
                }
                product.setDiscountPercent(Integer.parseInt(discountField.getText()));
                product.setCategoryId(categoryBox.getSelectionModel().getSelectedItem().getId());
                product.setImagePath(imageField.getText());
                product.setBadge(badgeField.getText().isEmpty() ? null : badgeField.getText());
                
                // Lấy màu sắc từ CheckBox
                List<String> colors = new ArrayList<>();
                for (CheckBox cb : colorCheckBoxes) {
                    if (cb.isSelected()) {
                        colors.add(cb.getText());
                    }
                }
                
                // Lấy kích cỡ từ CheckBox
                List<String> sizes = new ArrayList<>();
                for (CheckBox cb : sizeCheckBoxes) {
                    if (cb.isSelected()) {
                        sizes.add(cb.getText());
                    }
                }
                // Sắp xếp size theo thứ tự từ nhỏ đến lớn trước khi lưu
                sizes = ProductDAO.sortSizes(sizes);
                
                int productId = productDAO.addProduct(product, colors, sizes);
                if (productId > 0) {
                    refreshProducts(table);
                    Alert success = new Alert(Alert.AlertType.INFORMATION, "Thêm sản phẩm thành công! ID: " + productId, ButtonType.OK);
                    success.showAndWait();
                } else {
                    showError("Không thể thêm sản phẩm. Vui lòng kiểm tra lại dữ liệu.");
                }
            } catch (Exception ex) {
                showError("Dữ liệu không hợp lệ: " + ex.getMessage());
            }
        });
    }
    
    private void handleToggleUserLock(TableView<User> table) {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String newLevel = "Blocked".equalsIgnoreCase(selected.getMembershipLevel()) ? "Silver" : "Blocked";
        userDAO.updateMembershipLevel(selected.getId(), newLevel);
        refreshUsers(table);
    }
    
    private void handleEditVoucher(TableView<Voucher> table) {
        Voucher selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        TextField descField = new TextField(selected.getDescriptionVn());
        TextField valueField = new TextField(selected.getDiscountValue() != null ? selected.getDiscountValue().toPlainString() : "");
        ComboBox<Voucher.DiscountType> typeBox = new ComboBox<>(FXCollections.observableArrayList(Voucher.DiscountType.values()));
        typeBox.getSelectionModel().select(selected.getDiscountType());
        TextField minField = new TextField(selected.getMinOrderAmount() != null ? selected.getMinOrderAmount().toPlainString() : "");
        TextField maxField = new TextField(selected.getMaxDiscountAmount() != null ? selected.getMaxDiscountAmount().toPlainString() : "");
        TextField usageField = new TextField(selected.getUsageLimit() != null ? selected.getUsageLimit().toString() : "");
        ChoiceBox<String> activeChoice = new ChoiceBox<>(FXCollections.observableArrayList("Có", "Không"));
        activeChoice.getSelectionModel().select(selected.isActive() ? "Có" : "Không");
        
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Mô tả:"), descField);
        form.addRow(1, new Label("Giá trị:"), valueField);
        form.addRow(2, new Label("Loại:"), typeBox);
        form.addRow(3, new Label("Đơn tối thiểu:"), minField);
        form.addRow(4, new Label("Giảm tối đa:"), maxField);
        form.addRow(5, new Label("Giới hạn dùng:"), usageField);
        form.addRow(6, new Label("Hoạt động:"), activeChoice);
        
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Sửa voucher");
        dialog.getDialogPane().setContent(form);
        dialog.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().filter(btn -> btn == ButtonType.OK).ifPresent(btn -> {
            try {
                selected.setDescriptionVn(descField.getText());
                selected.setDiscountType(typeBox.getSelectionModel().getSelectedItem());
                selected.setDiscountValue(new BigDecimal(valueField.getText()));
                selected.setMinOrderAmount(minField.getText().isEmpty() ? null : new BigDecimal(minField.getText()));
                selected.setMaxDiscountAmount(maxField.getText().isEmpty() ? null : new BigDecimal(maxField.getText()));
                selected.setUsageLimit(usageField.getText().isEmpty() ? null : Integer.parseInt(usageField.getText()));
                selected.setActive("Có".equals(activeChoice.getSelectionModel().getSelectedItem()));
                voucherDAO.updateVoucher(selected);
                refreshVouchers(table);
            } catch (Exception ex) {
                showError("Dữ liệu không hợp lệ: " + ex.getMessage());
            }
        });
    }
    
    private void handleAddVoucher(TableView<Voucher> table) {
        TextField codeField = new TextField();
        TextField descField = new TextField();
        TextField valueField = new TextField();
        ComboBox<Voucher.DiscountType> typeBox = new ComboBox<>(FXCollections.observableArrayList(Voucher.DiscountType.values()));
        typeBox.getSelectionModel().select(Voucher.DiscountType.PERCENTAGE);
        TextField minField = new TextField();
        TextField maxField = new TextField();
        TextField usageField = new TextField();
        DatePicker startPicker = new DatePicker();
        DatePicker endPicker = new DatePicker();
        
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Mã:"), codeField);
        form.addRow(1, new Label("Mô tả:"), descField);
        form.addRow(2, new Label("Giá trị:"), valueField);
        form.addRow(3, new Label("Loại:"), typeBox);
        form.addRow(4, new Label("Đơn tối thiểu:"), minField);
        form.addRow(5, new Label("Giảm tối đa:"), maxField);
        form.addRow(6, new Label("Giới hạn dùng:"), usageField);
        form.addRow(7, new Label("Bắt đầu:"), startPicker);
        form.addRow(8, new Label("Kết thúc:"), endPicker);
        
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Thêm voucher");
        dialog.getDialogPane().setContent(form);
        dialog.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().filter(btn -> btn == ButtonType.OK).ifPresent(btn -> {
            try {
                Voucher v = new Voucher();
                v.setCode(codeField.getText());
                v.setDescription(descField.getText());
                v.setDescriptionVn(descField.getText());
                v.setDiscountType(typeBox.getSelectionModel().getSelectedItem());
                v.setDiscountValue(new BigDecimal(valueField.getText()));
                v.setMinOrderAmount(minField.getText().isEmpty() ? BigDecimal.ZERO : new BigDecimal(minField.getText()));
                v.setMaxDiscountAmount(maxField.getText().isEmpty() ? null : new BigDecimal(maxField.getText()));
                v.setUsageLimit(usageField.getText().isEmpty() ? null : Integer.parseInt(usageField.getText()));
                v.setStartDate(startPicker.getValue());
                v.setEndDate(endPicker.getValue());
                v.setActive(true);
                voucherDAO.createVoucher(v);
                refreshVouchers(table);
            } catch (Exception ex) {
                showError("Dữ liệu không hợp lệ: " + ex.getMessage());
            }
        });
    }
    
    private void handleDeleteVoucher(TableView<Voucher> table) {
        Voucher selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa voucher " + selected.getCode() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().filter(btn -> btn == ButtonType.OK).ifPresent(btn -> {
            voucherDAO.deleteVoucher(selected.getId());
            refreshVouchers(table);
        });
    }
    
    private void handleDeleteReview(TableView<Review> table) {
        Review selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa bình luận của " + selected.getUserName() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().filter(btn -> btn == ButtonType.OK).ifPresent(btn -> {
            reviewDAO.deleteReview(selected.getId());
            refreshReviews(table);
        });
    }
    
    private void handleUpdateOrderStatus(TableView<Order> table, ChoiceBox<String> statusChoice) {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String status = statusChoice.getSelectionModel().getSelectedItem();
        orderDAO.updateOrderStatus(selected.getId(), status);
        refreshOrders(table);
    }
    
    private BigDecimal sumOrders(List<Order> orders) {
        BigDecimal total = BigDecimal.ZERO;
        for (Order o : orders) {
            if (o.getTotalAmount() != null) {
                total = total.add(o.getTotalAmount());
            }
        }
        return total;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
    
    private Tab createOrdersTab() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("admin-table-panel");
        
        // Header với title và search
        HBox header = new HBox(15);
        Label title = new Label("🛍️ Quản lý Đơn hàng");
        title.getStyleClass().add("admin-section-title");
        TextField searchField = new TextField();
        searchField.setPromptText("Tìm theo mã đơn, user ID, trạng thái...");
        searchField.getStyleClass().add("admin-search-field");
        HBox.setHgrow(title, Priority.ALWAYS);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        header.getChildren().addAll(title, searchField);
        
        // Action buttons + trạng thái
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Đã nhận", "Đang giao", "Hoàn thành");
        statusCombo.setValue("Đã nhận");
        statusCombo.setPrefWidth(150);
        
        Button approveBtn = new Button("Cập nhật");
        approveBtn.getStyleClass().addAll("admin-btn", "admin-btn-approve");
        Button cancelBtn = new Button("Hủy đơn");
        cancelBtn.getStyleClass().addAll("admin-btn", "admin-btn-cancel");
        Button reloadBtn = new Button("Tải lại");
        reloadBtn.getStyleClass().addAll("admin-btn", "admin-btn-reload");
        HBox buttons = new HBox(10, statusCombo, approveBtn, cancelBtn, reloadBtn);
        
        // Table
        TableView<Order> table = new TableView<>();
        table.getStyleClass().add("admin-data-table");
        
        TableColumn<Order, Integer> idCol = new TableColumn<>("Mã ĐH");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Order, Integer> userIdCol = new TableColumn<>("User ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        TableColumn<Order, String> totalCol = new TableColumn<>("Tổng tiền (VND)");
        totalCol.setCellValueFactory(c -> new SimpleStringProperty(formatCurrency(c.getValue().getTotalAmount())));
        TableColumn<Order, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(c -> {
            String status = c.getValue().getStatus();
            return new SimpleStringProperty(status != null ? status : "");
        });
        TableColumn<Order, String> createdCol = new TableColumn<>("Ngày đặt");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        createdCol.setCellValueFactory(c -> c.getValue().getCreatedAt() == null ? new SimpleStringProperty("") :
            new SimpleStringProperty(c.getValue().getCreatedAt().format(fmt)));
        TableColumn<Order, String> paymentCol = new TableColumn<>("Phương thức");
        paymentCol.setCellValueFactory(c -> new SimpleStringProperty("Thanh toán khi nhận hàng"));
        
        table.getColumns().addAll(idCol, userIdCol, totalCol, statusCol, createdCol, paymentCol);
        refreshOrders(table);
        
        approveBtn.setOnAction(e -> {
            Order selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String status = statusCombo.getValue();
                // Map hiển thị sang status trong DB
                switch (status) {
                    case "Đã nhận":
                        orderDAO.updateOrderStatus(selected.getId(), "Đã nhận");
                        break;
                    case "Đang giao":
                        orderDAO.updateOrderStatus(selected.getId(), "Đang giao");
                        break;
                    case "Hoàn thành":
                        orderDAO.updateOrderStatus(selected.getId(), "Hoàn thành");
                        break;
                }
                refreshOrders(table);
            }
        });
        
        cancelBtn.setOnAction(e -> {
            Order selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                orderDAO.updateOrderStatus(selected.getId(), "Đã hủy");
                refreshOrders(table);
            }
        });
        
        reloadBtn.setOnAction(e -> refreshOrders(table));
        
        panel.getChildren().addAll(header, buttons, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        Tab tab = new Tab("Đơn hàng", panel);
        tab.setClosable(false);
        return tab;
    }
    
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f ₫", amount.doubleValue());
    }
    
    private String formatDiscount(Voucher voucher) {
        if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
            return voucher.getDiscountValue() != null ? voucher.getDiscountValue().stripTrailingZeros().toPlainString() + " %" : "";
        }
        return formatCurrency(voucher.getDiscountValue());
    }
    
    private String removeVietnameseAccents(String str) {
        str = str.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        str = str.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        str = str.replaceAll("[ìíịỉĩ]", "i");
        str = str.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        str = str.replaceAll("[ùúụủũưừứựửữ]", "u");
        str = str.replaceAll("[ỳýỵỷỹ]", "y");
        str = str.replaceAll("[đ]", "d");
        str = str.replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A");
        str = str.replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E");
        str = str.replaceAll("[ÌÍỊỈĨ]", "I");
        str = str.replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O");
        str = str.replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U");
        str = str.replaceAll("[ỲÝỴỶỸ]", "Y");
        str = str.replaceAll("[Đ]", "D");
        return str;
    }
}

