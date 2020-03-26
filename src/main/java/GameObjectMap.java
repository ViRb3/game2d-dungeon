import game2D.Tile;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Keeps track of GameObjects and the positions of tiles they cover
 */
class GameObjectMap {
    private Map<Point, ArrayList<GameObject>> gameObjectMap = new HashMap<>();
    private Map<GameObject, ArrayList<Point>> reverseGameObjectMap = new HashMap<>();

    GameObjectMap() {
    }

    void update(GameObject gameObject) {
        remove(gameObject);

        List<Tile> surroundingTiles = gameObject.getCoveredTiles();
        for (Tile tile : surroundingTiles) {
            Point tileCoords = new Point(tile.getXC(), tile.getYC());
            addGameObject(tileCoords, gameObject);
            addTileCoords(gameObject, tileCoords);
        }
    }

    public void remove(GameObject gameObject) {
        ArrayList<Point> oldCoords = reverseGameObjectMap.getOrDefault(gameObject, null);
        if (oldCoords != null) {
            for (Point oldCoord : oldCoords) {
                gameObjectMap.get(oldCoord).removeIf(o -> o == gameObject);
            }
            reverseGameObjectMap.get(gameObject).clear();
        }
    }

    private void addGameObject(Point tileCoords, GameObject gameObject) {
        ArrayList<GameObject> gameObjects = gameObjectMap.getOrDefault(tileCoords, null);
        if (gameObjects == null) {
            gameObjects = new ArrayList<>();
        }
        gameObjects.add(gameObject);
        gameObjectMap.put(tileCoords, gameObjects);
    }

    private void addTileCoords(GameObject gameObject, Point tileCoord) {
        ArrayList<Point> tileCoords = reverseGameObjectMap.getOrDefault(gameObject, null);
        if (tileCoords == null) {
            tileCoords = new ArrayList<>();
        }
        tileCoords.add(tileCoord);
        reverseGameObjectMap.put(gameObject, tileCoords);
    }

    Optional<List<GameObject>> get(int x, int y) {
        return Optional.ofNullable(gameObjectMap.getOrDefault(new Point(x, y), null));
    }
}
