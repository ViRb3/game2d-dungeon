import game2D.Tile;
import game2D.TileMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

class GameState {
    public static int screenWidth = 1024;
    public static int screenHeight = 768;
    public static int screenHeightMiddle = screenHeight / 2;
    public static int screenWidthMiddle = screenWidth / 2;

    public float gravity = 0.002f;
    public long score = 0;

    public TileMap tileMap = new TileMap();
    public GameObjectMap gameObjectMap = new GameObjectMap();
    public List<GameObject> gameObjects = new ArrayList<>();
    private List<GameObject> garbage = new ArrayList<>();

    public boolean gameOver = false;
    public boolean levelCompleted = false;

    private Player player;
    private boolean drawCollisionBoxes;

    // Get offset needed for camera to center on the player
    public Point getCameraOffset() {
        Point offset = new Point(getPlayer().getCenterX() - screenWidthMiddle,
                getPlayer().getCenterY() - screenHeightMiddle);
        reboundCameraOffset(offset, EnumSet.of(Direction.LEFT, Direction.RIGHT));
        // we want to add these offsets to the player's x and y, so invert them
        offset.x = -offset.x;
        offset.y = -offset.y;
        return offset;
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT;
    }

    public boolean isDrawCollisionBoxes() {
        return drawCollisionBoxes;
    }

    public void setDrawCollisionBoxes(boolean status) {
        drawCollisionBoxes = status;
        tileMap.setDrawCollisionBoxes(status);
    }

    // Rebound the camera offset so it does not look outside of the game map
    private void reboundCameraOffset(Point offset, EnumSet<Direction> directions) {
        int boundsStartX = 0;
        int boundsEndX = tileMap.getPixelWidth();
        int boundsStartY = 0;
        int boundsEndY = tileMap.getPixelHeight();
        if (offset.x < boundsStartX && directions.contains(Direction.LEFT)) {
            offset.x = boundsStartX;
        } else if (offset.x + screenWidth > boundsEndX && directions.contains(Direction.RIGHT)) {
            offset.x = boundsEndX - screenWidth;
        }
        if (offset.y < boundsStartY && directions.contains(Direction.UP)) {
            offset.y = boundsStartY;
        } else if (offset.y + screenHeight > boundsEndY && directions.contains(Direction.DOWN)) {
            offset.y = boundsEndY - screenHeight;
        }
    }

    public Player getPlayer() {
        while (player == null) {
            Optional<GameObject> result = gameObjects.stream().filter(o -> o instanceof Player).findFirst();
            if (result.isPresent()) {
                player = (Player) result.get();
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return player;
    }

    public boolean isBlockingTile(Tile tile) {
        char c = tile.getCharacter();
        switch (c) {
            case 'f':
            case 'w':
                return true;
            default:
                return false;
        }
    }

    public boolean isBlockingGameObject(GameObject gameObject) {
        return true;
    }

    public AbsoluteRectangle getTileAbsoluteRect(Tile tile) {
        return new AbsoluteRectangle(tile.getXC(), tile.getYC(), tileMap.getTileWidth(), tileMap.getTileHeight());
    }

    // Get all tiles intersecting with given rectangle
    public List<Tile> getTilesAtAbsoluteRect(AbsoluteRectangle rect) {
        List<Tile> tiles = new ArrayList<>();
        int tileWidth = tileMap.getTileWidth();
        int tileHeight = tileMap.getTileHeight();
        int startX = rect.x / tileWidth;
        int startY = rect.y / tileHeight;
        int endX = (int) Math.ceil((rect.x + rect.width) / (float) tileWidth);
        int endY = (int) Math.ceil((rect.y + rect.height) / (float) tileHeight);
        for (int x = startX; x < endX; x++)
            for (int y = startY; y < endY; y++) {
                Tile tile = tileMap.getTile(x, y);
                if (tile != null)
                    tiles.add(tile);
            }
        return tiles;
    }

    public void addGameObject(GameObject gameObject) {
        gameObjects.add(gameObject);
        gameObjectMap.update(gameObject);
    }

    public void removeGameObject(GameObject gameObject) {
        gameObjectMap.remove(gameObject);
        garbage.add(gameObject);
    }

    public void collectGarbage() {
        garbage.forEach(o -> gameObjects.remove(o));
    }

    public Point calcTileCoordsToAbsolute(int x, int y) {
        return new Point(x * tileMap.getTileWidth(), y * tileMap.getTileHeight());
    }

    public Point calcAbsoluteCoordsToTile(int x, int y) {
        return new Point(x / tileMap.getTileWidth(), y / tileMap.getTileHeight());
    }
}
