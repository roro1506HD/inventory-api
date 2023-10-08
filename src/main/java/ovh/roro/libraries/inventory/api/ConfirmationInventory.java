package ovh.roro.libraries.inventory.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.api.context.ConfirmationContext;
import ovh.roro.libraries.inventory.api.instance.ConfirmationInventoryInstance;

@ApiStatus.NonExtendable
public interface ConfirmationInventory<T, U extends InventoryPlayerHolder> extends Inventory<ConfirmationContext<T, U>, ConfirmationInventoryInstance<T, U>, U> {

    static <T, U extends InventoryPlayerHolder> @NotNull ConfirmationInventory<T, U> of(@NotNull ConfirmationInventoryInstance<T, U> inventoryInstance) {
        return InventoryManager.inventoryManager().createConfirmationInventory(inventoryInstance);
    }

    void openConfirmation(@NotNull U player, @NotNull T value);

}
