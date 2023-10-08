package ovh.roro.libraries.inventory.api.instance;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryContent;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.slot.SlotType;
import ovh.roro.libraries.language.api.Translation;

@ApiStatus.OverrideOnly
public interface ClassicInventoryInstance<T, U extends InventoryPlayerHolder> extends InventoryInstance<T, U> {

    @NotNull Translation title(@NotNull U player, @Nullable T value);

    int rows();

    default int maxStackSize() {
        return 64;
    }

    @NotNull SlotType slotType(int index);

    void buildInventory(@NotNull InventoryContent<T, U> content);

}
