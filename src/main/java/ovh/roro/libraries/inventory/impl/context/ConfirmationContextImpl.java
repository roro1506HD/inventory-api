package ovh.roro.libraries.inventory.impl.context;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.context.ConfirmationContext;
import ovh.roro.libraries.inventory.api.instance.ConfirmationInventoryInstance;

import java.util.function.Consumer;

@ApiStatus.Internal
public record ConfirmationContextImpl<T, U extends InventoryPlayerHolder>(
        @NotNull ConfirmationInventoryInstance<T, U> inventoryInstance,
        @NotNull T inventoryValue
) implements ConfirmationContext<T, U> {

    @Override
    public @NotNull Consumer<@NotNull U> confirmationCallback() {
        return player -> this.inventoryInstance.handleConfirmation(player, this.inventoryValue);
    }
}
