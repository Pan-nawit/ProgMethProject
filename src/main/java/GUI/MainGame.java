package GUI;

import GameLogic.GameLogic;
import Item.Item;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class MainGame extends Application {

    public static int selectedStage  = 1;
    public static int unlockedStages = 1;

    private GameLogic gameLogic;
    private boolean w, a, s, d, isMousePressed;
    private double mouseScreenX = 400, mouseScreenY = 300;
    private AnimationTimer gameLoop;
    private Stage primaryStage;

    // ── Layout constants ──────────────────────────────
    public static final int W            = 800;
    public static final int H            = 600;
    public static final int HUD_HEIGHT   = 56;   // top HUD bar
    public static final int IBAR_HEIGHT  = 52;   // bottom item bar
    public static final int PLAY_H       = H - HUD_HEIGHT - IBAR_HEIGHT; // 492
    public static final int[] STAGE_DURATIONS = {30, 60, 90};

    // ── Palette ──────────────────────────────────────
    private static final String C_BG       = "#1b1a17";
    private static final String C_TILE_A   = "#1f1e1b";
    private static final String C_TILE_B   = "#222018";
    private static final String C_HUD_BG   = "#0e0d0b";
    private static final String C_RED      = "#c0392b";
    private static final String C_RED_LT   = "#e74c3c";
    private static final String C_GREEN    = "#27ae60";
    private static final String C_GREEN_LT = "#2ecc71";
    private static final String C_GOLD     = "#f1c40f";
    private static final String C_AMBER    = "#e67e22";
    private static final String C_TEXT     = "#d4cfc4";
    private static final String C_MUTED    = "#6b6560";

    // ── Image & Font ──────────────────────────────────
    private Image playerImage;
    private Font pixelFont;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Zombal Survivie");
        primaryStage.setResizable(false);

        // Load font
        try {
            Font.loadFont(getClass().getResourceAsStream("/PressStart2P.ttf"), 10);
        } catch (Exception e) {
            System.err.println("❌ ไม่สามารถโหลดฟอนต์ PressStart2P.ttf ได้: " + e.getMessage());
        }

        showMainMenu();
        primaryStage.show();

        try {
            playerImage = new Image(getClass().getResourceAsStream("/player.png"));
        } catch (Exception e) {
            System.err.println("❌ ไม่สามารถโหลดภาพ player.png ได้: " + e.getMessage());
        }
    }

    // ── Screens ──────────────────────────────────────

    private void showMainMenu() {
        if (gameLoop != null) gameLoop.stop();
        MainMenu menu = new MainMenu(primaryStage, this::startGame);
        primaryStage.setScene(menu.buildScene());
    }

    public void startGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        gameLogic = new GameLogic();
        gameLogic.initGame(selectedStage);

        Canvas canvas = new Canvas(W, H);
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        scene.setCursor(javafx.scene.Cursor.NONE); // hide OS cursor

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W -> w = true;
                case A -> a = true;
                case S -> s = true;
                case D -> d = true;
                case R -> {
                    // R is ONLY for restarting when dead now
                    if (gameLogic.isGameOver) { startGame(); }
                }
                case ENTER -> {
                    // ENTER is used to proceed when you survive
                    if (gameLogic.isWon) {
                        if (selectedStage == 3) {
                            showCredits();
                        } else {
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
                case W -> w = false; case A -> a = false;
                case S -> s = false; case D -> d = false;
            }
        });
        scene.setOnMousePressed(e  -> { if (e.isPrimaryButtonDown()) isMousePressed = true; });
        scene.setOnMouseReleased(e -> isMousePressed = false);
        scene.setOnMouseMoved(e    -> { mouseScreenX = e.getX(); mouseScreenY = e.getY(); });
        scene.setOnMouseDragged(e  -> { mouseScreenX = e.getX(); mouseScreenY = e.getY(); });

        primaryStage.setScene(scene);

        gameLoop = new AnimationTimer() {
            @Override public void handle(long now) {
                float lmx = (float) mouseScreenX;
                float lmy = (float)(mouseScreenY - HUD_HEIGHT);
                gameLogic.player.setMousePos(lmx, lmy);
                gameLogic.update(w, a, s, d, isMousePressed);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.clearRect(0, 0, W, H);
                drawGame(gc, gameLogic);
            }
        };
        gameLoop.start();
    }

    private void showCredits() {
        if (gameLoop != null) gameLoop.stop();
        Canvas canvas = new Canvas(W, H);
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        drawCredits(gc);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE ||
                    e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                unlockedStages = 1;
                selectedStage  = 1;
                showMainMenu();
            }
        });
        primaryStage.setScene(scene);
    }

    // ── Main draw ─────────────────────────────────────

    private void drawGame(GraphicsContext gc, GameLogic logic) {
        drawArena(gc);
        drawEntities(gc, logic);
        drawHudTop(gc, logic);
        drawItemBar(gc, logic);
        drawVignette(gc);
        drawCrosshair(gc, logic);
        drawOverlays(gc, logic);
    }

    // ── Arena (tiled floor + border) ──────────────────

    private void drawArena(GraphicsContext gc) {
        gc.setFill(Color.web(C_HUD_BG));
        gc.fillRect(0, 0, W, H);

        int tileSize = 40;
        for (int row = 0; row * tileSize < PLAY_H + tileSize; row++) {
            for (int col = 0; col * tileSize < W; col++) {
                boolean odd = (row + col) % 2 == 0;
                gc.setFill(Color.web(odd ? C_TILE_A : C_TILE_B));
                gc.fillRect(col * tileSize,
                        HUD_HEIGHT + row * tileSize,
                        tileSize, tileSize);
            }
        }

        gc.setStroke(Color.web("#2a2925", 0.6));
        gc.setLineWidth(0.5);
        for (int x = 0; x <= W; x += tileSize)
            gc.strokeLine(x, HUD_HEIGHT, x, HUD_HEIGHT + PLAY_H);
        for (int y = 0; y <= PLAY_H; y += tileSize)
            gc.strokeLine(0, HUD_HEIGHT + y, W, HUD_HEIGHT + y);

        gc.setStroke(Color.web(C_RED, 0.25));
        gc.setLineWidth(2);
        gc.strokeRect(1, HUD_HEIGHT + 1, W - 2, PLAY_H - 2);
    }

    // ── Entities ──────────────────────────────────────

    private void drawEntities(GraphicsContext gc, GameLogic logic) {
        // Items on ground
        for (var item : logic.itemsOnGround) {
            int ix = item.getX();
            int iy = item.getY() + HUD_HEIGHT;

            // Outer glow / hit box
            gc.setFill(Color.web(C_GOLD, 0.25));
            gc.fillRoundRect(ix - 3, iy - 3, 26, 26, 6, 6);

            // Colored item block
            gc.setFill(getItemColor(item.getName()));
            gc.fillRoundRect(ix, iy, 20, 20, 4, 4);
            gc.setStroke(Color.web(C_GOLD, 0.8));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(ix, iy, 20, 20, 4, 4);

            // Item text (White for better contrast)
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 6));
            gc.fillText(item.getName().substring(0, Math.min(3, item.getName().length())).toUpperCase(), ix + 2, iy + 13);
        }

        // Bullets
        for (var b : logic.bullets) {
            int bx = b.getX();
            int by = b.getY() + HUD_HEIGHT;
            gc.setFill(Color.web(C_GOLD, 0.3));
            gc.fillOval(bx - 5, by - 5, 10, 10);
            gc.setFill(Color.web(C_GOLD));
            gc.fillOval(bx - 3, by - 3, 6, 6);
        }

        // Enemies
        for (var enemy : logic.enemies) {
            int ex = enemy.getX();
            int ey = enemy.getY() + HUD_HEIGHT;
            int ew = enemy.getWidth();
            int eh = enemy.getHeight();

            gc.setFill(Color.web("#000000", 0.35));
            gc.fillOval(ex + 2, ey + eh - 4, ew - 4, 8);

            gc.setFill(enemy.getEnemyColor());
            gc.fillRoundRect(ex, ey, ew, eh, 4, 4);
            gc.setStroke(Color.web("#000000", 0.6));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(ex, ey, ew, eh, 4, 4);

            double hpPct = (double) enemy.getHP() / Math.max(enemy.getMaxHp(), 1);
            gc.setFill(Color.web("#000000", 0.7));
            gc.fillRoundRect(ex, ey - 9, ew, 5, 2, 2);
            Color barColor = hpPct > 0.6 ? Color.web(C_GREEN_LT)
                    : hpPct > 0.3 ? Color.web(C_AMBER)
                    : Color.web(C_RED_LT);
            gc.setFill(barColor);
            gc.fillRoundRect(ex, ey - 9, (int)(ew * hpPct), 5, 2, 2);
        }

        // Player
        int px = logic.player.getX();
        int py = logic.player.getY() + HUD_HEIGHT;
        int pw = logic.player.getWidth();
        int ph = logic.player.getHeight();

        gc.setFill(Color.web("#000000", 0.4));
        gc.fillOval(px + 3, py + ph - 4, pw - 6, 8);

        if (playerImage != null) {
            gc.drawImage(playerImage, px, py);
        } else {
            gc.setFill(Color.web("#2980b9"));
            gc.fillRoundRect(px, py, pw, ph, 5, 5);
        }

        // Aim line
        if (!logic.isGameOver && !logic.isWon) {
            float lmx = logic.player.getMouseX();
            float lmy = logic.player.getMouseY();
            if (lmx >= 0) {
                gc.setStroke(Color.web("#ffffff", 0.08));
                gc.setLineWidth(1);
                gc.setLineDashes(4, 6);
                gc.strokeLine(px + pw / 2.0, py + ph / 2.0,
                        lmx, lmy + HUD_HEIGHT);
                gc.setLineDashes(null);
            }
        }
    }

    // ── HUD Top ───────────────────────────────────────

    private void drawHudTop(GraphicsContext gc, GameLogic logic) {
        int stageDuration = STAGE_DURATIONS[selectedStage - 1];
        int remaining = Math.max(0, stageDuration - (int) logic.getElapsedSeconds());
        boolean urgent = remaining <= 10;

        gc.setFill(Color.web(C_HUD_BG));
        gc.fillRect(0, 0, W, HUD_HEIGHT);
        gc.setStroke(Color.web(C_RED, 0.5));
        gc.setLineWidth(1.5);
        gc.strokeLine(0, HUD_HEIGHT, W, HUD_HEIGHT);

        // ── Left: HP hearts ──
        int maxHp = 5;
        int curHp = logic.player.getHp();
        gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 10));
        gc.setFill(Color.web(C_MUTED));
        gc.fillText("HP", 14, 20);
        for (int i = 0; i < maxHp; i++) {
            boolean filled = i < curHp;
            drawHeart(gc, 14 + (i * 26), 28, filled);
        }

        // ── Left-ish: Status effects ──
        int statusX = 150;
        for (var status : logic.player.getStatusList()) {
            String label = status.getName().equals("Bleeding") ? "🩸" : "🦵";
            gc.setFill(Color.web(C_RED, 0.8));
            gc.fillRoundRect(statusX, 12, 105, 18, 5, 5); // Widened to 105 to prevent cutoff!
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Press Start 2P", 8)); // Slightly smaller font to guarantee fit
            gc.fillText(label + " " + status.getName(), statusX + 4, 25); // Using full name now!
            statusX += 115;
        }

        // ── Center: Timer ──
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web(C_MUTED));
        gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 10));
        gc.fillText("STAGE " + selectedStage + "  ·  " + getDifficultyLabel(), 400, 18);

        gc.setFill(urgent ? Color.web(C_RED_LT) : Color.web(C_TEXT));
        gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 22));
        gc.fillText(formatTime(remaining), 400, 43);

        double timerPct = (double) remaining / stageDuration;
        int barW = 140;
        gc.setFill(Color.web("#2a2925"));
        gc.fillRoundRect(400 - barW / 2.0, HUD_HEIGHT - 6, barW, 4, 2, 2);
        gc.setFill(urgent ? Color.web(C_RED_LT) : Color.web(C_GREEN_LT));
        gc.fillRoundRect(400 - barW / 2.0, HUD_HEIGHT - 6, (int)(barW * timerPct), 4, 2, 2);

        // ── Right: Score + Wave ──
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(Color.web(C_GOLD));
        gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 18));
        gc.fillText("Score: " + logic.score, W - 20, 26);

        gc.setFill(Color.web(C_MUTED));
        gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 10));
        gc.fillText("WAVE " + logic.wave, W - 20, 44);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // Custom Vector Heart method (Fixes ugly text borders and weird sizing)
    private void drawHeart(GraphicsContext gc, double x, double y, boolean filled) {
        gc.setFill(filled ? Color.web(C_RED_LT) : Color.web("#2c1e1e"));

        // Two top circles
        gc.fillOval(x, y, 12, 12);
        gc.fillOval(x + 10, y, 12, 12);

        // Bottom triangle to complete the heart
        gc.beginPath();
        gc.moveTo(x + 1, y + 8);
        gc.lineTo(x + 21, y + 8);
        gc.lineTo(x + 11, y + 20);
        gc.closePath();
        gc.fill();
    }

    // ── Item bar ──────────────────────────────────────

    private void drawItemBar(GraphicsContext gc, GameLogic logic) {
        int barY = H - IBAR_HEIGHT;
        var inventory = logic.player.getInventory();
        int selected = logic.player.getSelectedItemIndex();

        gc.setFill(Color.web(C_HUD_BG));
        gc.fillRect(0, barY, W, IBAR_HEIGHT);
        gc.setStroke(Color.web(C_RED, 0.4));
        gc.setLineWidth(1.5);
        gc.strokeLine(0, barY, W, barY);

        int slotSize = 44;
        int gap = 10;
        int slotCount = 5;
        int totalW = slotCount * slotSize + (slotCount - 1) * gap;
        int startX = (W - totalW) / 2;

        for (int i = 0; i < slotCount; i++) {
            int sx = startX + i * (slotSize + gap);
            int sy = barY + 4;
            boolean isSel = (i == selected);

            // Slot Background
            gc.setFill(isSel ? Color.web("#2c1f0a") : Color.web("#131210"));
            gc.fillRoundRect(sx, sy, slotSize, slotSize, 6, 6);
            gc.setStroke(isSel ? Color.web(C_GOLD) : Color.web("#3a3830"));
            gc.setLineWidth(isSel ? 2 : 1);
            gc.strokeRoundRect(sx, sy, slotSize, slotSize, 6, 6);

            // Slot number (Top Left)
            gc.setFill(Color.web(isSel ? C_GOLD : C_MUTED, 0.8));
            gc.setFont(Font.font("Press Start 2P", 9));
            gc.fillText(String.valueOf(i + 1), sx + 4, sy + 12);

            // Item Inside Slot
            if (i < inventory.size()) {
                Item item = inventory.get(i);

                // Color Banner in the middle
                gc.setFill(getItemColor(item.getName()));
                gc.fillRoundRect(sx + 4, sy + 16, slotSize - 8, 14, 3, 3);

                // Item Name
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 6));
                gc.setTextAlign(TextAlignment.CENTER);
                String shortName = item.getName().substring(0, Math.min(6, item.getName().length())).toUpperCase();
                gc.fillText(shortName, sx + slotSize / 2.0, sy + 26);

                // Ammo Badge
                gc.setFill(Color.web("#000000", 0.85));
                gc.fillRoundRect(sx + slotSize - 18, sy + slotSize - 14, 16, 12, 3, 3);
                gc.setFill(Color.web(isSel ? C_GOLD : C_TEXT));
                gc.setFont(Font.font("Press Start 2P", 7));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("" + item.getAmount(), sx + slotSize - 10, sy + slotSize - 5);
                gc.setTextAlign(TextAlignment.LEFT);
            }
        }

        // Controls hint
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(Color.web(C_MUTED));
        gc.setFont(Font.font("Press Start 2P", 8));
        gc.fillText("Q/E Switch   F Use", W - 20, barY + 30);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ── Vignette ──────────────────────────────────────

    private void drawVignette(GraphicsContext gc) {
        RadialGradient vignette = new RadialGradient(
                0, 0, 0.5, 0.5, 0.75, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.TRANSPARENT),
                new Stop(1.0, Color.web("#000000", 0.65))
        );
        gc.setFill(vignette);
        gc.fillRect(0, HUD_HEIGHT, W, PLAY_H);
    }

    // ── Crosshair ─────────────────────────────────────

    private void drawCrosshair(GraphicsContext gc, GameLogic logic) {
        if (logic.isGameOver || logic.isWon) return;
        double mx = mouseScreenX;
        double my = mouseScreenY;
        int gap = 4, len = 10;

        gc.setStroke(Color.web("#000000", 0.5));
        gc.setLineWidth(3);
        gc.strokeLine(mx - gap - len, my, mx - gap, my);
        gc.strokeLine(mx + gap, my, mx + gap + len, my);
        gc.strokeLine(mx, my - gap - len, mx, my - gap);
        gc.strokeLine(mx, my + gap, mx, my + gap + len);

        gc.setStroke(Color.web(C_TEXT));
        gc.setLineWidth(1.5);
        gc.strokeLine(mx - gap - len, my, mx - gap, my);
        gc.strokeLine(mx + gap, my, mx + gap + len, my);
        gc.strokeLine(mx, my - gap - len, mx, my - gap);
        gc.strokeLine(mx, my + gap, mx, my + gap + len);

        gc.setStroke(Color.web(C_RED_LT, 0.8));
        gc.setLineWidth(1);
        gc.strokeOval(mx - 3, my - 3, 6, 6);
    }

    // ── Overlays ──────────────────────────────────────

    private void drawOverlays(GraphicsContext gc, GameLogic logic) {
        if (logic.isGameOver) {
            drawOverlayPanel(gc, "YOU DIED", C_RED_LT,
                    "Score  " + logic.score,
                    "[ R ] Restart   ·   [ ESC ] Menu");
        } else if (logic.isWon) {
            String action = selectedStage == 3 ? "[ ENTER ] Credits" : "[ ENTER ] Next Stage";
            drawOverlayPanel(gc, "SURVIVED!", C_GREEN_LT,
                    "Stage " + selectedStage + " Clear   ·   Score  " + logic.score,
                    action + "   ·   [ ESC ] Menu");
        }
    }

    private void drawOverlayPanel(GraphicsContext gc, String title, String titleColor,
                                  String sub, String hint) {
        gc.setFill(Color.web("#000000", 0.78));
        gc.fillRect(0, 0, W, H);

        int pw = 500, ph = 180;
        int px = (W - pw) / 2, py = (H - ph) / 2;
        gc.setFill(Color.web(C_HUD_BG, 0.97));
        gc.fillRoundRect(px, py, pw, ph, 12, 12);
        gc.setStroke(Color.web(titleColor, 0.6));
        gc.setLineWidth(2);
        gc.strokeRoundRect(px, py, pw, ph, 12, 12);

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.web(titleColor));
        gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 36));
        gc.fillText(title, W / 2.0, py + 70);

        gc.setFill(Color.web(C_TEXT));
        gc.setFont(Font.font("Press Start 2P", 14));
        gc.fillText(sub, W / 2.0, py + 110);

        gc.setFill(Color.web(C_MUTED));
        gc.setFont(Font.font("Press Start 2P", 11));
        gc.fillText(hint, W / 2.0, py + 145);

        gc.setStroke(Color.web(titleColor, 0.3));
        gc.setLineWidth(1);
        gc.strokeLine(px + 30, py + ph - 16, px + pw - 30, py + ph - 16);

        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ── Credits ───────────────────────────────────────

    private void drawCredits(GraphicsContext gc) {
        gc.setFill(Color.web(C_BG));
        gc.fillRect(0, 0, W, H);
        for (int r = 0; r < H / 40 + 1; r++)
            for (int c = 0; c < W / 40 + 1; c++) {
                gc.setFill(Color.web((r + c) % 2 == 0 ? C_TILE_A : C_TILE_B, 0.5));
                gc.fillRect(c * 40, r * 40, 40, 40);
            }

        RadialGradient v = new RadialGradient(0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT), new Stop(1, Color.web("#000000", 0.85)));
        gc.setFill(v);
        gc.fillRect(0, 0, W, H);

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.web(C_GREEN, 0.07));
        gc.fillOval(240, 40, 320, 320);
        gc.setFill(Color.web(C_GREEN_LT));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        gc.fillText("☣", W / 2.0, 220);

        gc.setFill(Color.web(C_GREEN_LT));
        gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 36));
        gc.fillText("CONGRATULATIONS!", W / 2.0, 290);

        gc.setFill(Color.web(C_TEXT));
        gc.setFont(Font.font("Press Start 2P", 16));
        gc.fillText("You survived all 3 stages.", W / 2.0, 325);

        gc.setFill(Color.web(C_RED_LT));
        gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 15));
        gc.fillText("You didn't get infected.", W / 2.0, 352);

        gc.setStroke(Color.web(C_MUTED, 0.4));
        gc.setLineWidth(1);
        gc.strokeLine(200, 370, 600, 370);

        gc.setFill(Color.web(C_GOLD));
        gc.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 13));
        gc.fillText("— CREDITS —", W / 2.0, 394);

        // Fixed Array Structure Below!
        String[][] creds = {
                {"Developer", "TBA"},
                {"Assets", "Custom"},
        };

        int cy = 418;
        for (String[] pair : creds) {
            gc.setFill(Color.web(C_MUTED));
            gc.setFont(Font.font("Press Start 2P", 12));
            gc.fillText(pair[0] + "   " + pair[1], W / 2.0, cy);
            cy += 20;
        }

        gc.setFill(Color.web(C_MUTED));
        gc.setFont(Font.font("Press Start 2P", 10));
        gc.fillText("[ ESC ] or [ ENTER ]  →  Main Menu", W / 2.0, H - 30);

        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ── Helpers ───────────────────────────────────────

    private Color getItemColor(String name) {
        return switch (name.toLowerCase()) {
            case "pistol"     -> Color.web("#7f8c8d");
            case "machinegun" -> Color.web(C_AMBER);
            case "shotgun"    -> Color.web("#8e6030");
            case "medkit"     -> Color.web(C_RED_LT);
            case "bandage"    -> Color.web("#bdc3c7");
            default           -> Color.web(C_GREEN);
        };
    }

    private String formatTime(int s) {
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private String getDifficultyLabel() {
        return switch (selectedStage) {
            case 1 -> "EASY · 30s";
            case 2 -> "MEDIUM · 60s";
            case 3 -> "HARD · 90s";
            default -> "";
        };
    }

    public static void main(String[] args) { launch(args); }
}