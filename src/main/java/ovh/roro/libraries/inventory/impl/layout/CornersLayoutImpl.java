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
public class CornersLayoutImpl implements Layout {

    public static final @NotNull CornersLayoutImpl INSTANCE = new CornersLayoutImpl();

    private static final @NotNull Int2ObjectMap<int[]> SLOTS = new Int2ObjectArrayMap<>();

    private CornersLayoutImpl() {
    }

    static {
        int[] possibleSizes = {9, 18, 27, 36, 45, 54};

        for (int size : possibleSizes) {
            IntList slots = new IntArrayList();

            for (int i = 0; i < size; i++) {
                int x = i % 9;
                int y = i / 9;
                int i1 = x > 4 ? -x + 8 : x;
                int i2 = y >= size / 18 ? -y + size / 9 - 1 : y;

                if (i1 + i2 < 2) {
                    slots.add(i);
                }
            }

            CornersLayoutImpl.SLOTS.put(size, slots.toIntArray());
        }
    }

    @Override
    public int @NotNull [] slots(int inventorySize) {
        return Objects.requireNonNull(CornersLayoutImpl.SLOTS.get(inventorySize), "Invalid inventory size");
    }
}
