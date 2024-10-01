package tomrowicki.mario.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import tomrowicki.mario.screens.PlayScreen;

import static tomrowicki.mario.MarioBros.*;

public class Turtle extends Enemy {

    public enum State { WALKING, SHELL, STATIONARY_SHELL, DESTROYED }
    private State currentState;
    private State previousState;

    private float stateTime;
    private Animation<TextureRegion> walkAnimation;
    private TextureRegion shell;
    private Array<TextureRegion> frames;
    private boolean setToDestroy;
    private boolean destroyed;
    private boolean isShellPushed;

    public Turtle(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<>();
        // Tworzymy animację chodzenia żółwia
        for (int i = 0; i < 2; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), i * 16, 0, 16, 24));
        }
        walkAnimation = new Animation<>(0.4f, frames);

        // Tekstura dla "skorupy" żółwia
        shell = new TextureRegion(screen.getAtlas().findRegion("turtle"), 64, 0, 16, 24);

        currentState = previousState = State.WALKING;
        stateTime = 0;
        setBounds(getX(), getY(), 16 / PPM, 24 / PPM);
        setToDestroy = false;
        destroyed = false;
        isShellPushed = false;
    }

    public void update(float dt) {
        stateTime += dt;

        if (setToDestroy && !destroyed) {
            world.destroyBody(b2body);
            destroyed = true;
            stateTime = 0;
        } else if (!destroyed) {
            switch (currentState) {
                case WALKING:
                    b2body.setLinearVelocity(velocity); // Żółw porusza się w przód
                    break;
                case SHELL:
                case STATIONARY_SHELL:
                    b2body.setLinearVelocity(0, 0); // Skorupa się nie rusza
                    break;
                case DESTROYED:
                    break;
            }

            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
            setRegion(getFrame(dt));
        }
    }

    private TextureRegion getFrame(float dt) {
        TextureRegion region;

        switch (currentState) {
            case SHELL:
            case STATIONARY_SHELL:
                region = shell; // Skorupa żółwia
                break;
            case WALKING:
            default:
                region = walkAnimation.getKeyFrame(stateTime, true); // Animacja chodzenia
                break;
        }

        return region;
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

        // Tworzymy głowę żółwia
        PolygonShape head = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-5, 8).scl(1 / PPM);
        vertices[1] = new Vector2(5, 8).scl(1 / PPM);
        vertices[2] = new Vector2(-3, 3).scl(1 / PPM);
        vertices[3] = new Vector2(3, 3).scl(1 / PPM);
        head.set(vertices);

        fdef.shape = head;
        fdef.restitution = 0.5f; // Pozwala na odbicie po skoku na głowę
        fdef.filter.categoryBits = ENEMY_HEAD_BIT;
        b2body.createFixture(fdef).setUserData(this);
    }

    public void draw(SpriteBatch batch) {
        if (!destroyed || stateTime < 1) {
            super.draw(batch);
        }
    }

    @Override
    public void hitOnHead() {
        if (currentState == State.WALKING) {
            currentState = State.SHELL; // Żółw staje się skorupą
        } else if (currentState == State.SHELL || currentState == State.STATIONARY_SHELL) {
            if (isShellPushed) {
                currentState = State.SHELL; // Zatrzymuje poruszającą się skorupę, nie niszczy jej
                b2body.setLinearVelocity(0, 0); // Zatrzymuje skorupę
            } else {
                currentState = State.SHELL; // Zostaje w stanie skorupy
            }
        }
    }

    public void onShellHit() {
        if (currentState == State.SHELL) {
            currentState = State.STATIONARY_SHELL;
            isShellPushed = !isShellPushed; // Odbicie skorupy
            if (isShellPushed) {
                b2body.setLinearVelocity(new Vector2(2, 0)); // Skorupa zostaje popchnięta
            } else {
                b2body.setLinearVelocity(0, 0); // Skorupa się zatrzymuje
            }
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
