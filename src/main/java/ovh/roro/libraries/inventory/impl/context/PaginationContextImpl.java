package ovh.roro.libraries.inventory.impl.context;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.PageableInventory;
import ovh.roro.libraries.inventory.api.context.PaginationContext;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public class PaginationContextImpl<T, U, V extends InventoryPlayerHolder> implements PaginationContext<T, U, V> {

    private final int elementsPerPage;
    private final @NotNull List<@NotNull U> elements;
    private final @NotNull List<@NotNull U> currentElements;

    private final @NotNull PageableInventory<T, U, V> inventory;
    private final @Nullable T inventoryValue;

    private int maxPage;
    private int currentPage;

    public PaginationContextImpl(int elementsPerPage, @NotNull PageableInventory<T, U, V> inventory, @Nullable T inventoryValue, int page) {
        this.elementsPerPage = elementsPerPage;
        this.elements = new ArrayList<>();
        this.currentElements = new ArrayList<>(elementsPerPage);

        this.inventory = inventory;
        this.inventoryValue = inventoryValue;
        this.currentPage = page;
    }

    @Override
    public void update(@NotNull List<U> elements) {
        this.elements.clear();
        this.elements.addAll(elements);

        this.maxPage = (int) Math.ceil(elements.size() / (double) this.elementsPerPage);
        this.currentPage = Math.min(this.maxPage, Math.max(0, this.currentPage));

        this.updateCurrentElements();
    }

    @Override
    public void nextPage() {
        this.changePage(1);
    }

    @Override
    public boolean hasNextPage() {
        return this.currentPage < this.maxPage - 1;
    }

    @Override
    public void previousPage() {
        this.changePage(-1);
    }

    @Override
    public boolean hasPreviousPage() {
        return this.currentPage > 0;
    }

    @Override
    public int currentPage() {
        return this.currentPage;
    }

    @Override
    public int maxPage() {
        return this.maxPage;
    }

    @NotNull
    @Override
    public List<U> currentElements() {
        return this.currentElements;
    }

    @Override
    public @NotNull PageableInventory<T, U, V> inventory() {
        return this.inventory;
    }

    @Nullable
    @Override
    public T inventoryValue() {
        return this.inventoryValue;
    }

    private void changePage(int offset) {
        int currentPage = this.currentPage;

        this.currentPage = Math.min(this.maxPage, this.currentPage + offset);

        if (currentPage != this.currentPage) {
            this.updateCurrentElements();
        }
    }

    private void updateCurrentElements() {
        int startIndex = this.currentPage * this.elementsPerPage;
        int endIndex = Math.min(startIndex + this.elementsPerPage, this.elements.size());

        this.currentElements.clear();

        for (int i = startIndex; i < endIndex; i++) {
            this.currentElements.add(this.elements.get(i));
        }
    }
}
