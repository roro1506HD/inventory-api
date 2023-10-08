package ovh.roro.libraries.inventory.impl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.event.InventoryCloseHandler;
import ovh.roro.libraries.inventory.api.event.InventoryOpenHandler;
import ovh.roro.libraries.inventory.api.instance.InventoryInstance;
import ovh.roro.libraries.inventory.api.slot.SlotType;
import ovh.roro.libraries.inventory.impl.content.InventoryContentImpl;
import ovh.roro.libraries.language.api.Translation;

import java.util.Optional;
import java.util.function.Function;

@ApiStatus.Internal
public abstract class InventoryImpl<T, U extends InventoryInstance<T, V>, V extends InventoryPlayerHolder> {

    protected final @NotNull U inventoryInstance;
    protected final @NotNull InventoryContentImpl<T, V> inventoryContent;

    protected final @Nullable InventoryOpenHandler<T, V> openHandler;
    protected final @Nullable InventoryCloseHandler<T, V> closeHandler;

    private boolean built;

    @SuppressWarnings("unchecked")
    public InventoryImpl(@NotNull U inventoryInstance, @NotNull Function<InventoryImpl<T, U, V>, InventoryContentImpl<T, V>> inventoryContentMapper) {
        this.inventoryInstance = inventoryInstance;
        this.inventoryContent = inventoryContentMapper.apply(this);

        this.openHandler = this.handler(InventoryOpenHandler.class);
        this.closeHandler = this.handler(InventoryCloseHandler.class);
    }

    @Nullable
    private <W> W handler(@NotNull Class<W> clazz) {
        if (clazz.isInstance(this.inventoryInstance)) {
            return clazz.cast(this.inventoryInstance);
        }

        return null;
    }

    public void ensureIsBuilt() {
        if (!this.built) {
            this.built = true;

            this.buildInventory();
        }
    }

    public abstract @NotNull Translation title(@NotNull V player, @Nullable T value);

    public abstract int rows();

    public abstract int maxStackSize();

    public abstract @NotNull SlotType slotType(int index);

    public abstract void buildInventory();

    public abstract void updateInventory(@NotNull V player, @Nullable T value);

    public @NotNull InventoryContentImpl<T, V> inventoryContent() {
        return this.inventoryContent;
    }

    public @NotNull Optional<InventoryOpenHandler<T, V>> openHandler() {
        return Optional.ofNullable(this.openHandler);
    }

    public @NotNull Optional<InventoryCloseHandler<T, V>> closeHandler() {
        return Optional.ofNullable(this.closeHandler);
    }
}
