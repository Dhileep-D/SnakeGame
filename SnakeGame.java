
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

/**
 * Retro Snake Game in Java (Swing) --------------------------------
 * Single-file, ready to compile and run: javac SnakeGame.java java SnakeGame
 *
 * Controls: Arrow Keys - Move P - Pause/Resume R - Restart (after Game Over or
 * anytime) ESC - Quit
 */
public class SnakeGame extends JFrame {

    public SnakeGame() {
        setTitle("Retro Snake – Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new GamePanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SnakeGame::new);
    }

    /**
     * GamePanel handles all game logic, drawing and input
     */
    static class GamePanel extends JPanel implements ActionListener, KeyListener {

        // Board & Gameplay constants
        static final int SCREEN_WIDTH = 600;
        static final int SCREEN_HEIGHT = 600;
        static final int UNIT_SIZE = 24; // grid cell size
        static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
        static final int STARTING_BODY = 5;
        static final int DELAY_MS = 90; // lower = faster

        // Snake state
        final int[] x = new int[GAME_UNITS]; // x-coordinates of snake body
        final int[] y = new int[GAME_UNITS]; // y-coordinates of snake body
        int bodyParts = STARTING_BODY;
        int applesEaten = 0;
        int appleX, appleY;
        char direction = 'R'; // 'U', 'D', 'L', 'R'
        boolean running = false;
        boolean paused = false;

        // Engine
        Timer timer;
        Random random;

        // UI helpers
        final Font scoreFont = new Font("Consolas", Font.BOLD, 20);
        final Font bigFont = new Font("Consolas", Font.BOLD, 48);
        final Stroke gridStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{2f, 2f}, 0f);

        GamePanel() {
            setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
            setBackground(new Color(18, 18, 18));
            setFocusable(true);
            addKeyListener(this);
            random = new Random();
            startGame();
        }

        void startGame() {
            bodyParts = STARTING_BODY;
            applesEaten = 0;
            direction = 'R';
            running = true;
            paused = false;
            // place head at center-left
            int startX = SCREEN_WIDTH / 2 - (STARTING_BODY * UNIT_SIZE);
            int startY = SCREEN_HEIGHT / 2;
            for (int i = 0; i < bodyParts; i++) {
                x[i] = startX - i * UNIT_SIZE;
                y[i] = startY;
            }
            newApple();
            if (timer != null) {
                timer.stop();
            }
            timer = new Timer(DELAY_MS, this);
            timer.start();
            repaint();
        }

        void newApple() {
            // Ensure apple spawns on a free cell (not on the snake)
            boolean placed = false;
            while (!placed) {
                appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
                appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
                placed = true;
                for (int i = 0; i < bodyParts; i++) {
                    if (x[i] == appleX && y[i] == appleY) {
                        placed = false;
                        break;
                    }
                }
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw grid (subtle)
            g2.setColor(new Color(60, 60, 60));
            g2.setStroke(gridStroke);
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                int y = i * UNIT_SIZE;
                g2.drawLine(0, y, SCREEN_WIDTH, y);
            }
            for (int i = 0; i < SCREEN_WIDTH / UNIT_SIZE; i++) {
                int x = i * UNIT_SIZE;
                g2.drawLine(x, 0, x, SCREEN_HEIGHT);
            }

            // Draw apple
            g2.setColor(new Color(255, 68, 68));
            g2.fillRoundRect(appleX + 3, appleY + 3, UNIT_SIZE - 6, UNIT_SIZE - 6, 8, 8);

            // Draw snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g2.setColor(new Color(0, 200, 140));
                } else {
                    // create a simple gradient tail
                    int shade = (int) (180 - (120.0 * i / Math.max(1, bodyParts - 1)));
                    g2.setColor(new Color(0, Math.max(shade, 40), 100));
                }
                g2.fillRoundRect(x[i] + 2, y[i] + 2, UNIT_SIZE - 4, UNIT_SIZE - 4, 10, 10);
            }

            // Score & status
            g2.setColor(new Color(240, 240, 240));
            g2.setFont(scoreFont);
            String scoreText = "Score: " + applesEaten;
            g2.drawString(scoreText, 12, 24);

            if (!running) {
                drawGameOver(g2);
            } else if (paused) {
                drawPaused(g2);
            }

            g2.dispose();
        }

        void drawPaused(Graphics2D g2) {
            g2.setFont(bigFont);
            String msg = "PAUSED";
            centerText(g2, msg, SCREEN_HEIGHT / 2);
            g2.setFont(scoreFont);
            centerText(g2, "Press P to resume", SCREEN_HEIGHT / 2 + 30);
        }

        void drawGameOver(Graphics2D g2) {
            g2.setFont(bigFont);
            centerText(g2, "GAME OVER", SCREEN_HEIGHT / 2 - 10);
            g2.setFont(scoreFont);
            centerText(g2, "Final Score: " + applesEaten, SCREEN_HEIGHT / 2 + 20);
            centerText(g2, "Press R to Restart or ESC to Quit", SCREEN_HEIGHT / 2 + 45);
        }

        void centerText(Graphics2D g2, String text, int y) {
            FontMetrics metrics = g2.getFontMetrics(g2.getFont());
            int x = (SCREEN_WIDTH - metrics.stringWidth(text)) / 2;
            g2.drawString(text, x, y);
        }

        void move() {
            // shift body
            for (int i = bodyParts; i > 0; i--) {
                x[i] = x[i - 1];
                y[i] = y[i - 1];
            }
            // move head
            switch (direction) {
                case 'U' ->
                    y[0] -= UNIT_SIZE;
                case 'D' ->
                    y[0] += UNIT_SIZE;
                case 'L' ->
                    x[0] -= UNIT_SIZE;
                case 'R' ->
                    x[0] += UNIT_SIZE;
            }
        }

        void checkApple() {
            if (x[0] == appleX && y[0] == appleY) {
                bodyParts++;
                applesEaten++;
                // Increase speed slightly every 5 apples
                if (applesEaten % 5 == 0 && timer.getDelay() > 40) {
                    timer.setDelay(timer.getDelay() - 5);
                }
                newApple();
            }
        }

        void checkCollisions() {
            // Check self-collision
            for (int i = bodyParts; i > 0; i--) {
                if (x[0] == x[i] && y[0] == y[i]) {
                    running = false;
                    break;
                }
            }
            // Check wall collision
            if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
                running = false;
            }
            if (!running) {
                timer.stop();
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (running && !paused) {
                move();
                checkApple();
                checkCollisions();
            }
            repaint();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
            if (key == KeyEvent.VK_P) {
                if (running) {
                    paused = !paused;
                }
            }
            if (key == KeyEvent.VK_R) {
                startGame();
                return;
            }
            // prevent reversing into itself
            if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && direction != 'R') {
                direction = 'L';
            }
            if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && direction != 'L') {
                direction = 'R';
            }
            if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && direction != 'D') {
                direction = 'U';
            }
            if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && direction != 'U') {
                direction = 'D';
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }
}
