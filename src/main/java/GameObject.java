import game2D.Sprite;
import game2D.Tile;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

abstract class GameObject {
    protected GameState gameState;
    protected List<RelativeRectangle> collisionBoxes;
    protected Sprite sprite;
    protected boolean flipped;
    // if disabled does not rebound from anything
    protected boolean solid = true;
    // if enabled does not rebound from any GameObjects
    protected boolean trigger = true;

    public GameObject(GameState gameState, AnimationState animationState) {
        this.gameState = gameState;
        this.sprite = new Sprite(animationState.getAnimation());
        this.collisionBoxes = animationState.getCollisionBoxes();
    }

    public void setScale(float scale) {
        sprite.setScale(scale);
    }

    public void setState(AnimationState animationState) {
        if (sprite.getAnimation() == animationState.getAnimation()) {
            return;
        }
        this.sprite.setAnimation(animationState.getAnimation());
        this.sprite.setAnimationFrame(0);
        this.collisionBoxes = animationState.getCollisionBoxes();
        restoreFlipState();
    }

    private void restoreFlipState() {
        if (flipped) {
            sprite.setFlipped(true);
            flipCollisionBoxes();
        } else {
            sprite.setFlipped(false);
        }
    }

    private void flipCollisionBoxes() {
        collisionBoxes.forEach(this::flipCollisionBox);
    }

    private void flipCollisionBox(RelativeRectangle collisionBox) {
        collisionBox.x = getUnscaledWidth() - (collisionBox.x + collisionBox.width);
    }

    public int getUnscaledWidth() {
        return sprite.getWidth();
    }

    public List<Tile> getCoveredTiles() {
        List<Tile> tiles = new ArrayList<>();
        for (AbsoluteRectangle collisionBox : getAbsoluteCollisionBoxes()) {
            tiles.addAll(gameState.getTilesAtAbsoluteRect(collisionBox));
        }
        return tiles;
    }

    public void tickUpdate(long elapsed) {
        applyGravity(elapsed);
        sprite.update(elapsed);

        List<TileCollision> collidingTiles = getCollidingTiles();
        List<GameObjectCollision> collidingGameObjects = getCollidingGameObjects();
        preReboundTick(elapsed, collidingTiles, collidingGameObjects);

        if (solid) {
            for (int i = 0; i < 100; i++) {
                collidingTiles = getCollidingTiles();
                if (!trigger)
                    collidingGameObjects = getCollidingGameObjects();
                else {
                    collidingGameObjects.clear();
                }
                Optional<Point> rebound = handleRebounds(collidingTiles, collidingGameObjects);
                if (!rebound.isPresent()) {
                    break;
                }
                if (rebound.get().x != 0) {
                    setX(getX() - rebound.get().x);
                    setVelocityX(0);
                }
                if (rebound.get().y != 0) {
                    setY(getY() - rebound.get().y);
                    setVelocityY(0);
                }
            }
        }

        postReboundTick(elapsed, collidingTiles, collidingGameObjects);
    }

    public abstract void preReboundTick(long elapsed, List<TileCollision> collidingTiles, List<GameObjectCollision> collidingGameObjects);

    public abstract void postReboundTick(long elapsed, List<TileCollision> collidingTiles, List<GameObjectCollision> collidingGameObjects);

    public void draw(Graphics2D g, int xo, int yo) {
        sprite.setOffsets(xo, yo);
        updateFlipped();
        sprite.draw(g);
        if (gameState.isDrawCollisionBoxes()) {
            drawCollisionBoxes(g, xo, yo);
        }
    }

    private void updateFlipped() {
        if (flipped && !sprite.isFlipped()) {
            sprite.setFlipped(true);
            flipCollisionBoxes();
        } else if (!flipped && sprite.isFlipped()) {
            sprite.setFlipped(false);
            flipCollisionBoxes();
        }
    }

    private void drawCollisionBoxes(Graphics2D g, int xo, int yo) {
        g.setColor(Color.red);
        for (AbsoluteRectangle collisionRect : getAbsoluteCollisionBoxes()) {
            drawOffsetRect(g, xo, yo, collisionRect);
        }

        AbsoluteRectangle collisionRect = getFullBodyRectangle();
        g.setColor(Color.blue);
        drawOffsetRect(g, xo, yo, collisionRect);
    }

    private void drawOffsetRect(Graphics2D g, int xo, int yo, AbsoluteRectangle collisionRect) {
        g.drawRect(collisionRect.x + xo, collisionRect.y + yo, collisionRect.width, collisionRect.height);
    }

    private List<AbsoluteRectangle> getAbsoluteCollisionBoxes() {
        return collisionBoxes.stream()
                .map(this::relativeToAbsolute)
                .collect(Collectors.toList());
    }

    public AbsoluteRectangle getFullBodyRectangle() {
        return new AbsoluteRectangle(getX(), getY(), getWidth(), getHeight());
    }

    // Convert rectangle that is relative to this GameObject into an absolute rectangle
    protected AbsoluteRectangle relativeToAbsolute(RelativeRectangle relativeRect) {
        AbsoluteRectangle rect = new AbsoluteRectangle(calcScaled(relativeRect.x),
                calcScaled(relativeRect.y), calcScaled(relativeRect.width), calcScaled(relativeRect.height));
        rect.translate(getX(), getY());
        return rect;
    }

    public int getX() {
        return (int) sprite.getX();
    }

    public void setCoords(Point pt) {
        setX(pt.x);
        setY(pt.y);
    }

    public void setX(int x) {
        sprite.setX(x);
        gameState.gameObjectMap.update(this);
    }

    public int getY() {
        return (int) sprite.getY();
    }

    public void setY(int y) {
        sprite.setY(y);
        gameState.gameObjectMap.update(this);
    }

    public int getWidth() {
        return calcScaled(sprite.getWidth());
    }

    public int getHeight() {
        return calcScaled(sprite.getHeight());
    }

    private int calcScaled(int number) {
        return (int) (number * sprite.getScale());
    }

    public abstract void keyPressed(KeyEvent e);

    public abstract void keyReleased(KeyEvent e);

    public int getUnscaledHeight() {
        return sprite.getHeight();
    }

    public void setVelocityX(float x) {
        sprite.setVelocityX(x);
    }

    public void setVelocityY(float y) {
        sprite.setVelocityY(0);
    }

    public void show() {
        sprite.show();
    }

    public int getCenterX() {
        return getX() + (getWidth() / 2);
    }

    public int getCenterY() {
        return getY() + (getHeight() / 2);
    }

    // Returns colliding pairs of this GameObject's collision boxes and another GameObject's collision boxes
    private List<Pair<AbsoluteRectangle, AbsoluteRectangle>> getCollidingBoxes(GameObject object) {
        ArrayList<Pair<AbsoluteRectangle, AbsoluteRectangle>> list = new ArrayList<>();
        for (AbsoluteRectangle c : getAbsoluteCollisionBoxes()) {
            for (AbsoluteRectangle c2 : object.getAbsoluteCollisionBoxes()) {
                if (c.intersects(c2))
                    list.add(new Pair<>(c, c2));
            }
        }
        return list;
    }

    // Gets this GameObject's collision boxes that collide with a tile
    private List<AbsoluteRectangle> getCollidingBoxes(Tile tile) {
        Rectangle tileRect = new Rectangle(tile.getXC(), tile.getYC(), gameState.tileMap.getTileWidth(),
                gameState.tileMap.getTileHeight());
        return getAbsoluteCollisionBoxes().stream()
                .filter(c -> c.intersects(tileRect))
                .collect(Collectors.toList());
    }

    // Gets all tiles that collide with this GameObject
    private List<TileCollision> getCollidingTiles() {
        List<TileCollision> list = new ArrayList<>();
        for (Tile tile : getCoveredTiles()) {
            List<AbsoluteRectangle> collisions = getCollidingBoxes(tile);
            if (collisions.size() > 0) {
                list.add(new TileCollision(tile, collisions));
            }
        }
        return list;
    }

    private List<GameObjectCollision> getCollidingGameObjects() {
        List<GameObjectCollision> list = new ArrayList<>();
        getCoveredTiles().stream()
                .map(t -> gameState.gameObjectMap.get(t.getXC(), t.getYC()))
                .filter(Optional::isPresent)
                .flatMap(os -> os.get().stream())
                .filter(o -> o.trigger)
                .filter(o -> o != this)
                .forEach(o -> {
                    List<Pair<AbsoluteRectangle, AbsoluteRectangle>> collidingBoxes = getCollidingBoxes(o);
                    list.add(new GameObjectCollision(o, collidingBoxes));
                });
        return list;
    }

    private void applyGravity(long elapsed) {
        sprite.setVelocityY(sprite.getVelocityY() + (gameState.gravity * elapsed));
    }

    private Optional<Point> handleRebounds(List<TileCollision> collidingTiles, List<GameObjectCollision> collidingGameObjects) {
        Optional<Point> a = calcBiggestTileRebound(collidingTiles);
        Optional<Point> b = calcBiggestGameObjectRebound(collidingGameObjects);
        if (a.isPresent() && b.isPresent()) {
            return Optional.of(calcBiggestChange(a.get(), b.get()));
        } else if (a.isPresent()) {
            return a;
        } else return b;
    }

    private Optional<Point> calcBiggestGameObjectRebound(List<GameObjectCollision> collidingGameObjects) {
        Optional<Point> result = Optional.empty();
        for (GameObjectCollision collision : collidingGameObjects) {
            GameObject targetGameObject = collision.getGameObject();
            if (!gameState.isBlockingGameObject(targetGameObject)) {
                continue;
            }
            for (Pair<AbsoluteRectangle, AbsoluteRectangle> pair : collision.getCollisionBoxes()) {
                Point rebound = calcSmallestRebound(pair.x, pair.y);
                result = result
                        .map(point -> Optional.of(calcBiggestChange(point, rebound)))
                        .orElseGet(() -> Optional.of(rebound));
            }
        }
        return result;
    }

    private Optional<Point> calcBiggestTileRebound(List<TileCollision> collidingTiles) {
        Optional<Point> result = Optional.empty();
        for (TileCollision collision : collidingTiles) {
            Tile tile = collision.getTile();
            if (!gameState.isBlockingTile(tile)) {
                continue;
            }
            AbsoluteRectangle tileRect = gameState.getTileAbsoluteRect(tile);
            for (AbsoluteRectangle collisionBox : collision.getCollisionBoxes()) {
                Point rebound = calcSmallestRebound(collisionBox, tileRect);
                result = result
                        .map(point -> Optional.of(calcBiggestChange(point, rebound)))
                        .orElseGet(() -> Optional.of(rebound));
            }
        }
        return result;
    }

    private Point calcBiggestChange(Point a, Point b) {
        if (Math.abs(a.x) < Math.abs(b.x)) {
            a.x = b.x;
        }
        if (Math.abs(a.y) < Math.abs(b.y)) {
            a.y = b.y;
        }
        return a;
    }

    private Point calcSmallestRebound(AbsoluteRectangle collisionBox, AbsoluteRectangle targetRect) {
        Rectangle penetration = collisionBox.intersection(targetRect);
        if (penetration.width < penetration.height) {
            int sign = Integer.signum(targetRect.x - collisionBox.x);
            return new Point(sign * penetration.width, 0);
        } else {
            int sign = Integer.signum(targetRect.y - collisionBox.y);
            return new Point(0, sign * penetration.height);
        }
    }
}
