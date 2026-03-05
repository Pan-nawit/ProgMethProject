package GUI;

import GameLogic.GameLogic;
import Item.Item;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class MainGame extends Application {

    public static int selectedStage = 1;
    public static int unlockedStages = 1; // only stage 1 unlocked at start

    private GameLogic gameLogic;
    private boolean w, a, s, d, isMousePressed;
    private AnimationTimer gameLoop;
    private Stage primaryStage;

    public static final int[] STAGE_DURATIONS = {30, 60, 90};
    public static final int HUD_HEIGHT = 52;
    public static final int ITEM_BAR_HEIGHT = 48; // bottom item bar
    // play area: y=52 to y=552 (500px tall), logical coords 0..500

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Zombie Survival");
        primaryStage.setResizable(false);
        showMainMenu();
        primaryStage.show();
    }

    private void showMainMenu() {
        if (gameLoop != null) gameLoop.stop();
        MainMenu menu = new MainMenu(primaryStage, this::startGame);
        primaryStage.setScene(menu.buildScene());
    }

    public void startGame() {
        gameLogic = new GameLogic();
        gameLogic.initGame(selectedStage);

        Canvas canvas = new Canvas(800, 600);
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W -> w = true;
                case A -> a = true;
                case S -> s = true;
                case D -> d = true;
                case R -> {
                    if (gameLogic.isGameOver) {
                        // R = restart same stage
                        startGame();
                    } else if (gameLogic.isWon) {
                        if (selectedStage == 3) {
                            // finished all — go to credits
                            showCredits();
                        } else {
                            // advance + unlock next
                            if (selectedStage + 1 > unlockedStages) unlockedStages = selectedStage + 1;
                            selectedStage++;
                            startGame();
                        }
                    }
                }
                case ESCAPE -> showMainMenu();
                case Q -> gameLogic.player.selectPrevItem();
                case E -> gameLogic.player.selectNextItem();
                case F -> gameLogic.player.useSelectedConsumable();
            }
        });
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W -> w = false;
                case A -> a = false;
                case S -> s = false;
                case D -> d = false;
            }
        });
        scene.setOnMousePressed(e -> { if (e.isPrimaryButtonDown()) isMousePressed = true; });
        scene.setOnMouseReleased(e -> isMousePressed = false);

        primaryStage.setScene(scene);

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gameLogic.update(w, a, s, d, isMousePressed);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.clearRect(0, 0, 800, 600);
                drawGame(gc, gameLogic);
            }
        };
        gameLoop.start();
    }

    private void showCredits() {
        if (gameLoop != null) gameLoop.stop();
        Canvas canvas = new Canvas(800, 600);
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw credits screen
        gc.setFill(Color.web("#0d0d0d"));
        gc.fillRect(0, 0, 800, 600);

        // Biohazard symbol area
        gc.setFill(Color.web("#2ecc71", 0.15));
        gc.fillOval(250, 60, 300, 300);

        gc.setFill(Color.web("#2ecc71"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("☣", 400, 230);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        gc.setFill(Color.web("#2ecc71"));
        gc.fillText("CONGRATULATIONS!", 400, 310);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setFill(Color.WHITE);
        gc.fillText("You survived all 3 stages.", 400, 355);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setFill(Color.web("#e94560"));
        gc.fillText("You don't get infected.", 400, 385);

        gc.setFill(Color.web("#555555"));
        gc.setFont(Font.font("Arial", 13));
        gc.fillText("─────────────────────────────────", 400, 415);

        gc.setFill(Color.web("#aaaaaa"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("CREDITS", 400, 440);

        String[][] credits = {
                {"Game Design & Logic", "@pun"},
                {"Player & Items", "@Z3TSUNA"},
                {"Enemies", "@BNiD"},
        };
        gc.setFont(Font.font("Arial", 13));
        int cy = 462;
        for (String[] pair : credits) {
            gc.setFill(Color.web("#f39c12"));
            gc.fillText(pair[0] + "  ", 400 - 10, cy);
            gc.setFill(Color.WHITE);
            cy += 22;
        }
        // role labels on right
        cy = 462;
        for (String[] pair : credits) {
            gc.setFill(Color.WHITE);
            gc.fillText("  " + pair[1], 400 + 10, cy);
            cy += 22;
        }

        gc.setFill(Color.web("#555555"));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("Press ESC to return to Main Menu", 400, 580);
        gc.setTextAlign(TextAlignment.LEFT);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                unlockedStages = 1; // reset unlock for new playthrough
                selectedStage = 1;
                showMainMenu();
            }
        });

        primaryStage.setScene(scene);
    }

    private void drawGame(GraphicsContext gc, GameLogic logic) {
        // Background
        gc.setFill(Color.web("#111111"));
        gc.fillRect(0, 0, 800, 600);

        // Play area
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillRect(0, HUD_HEIGHT, 800, 600 - HUD_HEIGHT - ITEM_BAR_HEIGHT);

        // Grid
        gc.setStroke(Color.web("#222222"));
        gc.setLineWidth(1);
        for (int i = 0; i < 800; i += 40) gc.strokeLine(i, HUD_HEIGHT, i, 600 - ITEM_BAR_HEIGHT);
        for (int j = HUD_HEIGHT; j < 600 - ITEM_BAR_HEIGHT; j += 40) gc.strokeLine(0, j, 800, j);

        // Player
        gc.setFill(Color.web("#3498db"));
        gc.fillRect(logic.player.getX(), logic.player.getY() + HUD_HEIGHT,
                logic.player.getWidth(), logic.player.getHeight());
        gc.setStroke(Color.web("#5dade2"));
        gc.setLineWidth(2);
        gc.strokeRect(logic.player.getX(), logic.player.getY() + HUD_HEIGHT,
                logic.player.getWidth(), logic.player.getHeight());

        // Enemies
        for (var enemy : logic.enemies) {
            int ey = enemy.getY() + HUD_HEIGHT;
            gc.setFill(enemy.getEnemyColor());
            gc.fillRect(enemy.getX(), ey, enemy.getWidth(), enemy.getHeight());
            // hp bar
            double hp = (double) enemy.getHP() / Math.max(enemy.getMaxHp(), 1);
            gc.setFill(Color.web("#1a1a1a"));
            gc.fillRect(enemy.getX(), ey - 8, enemy.getWidth(), 5);
            gc.setFill(hp > 0.5 ? Color.web("#2ecc71") : Color.web("#e74c3c"));
            gc.fillRect(enemy.getX(), ey - 8, (int)(enemy.getWidth() * hp), 5);
        }

        // Bullets
        gc.setFill(Color.YELLOW);
        for (var b : logic.bullets) {
            gc.fillOval(b.getX() - 3, b.getY() + HUD_HEIGHT - 3, 6, 6);
        }

        // Items on ground
        for (var item : logic.itemsOnGround) {
            gc.setFill(Color.web("#f39c12"));
            gc.fillRoundRect(item.getX(), item.getY() + HUD_HEIGHT, 16, 16, 4, 4);
            gc.setFill(Color.web("#0d0d0d"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
            gc.fillText(item.getName().substring(0, Math.min(3, item.getName().length())),
                    item.getX() + 1, item.getY() + HUD_HEIGHT + 11);
        }

        // ── HUD TOP ──────────────────────────────────
        int stageDuration = STAGE_DURATIONS[selectedStage - 1];
        int remaining = Math.max(0, stageDuration - (int) logic.getElapsedSeconds());

        gc.setFill(Color.web("#0d0d0d", 0.9));
        gc.fillRect(0, 0, 800, 50);

        // Stage
        gc.setFill(Color.web("#e94560"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("STAGE " + selectedStage, 15, 22);
        gc.setFill(Color.web("#888888"));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText(getDifficultyName(), 15, 38);

        // Timer
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(remaining <= 10 ? Color.web("#e74c3c") : Color.WHITE);
        gc.fillText(formatTime(remaining), 400, 34);
        gc.setTextAlign(TextAlignment.LEFT);

        // Timer bar
        double timerPct = (double) remaining / stageDuration;
        gc.setFill(Color.web("#333333"));
        gc.fillRect(0, 48, 800, 4);
        gc.setFill(remaining <= 10 ? Color.web("#e74c3c") : Color.web("#2ecc71"));
        gc.fillRect(0, 48, (int)(800 * timerPct), 4);

        // HP bar (right)
        gc.setFill(Color.web("#333333"));
        gc.fillRect(620, 10, 160, 14);
        double hpPct = (double) logic.player.getHp() / 5.0;
        gc.setFill(hpPct > 0.4 ? Color.web("#e74c3c") : Color.web("#c0392b"));
        gc.fillRect(620, 10, (int)(160 * hpPct), 14);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("HP  " + logic.player.getHp() + " / 5", 700, 21);
        gc.setTextAlign(TextAlignment.LEFT);

        // Score / Wave
        gc.setFill(Color.web("#f39c12"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("Score: " + logic.score, 630, 38);
        gc.setFill(Color.web("#888888"));
        gc.setFont(Font.font("Arial", 11));
        gc.fillText("Wave " + logic.wave, 740, 38);

        // Status effects (below HUD, top-left)
        int statusY = HUD_HEIGHT + 14;
        for (var status : logic.player.getStatusList()) {
            gc.setFill(Color.web("#e74c3c", 0.85));
            gc.fillRoundRect(8, statusY - 12, 100, 16, 5, 5);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.fillText("⚠ " + status.getName(), 12, statusY);
            statusY += 20;
        }

        // ── ITEM BAR (bottom) ────────────────────────
        drawItemBar(gc, logic);

        // ── OVERLAYS ────────────────────────────────
        if (logic.isGameOver) {
            drawOverlay(gc, "YOU DIED", "#e74c3c",
                    "Score: " + logic.score,
                    "Press R  Restart   |   ESC  Main Menu");
        } else if (logic.isWon) {
            boolean lastStage = selectedStage == 3;
            String action = lastStage ? "Press R  Credits" : "Press R  Next Stage";
            drawOverlay(gc, "YOU SURVIVED!", "#2ecc71",
                    "Stage " + selectedStage + " Clear  •  Score: " + logic.score,
                    action + "   |   ESC  Main Menu");
        }
    }

    private void drawItemBar(GraphicsContext gc, GameLogic logic) {
        int barY = 600 - ITEM_BAR_HEIGHT;
        gc.setFill(Color.web("#0d0d0d", 0.95));
        gc.fillRect(0, barY, 800, ITEM_BAR_HEIGHT);
        gc.setStroke(Color.web("#333333"));
        gc.setLineWidth(1);
        gc.strokeLine(0, barY, 800, barY);

        var inventory = logic.player.getInventory();
        int selected = logic.player.getSelectedItemIndex();
        int slotSize = 38;
        int startX = 10;

        // Controls hint
        gc.setFill(Color.web("#555555"));
        gc.setFont(Font.font("Arial", 10));
        gc.fillText("Q/E: Switch   F: Use item", 10, barY + 44);

        // Draw up to 6 slots
        for (int i = 0; i < 6; i++) {
            int sx = startX + i * (slotSize + 4);
            int sy = barY + 5;
            boolean isSelected = (i == selected);

            // Slot bg
            gc.setFill(isSelected ? Color.web("#1a4a8a") : Color.web("#1a1a2e"));
            gc.fillRoundRect(sx, sy, slotSize, slotSize, 6, 6);
            gc.setStroke(isSelected ? Color.web("#3498db") : Color.web("#333355"));
            gc.setLineWidth(isSelected ? 2 : 1);
            gc.strokeRoundRect(sx, sy, slotSize, slotSize, 6, 6);

            // Slot number
            gc.setFill(Color.web("#444466"));
            gc.setFont(Font.font("Arial", 9));
            gc.fillText(String.valueOf(i + 1), sx + 3, sy + 10);

            if (i < inventory.size()) {
                Item item = inventory.get(i);
                // Item color
                gc.setFill(getItemColor(item.getName()));
                gc.fillRoundRect(sx + 6, sy + 10, 26, 20, 4, 4);
                // Item label
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(item.getName().substring(0, Math.min(4, item.getName().length())),
                        sx + slotSize / 2.0, sy + 24);
                // Amount
                gc.setFill(Color.web("#f39c12"));
                gc.setFont(Font.font("Arial", 9));
                gc.fillText("x" + item.getAmount(), sx + slotSize / 2.0, sy + 34);
                gc.setTextAlign(TextAlignment.LEFT);
            }
        }
    }

    private Color getItemColor(String name) {
        return switch (name.toLowerCase()) {
            case "pistol" -> Color.web("#7f8c8d");
            case "machinegun" -> Color.web("#e67e22");
            case "medkit" -> Color.web("#e74c3c");
            case "bandage" -> Color.web("#ecf0f1");
            default -> Color.web("#27ae60");
        };
    }

    private void drawOverlay(GraphicsContext gc, String headline, String color, String sub, String hint) {
        gc.setFill(Color.web("#000000", 0.75));
        gc.fillRect(0, 0, 800, 600);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web(color));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 56));
        gc.fillText(headline, 400, 260);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 22));
        gc.fillText(sub, 400, 310);
        gc.setFill(Color.web("#aaaaaa"));
        gc.setFont(Font.font("Arial", 15));
        gc.fillText(hint, 400, 355);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private String formatTime(int s) {
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private String getDifficultyName() {
        return switch (selectedStage) {
            case 1 -> "Easy  —  Survive 30s";
            case 2 -> "Medium  —  Survive 60s";
            case 3 -> "Hard  —  Survive 90s";
            default -> "";
        };
    }

    public static void main(String[] args) { launch(args); }
}