package ovh.roro.libraries.inventory.impl.content;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.instance.PageableInventoryInstance;
import ovh.roro.libraries.inventory.api.slot.Slot;
import ovh.roro.libraries.inventory.api.slot.SlotType;
import ovh.roro.libraries.inventory.impl.InventoryImpl;
import ovh.roro.libraries.inventory.impl.pageable.PageableInventoryImpl;

@ApiStatus.Internal
@SuppressWarnings("rawtypes")
public class PageableInventoryContentImpl<T, U extends InventoryPlayerHolder> extends InventoryContentImpl<T, U> {

    private @MonotonicNonNull PageableInventoryInstance inventoryInstance;

    public PageableInventoryContentImpl(@NotNull InventoryManager inventoryManager, @NotNull InventoryImpl<T, ?, U> inventory) {
        super(inventoryManager, inventory);
    }

    @Override
    protected @NotNull Slot createSlot(int index) {
        if (this.inventoryInstance == null) {
            this.inventoryInstance = ((PageableInventoryImpl) super.inventory).instance();
        }

        for (int i : this.inventoryInstance.elementsSlots()) {
            if (i == index) {
                return SlotType.ATTACHED.createSlot();
            }
        }

        if (index == this.inventoryInstance.previousItemSlot() || index == this.inventoryInstance.nextItemSlot()) {
            return SlotType.DYNAMIC.createSlot();
        }

        return super.createSlot(index);
    }
}
