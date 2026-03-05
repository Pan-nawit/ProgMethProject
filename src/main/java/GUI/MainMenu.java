package GUI;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * MainMenu — live animated background using the tileset terrain sprites.
 * A player character walks across the bottom of the screen.
 */
public class MainMenu {

    private final Stage stage;
    private final Runnable onPlay;
    private final Image[] terrain;
    private final Image[] playerFrames;
    private AnimationTimer bgLoop;

    // Palette — matching MainGame
    private static final String C_BG     = "#0a0806";
    private static final String C_RED    = "#c0392b";
    private static final String C_GREEN  = "#27ae60";
    private static final String C_GOLD   = "#f1c40f";
    private static final String C_TEXT   = "#d8cfc0";
    private static final String C_MUTED  = "#5a4030";
    private static final String C_BORDER = "#2a1808";

    public MainMenu(Stage stage, Runnable onPlay, Image[] terrain, Image[] playerFrames) {
        this.stage        = stage;
        this.onPlay       = onPlay;
        this.terrain      = terrain;
        this.playerFrames = playerFrames;
    }

    // ═════════════════════════════════════════════════
    //  Build scene
    // ═════════════════════════════════════════════════

    public Scene buildScene() {
        Canvas bg       = new Canvas(800, 600);
        StackPane ui    = new StackPane();
        StackPane root  = new StackPane(bg, ui);
        root.setBackground(Background.EMPTY);

        long[] tick = {0};
        long[] last = {0};

        bgLoop = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now - last[0] > 110_000_000L) { tick[0]++; last[0] = now; }
                drawBackground(bg.getGraphicsContext2D(), tick[0]);
            }
        };
        bgLoop.start();

        showMain(ui);

        Scene sc = new Scene(root, 800, 600);
        // Stop the background animation when this scene is replaced
        stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != sc) bgLoop.stop();
        });
        return sc;
    }

    // ─────────────────────────────────────────────────
    //  Animated background
    // ─────────────────────────────────────────────────

    private void drawBackground(GraphicsContext gc, long tick) {
        // Tiled terrain floor
        int ts = 96;
        for (int r = 0; r * ts < 600; r++) {
            for (int c = 0; c * ts < 800; c++) {
                int idx = Math.abs(r*13 + c*7 + (r*c)%3) % 4;
                if (terrain != null && terrain[idx] != null)
                    gc.drawImage(terrain[idx], c*ts, r*ts, ts, ts);
                else {
                    gc.setFill(Color.web((r+c)%2==0 ? "#2e2418" : "#271e14"));
                    gc.fillRect(c*ts, r*ts, ts, ts);
                }
            }
        }

        // Radial vignette — heavy so the text is readable
        gc.setFill(new RadialGradient(0,0,0.5,0.5,0.68,true,CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#000000",0.58)),
                new Stop(1, Color.web("#000000",0.94))));
        gc.fillRect(0, 0, 800, 600);

        // Bottom edge separator line
        gc.setStroke(Color.web("#6e1010", 0.6)); gc.setLineWidth(2);
        gc.strokeLine(0, 548, 800, 548);

        // Player walking across the bottom
        if (playerFrames != null) {
            int pf = (int)(tick % 9);
            int walkX = (int)((tick * 2) % 860) - 40;
            if (playerFrames[pf] != null)
                gc.drawImage(playerFrames[pf], walkX, 500, 56, 66);
        }
    }

    // ═════════════════════════════════════════════════
    //  Views
    // ═════════════════════════════════════════════════

    private void showMain(StackPane holder) {
        holder.getChildren().clear();

        VBox layout = new VBox(14);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(55, 80, 55, 80));

        Label icon  = lbl("☣",  54, C_RED,   true);
        Label title = lbl("ZOMBAL SURVIVIE", 36, C_TEXT, true);
        Label tag   = lbl("survive · or become one of them", 12, C_MUTED, false);
        Label div   = lbl("───────────────────────────", 11, C_BORDER, false);

        Button play     = btn("[ PLAY ]",      C_GREEN);
        Button settings = btn("[ SETTINGS ]",  C_TEXT);
        Button exit     = btn("[ EXIT ]",       C_MUTED);

        play.setOnAction(e -> { bgLoop.stop(); showStages(holder); });
        settings.setOnAction(e -> showSettings(holder));
        exit.setOnAction(e -> System.exit(0));

        layout.getChildren().addAll(icon, title, tag, div, play, settings, exit);
        holder.getChildren().add(layout);
    }

    // ── Stage selection ─────────────────────────────

    private void showStages(StackPane holder) {
        holder.getChildren().clear();

        VBox layout = new VBox(26);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        Label title = lbl("SELECT STAGE", 26, C_TEXT, true);

        // Top row: stages 1-3
        HBox topRow = new HBox(14);
        topRow.setAlignment(Pos.CENTER);
        topRow.getChildren().addAll(
                stageCard(1, "30s",  "EASY",    C_GREEN,  "#112a18"),
                stageCard(2, "60s",  "MEDIUM",  C_GOLD,   "#2a2208"),
                stageCard(3, "90s",  "HARD",    C_RED,    "#2a0808")
        );

        // Stage 4 — full-width endless card below
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER);
        bottomRow.getChildren().add(endlessCard());

        Button back = btn("[ BACK ]", C_MUTED);
        back.setOnAction(e -> { showMain(holder); bgLoop.start(); });

        layout.getChildren().addAll(title, topRow, bottomRow, back);
        holder.getChildren().add(layout);
    }

    private VBox stageCard(int num, String time, String diff, String accent, String bgHex) {
        boolean locked = num > MainGame.unlockedStages;
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(22));
        card.setPrefSize(188, 200);
        card.setBackground(new Background(new BackgroundFill(
                Color.web(locked ? "#0c0a08" : bgHex), new CornerRadii(8), null)));
        card.setStyle(
                "-fx-border-color:" + (locked ? "#1e1410" : accent) +
                        "; -fx-border-width:2; -fx-border-radius:8;");

        Label stLbl  = lbl("STAGE " + num,  18, locked ? C_MUTED : accent, true);
        Label timLbl = lbl("⏱  " + time,    13, locked ? "#281c10" : C_TEXT, false);
        Label difLbl = lbl(diff,             12, locked ? "#281c10" : accent, true);

        if (locked) {
            card.getChildren().addAll(stLbl, timLbl, difLbl,
                    lbl("🔒  LOCKED", 12, "#281c10", true),
                    lbl("Clear Stage " + (num - 1), 10, "#201408", false));
        } else {
            Button start = btn("[ START ]", accent);
            start.setPrefWidth(144);
            start.setOnAction(e -> { MainGame.selectedStage = num; if (onPlay != null) onPlay.run(); });
            card.getChildren().addAll(stLbl, timLbl, difLbl, start);
        }
        return card;
    }

    /** Wide card for the Endless (Stage 4) mode */
    private HBox endlessCard() {
        boolean locked = 4 > MainGame.unlockedStages;
        String accent  = "#9b59b6";   // purple for endless
        String bgHex   = locked ? "#0c0a08" : "#1a0a28";

        HBox card = new HBox(30);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 30, 18, 30));
        card.setPrefWidth(622);   // matches the 3-card strip width
        card.setBackground(new Background(new BackgroundFill(
                Color.web(bgHex), new CornerRadii(8), null)));
        card.setStyle("-fx-border-color:" + (locked ? "#1e1410" : accent)
                + "; -fx-border-width:2; -fx-border-radius:8;");

        // Left info block
        VBox info = new VBox(5);
        info.setAlignment(Pos.CENTER_LEFT);
        info.getChildren().addAll(
                lbl("STAGE 4  ·  ENDLESS", 18, locked ? C_MUTED : accent, true),
                lbl("∞  Unlimited time — survive as long as you can", 12, locked ? "#281c10" : C_TEXT, false),
                lbl("All zombie types  ·  Frequent item drops  ·  Shotgun spawns", 10, locked ? "#201408" : "#7a5a9a", false)
        );

        // Right — lock or start
        if (locked) {
            VBox lockBox = new VBox(4);
            lockBox.setAlignment(Pos.CENTER);
            lockBox.getChildren().addAll(
                    lbl("🔒  LOCKED", 13, "#281c10", true),
                    lbl("Clear Stage 3", 10, "#201408", false)
            );
            HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
            card.getChildren().addAll(info, lockBox);
        } else {
            Button start = btn("[ START ]", accent);
            start.setPrefWidth(144);
            start.setOnAction(e -> { MainGame.selectedStage = 4; if (onPlay != null) onPlay.run(); });
            HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
            card.getChildren().addAll(info, start);
        }
        return card;
    }

    // ── Settings ─────────────────────────────────────

    private void showSettings(StackPane holder) {
        holder.getChildren().clear();

        VBox layout = new VBox(22);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));

        Label title = lbl("SETTINGS", 26, C_TEXT, true);

        // Key bindings panel
        VBox keysPanel = new VBox(8);
        keysPanel.setPadding(new Insets(18));
        keysPanel.setMaxWidth(450);
        keysPanel.setBackground(new Background(new BackgroundFill(
                Color.web("#0a0806"), new CornerRadii(8), null)));
        keysPanel.setStyle("-fx-border-color:#2a1808; -fx-border-width:1; -fx-border-radius:8;");
        keysPanel.getChildren().add(lbl("KEY BINDINGS", 12, C_GOLD, true));

        for (String[] kv : new String[][]{
                {"W / A / S / D", "Move"},
                {"Left Click",    "Shoot toward cursor"},
                {"Q / E",         "Switch item"},
                {"F",             "Use consumable"},
                {"R",             "Restart / Next stage"},
                {"ESC",           "Main menu"}}) {
            HBox row = new HBox(16);
            Label k = lbl(kv[0], 12, C_GOLD, true);  k.setMinWidth(140);
            Label v = lbl(kv[1], 12, C_TEXT, false);
            row.getChildren().addAll(k, v);
            keysPanel.getChildren().add(row);
        }

        Button back = btn("[ BACK ]", C_MUTED);
        back.setOnAction(e -> showMain(holder));

        layout.getChildren().addAll(title, keysPanel, back);
        holder.getChildren().add(layout);
    }

    // ═════════════════════════════════════════════════
    //  UI helpers
    // ═════════════════════════════════════════════════

    private Label lbl(String text, double size, String color, boolean bold) {
        Label l = new Label(text);
        l.setFont(bold ? Font.font("Monospaced", FontWeight.BOLD, size)
                : Font.font("Monospaced", size));
        l.setTextFill(Color.web(color));
        return l;
    }

    private Button btn(String text, String fg) {
        Button b = new Button(text);
        b.setPrefWidth(246); b.setPrefHeight(44);
        b.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
        String base  = "-fx-background-color:#0e0a06cc; -fx-text-fill:" + fg
                + "; -fx-background-radius:5; -fx-border-color:" + fg
                + "66; -fx-border-width:1; -fx-border-radius:5; -fx-cursor:hand;";
        String hover = "-fx-background-color:#0e0a06; -fx-text-fill:" + fg
                + "; -fx-background-radius:5; -fx-border-color:" + fg
                + "; -fx-border-width:1.5; -fx-border-radius:5; -fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e  -> b.setStyle(base));
        return b;
    }
}