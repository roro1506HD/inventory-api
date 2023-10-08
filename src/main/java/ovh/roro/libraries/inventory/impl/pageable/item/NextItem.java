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
public class NextItem implements ItemInstance<PaginationContext, InventoryPlayerHolder>, ItemClickHandler<PaginationContext, InventoryPlayerHolder> {

    private static final ItemBuilder AIR = ItemBuilder.of(Material.AIR);

    private final @NotNull InventoryManager inventoryManager;

    public NextItem(@NotNull InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public @NotNull ItemBuilder buildItem(@NotNull InventoryPlayerHolder player, @Nullable PaginationContext value) {
        Preconditions.checkNotNull(value);

        if (!value.hasNextPage()) {
            PageableInventoryImpl inventory = (PageableInventoryImpl) value.inventory();
            InventoryContentImpl content = inventory.inventoryContent();
            Layout layout = content.layout();

            if (layout == null || !ArrayUtils.contains(layout.slots(inventory.instance().rows() * 9), inventory.instance().nextItemSlot())) {
                return NextItem.AIR;
            }

            return content.layoutItem().instance().buildItem(player, null);
        }

        return ((PageableInventoryInstance) value.inventory().instance()).nextItemBuilder(value.currentPage() + 2, value.maxPage());
    }

    @Override
    public void onClick(@NotNull InventoryPlayerHolder player, boolean isShiftClick, @Nullable PaginationContext value) {
        Preconditions.checkNotNull(value);

        if (!value.hasNextPage()) {
            return;
        }

        value.nextPage();

        this.inventoryManager.updateInventory(player);
    }
}
