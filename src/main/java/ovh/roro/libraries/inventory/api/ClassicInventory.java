package ovh.roro.libraries.inventory.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.instance.ClassicInventoryInstance;

@ApiStatus.NonExtendable
public interface ClassicInventory<T, U extends InventoryPlayerHolder> extends Inventory<T, ClassicInventoryInstance<T, U>, U> {

    static <T, U extends InventoryPlayerHolder> @NotNull ClassicInventory<T, U> of(@NotNull ClassicInventoryInstance<T, U> inventoryInstance) {
        return InventoryManager.inventoryManager().createInventory(inventoryInstance);
    }

    void open(@NotNull U player, @Nullable T value);

}
