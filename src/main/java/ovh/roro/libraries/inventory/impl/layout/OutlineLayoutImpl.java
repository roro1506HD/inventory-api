package ovh.roro.libraries.inventory.impl.layout;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.api.layout.Layout;

import java.util.Objects;

@ApiStatus.Internal
public class OutlineLayoutImpl implements Layout {

    public static final @NotNull OutlineLayoutImpl INSTANCE = new OutlineLayoutImpl();

    private static final @NotNull Int2ObjectMap<int[]> SLOTS = new Int2ObjectArrayMap<>();

    private OutlineLayoutImpl() {
    }

    static {
        int[] possibleSizes = {9, 18, 27, 36, 45, 54};

        for (int size : possibleSizes) {
            IntList slots = new IntArrayList();
            int lastRow = size / 9 - 1;

            for (int i = 0; i < size; i++) {
                int x = i % 9;
                int y = i / 9;

                if (x == 0 || x == 8 || y == 0 || y == lastRow) {
                    slots.add(i);
                }
            }

            OutlineLayoutImpl.SLOTS.put(size, slots.toIntArray());
        }
    }

    @Override
    public int @NotNull [] slots(int inventorySize) {
        return Objects.requireNonNull(OutlineLayoutImpl.SLOTS.get(inventorySize), "Invalid inventory size");
    }
}
