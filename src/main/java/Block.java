import game2D.Animation;

import java.awt.event.KeyEvent;
import java.util.List;

class Block extends GameObject {
    public Block(GameState gameState, AnimationState animationState) {
        super(gameState, animationState);
        this.trigger = false;
    }

    public static Block create(GameState gameState) {
        Animation animation = new Animation();
        animation.loadAnimationFromSheet("images/block.png", 1, 1, 60);
        Block npc = new Block(gameState, new AnimationState(animation));
        npc.setScale(4);
        return npc;
    }

    @Override
    public void preReboundTick(long elapsed, List<TileCollision> collidingTiles, List<GameObjectCollision> collidingGameObjects) {

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
