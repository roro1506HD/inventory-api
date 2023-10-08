package ovh.roro.libraries.inventory.api.event;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;

@ApiStatus.OverrideOnly
public interface InventoryCloseHandler<T, U extends InventoryPlayerHolder> {

    void onClose(@NotNull U player, @Nullable T value);

}
