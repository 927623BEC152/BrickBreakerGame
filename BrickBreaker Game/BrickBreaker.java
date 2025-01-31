import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;

public class BrickBreaker extends JFrame implements KeyListener, ActionListener {
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 10;
    private static final int BALL_RADIUS = 20;
    private static final int ROWS = 5;
    private static final int COLS = 8;
    private static final int BRICK_GAP = 4;
    private static final String HIGH_SCORE_FILE = "highscore.dat";
    private static final int STEALTH_RANGE = 100; // Brick visibility range for Stealth Mode

    private Timer timer;
    private int paddleX = SCREEN_WIDTH / 2 - PADDLE_WIDTH / 2;
    private int ballX = SCREEN_WIDTH / 2;
    private int ballY = SCREEN_HEIGHT / 2;
  
  private int ballXDir = -2;
    private int ballYDir = -3;
    private boolean[][] bricks;
    private String gameMode = "Classic";
    private int score = 0;
    private int highScore = 0;
    private boolean isPaused = false; // Pause state
    private GamePanel gamePanel;

    public BrickBreaker(String mode) {
        this.gameMode = mode;

        setTitle("Brick Breaker - " + mode);
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loadHighScore();

        gamePanel = new GamePanel();
        add(gamePanel);
        addKeyListener(this);

        bricks = new boolean[ROWS][COLS];
        initializeBricks();

        timer = new Timer(10, this);
       
 timer.start();
           setVisible(true);
    }
     private void initializeBricks() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                bricks[i][j] = true;
            }
        }
        if (gameMode.equals("Puzzle Fusion")) {
            initializePuzzleFusionBricks();
        }
    }
    private void initializePuzzleFusionBricks() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                bricks[i][j] = false;
            }
        }
        bricks[0][0] = true;
        bricks[1][0] = true;
        bricks[2][0] = true;
        bricks[2][1] = true;
        bricks[0][5] = true;
        bricks[1][5] = true;
        bricks[2][5] = true;
        bricks[2][4] = true;
    
}
  private void loadHighScore() {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(HIGH_SCORE_FILE))) {
            highScore = dis.readInt();
        } catch (IOException e) {
            highScore = 0;
        }
    }
    private void saveHighScore() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(HIGH_SCORE_FILE))) {
            dos.writeInt(highScore);
        } catch (IOException e) {
            System.err.println("Failed to save high score.");
        }
    }
   @Override
    public void actionPerformed(ActionEvent e) {
        if (isPaused) return; // Skip updates when paused
        if (gameMode.equals("Inverted Mode")) {
            paddleX = ballX - PADDLE_WIDTH / 2;
            if (paddleX < 0) paddleX = 0;
            if (paddleX > SCREEN_WIDTH - PADDLE_WIDTH) paddleX = SCREEN_WIDTH - PADDLE_WIDTH;
        }

       
        ballX += ballXDir;
        ballY += ballYDir;
        if (ballX <= 0 || ballX >= SCREEN_WIDTH - BALL_RADIUS) ballXDir = -ballXDir;
        if (ballY <= 0) ballYDir = -ballYDir;
        if (new Rectangle(ballX, ballY, BALL_RADIUS, BALL_RADIUS)
                .intersects(new Rectangle(paddleX, SCREEN_HEIGHT - 50, PADDLE_WIDTH, PADDLE_HEIGHT))) {
            ballYDir = -ballYDir;
        }
        int brickWidth = (SCREEN_WIDTH - (COLS + 1) * BRICK_GAP) / COLS;
        int brickHeight = 30;
        boolean allBricksCleared = true;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (bricks[i][j]) {
                    allBricksCleared = false;
                    int brickX = j * (brickWidth + BRICK_GAP) + BRICK_GAP;
                    int brickY = i * (brickHeight + BRICK_GAP) + 50;
                    if (new Rectangle(ballX, ballY, BALL_RADIUS, BALL_RADIUS)
                            .intersects(new Rectangle(brickX, brickY, brickWidth, brickHeight))) {
                        bricks[i][j] = false;
                        ballYDir = -ballYDir;
                        score += 10;
                    }
                }
         
   }
        }

        if (allBricksCleared) {
            endGame("You Won!");
        }
        if (ballY > SCREEN_HEIGHT) {
            endGame("Game Over!");
        }
        gamePanel.repaint();
    }
    private void endGame(String message) {
        timer.stop();
        if (score > highScore) {
            highScore = score;
            saveHighScore();
            message += " New High Score!";
        }
        JOptionPane.showMessageDialog(this, message + " Final Score: " + score, "Brick Breaker", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P) {
            isPaused = !isPaused; // Toggle pause state
            return;
       
     }
        if (!isPaused) {
            if (!gameMode.equals("Inverted Mode")) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT && paddleX > 0) {
                    paddleX -= 20;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT && paddleX < SCREEN_WIDTH - PADDLE_WIDTH) {
                    paddleX += 20;
                }
            } else {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) ballXDir = Math.max(ballXDir - 1, -5);
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) ballXDir = Math.min(ballXDir + 1, 5);
                if (e.getKeyCode() == KeyEvent.VK_UP) ballYDir = Math.max(ballYDir - 1, -5);
                if (e.getKeyCode() == KeyEvent.VK_DOWN) ballYDir = Math.min(ballYDir + 1, 5);
            }
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

  
       private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            g.setColor(Color.RED);
            g.fillRect(paddleX, SCREEN_HEIGHT - 50, PADDLE_WIDTH, PADDLE_HEIGHT);
            g.setColor(Color.WHITE);
            g.fillOval(ballX, ballY, BALL_RADIUS, BALL_RADIUS);
            int brickWidth = (SCREEN_WIDTH - (COLS + 1) * BRICK_GAP) / COLS;
            int brickHeight = 30;
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    if (bricks[i][j]) {
                        int brickX = j * (brickWidth + BRICK_GAP) + BRICK_GAP;
                        int brickY = i * (brickHeight + BRICK_GAP) + 50;
                        // Stealth Mode: Make bricks visible only if within range
                        if (gameMode.equals("Stealth Mode")) {
                            int ballCenterX = ballX + BALL_RADIUS / 2;
                            int ballCenterY = ballY + BALL_RADIUS / 2;
                            int brickCenterX = brickX + brickWidth / 2;
                            int brickCenterY = brickY + brickHeight / 2;
                            double distance = Math.sqrt(Math.pow(ballCenterX - brickCenterX, 2) + Math.pow(ballCenterY - brickCenterY, 2));
                           
                       if (distance > STEALTH_RANGE) {
                                continue; // Skip rendering brick
                            }
                        }
                        switch (i) {
                            case 0 -> g.setColor(Color.RED);
                            case 1 -> g.setColor(Color.ORANGE);
                            case 2 -> g.setColor(Color.YELLOW);
                            case 3 -> g.setColor(Color.GREEN);
                            case 4 -> g.setColor(Color.BLUE);
                        }
                        g.fillRect(brickX, brickY, brickWidth, brickHeight);
                    }
                }
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + score, 20, 30);
            g.drawString("High Score: " + highScore, SCREEN_WIDTH - 200, 30);
            if (isPaused) {
                g.setFont(new Font("Arial", Font.BOLD, 40));
                g.drawString("PAUSED", SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2);
            }
        }
    }

 
   public static void main(String[] args) {
        String[] options = {"Classic", "Puzzle Fusion", "Inverted Mode", "Stealth Mode"};
        int choice = JOptionPane.showOptionDialog(null, "Select Game Mode:", "Brick Breaker Modes",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == -1) {
            System.exit(0);
        }
        String mode = options[choice];
        new BrickBreaker(mode);
    }
}

