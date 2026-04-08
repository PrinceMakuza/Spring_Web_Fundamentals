package com.ecommerce;

import com.ecommerce.controller.AdminController;
import com.ecommerce.controller.ProductController;
import com.ecommerce.dao.DatabaseConnection;
import com.ecommerce.util.PerformanceMonitor;
import com.ecommerce.util.ReportGenerator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * MainApp is the entry point for the Smart E-Commerce System.
 * Implements a professional dark-mode UI with sidebar navigation.
 *
 * Layout:
 * - Left sidebar (250px) with navigation buttons
 * - Main content area that swaps views based on selection
 *
 * Role-Based Views:
 * - Regular user sees: Browse Products, Search
 * - Admin user sees: Product Management, Category Management, Performance Reports
 * Both roles share the same underlying CRUD and service layer.
 */
public class MainApp extends Application {

    private StackPane contentArea;
    private ProductController productController;
    private AdminController adminController;
    private VBox performanceView;
    private VBox reportsView;

    // Track active sidebar button for styling
    private Button activeButton = null;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart E-Commerce System");

        // Main layout: sidebar + content
        BorderPane mainLayout = new BorderPane();

        // Sidebar
        VBox sidebar = createSidebar();

        // Content area wrapped in ScrollPane for access on smaller screens
        contentArea = new StackPane();
        contentArea.getStyleClass().add("main-content");
        
        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("content-scroll-pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        mainLayout.setLeft(sidebar);
        mainLayout.setCenter(scrollPane);

        // Create views (lazy-initialized on first click)
        productController = new ProductController();
        adminController = new AdminController();
        performanceView = createPerformanceView();
        reportsView = createReportsView();

        // Wire admin changes to refresh product browser
        adminController.setOnDataChanged(() -> productController.refreshData());

        // Default view
        showView(productController);

        Scene scene = new Scene(mainLayout, 1050, 700);

        // Apply dark mode CSS
        String cssPath = getClass().getResource("/css/styles.css") != null
            ? getClass().getResource("/css/styles.css").toExternalForm()
            : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }

    /**
     * Creates the left sidebar navigation panel.
bar navigation panel.
     * Contains navigation buttons grouped by role (User / Admin).
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(250);
        sidebar.setMinWidth(250);
        sidebar.setMaxWidth(250);

        // Header with app branding
        VBox header = new VBox(4);
        header.getStyleClass().add("sidebar-header");
        Label appTitle = new Label("Smart E-Commerce");
        appTitle.getStyleClass().add("sidebar-title");
        Label appSubtitle = new Label("Management System v1.0");
        appSubtitle.getStyleClass().add("sidebar-subtitle");
        header.getChildren().addAll(appTitle, appSubtitle);

        // User section
        Label userSection = new Label("USER");
        userSection.getStyleClass().add("sidebar-section-label");

        Button browseBtn = createSidebarButton("📦  Browse Products");
        browseBtn.setOnAction(e -> {
            setActiveButton(browseBtn);
            showView(productController);
        });

        // Admin section
        Label adminSection = new Label("ADMIN");
        adminSection.getStyleClass().add("sidebar-section-label");

        Button adminBtn = createSidebarButton("⚙  Admin Panel");
        adminBtn.setOnAction(e -> {
            setActiveButton(adminBtn);
            showView(adminController);
        });

        Button perfBtn = createSidebarButton("📊  Performance Reports");
        perfBtn.setOnAction(e -> {
            setActiveButton(perfBtn);
            refreshPerformanceView();
            showView(performanceView);
        });

        Button reportsBtn = createSidebarButton("📋  Generate Reports");
        reportsBtn.setOnAction(e -> {
            setActiveButton(reportsBtn);
            showView(reportsView);
        });

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer with version info
        VBox footer = new VBox(4);
        footer.setPadding(new Insets(15));
        Label version = new Label("Java 21 + PostgreSQL");
        version.getStyleClass().add("label-muted");
        version.setStyle("-fx-font-size: 11px;");
        Label copyright = new Label("© 2026 E-Commerce");
        copyright.getStyleClass().add("label-muted");
        copyright.setStyle("-fx-font-size: 10px;");
        footer.getChildren().addAll(version, copyright);

        // Set initial active button
        setActiveButton(browseBtn);

        sidebar.getChildren().addAll(
            header,
            userSection,
            browseBtn,
            createDivider(),
            adminSection,
            adminBtn,
            perfBtn,
            reportsBtn,
            spacer,
            createDivider(),
            footer
        );

        return sidebar;
    }

    /**
     * Creates a styled sidebar navigation button.
     */
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    /**
     * Sets the active sidebar button styling.
     */
    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-button-active");
        }
        button.getStyleClass().add("sidebar-button-active");
        activeButton = button;
    }

    private Region createDivider() {
        Region divider = new Region();
        divider.getStyleClass().add("sidebar-divider");
        return divider;
    }

    /**
     * Switches the main content area to display the specified view.
     */
    private void showView(javafx.scene.Node view) {
        contentArea.getChildren().setAll(view);
    }

    /**
     * Creates the Performance Reports view showing query timing data.
     */
    private VBox createPerformanceView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(0));

        VBox header = new VBox(5);
        Label title = new Label("📊  Performance Reports");
        title.getStyleClass().add("content-title");
        Label subtitle = new Label("Query optimization analysis — indexes + caching");
        subtitle.getStyleClass().add("content-subtitle");
        header.getChildren().addAll(title, subtitle);

        view.getChildren().addAll(header);
        view.setId("performanceView");

        return view;
    }

    /**
     * Refreshes the performance view with current timing data.
     */
    private void refreshPerformanceView() {
        VBox view = performanceView;

        // Remove all children after the header
        while (view.getChildren().size() > 1) {
            view.getChildren().remove(1);
        }

        // Stats cards row
        HBox statsRow = new HBox(15);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        var timings = PerformanceMonitor.getAllTimings();

        // Show the current timing stats
        if (timings.isEmpty()) {
            VBox emptyCard = new VBox(10);
            emptyCard.getStyleClass().add("card");
            Label emptyLabel = new Label("No performance data yet. Browse/search products to generate timing data.");
            emptyLabel.getStyleClass().add("label-muted");
            emptyLabel.setWrapText(true);

            Button runTestBtn = new Button("🔬  Run Performance Test");
            runTestBtn.getStyleClass().add("button-primary");
            runTestBtn.setOnAction(e -> {
                try {
                    // Run test queries to populate timing data
                    for (int i = 0; i < 5; i++) {
                        long start = System.nanoTime();
                        productController.refreshData();
                        long elapsed = (System.nanoTime() - start) / 1_000_000;
                        PerformanceMonitor.record("Browse All Products", elapsed);
                    }
                    refreshPerformanceView();
                } catch (Exception ex) {
                    // ignore
                }
            });

            emptyCard.getChildren().addAll(emptyLabel, runTestBtn);
            view.getChildren().add(emptyCard);
        } else {
            for (var entry : timings.entrySet()) {
                VBox card = new VBox(6);
                card.getStyleClass().add("stat-card");
                card.setPrefWidth(250);

                Label queryName = new Label(entry.getKey());
                queryName.getStyleClass().add("label-bright");
                queryName.setStyle("-fx-font-weight: bold;");

                double avg = entry.getValue().stream().mapToLong(Long::longValue).average().orElse(0);
                Label avgLabel = new Label(String.format("%.1f ms", avg));
                avgLabel.getStyleClass().add("stat-value");

                Label countLabel = new Label(entry.getValue().size() + " executions");
                countLabel.getStyleClass().add("stat-label");

                card.getChildren().addAll(queryName, avgLabel, countLabel);
                statsRow.getChildren().add(card);
            }
            view.getChildren().add(statsRow);
        }

        // Summary text area showing PerformanceMonitor output
        VBox summaryCard = new VBox(10);
        summaryCard.getStyleClass().add("card");

        Label summaryTitle = new Label("Detailed Timing Log");
        summaryTitle.getStyleClass().add("label-bright");
        summaryTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea summaryArea = new TextArea(PerformanceMonitor.generateSummary());
        summaryArea.setEditable(false);
        summaryArea.setPrefRowCount(12);
        summaryArea.setStyle("-fx-control-inner-background: #1a1a30; -fx-text-fill: #c0c8e0; -fx-font-family: 'Consolas', monospace;");

        summaryCard.getChildren().addAll(summaryTitle, summaryArea);
        view.getChildren().add(summaryCard);
    }

    /**
     * Creates the Reports generation view for validation_report.txt and performance_report.txt.
     */
    private VBox createReportsView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(0));

        VBox header = new VBox(5);
        Label title = new Label("📋  Generate Reports");
        title.getStyleClass().add("content-title");
        Label subtitle = new Label("Auto-generate validation and performance reports");
        subtitle.getStyleClass().add("content-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Report generation cards
        HBox cardsRow = new HBox(20);

        // Validation Report Card
        VBox valCard = new VBox(12);
        valCard.getStyleClass().add("card");
        valCard.setPrefWidth(400);

        Label valTitle = new Label("Validation Report");
        valTitle.getStyleClass().add("label-bright");
        valTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label valDesc = new Label("Tests CRUD operations, search, pagination,\ncache behavior, and database constraints.");
        valDesc.getStyleClass().add("label-muted");
        valDesc.setWrapText(true);

        Label valStatus = new Label("");
        valStatus.getStyleClass().add("label-muted");

        Button valBtn = new Button("🧪  Generate validation_report.txt");
        valBtn.getStyleClass().add("button-success");
        valBtn.setOnAction(e -> {
            valStatus.setText("Running tests...");
            try {
                ReportGenerator gen = new ReportGenerator();
                gen.generateValidationReport("validation_report.txt");
                valStatus.setText("✓ Report generated: validation_report.txt");
                valStatus.setStyle("-fx-text-fill: #38b86c;");
            } catch (Exception ex) {
                valStatus.setText("✗ Error: " + ex.getMessage());
                valStatus.setStyle("-fx-text-fill: #e05555;");
            }
        });

        valCard.getChildren().addAll(valTitle, valDesc, valBtn, valStatus);

        // Performance Report Card
        VBox perfCard = new VBox(12);
        perfCard.getStyleClass().add("card");
        perfCard.setPrefWidth(400);

        Label perfTitle = new Label("Performance Report");
        perfTitle.getStyleClass().add("label-bright");
        perfTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label perfDesc = new Label("Measures query times before/after optimization\n(indexes + caching). Runs 5 iterations each.");
        perfDesc.getStyleClass().add("label-muted");
        perfDesc.setWrapText(true);

        Label perfStatus = new Label("");
        perfStatus.getStyleClass().add("label-muted");

        Button perfBtn = new Button("⚡  Generate performance_report.txt");
        perfBtn.getStyleClass().add("button-primary");
        perfBtn.setOnAction(e -> {
            perfStatus.setText("Benchmarking queries...");
            try {
                ReportGenerator gen = new ReportGenerator();
                gen.generatePerformanceReport("performance_report.txt");
                perfStatus.setText("✓ Report generated: performance_report.txt");
                perfStatus.setStyle("-fx-text-fill: #38b86c;");
            } catch (Exception ex) {
                perfStatus.setText("✗ Error: " + ex.getMessage());
                perfStatus.setStyle("-fx-text-fill: #e05555;");
            }
        });

        perfCard.getChildren().addAll(perfTitle, perfDesc, perfBtn, perfStatus);

        cardsRow.getChildren().addAll(valCard, perfCard);

        view.getChildren().addAll(header, cardsRow);
        return view;
    }

    @Override
    public void stop() {
        // Shut down the connection pool when application exits
        DatabaseConnection.closePool();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
