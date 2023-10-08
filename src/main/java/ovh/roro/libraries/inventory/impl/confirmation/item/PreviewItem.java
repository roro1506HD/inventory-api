package ovh.roro.libraries.inventory.impl.confirmation.item;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.context.ConfirmationContext;
import ovh.roro.libraries.inventory.api.instance.ItemInstance;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;

@ApiStatus.Internal
@SuppressWarnings("rawtypes")
public class PreviewItem implements ItemInstance<ConfirmationContext, InventoryPlayerHolder> {

    @Override
    public @NotNull ItemBuilder buildItem(@NotNull InventoryPlayerHolder player, @Nullable ConfirmationContext value) {
        Preconditions.checkNotNull(value);

        //noinspection unchecked
        return value.inventoryInstance().buildPreviewItem(player, value.inventoryValue());
    }
}
