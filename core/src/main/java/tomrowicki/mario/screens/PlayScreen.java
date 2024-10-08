package tomrowicki.mario.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import tomrowicki.mario.MarioBros;
import tomrowicki.mario.scenes.Hud;
import tomrowicki.mario.sprites.Enemy;
import tomrowicki.mario.sprites.Goomba;
import tomrowicki.mario.sprites.Mario;
import tomrowicki.mario.sprites.Turtle;
import tomrowicki.mario.sprites.items.Item;
import tomrowicki.mario.sprites.items.ItemDef;
import tomrowicki.mario.sprites.items.Mushroom;
import tomrowicki.mario.tools.B2WorldCreator;
import tomrowicki.mario.tools.WorldContactListener;

import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static tomrowicki.mario.MarioBros.PPM;

public class PlayScreen implements Screen {

    private MarioBros game;
    private TextureAtlas atlas;

    private OrthographicCamera gameCam;
    private Viewport gameViewport;
    private Hud hud;

    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    // box2d variables
    private World world;
    private Box2DDebugRenderer b2dr;
    private B2WorldCreator creator;

    private Mario player;

    private Music music;

    private Array<Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;

    private Turtle turtle;
    private Goomba goomba;

    public PlayScreen(MarioBros game) {
        atlas = new TextureAtlas("Mario_and_Enemies.pack");
        this.game = game;
        this.gameCam = new OrthographicCamera();
        this.gameViewport = new FitViewport(MarioBros.V_WIDTH / PPM, MarioBros.V_HEIGHT / PPM, gameCam);
        this.hud = new Hud(game.batch);

        mapLoader = new TmxMapLoader();
        map = mapLoader.load("level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / PPM);
        gameCam.position.set(gameViewport.getWorldWidth() / 2, gameViewport.getWorldHeight() / 2, 0);

        world = new World(new Vector2(0, -10), true);
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this);

        player = new Mario(this);

        world.setContactListener(new WorldContactListener());

        music = MarioBros.manager.get("audio/music/mario_music.ogg", Music.class);
        music.setLooping(true);
//        music.play(); // turns music on

        items = new Array<>();
        itemsToSpawn = new LinkedBlockingQueue<>();
    }

    public void spawnItem(ItemDef itemDef) {
        itemsToSpawn.add(itemDef);
    }

    public void handleSpawningItems() {
        if (!itemsToSpawn.isEmpty()) {
            ItemDef itemDef = itemsToSpawn.poll();
            if (itemDef.type == Mushroom.class) {
                items.add(new Mushroom(this, itemDef.position.x, itemDef.position.y));
            }
        }
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    @Override
    public void show() {

    }

    public void handleInput(float dt) {
        if (player.currentState != Mario.State.DEAD) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                player.b2body.applyLinearImpulse(new Vector2(0, 4f), player.b2body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.b2body.getLinearVelocity().x <= 2) {
                player.b2body.applyLinearImpulse(new Vector2(0.1f, 0f), player.b2body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.b2body.getLinearVelocity().x >= -2) {
                player.b2body.applyLinearImpulse(new Vector2(-0.1f, 0f), player.b2body.getWorldCenter(), true);
            }
        }
    }

    public void update(float dt) {
        handleInput(dt);
        handleSpawningItems();

        // Brent sets the timeStep to 1/60f but that makes the game too fast for 120Hz refreshing rate
        world.step(1/120f, 6, 2);
        for (Enemy enemy : creator.getGoombas()) {
            enemy.update(dt);
            if (enemy.getX() < player.getX() + 224 / PPM) {
                enemy.b2body.setActive(true);
            }
        }

        for (Enemy enemy : creator.getTurtles()) {
            enemy.update(dt);
            if (enemy.getX() < player.getX() + 224 / PPM) {
                enemy.b2body.setActive(true);
            }
        }

        for (Item item : items) {
            item.update(dt);
        }

        player.update(dt);
        hud.update(dt);

        if (player.currentState != Mario.State.DEAD) {
            gameCam.position.x = player.b2body.getPosition().x;
        }

        gameCam.update();
        renderer.setView(gameCam);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl20.glEnable(GL20.GL_BLEND); // fix for handling transparent layers by debug lines

        renderer.render();

        // render Box2d debug lines
        b2dr.render(world, gameCam.combined);

        game.batch.setProjectionMatrix(gameCam.combined);
        game.batch.begin();
        player.draw(game.batch);
        for (Enemy enemy : creator.getGoombas()) {
            enemy.draw(game.batch);
        }
        for (Enemy enemy : creator.getTurtles()) {
            enemy.draw(game.batch);
        }
        for (Item item : items) {
            item.draw(game.batch);
        }
        game.batch.end();

        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        if (gameOver()) {
            game.setScreen(new GameOverScreen(game));
            dispose();
        }
    }

    public boolean gameOver() {
        if (player.currentState == Mario.State.DEAD && player.getStateTimer() > 3) {
            return true;
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
    }

    public TiledMap getMap() {
        return map;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }
}
