import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static javafx.application.Application.launch;
import static javafx.scene.input.KeyCode.A;
import static javafx.scene.input.KeyCode.D;
import static javafx.scene.input.KeyCode.S;
import static javafx.scene.input.KeyCode.W;


public class SnakeGame extends Application {
    
    private static final int INITIAL_SPEED = 4;
    private int currentSpeed = INITIAL_SPEED;
    private static final int BOARD_WIDTH = 25;
    private static final int BOARD_HEIGHT = 20;
    private int foodX;
    private int foodY;
    private static final int CELL_SIZE = 25;
    private List<Position> snake = new ArrayList<>();
    private Direction direction = Direction.LEFT;
    private Direction lastDirection = Direction.LEFT;
    private boolean gameOver = false;
    private Random random = new Random();
    private int score = 0;
    private Text scoreText = new Text("Score: " + score);

    public enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    public static class Position {
        int x;
        int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public boolean equals(Position other) {
            return this.x == other.x && this.y == other.y;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            initializeGame();

            BorderPane root = new BorderPane();
            Canvas canvas = new Canvas(BOARD_WIDTH * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE + 2 * CELL_SIZE);
            GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

            Button restartButton = new Button("Restart");
            restartButton.setOnAction(e -> resetGame());

            HBox topContainer = new HBox(restartButton, scoreText);
            topContainer.setAlignment(Pos.CENTER);
            topContainer.setSpacing(10);

            root.setTop(topContainer);
            root.setCenter(canvas);

            CheckerBackground(root);

            final long[] then = {System.nanoTime()};
            new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (gameOver) {
                        GameOver(graphicsContext);
                        return;
                    }

                    if (now - then[0] > 1000000000 / currentSpeed) {
                        then[0] = now;
                        moveSnake(graphicsContext);
                    }
                }
            }.start();

            Scene scene = new Scene(root, BOARD_WIDTH * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE + 2 * CELL_SIZE);

            
            scene.addEventFilter(KeyEvent.KEY_PRESSED, this::KeyPress);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Snake Game");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeGame() {
        generateFood();
        initializeSnake();
    }

    private void initializeSnake() {
        snake.clear();
        int initialSnakeSize = 3;
        for (int i = 0; i < initialSnakeSize; i++) {
            snake.add(new Position(BOARD_WIDTH / 2, BOARD_HEIGHT / 2));
        }
    }

    private void generateFood() {
        do {
            foodX = random.nextInt(BOARD_WIDTH);
            foodY = random.nextInt(BOARD_HEIGHT);
        } while (snake.contains(new Position(foodX, foodY)));
    }

    private void moveSnake(GraphicsContext graphicsContext) {
        updateSnakePosition();

        FoodEating();

        SelfDestruction();

        GameBoard(graphicsContext);

        lastDirection = direction;
    }

    private void updateSnakePosition() {
        for (int i = snake.size() - 1; i >= 1; i--) {
            snake.get(i).x = snake.get(i - 1).x;
            snake.get(i).y = snake.get(i - 1).y;
        }

        switch (direction) {
            case UP -> snake.get(0).y--;
            case DOWN -> snake.get(0).y++;
            case LEFT -> snake.get(0).x--;
            case RIGHT -> snake.get(0).x++;
        }

        SnakeWrap();
    }

    private void SnakeWrap() {
        snake.get(0).x = Math.floorMod(snake.get(0).x, BOARD_WIDTH);
        snake.get(0).y = Math.floorMod(snake.get(0).y, BOARD_HEIGHT);
    }

    private void FoodEating() {
        if (foodX == snake.get(0).x && foodY == snake.get(0).y) {
            snake.add(new Position(-1, -1));
            score++;
            scoreText.setText("Score: " + score);
            generateFood();
            Speedup();
        }
    }

    private void SelfDestruction() {
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(0).equals(snake.get(i))) {
                gameOver = true;
            }
        }
    }

    private void GameBoard(GraphicsContext graphicsContext) {
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.clearRect(0, 0, BOARD_WIDTH * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE + 2 * CELL_SIZE);

        drawFood(graphicsContext);

        drawSnake(graphicsContext);
    }

    private void drawFood(GraphicsContext graphicsContext) {
        graphicsContext.setFill(Color.GOLDENROD);
        graphicsContext.setStroke(Color.RED);
        graphicsContext.fillRect(foodX * CELL_SIZE + 1, foodY * CELL_SIZE + 1 + CELL_SIZE, CELL_SIZE - 2, CELL_SIZE - 2);
        graphicsContext.strokeRect(foodX * CELL_SIZE + 1, foodY * CELL_SIZE + 1 + CELL_SIZE, CELL_SIZE - 2, CELL_SIZE - 2);
    }

    private void drawSnake(GraphicsContext graphicsContext) {
        for (int i = 0; i < snake.size(); i++) {
            graphicsContext.setFill(i == 0 ? Color.RED : Color.GOLDENROD);
            graphicsContext.fillRect(snake.get(i).x * CELL_SIZE + 1, snake.get(i).y * CELL_SIZE + 1 + CELL_SIZE, CELL_SIZE - 2,
                    CELL_SIZE - 2);
            graphicsContext.setStroke(Color.BLACK);
            graphicsContext.strokeRect(snake.get(i).x * CELL_SIZE + 1, snake.get(i).y * CELL_SIZE + 1 + CELL_SIZE, CELL_SIZE - 2,
                    CELL_SIZE - 2);
        }
    }

    private void KeyPress(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case W -> direction = (lastDirection != Direction.DOWN) ? Direction.UP : lastDirection;
            case A -> direction = (lastDirection != Direction.RIGHT) ? Direction.LEFT : lastDirection;
            case S -> direction = (lastDirection != Direction.UP) ? Direction.DOWN : lastDirection;
            case D -> direction = (lastDirection != Direction.LEFT) ? Direction.RIGHT : lastDirection;
        }
    }

    private void GameOver(GraphicsContext graphicsContext) {
        graphicsContext.setFill(Color.RED);
        graphicsContext.setFont(new Font("", 50));
        graphicsContext.fillText("GAME OVER", 100, 250);
    }

    private void Speedup() {
        currentSpeed++;
    }

    private void resetGame() {
        initializeSnake();
        direction = Direction.LEFT;
        lastDirection = Direction.LEFT;
        gameOver = false;
        currentSpeed = INITIAL_SPEED;
        score = 0;
        scoreText.setText("Score: " + score);
        generateFood();
    }

    private void CheckerBackground(BorderPane root) {
        BackgroundFill[] fill = {
               new BackgroundFill(Color.web("#9ccc47"), CornerRadii.EMPTY, null)
        };
        root.setBackground(new Background(fill, null));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
