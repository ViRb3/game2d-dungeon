import game2D.Animation;
import game2D.Sound;

import java.awt.event.KeyEvent;
import java.util.List;

class Treasure extends GameObject {
    private AnimationState openState;
    private static Sound points = new Sound("sounds/points.wav");

    public Treasure(GameState gameState, AnimationState closedState, AnimationState openState) {
        super(gameState, closedState);
        this.openState = openState;
        this.trigger = true;
    }

    public static Treasure create(GameState gameState) {
        Animation closed = new Animation();
        closed.loadAnimationFromSheet("images/treasure_closed.png", 1, 1, 60);
        Animation open = new Animation();
        open.loadAnimationFromSheet("images/treasure_open.png", 1, 1, 60);
        Treasure npc = new Treasure(gameState, new AnimationState(closed), new AnimationState(open));
        npc.setScale(4);
        return npc;
    }

    @Override
    public void preReboundTick(long elapsed, List<TileCollision> collidingTiles, List<GameObjectCollision> collidingGameObjects) {
        if (sprite.getAnimation() == openState.getAnimation()) {
            return;
        }

        if (collidingGameObjects.stream()
                .map(GameObjectCollision::getGameObject)
                .filter(o -> o instanceof Player)
                .map(o -> (Player) o)
                .anyMatch(Player::isAttacking)) {
            claim();
        }
    }

    public void claim() {
        points.play();
        setState(openState);
        gameState.score += 200;
    }

    @Override
    public void postReboundTick(long elapsed, List<TileCollision> collidingTiles, List<GameObjectCollision> collidingGameObjects) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
