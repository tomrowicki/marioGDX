package tomrowicki.mario.sprites.items;

import com.badlogic.gdx.math.Vector2;

public class ItemDef implements Comparable<ItemDef> {

    public Vector2 position;
    public Class<?> type;

    public ItemDef(Vector2 position, Class<?> type) {
        this.position = position;
        this.type = type;
    };

    @Override
    public int compareTo(ItemDef o) {
        return 0;
    }
}
