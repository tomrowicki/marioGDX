package tomrowicki.mario;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import tomrowicki.mario.screens.PlayScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MarioBros extends Game {

    public static final int V_WIDTH = 400; // v stands for 'virtual'
    public static final int V_HEIGHT = 208;
    public static final float PPM = 100; // pixels per meter

    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new PlayScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
