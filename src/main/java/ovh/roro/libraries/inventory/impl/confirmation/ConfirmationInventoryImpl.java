package ovh.roro.libraries.inventory.impl.confirmation;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.ConfirmationInventory;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.context.ConfirmationContext;
import ovh.roro.libraries.inventory.api.instance.ConfirmationInventoryInstance;
import ovh.roro.libraries.inventory.api.item.defaults.DefaultItems;
import ovh.roro.libraries.inventory.api.layout.Layout;
import ovh.roro.libraries.inventory.api.slot.SlotType;
import ovh.roro.libraries.inventory.impl.InventoryImpl;
import ovh.roro.libraries.inventory.impl.confirmation.item.CancelItem;
import ovh.roro.libraries.inventory.impl.confirmation.item.ConfirmItem;
import ovh.roro.libraries.inventory.impl.confirmation.item.PreviewItem;
import ovh.roro.libraries.inventory.impl.content.InventoryContentImpl;
import ovh.roro.libraries.inventory.impl.context.ConfirmationContextImpl;
import ovh.roro.libraries.language.api.Translation;

import java.util.List;

@ApiStatus.Internal
public class ConfirmationInventoryImpl<T, U extends InventoryPlayerHolder> extends InventoryImpl<ConfirmationContext<T, U>, ConfirmationInventoryInstance<T, U>, U> implements ConfirmationInventory<T, U> {

    public ConfirmationInventoryImpl(@NotNull ConfirmationInventoryInstance<T, U> inventoryInstance) {
        super(inventoryInstance, InventoryContentImpl::new);
    }

    @Override
    public @NotNull ConfirmationInventoryInstance<T, U> instance() {
        return this.inventoryInstance;
    }

    @Override
    public @NotNull List<U> viewers() {
        return InventoryManager.inventoryManager().getInventoryViewers(this);
    }

    @Override
    public @NotNull Translation title(@NotNull U player, @Nullable ConfirmationContext<T, U> value) {
        return Translation.translation("core.inventory.confirmation.title");
    }

    @Override
    public int rows() {
        return 5;
    }

    @Override
    public int maxStackSize() {
        return 64;
    }

    @Override
    public @NotNull SlotType slotType(int index) {
        return SlotType.DYNAMIC;
    }

    @Override
    public void buildInventory() {
        this.inventoryContent.item(3, 3, new ConfirmItem());
        this.inventoryContent.item(5, 3, new CancelItem());
        this.inventoryContent.item(4, 1, new PreviewItem());

        this.inventoryContent.layout(Layout.OUTLINE, DefaultItems.separator(this.inventoryInstance.layoutMaterial()));
    }

    @Override
    public void updateInventory(@NotNull U player, @Nullable ConfirmationContext<T, U> value) {
    }

    @Override
    public void openConfirmation(@NotNull U player, @NotNull T value) {
        InventoryManager.inventoryManager().openInventory(this, player, new ConfirmationContextImpl<>(this.inventoryInstance, value));
    }
}
