package tomrowicki.mario.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import tomrowicki.mario.scenes.Hud;

import static tomrowicki.mario.MarioBros.BRICK_BIT;
import static tomrowicki.mario.MarioBros.DESTROYED_BIT;

public class Brick extends InteractiveTileObject{

    public Brick(World world, TiledMap map, Rectangle bounds) {
        super(world, map, bounds);
        fixture.setUserData(this);
        setCategoryFilter(BRICK_BIT);
    }

    @Override
    public void onHeadHit() {
        Gdx.app.log("Brick", "onHeadHit");
        setCategoryFilter(DESTROYED_BIT);
        getCell().setTile(null);
        Hud.addScore(200);
    }
}
