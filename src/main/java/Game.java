import game2D.GameCore;
import game2D.Sound;
import game2D.Util;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

public class Game extends GameCore {
    private GameState state;
    private int level;

    public static void main(String[] args) {
        Game game = new Game();
        game.init(1);
        game.run(false, GameState.screenWidth, GameState.screenHeight);
    }

    private void init(int level) {
        long oldScore = 0;
        if (state != null && !state.gameOver) {
            oldScore = state.score;
        }
        state = new GameState();
        state.score += oldScore;

        this.level = level;
        state.tileMap.loadMap("maps", String.format("map%d.txt", level), 4);

        Player player = Player.create(state);
        player.setX(2 * state.tileMap.getTileWidth());
        player.setY(1);
        player.show();

        if (level == 1) {
            initLevelOne();
        } else {
            initLevelTwo();
        }

        // player always on top
        state.addGameObject(player);
    }

    private void initLevelOne() {
        NPC npc = NPC.create(state);
        npc.setCoords(state.calcTileCoordsToAbsolute(5, 9));
        npc.show();

        Block block = Block.create(state);
        block.setCoords(state.calcTileCoordsToAbsolute(9, 3));
        block.show();

        state.addGameObject(block);
        state.addGameObject(npc);
    }

    private void initLevelTwo() {
        NPC npc = NPC.create(state);
        npc.setCoords(state.calcTileCoordsToAbsolute(6, 9));
        npc.show();
        NPC npc2 = NPC.create(state);
        npc2.setCoords(state.calcTileCoordsToAbsolute(12, 9));
        npc2.show();
        NPC npc3 = NPC.create(state);
        npc3.setCoords(state.calcTileCoordsToAbsolute(8, 16));
        npc3.show();
        NPC npc4 = NPC.create(state);
        npc4.setCoords(state.calcTileCoordsToAbsolute(12, 16));
        npc4.show();
        NPC npc5 = NPC.create(state);
        npc5.setCoords(state.calcTileCoordsToAbsolute(14, 16));
        npc5.show();

        Treasure treasure = Treasure.create(state);
        treasure.setCoords(state.calcTileCoordsToAbsolute(7, 12));
        treasure.show();

        state.addGameObject(treasure);
        state.addGameObject(npc);
        state.addGameObject(npc2);
        state.addGameObject(npc3);
        state.addGameObject(npc4);
        state.addGameObject(npc5);
    }

    public void keyReleased(KeyEvent e) {
        state.gameObjects.forEach(o -> o.keyReleased(e));
    }

    public void keyPressed(KeyEvent e) {
        if (state.gameOver || state.levelCompleted && level > 1) {
            init(1);
            return;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                stop();
                break;
            case KeyEvent.VK_F:
                state.setDrawCollisionBoxes(!state.isDrawCollisionBoxes());
                break;
        }
        state.gameObjects.forEach(o -> o.keyPressed(e));
    }

    public void update(long elapsed) {
        for (GameObject gameObject : state.gameObjects)
            gameObject.tickUpdate(elapsed);
        state.collectGarbage();
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.decode("#332a29"));
        g.fillRect(0, 0, getWidth(), getHeight());

        if (state.levelCompleted) {
            if (level > 1) {
                gameWon(g);
            } else {
                init(level + 1);
            }
            return;
        }

        if (state.gameOver) {
            gameOver(g);
            return;
        }

        Point cameraOffset = state.getCameraOffset();
        // Apply offsets to tile map and draw it
        state.tileMap.draw(g, cameraOffset.x, cameraOffset.y);
        // then draw sprites
        state.gameObjects.forEach(o -> o.draw(g, cameraOffset.x, cameraOffset.y));
        // and finally score
        printScore(g);

    }

    private static Sound winSound = new Sound(new RandomFadeFilterStream(Util.readResource("sounds/win.wav")));

    private void gameWon(Graphics g) {
        winSound.play();
        String a = "Game Won!";
        String b = String.format("Score: %d", state.score);
        String c = "Press any key to restart...";
        g.setColor(Color.white);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 50));
        g.drawString(a, 100, 100);
        g.drawString(b, 100, 200);
        g.drawString(c, 100, 300);
    }

    private void gameOver(Graphics g) {
        String a = "Game Over!";
        String b = String.format("Score: %d", state.score);
        String c = "Press any key to restart...";
        g.setColor(Color.white);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 50));
        g.drawString(a, 100, 100);
        g.drawString(b, 100, 200);
        g.drawString(c, 100, 300);
    }

    private void printScore(Graphics g) {
        String msg = String.format("Score: %d", state.score);
        g.setColor(Color.white);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 15));
        g.drawString(msg, getWidth() - 80, 50);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point absoluteCoords = state.getCameraOffset();
        absoluteCoords.x -= e.getX();
        absoluteCoords.x *= -1;
        absoluteCoords.y -= e.getY();
        absoluteCoords.y *= -1;
        Point tileCoords = state.calcAbsoluteCoordsToTile(absoluteCoords.x, absoluteCoords.y);
        Point absoluteCoordsRounded = state.calcTileCoordsToAbsolute(tileCoords.x, tileCoords.y);

        Optional<List<GameObject>> gameObjects = state.gameObjectMap.get(absoluteCoordsRounded.x, absoluteCoordsRounded.y);
        if (gameObjects.isPresent()) {
            for (GameObject gameObject : gameObjects.get()) {
                if (gameObject instanceof Treasure) {
                    ((Treasure) gameObject).claim();
                } else if (gameObject instanceof NPC) {
                    ((NPC) gameObject).die();
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
