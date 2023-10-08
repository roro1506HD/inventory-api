package ovh.roro.libraries.inventory.api.item;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.instance.StaticItemInstance;

@ApiStatus.NonExtendable
public interface StaticItem extends Item<Object, InventoryPlayerHolder> {

    static @NotNull StaticItem of(@NotNull ItemBuilder itemBuilder) {
        return InventoryManager.inventoryManager().createStaticItem(() -> itemBuilder);
    }

    static @NotNull StaticItem of(@NotNull StaticItemInstance itemInstance) {
        return InventoryManager.inventoryManager().createStaticItem(itemInstance);
    }
}
