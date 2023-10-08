package ovh.roro.libraries.inventory.api.instance;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;

@ApiStatus.OverrideOnly
public interface StaticItemInstance extends ItemInstance<Object, InventoryPlayerHolder> {

    @Override
    default @NotNull ItemBuilder buildItem(@NotNull InventoryPlayerHolder player, @Nullable Object value) {
        return this.buildItem();
    }

    @NotNull ItemBuilder buildItem();

}
