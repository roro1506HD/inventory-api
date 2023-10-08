package ovh.roro.libraries.inventory.api.slot;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;

@ApiStatus.NonExtendable
@SuppressWarnings("rawtypes")
public interface Slot<T, U extends InventoryPlayerHolder> {

    @Nullable ItemBuilder createItem(@NotNull U player, @Nullable T value);

    @Nullable Item item();

    void item(@Nullable Item item);

    long hash();

    void update();

}
