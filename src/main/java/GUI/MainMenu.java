package GUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainMenu {

    private Stage stage;
    private Runnable onPlayCallback;
    private double musicVolume = 0.7;
    private double sfxVolume = 0.8;

    private static final String FONT = "Press Start 2P";

    public MainMenu(Stage stage, Runnable onPlayCallback) {
        this.stage = stage;
        this.onPlayCallback = onPlayCallback;
    }

    public Scene buildScene() {

        // load custom font
        Font.loadFont(getClass().getResourceAsStream("/PressStart2P.ttf"), 24);

        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#1a1a2e"), null, null)));

        StackPane holder = new StackPane();
        root.getChildren().add(holder);

        showMain(holder);

        return new Scene(root, 800, 600);
    }

    private void showMain(StackPane holder) {
        holder.getChildren().clear();

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        Label title = new Label("ZOMBAL SURVIVIE");
        title.setFont(Font.font(FONT, 32));
        title.setTextFill(Color.web("#e94560"));

        Label subtitle = new Label("Survive. At all costs.");
        subtitle.setFont(Font.font(FONT, 12));
        subtitle.setTextFill(Color.web("#888888"));

        Button playBtn = menuButton("PLAY", "#e94560");
        Button settingsBtn = menuButton("SETTINGS", "#0f3460");
        Button exitBtn = menuButton("EXIT", "#333355");

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
        title.setFont(Font.font(FONT, 20));
        title.setTextFill(Color.web("#e94560"));

        HBox stagesRow = new HBox(20);
        stagesRow.setAlignment(Pos.CENTER);

        stagesRow.getChildren().addAll(
                stageCard("STAGE 1", "30 Seconds", "Easy", "#2ecc71", 1),
                stageCard("STAGE 2", "60 Seconds", "Medium", "#f39c12", 2),
                stageCard("STAGE 3", "90 Seconds", "Hard", "#e74c3c", 3)
        );

        Button backBtn = menuButton("BACK", "#333355");
        backBtn.setOnAction(e -> showMain(holder));

        layout.getChildren().addAll(title, new Label(""), stagesRow, new Label(""), backBtn);
        holder.getChildren().add(layout);
    }

    private VBox stageCard(String stageName, String duration, String diff, String color, int stageNum) {

        boolean locked = stageNum > MainGame.unlockedStages;

        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefSize(180, 220);

        String borderColor = locked ? "#444455" : color;
        String bgColor = locked ? "#111122" : "#0f3460";

        card.setBackground(new Background(new BackgroundFill(Color.web(bgColor), new CornerRadii(12), null)));
        card.setStyle("-fx-border-color: " + borderColor + "; -fx-border-width: 2; -fx-border-radius: 12;");

        Label nameLabel = new Label(stageName);
        nameLabel.setFont(Font.font(FONT, 12));
        nameLabel.setTextFill(locked ? Color.web("#555566") : Color.web(color));

        Label durLabel = new Label(duration);
        durLabel.setFont(Font.font(FONT, 10));
        durLabel.setTextFill(locked ? Color.web("#444455") : Color.WHITE);

        Label diffLabel = new Label(diff);
        diffLabel.setFont(Font.font(FONT, 10));
        diffLabel.setTextFill(locked ? Color.web("#444455") : Color.web(color));

        if (locked) {

            Label lockLabel = new Label("LOCKED");
            lockLabel.setFont(Font.font(FONT, 10));
            lockLabel.setTextFill(Color.web("#555566"));

            Label hintLabel = new Label("Clear Stage " + (stageNum - 1));
            hintLabel.setFont(Font.font(FONT, 8));
            hintLabel.setTextFill(Color.web("#444455"));

            card.getChildren().addAll(nameLabel, durLabel, diffLabel, new Label(""), lockLabel, hintLabel);

        } else {

            Button startBtn = new Button("START");
            startBtn.setFont(Font.font(FONT, 10));
            startBtn.setStyle(
                    "-fx-background-color: " + color + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8 20;"
            );

            startBtn.setOnAction(e -> {
                MainGame.selectedStage = stageNum;
                if (onPlayCallback != null) onPlayCallback.run();
            });

            card.getChildren().addAll(nameLabel, durLabel, diffLabel, new Label(""), startBtn);
        }

        return card;
    }

    private void showSettings(StackPane holder) {

        holder.getChildren().clear();

        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));

        Label title = new Label("SETTINGS");
        title.setFont(Font.font(FONT, 20));
        title.setTextFill(Color.web("#e94560"));

        VBox musicBox = sliderSetting("Music Volume", musicVolume, v -> musicVolume = v);
        VBox sfxBox = sliderSetting("SFX Volume", sfxVolume, v -> sfxVolume = v);

        Button backBtn = menuButton("BACK", "#333355");
        backBtn.setOnAction(e -> showMain(holder));

        layout.getChildren().addAll(title, musicBox, sfxBox, backBtn);

        holder.getChildren().add(layout);
    }

    private VBox sliderSetting(String label, double init, java.util.function.DoubleConsumer cb) {

        VBox box = new VBox(5);
        box.setMaxWidth(420);

        HBox header = new HBox();

        Label lbl = new Label(label);
        lbl.setFont(Font.font(FONT, 10));
        lbl.setTextFill(Color.WHITE);

        Label val = new Label(String.format("%.0f%%", init * 100));
        val.setFont(Font.font(FONT, 10));
        val.setTextFill(Color.web("#e94560"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(lbl, spacer, val);

        Slider slider = new Slider(0, 1, init);

        slider.valueProperty().addListener((o, old, v) -> {
            cb.accept(v.doubleValue());
            val.setText(String.format("%.0f%%", v.doubleValue() * 100));
        });

        box.getChildren().addAll(header, slider);

        return box;
    }

    private Button menuButton(String text, String color) {

        Button btn = new Button(text);

        btn.setPrefWidth(260);
        btn.setPrefHeight(48);
        btn.setFont(Font.font(FONT, 12));

        btn.setStyle(
                "-fx-background-color:" + color + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-background-radius:10;" +
                        "-fx-cursor:hand;"
        );

        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));

        return btn;
    }
}