package GUI;
import GameLogic.GameLogic;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainGame extends Application {

    private GameLogic gameLogic;
    private boolean w, a, s, d, isMousePressed; // ปุ่มกด

    @Override
    public void start(Stage primaryStage) {
        // 1. ตั้งค่าหน้าจอ
        Canvas canvas = new Canvas(800, 600);
        GameLogic gameLogic = new GameLogic();

        // (หมายเหตุ: ถ้าใช้ JavaFX เพียวๆ แนะนำให้เปลี่ยน method draw ใน Enemy ให้ใช้ GraphicsContext แทน Graphics2D จะง่ายกว่าครับ)
        // แต่ในที่นี้สมมติว่าคุณจัดการเรื่องวาดรูปได้แล้ว

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        // 2. รับ Input
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W -> w = true;
                case A -> a = true;
                case S -> s = true;
                case D -> d = true;
                case R -> { // กด R เพื่อเริ่มใหม่ตอนตาย
                    if (gameLogic.isGameOver) gameLogic.initGame();
                }
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

        primaryStage.setTitle("Survival Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 3. Game Loop (หัวใจสำคัญ)
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // ส่งค่าปุ่มกดไปให้ Logic คำนวณ
                gameLogic.update(w, a, s, d, isMousePressed);

                // เคลียร์หน้าจอเก่า
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.clearRect(0, 0, 800, 600);

                // วาดทุกอย่างใหม่
                // *** ตรงนี้คุณต้องแปลงระบบวาดรูปให้เข้ากับ Platform ที่ใช้ ***
                // เช่น:
                drawGame(gc, gameLogic);
            }
        };
        // สำหรับเช็คว่ากดเมาส์ลง (Start Press)
        scene.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown()) { // เช็คว่าเป็นคลิกซ้าย
                isMousePressed = true;
            }
        });

// สำหรับเช็คว่าปล่อยเมาส์ (Release)
        scene.setOnMouseReleased(e -> {
            isMousePressed = false;
        });
        timer.start();
    }

    // ตัวอย่างฟังก์ชันวาดบน JavaFX
    private void drawGame(GraphicsContext gc, GameLogic logic) {
        // วาดพื้นหลัง
        gc.setFill(javafx.scene.paint.Color.LIGHTGRAY);
        gc.fillRect(0, 0, 800, 600);

        // วาด Player
        gc.setFill(javafx.scene.paint.Color.BLUE);
        gc.fillRect(logic.player.getX(), logic.player.getY(), logic.player.getWidth(), logic.player.getHeight());

        // วาด Enemies
        for (var enemy : logic.enemies) {
            // เช็คสถานะ Hit เพื่อเปลี่ยนสี
            // (ต้องเพิ่ม getter isHit ใน BaseEnemy ถ้าจะใช้)
            gc.setFill(javafx.scene.paint.Color.RED);
            gc.fillRect(enemy.getX(), enemy.getY(), 32, 32); // ใช้ขนาดจาก enemy.getWidth() จะดีกว่า

            // วาดหลอดเลือด Enemy
            gc.setFill(javafx.scene.paint.Color.GREEN);
            double hpPercent = (double)enemy.getHP() / 10.0; // สมมติ MaxHP = 10 (ควรมี method getMaxHP)
            gc.fillRect(enemy.getX(), enemy.getY() - 10, 32 * hpPercent, 5);
        }

        // วาด UI (HP Player)
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillText("HP: " + logic.player.getHp(), 20, 30);
        gc.fillText("Score: " + logic.score, 20, 50);

        if (logic.isGameOver) {
            gc.setFill(javafx.scene.paint.Color.RED);
            gc.fillText("GAME OVER - Press R to Restart", 300, 300);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}