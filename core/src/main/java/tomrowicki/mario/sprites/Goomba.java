package tomrowicki.mario.sprites;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import tomrowicki.mario.MarioBros;
import tomrowicki.mario.screens.PlayScreen;

import static tomrowicki.mario.MarioBros.*;

public class Goomba extends Enemy {

    private float stateTime;
    private Animation<TextureRegion> walkAnimation;
    private Array<TextureRegion> frames;
    private boolean setToDestroy;
    private boolean destroyed;

    public Goomba(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<>();
        for (int i = 0; i < 2; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("goomba"), i * 16, 0, 16, 16));
        }
        walkAnimation = new Animation<>(0.4f, frames);
        stateTime = 0;
        setBounds(getX(), getY(), 16 / PPM, 16 / PPM);
        setToDestroy = false;
        destroyed = false;
    }

    public void update(float dt){
        stateTime += dt;
        if(setToDestroy && !destroyed){
            world.destroyBody(b2body);
            destroyed = true;
            setRegion(new TextureRegion(screen.getAtlas().findRegion("goomba"), 32, 0, 16, 16));
            stateTime = 0;
        }
        else if(!destroyed) {
            b2body.setLinearVelocity(velocity);
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
            setRegion(walkAnimation.getKeyFrame(stateTime, true));
        }
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Turtle) {
            if (((Turtle) enemy).currentState == Turtle.State.MOVING_SHELL) {
                setToDestroy = true;
            }
            reverseVelocity(true, false);
        } else {
            reverseVelocity(true, false);
        }
    }

    @Override
    protected void defineEnemy() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / PPM);

        fdef.filter.categoryBits = ENEMY_BIT;
        fdef.filter.maskBits = GROUND_BIT | COIN_BIT | BRICK_BIT | ENEMY_BIT | OBJECT_BIT | MARIO_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        // Create the head
        PolygonShape head = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-5, 8).scl(1 / PPM);
        vertices[1] = new Vector2(5, 8).scl(1 / PPM);
        vertices[2] = new Vector2(-3, 3).scl(1 / PPM);
        vertices[3] = new Vector2(3, 3).scl(1 / PPM);
        head.set(vertices);

        fdef.shape = head;
        fdef.restitution = 0.5f; // allows bouncing off the head
        fdef.filter.categoryBits = ENEMY_HEAD_BIT;
        b2body.createFixture(fdef).setUserData(this);
    }

    public void draw(SpriteBatch batch) {
        if (!destroyed || stateTime < 1) {
            super.draw(batch);
        }
    }

    @Override
    public void hitOnHead(Mario mario) {
        setToDestroy = true;
        MarioBros.manager.get("audio/sounds/stomp.wav", Sound.class).play();
    }
}
