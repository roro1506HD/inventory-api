package ovh.roro.libraries.inventory.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.instance.ItemInstance;

@ApiStatus.NonExtendable
public interface Item<T, U extends InventoryPlayerHolder> {

    static <T, U extends InventoryPlayerHolder> @NotNull Item<T, U> of(@NotNull ItemInstance<T, U> itemInstance) {
        return InventoryManager.inventoryManager().createItem(itemInstance);
    }

    @NotNull ItemInstance<T, U> instance();

    @NotNull ItemStack asBukkitItem(@NotNull U player, @Nullable T value);

    @NotNull net.minecraft.world.item.ItemStack asMinecraftItem(@NotNull U player, @Nullable T value);

    boolean isSimilar(@NotNull ItemStack itemStack);

    boolean isSimilar(@NotNull net.minecraft.world.item.ItemStack itemStack);

}
