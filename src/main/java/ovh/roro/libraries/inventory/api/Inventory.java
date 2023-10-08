package ovh.roro.libraries.inventory.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.api.instance.InventoryInstance;

import java.util.List;

@ApiStatus.NonExtendable
public interface Inventory<T, U extends InventoryInstance<T, V>, V extends InventoryPlayerHolder> {

    @NotNull U instance();

    @NotNull List<V> viewers();

}
