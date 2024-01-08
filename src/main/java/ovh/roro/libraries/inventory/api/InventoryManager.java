package ovh.roro.libraries.inventory.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.instance.ClassicInventoryInstance;
import ovh.roro.libraries.inventory.api.instance.ConfirmationInventoryInstance;
import ovh.roro.libraries.inventory.api.instance.InventoryInstance;
import ovh.roro.libraries.inventory.api.instance.ItemInstance;
import ovh.roro.libraries.inventory.api.instance.PageableInventoryInstance;
import ovh.roro.libraries.inventory.api.instance.StaticItemInstance;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;
import ovh.roro.libraries.inventory.api.item.StaticItem;
import ovh.roro.libraries.inventory.api.item.defaults.DefaultItemFactory;
import ovh.roro.libraries.inventory.impl.InventoryManagerImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@ApiStatus.NonExtendable
@SuppressWarnings("rawtypes")
public interface InventoryManager {

    static @NotNull InventoryManager inventoryManager() {
        Class<?> callerClass = StackWalker.getInstance().getCallerClass();

        return InventoryManagerImpl.getOrCreate(callerClass);
    }

    void register(@NotNull Function<UUID, InventoryPlayerHolder> playerMapper);

    <T, U extends InventoryInstance<T, V>, V extends InventoryPlayerHolder> void openInventory(@NotNull Inventory<T, U, V> inventory, @NotNull V player, @Nullable T value);

    void openPreviousInventory(@NotNull InventoryPlayerHolder player);

    void openPreviousInventory(@NotNull InventoryPlayerHolder player, int inventoriesToSkip);

    boolean hasPreviousInventory(@NotNull InventoryPlayerHolder player);

    void updateInventory(@NotNull InventoryPlayerHolder player);

    void softCloseInventory(@NotNull InventoryPlayerHolder player);

    <T extends InventoryPlayerHolder> @NotNull List<T> getInventoryViewers(@NotNull Inventory<?, ?, T> inventory);

    @NotNull Optional<Item> parseItem(@Nullable ItemStack itemStack);

    @NotNull Optional<Item> parseItem(@Nullable net.minecraft.world.item.ItemStack itemStack);

    <T, U extends InventoryPlayerHolder> @NotNull ClassicInventory<T, U> createInventory(@NotNull ClassicInventoryInstance<T, U> inventoryInstance);

    <T, U, V extends InventoryPlayerHolder> @NotNull PageableInventory<T, U, V> createPageableInventory(@NotNull PageableInventoryInstance<T, U, V> inventoryInstance);

    <T, U extends InventoryPlayerHolder> @NotNull ConfirmationInventory<T, U> createConfirmationInventory(@NotNull ConfirmationInventoryInstance<T, U> inventoryInstance);

    <T, U extends InventoryPlayerHolder> @NotNull Item<T, U> createItem(@NotNull ItemInstance<T, U> itemInstance);

    <T extends InventoryPlayerHolder> @NotNull StaticItem createStaticItem(@NotNull StaticItemInstance itemInstance);

    @NotNull ItemBuilder createItemBuilder(@NotNull Material material);

    @NotNull ItemBuilder createItemBuilder(@NotNull Material material, int amount);

    @NotNull ItemBuilder fromLegacy(@NotNull ItemStack itemStack);

    @NotNull ItemBuilder fromLegacy(@NotNull net.minecraft.world.item.ItemStack itemStack);

    @NotNull <T, U extends InventoryPlayerHolder> net.minecraft.world.item.ItemStack toMinecraftStack(@NotNull Item<T, U> item, @NotNull U player, @Nullable T value);

    @NotNull DefaultItemFactory defaultItemFactory();

}
