package GUI;

import Sound.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainMenu {

    private final Stage stage;
    private final Runnable onPlay;
    private final Image[] terrain;
    private final Image[] playerFrames;
    private AnimationTimer bgLoop;

    private static final String C_BG     = "#0a0806";
    private static final String C_RED    = "#c0392b";
    private static final String C_GREEN  = "#27ae60";
    private static final String C_GOLD   = "#f1c40f";
    private static final String C_TEXT   = "#d8cfc0";
    private static final String C_MUTED  = "#5a4030";
    private static final String C_BORDER = "#2a1808";

    public MainMenu(Stage stage, Runnable onPlay, Image[] terrain, Image[] playerFrames) {
        this.stage = stage;
        this.onPlay = onPlay;
        this.terrain = terrain;
        this.playerFrames = playerFrames;
    }

    public Scene buildScene() {
        Canvas bg = new Canvas(800, 600);
        StackPane ui = new StackPane();
        StackPane root = new StackPane(bg, ui);
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
        stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != sc) bgLoop.stop();
        });
        return sc;
    }

    private void drawBackground(GraphicsContext gc, long tick) {
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

        gc.setFill(new RadialGradient(0,0,0.5,0.5,0.68,true,CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#000000",0.58)),
                new Stop(1, Color.web("#000000",0.94))));
        gc.fillRect(0, 0, 800, 600);

        gc.setStroke(Color.web("#6e1010", 0.6)); gc.setLineWidth(2);
        gc.strokeLine(0, 548, 800, 548);
    }

    private void showMain(StackPane holder) {
        holder.getChildren().clear();

        VBox layout = new VBox(14);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(55, 80, 55, 80));

        Label icon = new Label("☣");
        icon.setFont(Font.font("Arial", FontWeight.BOLD, 90));
        icon.setTextFill(Color.web(C_RED));

        Label title = lbl("ZOMBAL SURVIVIE", 28, C_TEXT, true);
        Label tag   = lbl("survive or become one of them", 12, C_MUTED, false);
        Label div   = lbl("───────────────────────────", 10, C_BORDER, false);

        Button play     = btn("[ PLAY ]",     C_GREEN, true);
        Button settings = btn("[ SETTINGS ]", C_TEXT, true);
        Button exit     = btn("[ EXIT ]",     C_MUTED, true);

        play.setOnAction(e -> { bgLoop.stop(); showStages(holder); });
        settings.setOnAction(e -> showSettings(holder));
        exit.setOnAction(e -> System.exit(0));

        layout.getChildren().addAll(icon, title, tag, div, play, settings, exit);
        holder.getChildren().add(layout);
    }

    private void showStages(StackPane holder) {
        holder.getChildren().clear();

        VBox layout = new VBox(26);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        Label title = lbl("SELECT STAGE", 22, C_TEXT, true);

        HBox topRow = new HBox(14);
        topRow.setAlignment(Pos.CENTER);
        topRow.getChildren().addAll(
                stageCard(1, "30s", "EASY",   C_GREEN, "#112a18"),
                stageCard(2, "60s", "MEDIUM", C_GOLD,  "#2a2208"),
                stageCard(3, "90s", "HARD",   C_RED,   "#2a0808")
        );

        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER);
        bottomRow.getChildren().add(endlessCard());

        Button back = btn("[ BACK ]", C_MUTED, true);
        back.setOnAction(e -> { showMain(holder); bgLoop.start(); });

        layout.getChildren().addAll(title, topRow, bottomRow, back);
        holder.getChildren().add(layout);
    }

    private VBox stageCard(int num, String time, String diff, String accent, String bgHex) {
        boolean locked = num > MainGame.unlockedStages;
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(22));
        card.setPrefSize(200, 200);
        card.setBackground(new Background(new BackgroundFill(
                Color.web(locked ? "#0c0a08" : bgHex), new CornerRadii(8), null)));
        card.setStyle("-fx-border-color:" + (locked ? "#1e1410" : accent)
                + "; -fx-border-width:2; -fx-border-radius:8;");

        Label stLbl = lbl("STAGE " + num, 14, locked ? C_MUTED : accent, true);
        Label timLbl = lbl("Time: " + time, 10, locked ? "#281c10" : C_TEXT, false);
        Label difLbl = lbl(diff, 10, locked ? "#281c10" : accent, true);

        if (locked) {
            card.getChildren().addAll(stLbl, timLbl, difLbl,
                    lbl("Clear Stage " + (num - 1), 8, "#201408", false));
        } else {
            Button start = btn("[ START ]", accent, true);
            start.setPrefWidth(144);
            start.setOnAction(e -> { MainGame.selectedStage = num; if (onPlay != null) onPlay.run(); });
            card.getChildren().addAll(stLbl, timLbl, difLbl, start);
        }
        return card;
    }

    private HBox endlessCard() {
        boolean locked = 4 > MainGame.unlockedStages;
        String accent  = "#9b59b6";
        String bgHex   = locked ? "#0c0a08" : "#1a0a28";

        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 30, 18, 30));
        card.setPrefWidth(630);
        card.setBackground(new Background(new BackgroundFill(
                Color.web(bgHex), new CornerRadii(8), null)));
        card.setStyle("-fx-border-color:" + (locked ? "#1e1410" : accent)
                + "; -fx-border-width:2; -fx-border-radius:8;");

        VBox info = new VBox(8);
        info.setAlignment(Pos.CENTER_LEFT);
        info.getChildren().addAll(
                lbl("STAGE 4 · ENDLESS", 14, locked ? C_MUTED : accent, true),
                lbl("∞ Survive as long as you can", 9, locked ? "#281c10" : C_TEXT, false),
                lbl("All zombies · High drops · Shotguns", 8, locked ? "#201408" : "#7a5a9a", false)
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (locked) {
            VBox lockBox = new VBox(6);
            lockBox.setAlignment(Pos.CENTER);
            lockBox.getChildren().addAll(
                    lbl("Clear Stage 3", 8, "#201408", false));
            card.getChildren().addAll(info, spacer, lockBox);
        } else {
            Button start = btn("[ START ]", accent, true);
            start.setPrefWidth(144);
            start.setOnAction(e -> { MainGame.selectedStage = 4; if (onPlay != null) onPlay.run(); });
            card.getChildren().addAll(info, spacer, start);
        }
        return card;
    }

    private void showSettings(StackPane holder) {
        holder.getChildren().clear();

        VBox layout = new VBox(22);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));

        Label title = lbl("SETTINGS", 22, C_TEXT, true);

        SoundManager sm = SoundManager.getInstance();

        VBox volPanel = new VBox(14);
        volPanel.setPadding(new Insets(18));
        volPanel.setMaxWidth(480);
        volPanel.setBackground(new Background(new BackgroundFill(
                Color.web("#0a0806"), new CornerRadii(8), null)));
        volPanel.setStyle("-fx-border-color:#2a1808; -fx-border-width:1; -fx-border-radius:8;");
        volPanel.getChildren().add(lbl("VOLUME", 10, C_GOLD, true));
        volPanel.getChildren().add(sliderRow("Music", sm.getMusicVolume(), v -> sm.setMusicVolume((float) v)));
        volPanel.getChildren().add(sliderRow("SFX", sm.getSfxVolume(), v -> sm.setSfxVolume((float) v)));

        VBox keysPanel = new VBox(10);
        keysPanel.setPadding(new Insets(18));
        keysPanel.setMaxWidth(480);
        keysPanel.setBackground(new Background(new BackgroundFill(
                Color.web("#0a0806"), new CornerRadii(8), null)));
        keysPanel.setStyle("-fx-border-color:#2a1808; -fx-border-width:1; -fx-border-radius:8;");
        keysPanel.getChildren().add(lbl("KEY BINDINGS", 10, C_GOLD, true));

        for (String[] kv : new String[][]{
                {"W A S D", "Move"},
                {"Left Click", "Shoot toward cursor"},
                {"1 ~ 6", "Switch item"},
                {"F", "Use consumable"},
                {"R", "Restart / Next stage"},
                {"ESC", "Main menu"}}) {
            HBox row = new HBox(16);
            Label k = lbl(kv[0], 9, C_GOLD, true); k.setMinWidth(150);
            Label v = lbl(kv[1], 9, C_TEXT, false);
            row.getChildren().addAll(k, v);
            keysPanel.getChildren().add(row);
        }

        Button back = btn("[ BACK ]", C_MUTED, true);
        back.setOnAction(e -> showMain(holder));

        layout.getChildren().addAll(title, volPanel, keysPanel, back);
        holder.getChildren().add(layout);
    }

    private VBox sliderRow(String labelText, double initValue, java.util.function.DoubleConsumer onChange) {
        VBox box = new VBox(8);

        HBox header = new HBox();
        Label name = lbl(labelText, 9, C_TEXT, false);
        Label pct = lbl(String.format("%.0f%%", initValue * 100), 9, C_GOLD, true);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(name, sp, pct);

        Slider slider = new Slider(0, 1, initValue);
        slider.setMaxWidth(444);
        slider.setStyle("-fx-accent: " + C_GOLD + ";");
        slider.valueProperty().addListener((o, old, v) -> {
            onChange.accept(v.doubleValue());
            pct.setText(String.format("%.0f%%", v.doubleValue() * 100));
        });

        box.getChildren().addAll(header, slider);
        return box;
    }

    private Font getCustomFont(double size, boolean bold) {
        Font customFont = Font.loadFont(getClass().getResourceAsStream("/Font/PressStart2P.ttf"), size);

        if (customFont != null) {
            return customFont;
        }

        return bold ? Font.font("Monospaced", FontWeight.BOLD, size)
                : Font.font("Monospaced", size);
    }

    private Label lbl(String text, double size, String color, boolean bold) {
        Label l = new Label(text);
        l.setFont(getCustomFont(size, bold));
        l.setTextFill(Color.web(color));
        return l;
    }

    private Button btn(String text, String fg, boolean bold) {
        Button b = new Button(text);
        b.setPrefWidth(246); b.setPrefHeight(44);
        b.setFont(getCustomFont(12, bold));
        String base = "-fx-background-color:#0e0a06cc; -fx-text-fill:" + fg
                + "; -fx-background-radius:5; -fx-border-color:" + fg
                + "66; -fx-border-width:1; -fx-border-radius:5; -fx-cursor:hand;";
        String hover = "-fx-background-color:#0e0a06; -fx-text-fill:" + fg
                + "; -fx-background-radius:5; -fx-border-color:" + fg
                + "; -fx-border-width:1.5; -fx-border-radius:5; -fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }
}