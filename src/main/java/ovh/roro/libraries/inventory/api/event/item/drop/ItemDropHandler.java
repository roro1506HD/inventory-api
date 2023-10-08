package ovh.roro.libraries.inventory.api.event.item.drop;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;

@ApiStatus.OverrideOnly
public interface ItemDropHandler<U extends InventoryPlayerHolder> {

    void onDrop(@NotNull U player);

}
