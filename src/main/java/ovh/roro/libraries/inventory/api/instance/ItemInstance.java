package ovh.roro.libraries.inventory.api.instance;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;

@ApiStatus.OverrideOnly
public interface ItemInstance<T, U extends InventoryPlayerHolder> {

    @NotNull
    ItemBuilder buildItem(@NotNull U player, @Nullable T value);

}
