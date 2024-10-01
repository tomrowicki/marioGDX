package tomrowicki.mario.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;
import tomrowicki.mario.MarioBros;
import tomrowicki.mario.scenes.Hud;
import tomrowicki.mario.screens.PlayScreen;

import static tomrowicki.mario.MarioBros.BRICK_BIT;
import static tomrowicki.mario.MarioBros.DESTROYED_BIT;

public class Brick extends InteractiveTileObject{

    public Brick(PlayScreen screen, MapObject object) {
        super(screen, object);
        fixture.setUserData(this);
        setCategoryFilter(BRICK_BIT);
    }

    @Override
    public void onHeadHit() {
        Gdx.app.log("Brick", "onHeadHit");
        setCategoryFilter(DESTROYED_BIT);
        getCell().setTile(null);
        Hud.addScore(200);
        MarioBros.manager.get("audio/sounds/breakblock.wav", Sound.class).play();
    }
}
