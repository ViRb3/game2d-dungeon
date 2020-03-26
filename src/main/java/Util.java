import game2D.Animation;

public class Util {
    public static Animation loadAnimationFromResource(String path, int columns, int rows, int frameDuration) {
        Animation animation = new Animation();
        animation.loadAnimationFromSheet(path, columns, rows, frameDuration);
        return animation;
    }
}
