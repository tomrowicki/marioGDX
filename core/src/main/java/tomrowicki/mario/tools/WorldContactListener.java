package tomrowicki.mario.tools;

import com.badlogic.gdx.physics.box2d.*;
import tomrowicki.mario.sprites.Enemy;
import tomrowicki.mario.sprites.InteractiveTileObject;

import static tomrowicki.mario.MarioBros.ENEMY_HEAD_BIT;
import static tomrowicki.mario.MarioBros.MARIO_BIT;

public class WorldContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        if (fixA.getUserData() == "head" || fixB.getUserData() == "head") {
            Fixture head = fixA.getUserData() == "head" ? fixA : fixB;
            Fixture object = head == fixA ? fixB : fixA;

            if (object.getUserData() != null && InteractiveTileObject.class.isAssignableFrom(object.getUserData().getClass())) {
                ((InteractiveTileObject) object.getUserData()).onHeadHit();
            }
        }

        switch (cDef) {
            case ENEMY_HEAD_BIT | MARIO_BIT:
                if (fixA.getFilterData().categoryBits == ENEMY_HEAD_BIT) {
                    ( (Enemy) fixA.getUserData()).hitOnHead();
                } else if (fixB.getFilterData().categoryBits == ENEMY_HEAD_BIT) {
                    ( (Enemy) fixB.getUserData()).hitOnHead();
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
