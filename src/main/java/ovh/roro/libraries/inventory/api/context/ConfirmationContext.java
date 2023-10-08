package ovh.roro.libraries.inventory.api.context;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.instance.ConfirmationInventoryInstance;

import java.util.function.Consumer;

@ApiStatus.NonExtendable
public interface ConfirmationContext<T, U extends InventoryPlayerHolder> {

    @NotNull Consumer<U> confirmationCallback();

    @NotNull ConfirmationInventoryInstance<T, U> inventoryInstance();

    @NotNull T inventoryValue();

}
