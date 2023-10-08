package ovh.roro.libraries.inventory.api.instance;

import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.context.ConfirmationContext;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;

@ApiStatus.OverrideOnly
public interface ConfirmationInventoryInstance<T, U extends InventoryPlayerHolder> extends InventoryInstance<ConfirmationContext<T, U>, U> {

    @NotNull ItemBuilder buildPreviewItem(@NotNull U player, @NotNull T value);

    @NotNull Material layoutMaterial();

    void handleConfirmation(@NotNull U player, @NotNull T value);

}
