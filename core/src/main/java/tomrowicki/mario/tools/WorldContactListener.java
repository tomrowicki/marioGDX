package tomrowicki.mario.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import tomrowicki.mario.sprites.Enemy;
import tomrowicki.mario.sprites.InteractiveTileObject;
import tomrowicki.mario.sprites.Mario;
import tomrowicki.mario.sprites.items.Item;

import static tomrowicki.mario.MarioBros.*;

public class WorldContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        switch (cDef) {
            case MARIO_HEAD_BIT | BRICK_BIT:
            case MARIO_HEAD_BIT | COIN_BIT:
                if (fixA.getFilterData().categoryBits == MARIO_HEAD_BIT) {
                    ((InteractiveTileObject) fixB.getUserData()).onHeadHit((Mario) fixA.getUserData());
                } else {
                    ((InteractiveTileObject) fixA.getUserData()).onHeadHit((Mario) fixB.getUserData());
                }
                break;
            case ENEMY_HEAD_BIT | MARIO_BIT: // when Mario collides with enemy's head
                if (fixA.getFilterData().categoryBits == ENEMY_HEAD_BIT) {
                    ((Enemy) fixA.getUserData()).hitOnHead();
                } else {
                    ((Enemy) fixB.getUserData()).hitOnHead();
                }
                break;
            case ENEMY_BIT | OBJECT_BIT: // when enemy walks into a pipe or smth
                if (fixA.getFilterData().categoryBits == ENEMY_BIT) {
                    ((Enemy) fixA.getUserData()).reverseVelocity(true, false);
                } else {
                    ((Enemy) fixB.getUserData()).reverseVelocity(true, false);
                }
                break;
            case MARIO_BIT | ENEMY_BIT:
                if (fixA.getFilterData().categoryBits == MARIO_BIT) {
                    ((Mario) fixA.getUserData()).hit();
                } else {
                    ((Mario) fixB.getUserData()).hit();
                }
                break;
            case ENEMY_BIT | ENEMY_BIT:
                ((Enemy) fixA.getUserData()).reverseVelocity(true, false);
                ((Enemy) fixB.getUserData()).reverseVelocity(true, false);
                break;
            case ITEM_BIT | OBJECT_BIT: // when enemy walks into a pipe or smth
                if (fixA.getFilterData().categoryBits == ITEM_BIT) {
                    ((Item) fixA.getUserData()).reverseVelocity(true, false);
                } else {
                    ((Item) fixB.getUserData()).reverseVelocity(true, false);
                }
                break;
            case ITEM_BIT | MARIO_BIT:
                if (!(fixA.getUserData() instanceof String) && !(fixB.getUserData() instanceof String)) {
                    if (fixA.getFilterData().categoryBits == ITEM_BIT)
                        ((Item) fixA.getUserData()).use((Mario) fixB.getUserData());
                    else
                        ((Item) fixB.getUserData()).use((Mario) fixA.getUserData());
                    break;
                }
        }
    }

    @Override
    public void endContact(Contact contact) {
//        Gdx.app.log("WorldContactListener", "endContact");

    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }
}
