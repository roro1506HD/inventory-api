package ovh.roro.libraries.inventory.impl.item;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.instance.StaticItemInstance;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;
import ovh.roro.libraries.inventory.api.item.StaticItem;

@ApiStatus.Internal
public class StaticItemImpl extends ItemImpl<Object, InventoryPlayerHolder> implements StaticItem {

    private final @NotNull ItemBuilder cachedBuilder;

    @SuppressWarnings("ConstantConditions")
    public StaticItemImpl(@NotNull StaticItemInstance itemInstance, int id) {
        super(itemInstance, id);

        this.cachedBuilder = itemInstance.buildItem(null, null);
    }

    @Override
    public @NotNull ItemBuilder buildItem(@NotNull InventoryPlayerHolder player, @Nullable Object value) {
        return this.cachedBuilder;
    }
}
