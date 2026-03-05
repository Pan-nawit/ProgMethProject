package GUI;

import GameLogic.GameLogic;
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

    public static int selectedStage = 1; // set by MainMenu stage card

    private GameLogic gameLogic;
    private boolean w, a, s, d, isMousePressed;
    private AnimationTimer gameLoop;
    private Stage primaryStage;

    // Stage durations in seconds
    public static final int[] STAGE_DURATIONS = {30, 60, 90};

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

    private void startGame() {
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
                        showMainMenu(); // back to menu on R after death
                    } else if (gameLogic.isWon) {
                        if (selectedStage < 3) {
                            selectedStage++;
                            startGame(); // advance to next stage
                        } else {
                            showMainMenu(); // finished all stages
                        }
                    }
                }
                case ESCAPE -> showMainMenu();
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

        scene.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown()) isMousePressed = true;
        });
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

    private void drawGame(GraphicsContext gc, GameLogic logic) {
        // Background
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillRect(0, 0, 800, 600);

        // Grid lines (subtle)
        gc.setStroke(Color.web("#222222"));
        gc.setLineWidth(1);
        for (int i = 0; i < 800; i += 40) gc.strokeLine(i, 0, i, 600);
        for (int j = 0; j < 600; j += 40) gc.strokeLine(0, j, 800, j);

        // Player
        gc.setFill(Color.web("#3498db"));
        gc.fillRect(logic.player.getX(), logic.player.getY(), logic.player.getWidth(), logic.player.getHeight());
        gc.setStroke(Color.web("#5dade2"));
        gc.setLineWidth(2);
        gc.strokeRect(logic.player.getX(), logic.player.getY(), logic.player.getWidth(), logic.player.getHeight());

        // Enemies
        for (var enemy : logic.enemies) {
            gc.setFill(Color.web("#c0392b"));
            gc.fillRect(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());

            // Health bar
            double hpPercent = (double) enemy.getHP() / Math.max(enemy.getMaxHp(), 1);
            gc.setFill(Color.web("#1a1a1a"));
            gc.fillRect(enemy.getX(), enemy.getY() - 8, enemy.getWidth(), 5);
            gc.setFill(hpPercent > 0.5 ? Color.web("#2ecc71") : Color.web("#e74c3c"));
            gc.fillRect(enemy.getX(), enemy.getY() - 8, (int)(enemy.getWidth() * hpPercent), 5);
        }

        // Bullets
        gc.setFill(Color.YELLOW);
        for (var b : logic.bullets) {
            gc.fillOval(b.getX() - 3, b.getY() - 3, 6, 6);
        }

        // ── HUD ──────────────────────────────────────────────
        int stageDuration = STAGE_DURATIONS[selectedStage - 1];
        int elapsed = (int)(logic.getElapsedSeconds());
        int remaining = Math.max(0, stageDuration - elapsed);

        // Top bar background
        gc.setFill(Color.web("#0d0d0d", 0.85));
        gc.fillRect(0, 0, 800, 50);

        // Stage label
        gc.setFill(Color.web("#e94560"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("STAGE " + selectedStage, 15, 22);
        gc.setFill(Color.web("#888888"));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText(getDifficultyName(), 15, 38);

        // Timer (center)
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        gc.setTextAlign(TextAlignment.CENTER);
        Color timerColor = remaining <= 10 ? Color.web("#e74c3c") : Color.WHITE;
        gc.setFill(timerColor);
        gc.fillText(formatTime(remaining), 400, 34);
        gc.setTextAlign(TextAlignment.LEFT);

        // Timer bar
        double timerPercent = (double) remaining / stageDuration;
        gc.setFill(Color.web("#333333"));
        gc.fillRect(0, 48, 800, 4);
        gc.setFill(remaining <= 10 ? Color.web("#e74c3c") : Color.web("#2ecc71"));
        gc.fillRect(0, 48, (int)(800 * timerPercent), 4);

        // HP bar
        gc.setFill(Color.web("#333333"));
        gc.fillRect(620, 15, 160, 14);
        double hpPercent = (double) logic.player.getHp() / 5.0;
        gc.setFill(Color.web("#e74c3c"));
        gc.fillRect(620, 15, (int)(160 * hpPercent), 14);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("HP  " + logic.player.getHp() + " / 5", 700, 26);
        gc.setTextAlign(TextAlignment.LEFT);

        // Score
        gc.setFill(Color.web("#f39c12"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("Score: " + logic.score, 680, 42);

        // Status effects
        int statusY = 65;
        for (var status : logic.player.getStatusList()) {
            gc.setFill(Color.web("#e74c3c", 0.85));
            gc.fillRoundRect(10, statusY, 90, 20, 6, 6);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 11));
            gc.fillText("⚠ " + status.getName(), 15, statusY + 14);
            statusY += 24;
        }

        // Wave indicator (bottom left)
        gc.setFill(Color.web("#888888"));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("Wave " + logic.wave, 15, 590);

        // ── OVERLAY SCREENS ─────────────────────────────────
        if (logic.isGameOver) {
            drawOverlay(gc, "YOU DIED", "#e74c3c",
                    "Score: " + logic.score,
                    "Press R → Main Menu  |  ESC → Main Menu");
        } else if (logic.isWon) {
            String nextMsg = selectedStage < 3
                    ? "Press R → Stage " + (selectedStage + 1)
                    : "Press R → Main Menu";
            drawOverlay(gc, "YOU SURVIVED!", "#2ecc71",
                    "Stage " + selectedStage + " Clear  •  Score: " + logic.score,
                    nextMsg + "  |  ESC → Main Menu");
        }
    }

    private void drawOverlay(GraphicsContext gc, String headline, String headlineColor,
                             String sub, String hint) {
        gc.setFill(Color.web("#000000", 0.72));
        gc.fillRect(0, 0, 800, 600);

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.web(headlineColor));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 56));
        gc.fillText(headline, 400, 260);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 22));
        gc.fillText(sub, 400, 310);

        gc.setFill(Color.web("#aaaaaa"));
        gc.setFont(Font.font("Arial", 15));
        gc.fillText(hint, 400, 360);

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private String formatTime(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    private String getDifficultyName() {
        return switch (selectedStage) {
            case 1 -> "Easy  —  Survive 30s";
            case 2 -> "Medium  —  Survive 60s";
            case 3 -> "Hard  —  Survive 90s";
            default -> "";
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}