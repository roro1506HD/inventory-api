package ovh.roro.libraries.inventory.impl.pageable.item;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.context.PaginationContext;
import ovh.roro.libraries.inventory.api.event.item.click.ItemClickHandler;
import ovh.roro.libraries.inventory.api.instance.ItemInstance;
import ovh.roro.libraries.inventory.api.instance.PageableInventoryInstance;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;
import ovh.roro.libraries.inventory.api.layout.Layout;
import ovh.roro.libraries.inventory.impl.content.InventoryContentImpl;
import ovh.roro.libraries.inventory.impl.pageable.PageableInventoryImpl;

@ApiStatus.Internal
@SuppressWarnings("rawtypes")
public class PreviousItem implements ItemInstance<PaginationContext, InventoryPlayerHolder>, ItemClickHandler<PaginationContext, InventoryPlayerHolder> {

    private final @NotNull InventoryManager inventoryManager;
    private final @NotNull ItemBuilder air;

    public PreviousItem(@NotNull InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        this.air = inventoryManager.createItemBuilder(Material.AIR);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public @NotNull ItemBuilder buildItem(@NotNull InventoryPlayerHolder player, @Nullable PaginationContext value) {
        Preconditions.checkNotNull(value);

        if (!value.hasPreviousPage()) {
            PageableInventoryImpl inventory = (PageableInventoryImpl) value.inventory();
            InventoryContentImpl content = inventory.inventoryContent();
            Layout layout = content.layout();

            if (layout == null || !ArrayUtils.contains(layout.slots(inventory.instance().rows() * 9), inventory.instance().previousItemSlot())) {
                return this.air;
            }

            return content.layoutItem().instance().buildItem(player, null);
        }

        return ((PageableInventoryInstance) value.inventory().instance()).previousItemBuilder(this.inventoryManager, value.currentPage(), value.maxPage());
    }

    @Override
    public void onClick(@NotNull InventoryPlayerHolder player, boolean isShiftClick, @Nullable PaginationContext value) {
        Preconditions.checkNotNull(value);

        if (!value.hasPreviousPage()) {
            return;
        }

        value.previousPage();

        this.inventoryManager.updateInventory(player);
    }
}
