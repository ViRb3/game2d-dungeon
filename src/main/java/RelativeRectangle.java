import java.awt.*;

public class RelativeRectangle extends Rectangle {
    public RelativeRectangle(double x, double y, double width, double height) {
        super((int) x, (int) y, (int) width, (int) height);
    }

    public RelativeRectangle(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
}
