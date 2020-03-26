import java.util.List;

public class GameObjectCollision {
    private GameObject gameObject;
    private List<Pair<AbsoluteRectangle, AbsoluteRectangle>> collisionBoxes;

    public GameObjectCollision(GameObject gameObject, List<Pair<AbsoluteRectangle, AbsoluteRectangle>> collisionBoxes) {
        this.gameObject = gameObject;
        this.collisionBoxes = collisionBoxes;
    }

    public GameObject getGameObject() {
        return gameObject;
    }

    public List<Pair<AbsoluteRectangle, AbsoluteRectangle>> getCollisionBoxes() {
        return collisionBoxes;
    }
}
