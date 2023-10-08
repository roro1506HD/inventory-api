package ovh.roro.libraries.inventory.impl.pageable;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.PageableInventory;
import ovh.roro.libraries.inventory.api.context.PaginationContext;
import ovh.roro.libraries.inventory.api.instance.PageableInventoryInstance;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.slot.SlotType;
import ovh.roro.libraries.inventory.impl.InventoryImpl;
import ovh.roro.libraries.inventory.impl.InventoryManagerImpl;
import ovh.roro.libraries.inventory.impl.content.PageableInventoryContentImpl;
import ovh.roro.libraries.inventory.impl.context.PaginationContextImpl;
import ovh.roro.libraries.language.api.Translation;

import java.util.List;

@ApiStatus.Internal
public class PageableInventoryImpl<T, U, V extends InventoryPlayerHolder> extends InventoryImpl<PaginationContext<T, U, V>, PageableInventoryInstance<T, U, V>, V> implements PageableInventory<T, U, V> {

    private final @NotNull InventoryManagerImpl inventoryManager;

    public PageableInventoryImpl(@NotNull InventoryManagerImpl inventoryManager, @NotNull PageableInventoryInstance<T, U, V> inventoryInstance) {
        super(inventoryInstance, PageableInventoryContentImpl::new);

        this.inventoryManager = inventoryManager;
    }

    @Override
    public @NotNull PageableInventoryInstance<T, U, V> instance() {
        return this.inventoryInstance;
    }

    @Override
    public @NotNull List<V> viewers() {
        return this.inventoryManager.getInventoryViewers(this);
    }

    @Override
    public @NotNull Translation title(@NotNull V player, @Nullable PaginationContext<T, U, V> value) {
        return this.inventoryInstance.title(player, value);
    }

    @Override
    public int rows() {
        return this.inventoryInstance.rows();
    }

    @Override
    public int maxStackSize() {
        return this.inventoryInstance.maxStackSize();
    }

    @Override
    public @NotNull SlotType slotType(int index) {
        return this.inventoryInstance.slotType(index);
    }

    @Override
    public void buildInventory() {
        int[] slots = this.inventoryInstance.elementsSlots();
        Item<?, V> item = this.inventoryManager.createItem(this.inventoryInstance.createSlotItem());

        int index = 0;
        for (int slot : slots) {
            this.inventoryContent.item(slot, item);

            int finalIndex = index;
            this.inventoryContent.attachment(slot, (player, value) -> {
                Preconditions.checkNotNull(value);

                List<@NotNull U> elements = value.currentElements();

                if (finalIndex >= elements.size()) {
                    return null;
                }

                return elements.get(finalIndex);
            });

            index++;
        }

        this.inventoryContent.item(this.inventoryInstance.previousItemSlot(), this.inventoryManager.previousItem());
        this.inventoryContent.item(this.inventoryInstance.nextItemSlot(), this.inventoryManager.nextItem());

        this.inventoryInstance.buildInventory(this.inventoryContent);
    }

    @Override
    public void updateInventory(@NotNull V player, @Nullable PaginationContext<T, U, V> value) {
        Preconditions.checkNotNull(value);

        value.update(this.inventoryInstance.elements(player, value.inventoryValue()));
    }

    @Override
    public void openPageable(@NotNull V player, @Nullable T value) {
        Preconditions.checkArgument(this.instance().rows() > 2, "Pageable inventories cannot be under 3 rows");

        PaginationContextImpl<T, U, V> context = new PaginationContextImpl<>(this.instance().elementsSlots().length, this, value);

        this.inventoryManager.openInventory(this, player, context);
    }
}
