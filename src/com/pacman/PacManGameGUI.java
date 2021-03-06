package com.pacman;

import com.pacman.engine.InputManager;
import com.pacman.engine.AwtGraphicsAdapter;
import com.pacman.engine.Drawer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PacManGameGUI extends PacManGame {

    private static final Color TEXT_COLOR = Color.WHITE;
    private static final boolean FULL_SCREEN = true;
    private static final int PANEL_WIDTH_TILE = 25;
    private static final int PANEL_HEIGHT_TILE = 9;

    private JFrame frame;
    private PacManPanel panel;
    private boolean playerWon;
    private boolean playerLost;
    private int currentLevel;

    public PacManGameGUI() throws IOException {
        super(LevelMap.loadFromImg(ImageIO.read(PacManGameGUI.class.getResourceAsStream("/level1.png"))), 30);
    }

    public void playGame(KeyListener listener) {
        this.frame = new JFrame();
        if (FULL_SCREEN) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);
        }
        frame.setVisible(true);
        if (FULL_SCREEN) resize();
        panel = new PacManPanel();
        frame.add(panel);
        if (listener != null) frame.addKeyListener(listener);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (!FULL_SCREEN) frame.pack();
        setup();
        super.playGame();
    }

    private void setup() {
    	currentLevel = getLevel();
    	setLevel(currentLevel + 1);
        playerWon = false;
        playerLost = false;
        panel.updateInfo();
    }

    private void resize() {
        int size;
        if (frame.getWidth() >= frame.getHeight()) {
            size = frame.getHeight() / (levelMap.getHeight() + 2);
        } else {
            size = frame.getWidth() / levelMap.getWidth();
        }
        setSize(size);
    }

    @Override
    protected void update() {
        panel.updateInfo();
        frame.repaint();
        if (playerLost || playerWon) {
            Boolean playAgain = null;
            do {
                InputManager.getInputs();
                if (InputManager.keyPressed(KeyEvent.VK_A)) {
                    playAgain = true;
                } else if (InputManager.keyPressed(KeyEvent.VK_B)) {
                    playAgain = false;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (playAgain == null);
            if (currentLevel < 3 && playAgain) {
            	try {
					this.levelMap = LevelMap.loadFromImg(ImageIO.read(PacManGameGUI.class.getResourceAsStream("/level" + currentLevel + ".png")));
				} catch (IOException e) {
					e.printStackTrace();
				}
                setup();
                super.playGame();
            } else {
                frame.setVisible(false);
                SwingUtilities.invokeLater(frame::dispose);
            }
        }
    }

    @Override
    protected void onDeath() {
        System.out.println("You died!");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onLose() {
        System.out.println("You lose!");
        playerLost = true;
        update();
    }

    @Override
    protected void onLevelComplete() {
        System.out.println("You win!");
        playerWon = true;
        update();
    }

    private class PacManPanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JLabel scoreLabel, livesLabel;

        public PacManPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            scoreLabel = new JLabel("Score: 0");
            livesLabel = new JLabel("Lives: " + lives);

            String fontName = scoreLabel.getFont().getName();
            Font font = new Font(fontName, Font.PLAIN, size);
            scoreLabel.setFont(font);
            livesLabel.setFont(font);

            scoreLabel.setForeground(TEXT_COLOR);
            livesLabel.setForeground(TEXT_COLOR);
            scoreLabel.setBackground(BG_COLOR);
            livesLabel.setBackground(BG_COLOR);
            livesLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            GamePanel gamePanel = new GamePanel();

            JPanel info = new JPanel();
            info.setBackground(BG_COLOR);
            info.setLayout(new GridLayout(1, 2));
            info.add(scoreLabel);
            info.add(livesLabel);
            Dimension size = new Dimension(gamePanel.getPreferredSize().width, 2 * PacManGameGUI.this.size);
            info.setPreferredSize(size);
            info.setMaximumSize(size);

            add(info);
            add(gamePanel);

            setBackground(BG_COLOR);
        }

        private void updateInfo() {
            scoreLabel.setText("Score: " + getScore());
            livesLabel.setText("Lives: " + lives);
        }
    }

    private class GamePanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Map<Integer, Font> pixelSizeToFont;

        public GamePanel() {
            Dimension size = new Dimension(levelMap.getWidth() * PacManGameGUI.this.size, levelMap.getHeight() * PacManGameGUI.this.size);
            setPreferredSize(size);
            setMaximumSize(size);
            pixelSizeToFont = new HashMap<>();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Drawer d = new AwtGraphicsAdapter(g);
            draw(d);

            if (playerWon || playerLost) {
                int panelWidth = size * PANEL_WIDTH_TILE;
                int panelHeight = size * PANEL_HEIGHT_TILE;

                int centerX = getWidth() / 2;

                int panelX = centerX - panelWidth / 2;
                int panelY = getHeight() / 4;

                int y = panelY;

                g.setColor(TEXT_COLOR);
                g.fillRect(panelX, panelY, panelWidth, panelHeight);

                g.setColor(BG_COLOR);
                int border = size / 4;
                g.fillRect(panelX + border, panelY + border,
                        panelWidth - 2 * border, panelHeight - 2 * border);

                String s = playerWon ? "You win!" : "You lose!";
                drawText(g, centerX, y, size * 4, s);
                y += size * 4;
                drawText(g, centerX, y, size * 3 / 2, String.format("Score: % 3d", getScore()));

                y += size * 3 / 2;
                if(getLevel() < 3)
                	drawText(g, centerX - panelWidth / 4, y, size * 3 / 2, "A - Next level");
                drawText(g, centerX + panelWidth / 4, y, size * 3 / 2, "B - Quit");
            }
        }

        private Font getFontWithSize(Graphics g, Font font, int fontSizePixels) {
            Font cached = pixelSizeToFont.get(fontSizePixels);
            if (cached != null && cached.getFontName().equals(font.getFontName())) {
                return cached;
            }
            Font f;
            float size = 1f;
            while (g.getFontMetrics(f = font.deriveFont(size)).getHeight() < fontSizePixels) {
                size++;
            }
            pixelSizeToFont.put(fontSizePixels, f);
            return f;
        }

        private void drawText(Graphics g, int x, int y, int fontSizePixels, String s) {
            Font f = getFontWithSize(g, g.getFont(), fontSizePixels);
            g.setFont(f);
            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(s);
            int textHeight = metrics.getHeight();
            g.setColor(TEXT_COLOR);
            g.drawString(s, x - textWidth / 2, y + textHeight);
        }
    }
}
