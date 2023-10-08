package ovh.roro.libraries.inventory.impl.item.defaults;

import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.event.item.click.ItemClickHandler;
import ovh.roro.libraries.inventory.api.instance.ItemInstance;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;
import ovh.roro.libraries.language.api.Translation;

@ApiStatus.Internal
class BackItem implements ItemInstance<Object, InventoryPlayerHolder>, ItemClickHandler<Object, InventoryPlayerHolder> {

    private final ItemBuilder backItem;
    private final ItemBuilder closeItem;

    private final InventoryManager inventoryManager;

    BackItem(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;

        this.backItem = inventoryManager.createItemBuilder(Material.BARRIER).name(Translation.translation("inventory.api.item.back.name"));
        this.closeItem = inventoryManager.createItemBuilder(Material.BARRIER).name(Translation.translation("inventory.api.item.close.name"));
    }

    @Override
    public @NotNull ItemBuilder buildItem(@NotNull InventoryPlayerHolder player, @Nullable Object value) {
        if (!this.inventoryManager.hasPreviousInventory(player)) {
            return this.closeItem;
        }

        return this.backItem;
    }

    @Override
    public void onClick(@NotNull InventoryPlayerHolder player, boolean isShiftClick, @Nullable Object value) {
        this.inventoryManager.openPreviousInventory(player);
    }
}