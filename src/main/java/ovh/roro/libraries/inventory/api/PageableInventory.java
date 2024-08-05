package ovh.roro.libraries.inventory.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.context.PaginationContext;
import ovh.roro.libraries.inventory.api.instance.PageableInventoryInstance;

@ApiStatus.NonExtendable
public interface PageableInventory<T, U, V extends InventoryPlayerHolder> extends Inventory<PaginationContext<T, U, V>, PageableInventoryInstance<T, U, V>, V> {

    static <T, U, V extends InventoryPlayerHolder> @NotNull PageableInventory<T, U, V> of(@NotNull PageableInventoryInstance<T, U, V> inventoryInstance) {
        return InventoryManager.inventoryManager().createPageableInventory(inventoryInstance);
    }

    void openPageable(@NotNull V player, @Nullable T value);

    void openPageable(@NotNull V player, @Nullable T value, int page);

}
