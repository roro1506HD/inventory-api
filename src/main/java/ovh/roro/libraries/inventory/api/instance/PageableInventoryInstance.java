package ovh.roro.libraries.inventory.api.instance;

import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.context.PaginationContext;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;
import ovh.roro.libraries.language.api.Placeholder;
import ovh.roro.libraries.language.api.Translation;

import java.util.List;

@ApiStatus.OverrideOnly
public interface PageableInventoryInstance<T, U, V extends InventoryPlayerHolder> extends ClassicInventoryInstance<PaginationContext<T, U, V>, V> {

    int @NotNull [] @NotNull [] DEFAULT_SLOTS = {
            {
                    10, 11, 12, 13, 14, 15, 16
            },
            {
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25
            },
            {
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34
            },
            {
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34,
                    37, 38, 39, 40, 41, 42, 43
            }
    };

    // Defaults in the bottom left corner
    int @NotNull [] DEFAULT_PREVIOUS_SLOTS = {
            18,
            27,
            36,
            45
    };

    // Defaults in the bottom right corner
    int @NotNull [] DEFAULT_NEXT_SLOTS = {
            26,
            35,
            44,
            53
    };

    default int @NotNull [] elementsSlots() {
        if (this.rows() > 2 && this.rows() <= PageableInventoryInstance.DEFAULT_SLOTS.length + 2) {
            return PageableInventoryInstance.DEFAULT_SLOTS[this.rows() - 3];
        }

        return new int[0];
    }

    default int previousItemSlot() {
        if (this.rows() > 2 && this.rows() <= PageableInventoryInstance.DEFAULT_PREVIOUS_SLOTS.length + 2) {
            return PageableInventoryInstance.DEFAULT_PREVIOUS_SLOTS[this.rows() - 3];
        }

        return 0;
    }

    default @NotNull ItemBuilder previousItemBuilder(int previousPage, int maxPage) {
        return ItemBuilder.of(Material.ARROW)
                .name(Translation.translation(
                        "inventory.api.item.pagination.previous.name",
                        Placeholder.number("previous_page", previousPage),
                        Placeholder.number("max_page", maxPage)
                ));
    }

    default int nextItemSlot() {
        if (this.rows() > 2 && this.rows() <= PageableInventoryInstance.DEFAULT_NEXT_SLOTS.length + 2) {
            return PageableInventoryInstance.DEFAULT_NEXT_SLOTS[this.rows() - 3];
        }

        return 0;
    }

    default @NotNull ItemBuilder nextItemBuilder(int nextPage, int maxPage) {
        return ItemBuilder.of(Material.ARROW)
                .name(Translation.translation(
                        "inventory.api.item.pagination.next.name",
                        Placeholder.number("next_page", nextPage),
                        Placeholder.number("max_page", maxPage)
                ));
    }

    @NotNull ItemInstance<@Nullable U, V> createSlotItem();

    @NotNull List<U> elements(@NotNull V player, @Nullable T value);

}
