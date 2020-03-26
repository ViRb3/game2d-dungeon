import game2D.Tile;

import java.util.List;

public class TileCollision {
    public TileCollision(Tile tile, List<AbsoluteRectangle> collisionBoxes) {
        this.tile = tile;
        this.collisionBoxes = collisionBoxes;
    }

    private Tile tile;
    private List<AbsoluteRectangle> collisionBoxes;

    public Tile getTile() {
        return tile;
    }

    public List<AbsoluteRectangle> getCollisionBoxes() {
        return collisionBoxes;
    }
}
