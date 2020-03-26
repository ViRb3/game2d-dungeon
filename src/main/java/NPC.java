import game2D.Animation;
import game2D.Sound;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

class NPC extends GameObject {
    private int direction = -1;
    private float forceX = -0.15f;

    private static Sound deathSound = new Sound("sounds/kill.wav");

    public NPC(GameState gameState, AnimationState animationState) {
        super(gameState, animationState);
    }

    public static NPC create(GameState gameState) {
        Animation idle = new Animation();
        idle.loadAnimationFromSheet("images/npc.png", 8, 1, 60);
        RelativeRectangle collisionBox = new RelativeRectangle(4, 0, 10, 16);
        NPC npc = new NPC(gameState, new AnimationState(idle, collisionBox));
        npc.setScale(5);
        return npc;
    }

    private Character[] boundaries = new Character[]{'m', 'w'};

    @Override
    public void preReboundTick(long elapsed, List<TileCollision> collidingTiles, List<GameObjectCollision> collidingGameObjects) {
        if (!solid) {
            // this NPC is dying
            if (this.getY() > gameState.tileMap.getPixelHeight() + GameState.screenHeight) {
                // delete it if out of bounds
                gameState.removeGameObject(this);
            }
            return;
        }

        if (collidingGameObjects.stream()
                .map(GameObjectCollision::getGameObject)
                .filter(o -> o instanceof Player)
                .map(o -> (Player) o)
                .anyMatch(Player::isAttacking)) {
            die();
            return;
        }

        if (collidingTiles.stream()
                .anyMatch(c -> Arrays.stream(boundaries)
                        .anyMatch(b -> b == c.getTile().getCharacter())) ||
                collidingGameObjects.stream()
                        .anyMatch(c -> c.getGameObject() instanceof NPC)) {
            direction *= -1;
            flipped = !flipped;
        }
        sprite.setVelocityX(direction * forceX);
    }

    public void die() {
        deathSound.play();
        gameState.score += 100;
        solid = false;
        trigger = false;
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
