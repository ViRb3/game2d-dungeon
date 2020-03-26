import game2D.Animation;
import game2D.Sound;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

class Player extends GameObject {
    private boolean inAir = false;
    private boolean pressedUp = false;
    private boolean pressedRight = false;
    private boolean pressedLeft = false;

    private float forceX = -0.3f;
    private float forceY = -0.7f;

    private AnimationState idleState;
    private AnimationState runningState;
    private AnimationState attackingState;

    private static Sound jumpSound = new Sound("sounds/jump.wav");
    private static Sound deathSound = new Sound("sounds/death.wav");
    private static Sound hitSound = new Sound("sounds/hit.wav");

    public boolean isAttacking() {
        return attacking;
    }

    private boolean attacking;

    public Player(GameState gameState, AnimationState idleState, AnimationState runningState, AnimationState attackingState) {
        super(gameState, idleState);
        this.idleState = idleState;
        this.runningState = runningState;
        this.attackingState = attackingState;
    }

    public static Player create(GameState gameState) {
        Animation idle = Util.loadAnimationFromResource("images/player_idle.png", 4, 1, 100);
        AnimationState idleState = new AnimationState(idle, new RelativeRectangle(20, 11, 10, 25));

        Animation running = Util.loadAnimationFromResource("images/player_running.png", 6, 1, 100);
        AnimationState runningState = new AnimationState(running, new RelativeRectangle(24, 12, 9, 24));

        Animation attacking = Util.loadAnimationFromResource("images/player_attacking.png", 4, 1, 100);
        List<RelativeRectangle> attackingCollisionBoxes = new ArrayList<>();
        attackingCollisionBoxes.add(new RelativeRectangle(21, 15, 10, 21));
        attackingCollisionBoxes.add(new RelativeRectangle(21, 15, 24, 12));
        attackingCollisionBoxes.add(new RelativeRectangle(31, 8, 12, 27));
        attackingCollisionBoxes.add(new RelativeRectangle(3, 24, 18, 11));
        AnimationState attackingState = new AnimationState(attacking, attackingCollisionBoxes);

        Player player = new Player(gameState, idleState, runningState, attackingState);
        player.setScale(3);
        return player;
    }

    @Override
    public void preReboundTick(long elapsed, List<TileCollision> collidingTiles, List<GameObjectCollision> collidingGameObjects) {
        if (this.getY() > gameState.tileMap.getPixelHeight() + GameState.screenHeight) {
            if (!solid) {
                // game over
                gameState.removeGameObject(this);
                gameState.gameOver = true;
            } else {
                // level completed!
                gameState.levelCompleted = true;
            }
            return;
        }

        if (!solid) {
            // NPC dying
            return;
        }

        if (!attacking && collidingGameObjects.stream()
                .map(GameObjectCollision::getGameObject)
                .anyMatch(o -> o instanceof NPC)) {
            solid = false;
            trigger = false;
            deathSound.play();
            return;
        }

        inAir = collidingTiles.stream().noneMatch(t -> t.getTile().getCharacter() == 'f');
    }

    @Override
    public void postReboundTick(long elapsed, List<TileCollision> collidingTiles, List<GameObjectCollision> collidingGameObjects) {
        sprite.setAnimationSpeed(1.0f);
        boolean moved = false;
        handleFlip();
        if (attacking) {
            setState(attackingState);
            hitSound.play();
        } else {
            handleJump();
            moved = handleMove();
        }
        if (attacking || !moved) {
            sprite.setVelocityX(sprite.getVelocityX() / 5);
        }
        if (!attacking && !moved) {
            setState(idleState);
        }
    }

    private boolean handleMove() {
        boolean moved = false;
        if (pressedLeft || pressedRight) {
            if (pressedLeft) {
                sprite.setAnimationSpeed(1.8f);
                sprite.setVelocityX(forceX);
            } else {
                sprite.setAnimationSpeed(1.8f);
                sprite.setVelocityX(-forceX);
            }
            setState(runningState);
            moved = true;
        }
        return moved;
    }

    private void handleJump() {
        if (pressedUp && !inAir) {
            jumpSound.play();
            sprite.setAnimationSpeed(1.8f);
            sprite.setVelocityY(forceY);
            inAir = true;
        }
    }

    private void handleFlip() {
        if (pressedLeft) {
            flipped = true;
        } else if (pressedRight) {
            flipped = false;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                pressedUp = true;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                pressedRight = true;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                pressedLeft = true;
                break;
            case KeyEvent.VK_SPACE:
                attacking = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                pressedUp = false;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                pressedRight = false;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                pressedLeft = false;
                break;
            case KeyEvent.VK_SPACE:
                attacking = false;
                break;
        }
    }
}
