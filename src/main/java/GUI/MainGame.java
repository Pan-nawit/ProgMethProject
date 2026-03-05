package GUI;

import GameLogic.GameLogic;
import Item.Item;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * MainGame — full sprite-driven UI using the Zombie Apocalypse Tileset assets.
 *
 * Drop the "assets/" folder (from the output bundle) next to your project
 * root / JAR so Java can resolve  assets/terrain_0.png  etc.
 */
public class MainGame extends Application {

    // ── Stage state ───────────────────────────────────
    public static int selectedStage  = 1;
    public static int unlockedStages = 1;

    // ── Runtime state ─────────────────────────────────
    private GameLogic gameLogic;
    private boolean w, a, s, d, mouseDown;
    private double mouseX = 400, mouseY = 300;
    private AnimationTimer loop;
    private Stage stage;

    // ── Layout ────────────────────────────────────────
    public static final int W      = 800;
    public static final int H      = 600;
    public static final int HUD_H  = 54;
    public static final int IBAR_H = 60;
    public static final int PLAY_H = H - HUD_H - IBAR_H;   // 486
    public static final int[] STAGE_SEC = {30, 60, 90, -1};  // -1 = endless

    // ── Animation ─────────────────────────────────────
    private long animTick = 0, lastAnimNs = 0;
    private static final long FRAME_NS = 110_000_000L;

    // ── Asset root ────────────────────────────────────
    private static final String A = "assets/";

    // ── Sprite arrays ─────────────────────────────────
    private Image[] terrain, playerWalk, zSkinny, zBig, zKid, blood, explosion, tree;
    private final Map<String, Image> slots   = new HashMap<>();
    private final Map<String, Image> pickups = new HashMap<>();

    // Pickup sprite indices (identified by visual inspection of the tileset)
    private static final int P_PISTOL     = 4;
    private static final int P_MACHINEGUN = 10;
    private static final int P_MEDKIT     = 13;
    private static final int P_MEDICINE   = 14;
    private static final int P_SHOTGUN    = 17;

    // ═════════════════════════════════════════════════
    //  Entry
    // ═════════════════════════════════════════════════

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        primaryStage.setTitle("Zombal Survivie");
        primaryStage.setResizable(false);
        loadAssets();
        showMainMenu();
        primaryStage.show();
    }

    // ═════════════════════════════════════════════════
    //  Asset loading
    // ═════════════════════════════════════════════════

    private void loadAssets() {
        terrain    = arr("terrain_",   4);
        playerWalk = arr("player_",    9);
        zSkinny    = arr("z_skinny_",  9);
        zBig       = arr("z_big_",     9);
        zKid       = arr("z_kid_",     9);
        blood      = arr("blood_",     5);
        explosion  = arr("explosion_", 6);
        tree       = arr("tree_",      9);

        for (String n : new String[]{
                "empty_slot","using_slot","gun_slot","shotgun_slot",
                "health_kit_slot","medicine_slot","double_ammo_slot",
                "single_ammo_slot","knife_slot","grenade_slot","coke_slot"}) {
            Image i = img(A + "slot_" + n + ".png");
            if (i != null) slots.put(n, i);
        }

        for (int i = 0; i <= 19; i++) {
            Image im = img(A + "pickup_" + i + ".png");
            if (im != null) pickups.put(String.valueOf(i), im);
        }
    }

    private Image[] arr(String prefix, int n) {
        Image[] a = new Image[n];
        for (int i = 0; i < n; i++) a[i] = img(A + prefix + i + ".png");
        return a;
    }

    private Image img(String path) {
        try { File f = new File(path); return f.exists() ? new Image(f.toURI().toString()) : null; }
        catch (Exception e) { return null; }
    }

    // ═════════════════════════════════════════════════
    //  Screen routing
    // ═════════════════════════════════════════════════

    void showMainMenu() {
        if (loop != null) loop.stop();
        stage.setScene(new MainMenu(stage, this::startGame, terrain, playerWalk).buildScene());
    }

    public void startGame() {
        gameLogic = new GameLogic();
        gameLogic.initGame(selectedStage);
        animTick = 0;

        Canvas canvas = new Canvas(W, H);
        Scene sc = new Scene(new StackPane(canvas));
        sc.setCursor(javafx.scene.Cursor.NONE);

        sc.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W -> w = true;  case A -> a = true;
                case S -> s = true;  case D -> d = true;
                case R -> {
                    if (gameLogic.isGameOver) {
                        // Endless: R restarts endless; timed: R restarts same stage
                        startGame();
                    } else if (gameLogic.isWon) {
                        if (selectedStage == 3) {
                            // Unlock endless (stage 4) and go to credits
                            if (unlockedStages < 4) unlockedStages = 4;
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
        sc.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W -> w = false; case A -> a = false;
                case S -> s = false; case D -> d = false;
            }
        });
        sc.setOnMousePressed(e  -> { if (e.isPrimaryButtonDown()) mouseDown = true; });
        sc.setOnMouseReleased(e -> mouseDown = false);
        sc.setOnMouseMoved(e    -> { mouseX = e.getX(); mouseY = e.getY(); });
        sc.setOnMouseDragged(e  -> { mouseX = e.getX(); mouseY = e.getY(); });
        stage.setScene(sc);

        loop = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now - lastAnimNs > FRAME_NS) { animTick++; lastAnimNs = now; }
                gameLogic.player.setMousePos((float) mouseX, (float)(mouseY - HUD_H));
                gameLogic.update(w, a, s, d, mouseDown);
                render(canvas.getGraphicsContext2D());
            }
        };
        loop.start();
    }

    private void showCredits() {
        if (loop != null) loop.stop();
        // Completing stage 3 always unlocks stage 4 (endless)
        if (unlockedStages < 4) unlockedStages = 4;
        Canvas c = new Canvas(W, H);
        Scene sc = new Scene(new StackPane(c));
        drawCredits(c.getGraphicsContext2D());
        sc.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.R ||
                    e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                selectedStage = 1; showMainMenu();
            }
        });
        stage.setScene(sc);
    }

    // ═════════════════════════════════════════════════
    //  Render pipeline
    // ═════════════════════════════════════════════════

    private void render(GraphicsContext gc) {
        gc.clearRect(0, 0, W, H);
        drawFloor(gc);
        drawEntities(gc);
        drawHud(gc);
        drawItemBar(gc);
        drawVignette(gc);
        drawCrosshair(gc);
        drawOverlay(gc);
    }

    // ─────────────────────────────────────────────────
    //  Floor
    // ─────────────────────────────────────────────────

    private void drawFloor(GraphicsContext gc) {
        gc.setFill(Color.web("#0a0806")); gc.fillRect(0, 0, W, H);
        int ts = 96;
        for (int r = 0; r * ts < PLAY_H + ts; r++) {
            for (int c = 0; c * ts < W + ts; c++) {
                int idx = Math.abs(r * 13 + c * 7 + (r * c) % 3) % 4;
                int tx = c * ts, ty = HUD_H + r * ts;
                if (terrain != null && terrain[idx] != null)
                    gc.drawImage(terrain[idx], tx, ty, ts, ts);
                else {
                    gc.setFill(Color.web((r+c)%2==0 ? "#2e2418" : "#271e14"));
                    gc.fillRect(tx, ty, ts, ts);
                }
            }
        }
        // Arena border
        gc.setStroke(Color.web("#5a0a0a", 0.8)); gc.setLineWidth(3);
        gc.strokeRect(1.5, HUD_H + 1.5, W - 3, PLAY_H - 3);
    }

    // ─────────────────────────────────────────────────
    //  Entities
    // ─────────────────────────────────────────────────

    private void drawEntities(GraphicsContext gc) {
        // Ground items — real pickup sprites
        for (var item : gameLogic.itemsOnGround) {
            int ix = item.getX(), iy = item.getY() + HUD_H;
            double pulse = Math.abs(Math.sin(animTick * 0.3));  // 0.0–1.0, never negative

            // Glow halo
            gc.setFill(Color.web("#f0c030", 0.18 * pulse));
            gc.fillOval(ix - 8, iy - 4, 36, 32);

            // Sprite
            Image pi = pickups.get(String.valueOf(pickupIdx(item.getName())));
            if (pi != null) {
                double pw = Math.min(pi.getWidth(), 48), ph = Math.min(pi.getHeight(), 48);
                gc.drawImage(pi, ix + (24 - pw) / 2.0, iy + (24 - ph) / 2.0, pw, ph);
            } else {
                gc.setFill(itemColor(item.getName()));
                gc.fillRoundRect(ix, iy, 22, 22, 4, 4);
            }

            // Gold border
            gc.setStroke(Color.web("#c8a020", 0.7 * pulse)); gc.setLineWidth(1);
            gc.strokeRoundRect(ix - 1, iy - 1, 26, 26, 4, 4);
        }

        // Bullets
        for (var b : gameLogic.bullets) {
            int bx = b.getX(), by = b.getY() + HUD_H;
            gc.setFill(Color.web("#ffe060", 0.35)); gc.fillOval(bx-5, by-5, 10, 10);
            gc.setFill(Color.web("#fff8c0")); gc.fillOval(bx-2, by-2, 4, 4);
        }

        // Enemies
        for (var enemy : gameLogic.enemies) {
            int ex = enemy.getX(), ey = enemy.getY() + HUD_H;
            int ew = enemy.getWidth(), eh = enemy.getHeight();
            String cls = enemy.getClass().getSimpleName().toLowerCase();

            // Shadow
            gc.setFill(Color.web("#000000", 0.38));
            gc.fillOval(ex + ew/4, ey + eh - 6, ew/2, 9);

            // Sprite
            Image[] frames;
            int dw = ew, dh = eh;
            if (cls.contains("juggernaut"))  { frames = zBig;    dw = 48; dh = 48; }
            else if (cls.contains("kid") ||
                    cls.contains("animal")) { frames = zKid;    dw = 28; dh = 28; }
            else                             { frames = zSkinny; }

            int ef = (int)(animTick % (frames != null ? frames.length : 1));
            if (frames != null && frames[ef % frames.length] != null)
                gc.drawImage(frames[ef % frames.length], ex, ey, dw, dh);
            else {
                gc.setFill(enemy.getEnemyColor()); gc.fillRoundRect(ex, ey, ew, eh, 4, 4);
            }

            // HP bar above entity
            double hp = (double) enemy.getHP() / Math.max(enemy.getMaxHp(), 1);
            gc.setFill(Color.web("#000000", 0.75));
            gc.fillRoundRect(ex - 2, ey - 11, ew + 4, 7, 2, 2);
            gc.setFill(hp > .6 ? Color.web("#27ae60") : hp > .3 ? Color.web("#e67e22") : Color.web("#c0392b"));
            gc.fillRoundRect(ex - 2, ey - 11, (ew + 4) * hp, 7, 2, 2);
        }

        // Player
        int px = gameLogic.player.getX(), py = gameLogic.player.getY() + HUD_H;
        int pw = gameLogic.player.getWidth(), ph = gameLogic.player.getHeight();

        gc.setFill(Color.web("#000000", 0.42));
        gc.fillOval(px + 6, py + ph - 5, pw - 12, 9);

        boolean moving = w || a || s || d;
        int pf = moving ? (int)(animTick % 9) : 0;
        if (playerWalk != null && playerWalk[pf] != null)
            gc.drawImage(playerWalk[pf], px, py, pw, ph);
        else {
            gc.setFill(Color.web("#3498db")); gc.fillRoundRect(px, py, pw, ph, 5, 5);
            gc.setStroke(Color.web("#5dade2")); gc.setLineWidth(2); gc.strokeRoundRect(px, py, pw, ph, 5, 5);
        }

        // Aim dotted line
        if (!gameLogic.isGameOver && !gameLogic.isWon) {
            float mx = gameLogic.player.getMouseX(), my = gameLogic.player.getMouseY();
            if (mx >= 0) {
                gc.setStroke(Color.web("#ffffff", 0.08)); gc.setLineWidth(1);
                gc.setLineDashes(3, 9);
                gc.strokeLine(px + pw / 2.0, py + ph / 2.0, mx, my + HUD_H);
                gc.setLineDashes(null);
            }
        }
    }

    // ─────────────────────────────────────────────────
    //  HUD top bar
    // ─────────────────────────────────────────────────

    private void drawHud(GraphicsContext gc) {
        boolean endless = gameLogic.isEndless;
        boolean ended   = gameLogic.isGameOver || gameLogic.isWon;
        // Use the snapshotted time when game has ended so the HUD timer freezes
        int elapsed = ended ? (int) gameLogic.finalElapsedSeconds
                : (int) gameLogic.getElapsedSeconds();
        int dur = endless ? -1 : STAGE_SEC[selectedStage - 1];
        int rem = endless ? elapsed : Math.max(0, dur - elapsed);
        boolean urgent = !endless && rem <= 10;

        // Panel
        gc.setFill(Color.web("#0a0806", 0.97)); gc.fillRect(0, 0, W, HUD_H);
        gc.setStroke(Color.web("#6e1010", 0.9)); gc.setLineWidth(2);
        gc.strokeLine(0, HUD_H, W, HUD_H);

        // ── HP hearts ─────────────────────────────
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 8));
        gc.setFill(Color.web("#4a3020")); gc.fillText("HEALTH", 12, 13);
        int hp = gameLogic.player.getHp();
        for (int i = 0; i < 5; i++) {
            gc.setFont(Font.font("Arial", 22));
            gc.setFill(i < hp ? Color.web("#c0392b") : Color.web("#2a1008"));
            gc.fillText(i < hp ? "♥" : "♡", 10 + i * 24, 42);
        }

        // Status badges
        int bx = 140;
        for (var st : gameLogic.player.getStatusList()) {
            boolean bleed = st.getName().equalsIgnoreCase("bleeding");
            gc.setFill(Color.web(bleed ? "#5a0808" : "#2a1a08", 0.95));
            gc.fillRoundRect(bx, 11, 72, 18, 5, 5);
            gc.setStroke(Color.web(bleed ? "#c0392b" : "#c07830", 0.45));
            gc.setLineWidth(1); gc.strokeRoundRect(bx, 11, 72, 18, 5, 5);
            gc.setFill(Color.web(bleed ? "#ffaaaa" : "#ffd090"));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 9));
            gc.fillText((bleed ? "🩸 " : "🦵 ") + st.getName().toUpperCase(), bx + 5, 23);
            bx += 78;
        }

        // ── Timer (center) ─────────────────────────
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#4a3020"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 9));
        gc.fillText("STAGE " + selectedStage + "  ·  " + diffLabel(), W / 2.0, 13);

        // Timer value — count-up (green) for endless, count-down for timed
        gc.setFill(endless ? Color.web("#27ae60") : (urgent ? Color.web("#e74c3c") : Color.web("#d8cfc0")));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 30));
        gc.fillText(fmtTime(rem), W / 2.0, 43);

        // Progress / pulse bar
        int bw = 160;
        gc.setFill(Color.web("#1a1008"));
        gc.fillRoundRect(W / 2.0 - bw / 2.0, HUD_H - 7, bw, 4, 2, 2);
        if (endless) {
            // Pulsing green bar for endless
            double pulse = Math.abs(Math.sin(animTick * 0.2));  // 0.0–1.0
            gc.setFill(Color.web("#27ae60", 0.4 + 0.6 * pulse));
            gc.fillRoundRect(W / 2.0 - bw / 2.0, HUD_H - 7, bw, 4, 2, 2);
        } else {
            gc.setFill(urgent ? Color.web("#c0392b") : Color.web("#27ae60"));
            gc.fillRoundRect(W / 2.0 - bw / 2.0, HUD_H - 7, bw * ((double) rem / dur), 4, 2, 2);
        }
        gc.setTextAlign(TextAlignment.LEFT);

        // ── Score + wave (right) ───────────────────
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(Color.web("#f1c40f"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 19));
        gc.fillText("$  " + gameLogic.score, W - 12, 29);
        gc.setFill(Color.web("#4a3020"));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
        gc.fillText("WAVE  " + gameLogic.wave, W - 12, 46);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ─────────────────────────────────────────────────
    //  Item bar (bottom)
    // ─────────────────────────────────────────────────

    private void drawItemBar(GraphicsContext gc) {
        int barY = H - IBAR_H;
        var inv   = gameLogic.player.getInventory();
        int sel   = gameLogic.player.getSelectedItemIndex();

        // Panel
        gc.setFill(Color.web("#0a0806", 0.97)); gc.fillRect(0, barY, W, IBAR_H);
        gc.setStroke(Color.web("#6e1010", 0.8)); gc.setLineWidth(2);
        gc.strokeLine(0, barY, W, barY);

        // 6 centered slots
        int slotW = 56, slotH = 50, gap = 6;
        int startX = (W - (6 * slotW + 5 * gap)) / 2;

        for (int i = 0; i < 6; i++) {
            int sx = startX + i * (slotW + gap);
            int sy = barY + 5;
            boolean isSel = (i == sel);

            // Slot background — real asset
            Image bg = isSel ? slots.get("using_slot") : slots.get("empty_slot");
            if (bg != null) {
                gc.drawImage(bg, sx, sy, slotW, slotH);
            } else {
                gc.setFill(isSel ? Color.web("#2e1800") : Color.web("#100c08"));
                gc.fillRoundRect(sx, sy, slotW, slotH, 5, 5);
                gc.setStroke(isSel ? Color.web("#f1c40f") : Color.web("#2a1808"));
                gc.setLineWidth(isSel ? 2 : 1);
                gc.strokeRoundRect(sx, sy, slotW, slotH, 5, 5);
            }

            // Slot number
            gc.setFill(isSel ? Color.web("#f1c40f", 0.9) : Color.web("#3a2010", 0.9));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 8));
            gc.fillText(String.valueOf(i + 1), sx + 3, sy + 10);

            if (i < inv.size()) {
                Item item = inv.get(i);
                String name = item.getName().toLowerCase();

                // Item icon — real slot sprite
                Image icon = slotIcon(name);
                if (icon != null)
                    gc.drawImage(icon, sx + 4, sy + 8, slotW - 8, slotH - 16);
                else {
                    gc.setFill(itemColor(item.getName()));
                    gc.fillRoundRect(sx + 10, sy + 12, slotW - 20, slotH - 24, 3, 3);
                }

                // Name label
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 7));
                gc.setFill(isSel ? Color.web("#f1c40f") : Color.web("#7a6040"));
                gc.fillText(name.substring(0, Math.min(7, name.length())).toUpperCase(),
                        sx + slotW / 2.0, sy + slotH - 7);

                // Ammo badge
                gc.setFill(Color.web("#000000", 0.78));
                gc.fillRoundRect(sx + slotW - 17, sy + 2, 16, 10, 2, 2);
                gc.setFill(isSel ? Color.web("#f1c40f") : Color.web("#8a6840"));
                gc.setFont(Font.font("Monospaced", 7));
                gc.fillText(String.valueOf(item.getAmount()), sx + slotW - 9, sy + 10);
                gc.setTextAlign(TextAlignment.LEFT);
            }
        }

        // Key hints on the left
        gc.setFill(Color.web("#2e1808"));
        gc.setFont(Font.font("Monospaced", 8));
        int hx = 12, hy = barY + 14;
        for (String hint : new String[]{"Q/E  switch","F    use","R    restart","ESC  menu"}) {
            gc.fillText(hint, hx, hy); hy += 12;
        }
    }

    // ─────────────────────────────────────────────────
    //  Vignette
    // ─────────────────────────────────────────────────

    private void drawVignette(GraphicsContext gc) {
        gc.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.75, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT), new Stop(1, Color.web("#000000", 0.75))));
        gc.fillRect(0, HUD_H, W, PLAY_H);
    }

    // ─────────────────────────────────────────────────
    //  Crosshair
    // ─────────────────────────────────────────────────

    private void drawCrosshair(GraphicsContext gc) {
        if (gameLogic.isGameOver || gameLogic.isWon) return;
        double mx = mouseX, my = mouseY;
        int g = 5, len = 10;
        gc.setStroke(Color.web("#000000", 0.6)); gc.setLineWidth(3);
        xhair(gc, mx, my, g, len);
        gc.setStroke(Color.web("#e0d8c8")); gc.setLineWidth(1.5);
        xhair(gc, mx, my, g, len);
        gc.setFill(Color.web("#c0392b", 0.9)); gc.fillOval(mx - 2.5, my - 2.5, 5, 5);
    }

    private void xhair(GraphicsContext gc, double mx, double my, int g, int l) {
        gc.strokeLine(mx-g-l, my, mx-g, my); gc.strokeLine(mx+g, my, mx+g+l, my);
        gc.strokeLine(mx, my-g-l, mx, my-g); gc.strokeLine(mx, my+g, mx, my+g+l);
    }

    // ─────────────────────────────────────────────────
    //  Death / win overlay
    // ─────────────────────────────────────────────────

    private void drawOverlay(GraphicsContext gc) {
        if (!gameLogic.isGameOver && !gameLogic.isWon) return;
        gc.setFill(Color.web("#000000", 0.82)); gc.fillRect(0, 0, W, H);

        boolean endless  = gameLogic.isEndless;
        boolean died     = gameLogic.isGameOver;

        // For endless mode death = "survived X time" shown with green panel (like a clear)
        boolean showGreen = !died || endless;

        String col   = showGreen ? "#27ae60" : "#c0392b";
        String title, sub, hint;

        if (endless && died) {
            // Endless death → show how long they survived (frozen at death)
            int secs = (int) gameLogic.finalElapsedSeconds;
            title = "YOU SURVIVED";
            sub   = "Time: " + fmtTime(secs) + "  ·  Score  " + gameLogic.score;
            hint  = "[ R ] Play Again    [ ESC ] Menu";
        } else if (!died) {
            // Timed stage clear
            title = "SURVIVED!";
            sub   = "Stage " + selectedStage + " Clear  ·  Score  " + gameLogic.score;
            hint  = (selectedStage == 3 ? "[ R ] Credits" : "[ R ] Next Stage") + "    [ ESC ] Menu";
        } else {
            // Timed stage death
            title = "YOU DIED";
            sub   = "Score   " + gameLogic.score;
            hint  = "[ R ] Restart    [ ESC ] Menu";
        }

        int pw = 500, ph = endless && died ? 175 : 155;
        int px = (W - pw) / 2, py = (H - ph) / 2;

        // Panel
        gc.setFill(Color.web("#080604", 0.98)); gc.fillRoundRect(px, py, pw, ph, 10, 10);
        gc.setStroke(Color.web(col, 0.80)); gc.setLineWidth(2.5); gc.strokeRoundRect(px, py, pw, ph, 10, 10);

        // Inner glow stripe at top
        gc.setFill(Color.web(col, 0.08)); gc.fillRoundRect(px, py, pw, 38, 10, 10);

        gc.setTextAlign(TextAlignment.CENTER);

        // Title
        gc.setFill(Color.web(col));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 38));
        gc.fillText(title, W / 2.0, py + 48);

        // For endless survived — show big time prominently
        if (endless && died) {
            int secs = (int) gameLogic.finalElapsedSeconds;
            gc.setFill(Color.web("#ffffff", 0.95));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
            gc.fillText(fmtTime(secs), W / 2.0, py + 80);
            gc.setFill(Color.web("#c8bfb0"));
            gc.setFont(Font.font("Monospaced", 12));
            gc.fillText("Score  " + gameLogic.score + "  ·  Wave  " + gameLogic.wave, W / 2.0, py + 102);
            gc.setFill(Color.web("#5a4030"));
            gc.setFont(Font.font("Monospaced", 10));
            gc.fillText(hint, W / 2.0, py + 126);
        } else {
            gc.setFill(Color.web("#c8bfb0")); gc.setFont(Font.font("Monospaced", 13)); gc.fillText(sub, W / 2.0, py + 85);
            gc.setFill(Color.web("#5a4030")); gc.setFont(Font.font("Monospaced", 10)); gc.fillText(hint, W / 2.0, py + 110);
        }

        // Accent line at panel bottom
        gc.setStroke(Color.web(col, 0.25)); gc.setLineWidth(1);
        gc.strokeLine(px + 24, py + ph - 14, px + pw - 24, py + ph - 14);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ─────────────────────────────────────────────────
    //  Credits
    // ─────────────────────────────────────────────────

    private void drawCredits(GraphicsContext gc) {
        int ts = 96;
        for (int r = 0; r * ts < H; r++)
            for (int c = 0; c * ts < W; c++) {
                int idx = Math.abs(r*13+c*7) % 4;
                if (terrain != null && terrain[idx] != null)
                    gc.drawImage(terrain[idx], c*ts, r*ts, ts, ts);
                else { gc.setFill(Color.web("#271e14")); gc.fillRect(c*ts,r*ts,ts,ts); }
            }
        gc.setFill(new RadialGradient(0,0,0.5,0.5,0.65,true,CycleMethod.NO_CYCLE,
                new Stop(0,Color.web("#000000",0.55)),new Stop(1,Color.web("#000000",0.92))));
        gc.fillRect(0,0,W,H);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#27ae60",0.12)); gc.fillOval(250,20,300,300);
        gc.setFill(Color.web("#27ae60")); gc.setFont(Font.font("Arial",FontWeight.BOLD,68)); gc.fillText("☣",W/2.0,215);
        gc.setFill(Color.web("#27ae60")); gc.setFont(Font.font("Monospaced",FontWeight.BOLD,30)); gc.fillText("CONGRATULATIONS!",W/2.0,276);
        gc.setFill(Color.web("#c8bfb0")); gc.setFont(Font.font("Monospaced",14)); gc.fillText("You survived all 3 stages.",W/2.0,306);
        gc.setFill(Color.web("#c0392b")); gc.setFont(Font.font("Monospaced",FontWeight.BOLD,13)); gc.fillText("You don't get infected.",W/2.0,330);
        gc.setFill(Color.web("#9b59b6")); gc.setFont(Font.font("Monospaced",FontWeight.BOLD,12)); gc.fillText("✦  STAGE 4 — ENDLESS MODE UNLOCKED  ✦",W/2.0,355);
        gc.setStroke(Color.web("#302010")); gc.setLineWidth(1); gc.strokeLine(200,372,600,372);
        gc.setFill(Color.web("#f1c40f")); gc.setFont(Font.font("Monospaced",FontWeight.BOLD,12)); gc.fillText("— C R E D I T S —",W/2.0,394);
        int cy = 416;
        for (String[] p : new String[][]{{"Game Logic & Design","@pun"},{"Player & Items","@Z3TSUNA"},{"Enemies","@BNiD"}}) {
            gc.setFill(Color.web("#5a4030")); gc.setFont(Font.font("Monospaced",12)); gc.fillText(p[0]+"   "+p[1],W/2.0,cy); cy+=22;
        }
        gc.setFill(Color.web("#302010")); gc.setFont(Font.font("Monospaced",10)); gc.fillText("[ R ] or [ ESC ]  →  Main Menu",W/2.0,H-18);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ═════════════════════════════════════════════════
    //  Helpers
    // ═════════════════════════════════════════════════

    private Image slotIcon(String n) {
        if (n.contains("pistol"))     return slots.get("gun_slot");
        if (n.contains("machinegun")) return slots.get("double_ammo_slot");
        if (n.contains("shotgun"))    return slots.get("shotgun_slot");
        if (n.contains("medkit"))     return slots.get("health_kit_slot");
        if (n.contains("bandage"))    return slots.get("medicine_slot");
        return null;
    }

    private int pickupIdx(String name) {
        String n = name.toLowerCase();
        if (n.contains("pistol"))     return P_PISTOL;
        if (n.contains("machinegun")) return P_MACHINEGUN;
        if (n.contains("shotgun"))    return P_SHOTGUN;
        if (n.contains("medkit"))     return P_MEDKIT;
        if (n.contains("bandage"))    return P_MEDICINE;
        return 0;
    }

    Color itemColor(String name) {
        return switch (name.toLowerCase()) {
            case "pistol"     -> Color.web("#7f8c8d");
            case "machinegun" -> Color.web("#e67e22");
            case "shotgun"    -> Color.web("#8e6030");
            case "medkit"     -> Color.web("#c0392b");
            case "bandage"    -> Color.web("#bdc3c7");
            default           -> Color.web("#27ae60");
        };
    }

    private String fmtTime(int s) { return String.format("%d:%02d", s/60, s%60); }
    private String diffLabel() {
        return switch (selectedStage) { case 1->"EASY · 30s"; case 2->"MEDIUM · 60s"; case 3->"HARD · 90s"; case 4->"ENDLESS ∞"; default->""; };
    }

    Image[] getTerrainTiles() { return terrain; }
    Image[] getPlayerFrames() { return playerWalk; }

    public static void main(String[] args) { launch(args); }
}