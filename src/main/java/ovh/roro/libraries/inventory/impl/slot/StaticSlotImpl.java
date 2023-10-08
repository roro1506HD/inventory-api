package ovh.roro.libraries.inventory.impl.slot;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.annotation.ItemRefresh;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;
import ovh.roro.libraries.inventory.api.slot.Slot;
import ovh.roro.libraries.inventory.impl.item.StaticItemImpl;

@ApiStatus.Internal
@SuppressWarnings({"rawtypes", "unchecked"})
public class StaticSlotImpl<T, U extends InventoryPlayerHolder> implements Slot<T, U> {

    public static final @NotNull SlotTypeImpl TYPE = new SlotTypeImpl(StaticSlotImpl::new);

    private long hash;

    private @Nullable Item<T, U> item;

    public StaticSlotImpl() {
        this.update();
    }

    @Override
    public @Nullable ItemBuilder createItem(@NotNull U player, @Nullable T value) {
        if (this.item == null) {
            return null;
        }

        return this.item.instance().buildItem(player, null);
    }

    @Override
    public @Nullable Item item() {
        return this.item;
    }

    @Override
    public void item(@Nullable Item item) {
        Preconditions.checkArgument(item instanceof StaticItemImpl, "Static slot can only use static items");

        this.item = item;
        this.update();
    }

    @Override
    public void update() {
        this.update(System.currentTimeMillis());
    }

    private void update(long currentTime) {
        this.hash = currentTime;
    }

    @Override
    public long hash() {
        if (this.item != null) {
            ItemRefresh annotation = this.item.instance().getClass().getAnnotation(ItemRefresh.class);

            if (annotation != null) {
                long currentTime = System.currentTimeMillis();
                long interval = annotation.value() * 50L;

                if (currentTime - this.hash >= interval) {
                    this.update(currentTime);
                }
            }
        }

        return this.hash;
    }
}
