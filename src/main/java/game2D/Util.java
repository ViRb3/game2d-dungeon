package game2D;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class Util {
    private static ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    public static Image loadImageFromResource(String path) {
        try (InputStream stream = readResource(path)) {
            return new ImageIcon(ImageIO.read(stream)).getImage();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream readResource(String path) {
        return classloader.getResourceAsStream(path);
    }
}
