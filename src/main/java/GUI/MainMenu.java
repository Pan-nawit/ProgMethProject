package GUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainMenu {

    public enum Screen { MAIN, SETTINGS, STAGE_SELECT }

    private Screen currentScreen = Screen.MAIN;
    private Stage stage;
    private Runnable onPlayCallback; // callback to start game

    // Settings state
    private double musicVolume = 0.7;
    private double sfxVolume = 0.8;

    public MainMenu(Stage stage, Runnable onPlayCallback) {
        this.stage = stage;
        this.onPlayCallback = onPlayCallback;
    }

    public Scene buildScene() {
        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#1a1a2e"), null, null)));

        StackPane contentHolder = new StackPane();
        root.getChildren().add(contentHolder);

        showMain(contentHolder);

        return new Scene(root, 800, 600);
    }

    private void showMain(StackPane holder) {
        holder.getChildren().clear();

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        // Title
        Label title = new Label("☣ ZOMBIE SURVIVAL");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setTextFill(Color.web("#e94560"));

        Label subtitle = new Label("Survive. At all costs.");
        subtitle.setFont(Font.font("Arial", 18));
        subtitle.setTextFill(Color.web("#888888"));

        // Buttons
        Button playBtn = menuButton("▶  PLAY", "#e94560");
        Button settingsBtn = menuButton("⚙  SETTINGS", "#0f3460");
        Button exitBtn = menuButton("✕  EXIT", "#333355");

        playBtn.setOnAction(e -> showStageSelect(holder));
        settingsBtn.setOnAction(e -> showSettings(holder));
        exitBtn.setOnAction(e -> System.exit(0));

        layout.getChildren().addAll(title, subtitle, new Label(""), playBtn, settingsBtn, exitBtn);
        holder.getChildren().add(layout);
    }

    private void showStageSelect(StackPane holder) {
        holder.getChildren().clear();

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        Label title = new Label("SELECT STAGE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#e94560"));

        HBox stagesRow = new HBox(20);
        stagesRow.setAlignment(Pos.CENTER);

        stagesRow.getChildren().addAll(
                stageCard("STAGE 1", "30 Seconds", "Easy", "#2ecc71", 1),
                stageCard("STAGE 2", "60 Seconds", "Medium", "#f39c12", 2),
                stageCard("STAGE 3", "90 Seconds", "Hard", "#e74c3c", 3)
        );

        Button backBtn = menuButton("← BACK", "#333355");
        backBtn.setOnAction(e -> showMain(holder));

        layout.getChildren().addAll(title, new Label(""), stagesRow, new Label(""), backBtn);
        holder.getChildren().add(layout);
    }

    private VBox stageCard(String stageName, String duration, String difficulty, String color, int stageNum) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefSize(180, 200);
        card.setBackground(new Background(new BackgroundFill(Color.web("#0f3460"), new CornerRadii(12), null)));
        card.setStyle("-fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 12;");

        Label nameLabel = new Label(stageName);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.web(color));

        Label durLabel = new Label("⏱ " + duration);
        durLabel.setFont(Font.font("Arial", 14));
        durLabel.setTextFill(Color.WHITE);

        Label diffLabel = new Label(difficulty);
        diffLabel.setFont(Font.font("Arial", 13));
        diffLabel.setTextFill(Color.web(color));

        Button startBtn = new Button("START");
        startBtn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 20;"
        );
        startBtn.setOnAction(e -> {
            MainGame.selectedStage = stageNum;
            if (onPlayCallback != null) onPlayCallback.run();
        });

        card.getChildren().addAll(nameLabel, durLabel, diffLabel, new Label(""), startBtn);
        return card;
    }

    private void showSettings(StackPane holder) {
        holder.getChildren().clear();

        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));

        Label title = new Label("⚙  SETTINGS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#e94560"));

        // Music volume
        VBox musicBox = sliderSetting("Music Volume", musicVolume, val -> musicVolume = val);
        VBox sfxBox = sliderSetting("SFX Volume", sfxVolume, val -> sfxVolume = val);

        // Key bindings info
        VBox keysBox = new VBox(8);
        keysBox.setAlignment(Pos.CENTER_LEFT);
        keysBox.setPadding(new Insets(15));
        keysBox.setBackground(new Background(new BackgroundFill(Color.web("#0f3460"), new CornerRadii(8), null)));
        keysBox.setMaxWidth(400);

        Label keysTitle = new Label("Key Bindings");
        keysTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        keysTitle.setTextFill(Color.web("#e94560"));

        String[][] keys = {{"W/A/S/D", "Move"}, {"Mouse Click", "Shoot"}, {"R", "Restart / Next Stage"}};
        for (String[] pair : keys) {
            HBox row = new HBox(10);
            Label key = new Label(pair[0]);
            key.setMinWidth(120);
            key.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            key.setTextFill(Color.web("#f39c12"));
            Label desc = new Label(pair[1]);
            desc.setFont(Font.font("Arial", 13));
            desc.setTextFill(Color.WHITE);
            row.getChildren().addAll(key, desc);
            keysBox.getChildren().add(row);
        }
        keysBox.getChildren().add(0, keysTitle);

        Button backBtn = menuButton("← BACK", "#333355");
        backBtn.setOnAction(e -> showMain(holder));

        layout.getChildren().addAll(title, musicBox, sfxBox, keysBox, backBtn);
        holder.getChildren().add(layout);
    }

    private VBox sliderSetting(String labelText, double initialValue, java.util.function.DoubleConsumer onChange) {
        VBox box = new VBox(5);
        box.setMaxWidth(400);

        HBox header = new HBox();
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Arial", 15));
        lbl.setTextFill(Color.WHITE);
        Label valLbl = new Label(String.format("%.0f%%", initialValue * 100));
        valLbl.setFont(Font.font("Arial", 15));
        valLbl.setTextFill(Color.web("#e94560"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(lbl, spacer, valLbl);

        Slider slider = new Slider(0, 1, initialValue);
        slider.setMaxWidth(400);
        slider.valueProperty().addListener((obs, old, val) -> {
            onChange.accept(val.doubleValue());
            valLbl.setText(String.format("%.0f%%", val.doubleValue() * 100));
        });

        box.getChildren().addAll(header, slider);
        return box;
    }

    private Button menuButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(260);
        btn.setPrefHeight(48);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }
}