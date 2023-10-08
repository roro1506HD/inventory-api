package ovh.roro.libraries.inventory.impl.slot;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.annotation.ItemRefresh;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;
import ovh.roro.libraries.inventory.api.slot.Slot;

import java.util.Objects;
import java.util.function.BiFunction;

@ApiStatus.Internal
@SuppressWarnings({"rawtypes", "unchecked"})
public class AttachedSlotImpl<T, U, V extends InventoryPlayerHolder> implements Slot<T, V> {

    public static final @NotNull SlotTypeImpl TYPE = new SlotTypeImpl(AttachedSlotImpl::new);

    private static final @NotNull BiFunction EMPTY_MAPPER = (crimsonPlayer, t) -> null;

    private long hash;

    private @Nullable Item<U, V> item;
    private @NotNull BiFunction<@NotNull V, @Nullable T, @Nullable U> valueMapper;

    public AttachedSlotImpl() {
        this.valueMapper = AttachedSlotImpl.EMPTY_MAPPER;
        this.update();
    }

    @Override
    public @Nullable ItemBuilder createItem(@NotNull V player, @Nullable T value) {
        if (this.item == null) {
            return null;
        }

        return this.item.instance().buildItem(player, this.valueMapper.apply(player, value));
    }

    @Override
    public @Nullable Item item() {
        return this.item;
    }

    @Override
    public void item(@Nullable Item item) {
        this.item = item;
        this.update();
    }

    public @Nullable BiFunction<@NotNull V, @Nullable T, @Nullable U> valueMapper() {
        return this.valueMapper;
    }

    public void valueMapper(@Nullable BiFunction<V, T, U> valueMapper) {
        this.valueMapper = Objects.requireNonNullElse(valueMapper, AttachedSlotImpl.EMPTY_MAPPER);
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
