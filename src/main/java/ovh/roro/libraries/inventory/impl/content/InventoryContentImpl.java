package ovh.roro.libraries.inventory.impl.content;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryContent;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.instance.ItemInstance;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.layout.Layout;
import ovh.roro.libraries.inventory.api.slot.Slot;
import ovh.roro.libraries.inventory.impl.InventoryImpl;
import ovh.roro.libraries.inventory.impl.slot.AttachedSlotImpl;
import ovh.roro.libraries.inventory.impl.slot.StaticSlotImpl;

import java.util.function.BiFunction;

@ApiStatus.Internal
@SuppressWarnings("rawtypes")
public class InventoryContentImpl<T, U extends InventoryPlayerHolder> implements InventoryContent<T, U> {

    protected final @NotNull InventoryImpl<T, ?, U> inventory;

    protected @Nullable Layout layout;
    protected @Nullable Item layoutItem;

    protected final @NotNull Slot @NotNull [] slots;

    public InventoryContentImpl(@NotNull InventoryImpl<T, ?, U> inventory) {
        this.inventory = inventory;

        this.slots = new Slot[inventory.rows() * 9];

        for (int i = 0; i < this.slots.length; i++) {
            this.slots[i] = this.createSlot(i);
        }
    }

    @NotNull
    protected Slot createSlot(int index) {
        return this.inventory.slotType(index).createSlot();
    }

    @Override
    public @NotNull Slot slot(int index) {
        return this.slots[index];
    }

    @Override
    public @NotNull Slot slot(int x, int y) {
        return this.slot(y * 9 + x);
    }

    @Nullable
    @Override
    public void item(int index, @Nullable Item item) {
        this.slots[index].item(item);
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Override
    public void item(int index, @Nullable ItemInstance item) {
        this.item(index, InventoryManager.inventoryManager().createItem(item));
    }

    @Override
    public void item(int x, int y, @Nullable Item item) {
        this.item(y * 9 + x, item);
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Override
    public void item(int x, int y, @Nullable ItemInstance item) {
        this.item(x, y, InventoryManager.inventoryManager().createItem(item));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> void attachment(int index, @NotNull BiFunction<@NotNull U, @Nullable T, @Nullable V> valueMapper) {
        Slot slot = this.slots[index];

        if (slot instanceof AttachedSlotImpl attachedSlot) {
            attachedSlot.valueMapper(valueMapper);
        } else {
            throw new IllegalArgumentException("Cannot add an attachment to something else than an AttachedSlot");
        }
    }

    @Override
    public <V> void attachment(int x, int y, @NotNull BiFunction<@NotNull U, @Nullable T, @Nullable V> valueMapper) {
        this.attachment(y * 9 + x, valueMapper);
    }

    @Override
    public void update(int index) {
        this.slots[index].update();
    }

    @Override
    public void update(int x, int y) {
        this.update(y * 9 + x);
    }

    @Override
    public void layout(@NotNull Layout layout, @NotNull Item item) {
        Preconditions.checkArgument(this.layout == null, "Only one layout can be applied at a time");

        this.layout = layout;
        this.layoutItem = item;

        for (int i : layout.slots(this.slots.length)) {
            if (this.slots[i].item() != null) {
                continue;
            }

            Slot slot = new StaticSlotImpl();

            slot.item(item);

            this.slots[i] = slot;
        }
    }

    @Nullable
    public Layout layout() {
        return this.layout;
    }

    @Nullable
    public Item layoutItem() {
        return this.layoutItem;
    }
}
