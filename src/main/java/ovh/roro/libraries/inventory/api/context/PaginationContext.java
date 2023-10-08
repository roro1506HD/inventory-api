package ovh.roro.libraries.inventory.api.context;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.PageableInventory;

import java.util.List;

@ApiStatus.NonExtendable
public interface PaginationContext<T, U, V extends InventoryPlayerHolder> {

    void update(@NotNull List<U> elements);

    void nextPage();

    boolean hasNextPage();

    void previousPage();

    boolean hasPreviousPage();

    int currentPage();

    int maxPage();

    @NotNull List<U> currentElements();

    @Nullable T inventoryValue();

    @NotNull PageableInventory<T, U, V> inventory();

}
