package ovh.roro.libraries.inventory.api.event.item.interact;

import org.bukkit.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;

@ApiStatus.OverrideOnly
public interface ItemInteractRightClickHandler<U extends InventoryPlayerHolder> {

    void onInteractRightClick(@NotNull U player, @Nullable Block clickedBlock);

}
