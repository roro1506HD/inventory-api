package ovh.roro.libraries.inventory.api.event.item.interact;

import org.bukkit.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;

@ApiStatus.OverrideOnly
public interface ItemInteractClickHandler<U extends InventoryPlayerHolder> extends ItemInteractLeftClickHandler<U>,
        ItemInteractRightClickHandler<U> {

    @Override
    default void onInteractLeftClick(@NotNull U player, @Nullable Block clickedBlock) {
        this.onClick(player, clickedBlock);
    }

    @Override
    default void onInteractRightClick(@NotNull U player, @Nullable Block clickedBlock) {
        this.onClick(player, clickedBlock);
    }

    void onClick(@NotNull U player, @Nullable Block clickedBlock);

}
