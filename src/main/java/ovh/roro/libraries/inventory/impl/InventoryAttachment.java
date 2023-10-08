package ovh.roro.libraries.inventory.impl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.Inventory;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.instance.InventoryInstance;

@ApiStatus.Internal
record InventoryAttachment<T, U extends InventoryInstance<T, V>, V extends InventoryPlayerHolder>(
        @NotNull Inventory<T, U, V> inventory,
        @Nullable T attachment
) {
}
