import game2D.Animation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class AnimationState {
    private final Animation animation;
    private final List<RelativeRectangle> collisionBoxes;

    public AnimationState(Animation animation, RelativeRectangle collisionBox) {
        this.animation = animation;
        this.collisionBoxes = new ArrayList<>();
        collisionBoxes.add(collisionBox);
    }

    public AnimationState(Animation animation, List<RelativeRectangle> collisionBoxes) {
        this.animation = animation;
        this.collisionBoxes = collisionBoxes;
    }

    public AnimationState(Animation animation) {
        this.animation = animation;
        this.collisionBoxes = new ArrayList<>();
        // default collision box surrounding whole image
        collisionBoxes.add(new RelativeRectangle(0, 0, animation.getImage().getWidth(null),
                animation.getImage().getHeight(null)));
    }

    public Animation getAnimation() {
        return animation;
    }

    public List<RelativeRectangle> getCollisionBoxes() {
        return collisionBoxes.stream().map(c -> (RelativeRectangle) c.clone()).collect(Collectors.toList());
    }
}
