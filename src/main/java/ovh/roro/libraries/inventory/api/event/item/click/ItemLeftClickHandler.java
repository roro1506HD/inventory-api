package ovh.roro.libraries.inventory.api.event.item.click;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;

@ApiStatus.OverrideOnly
public interface ItemLeftClickHandler<T, U extends InventoryPlayerHolder> {

    void onLeftClick(@NotNull U player, boolean isShiftClick, @Nullable T value);

}
