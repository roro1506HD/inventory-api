package ovh.roro.libraries.inventory.impl.item;

import net.minecraft.nbt.Tag;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.event.item.click.ItemLeftClickHandler;
import ovh.roro.libraries.inventory.api.event.item.click.ItemRightClickHandler;
import ovh.roro.libraries.inventory.api.event.item.drop.ItemDropHandler;
import ovh.roro.libraries.inventory.api.event.item.drop.ItemInventoryDropHandler;
import ovh.roro.libraries.inventory.api.event.item.interact.ItemInteractLeftClickHandler;
import ovh.roro.libraries.inventory.api.event.item.interact.ItemInteractRightClickHandler;
import ovh.roro.libraries.inventory.api.instance.ItemInstance;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;

import java.util.Optional;

@ApiStatus.Internal
public class ItemImpl<T, U extends InventoryPlayerHolder> implements Item<T, U> {

    private final @NotNull InventoryManager inventoryManager;

    private final @NotNull ItemInstance<T, U> itemInstance;
    private final int id;

    private final @Nullable ItemDropHandler<U> dropHandler;
    private final @Nullable ItemInventoryDropHandler<T, U> inventoryDropHandler;

    private final @Nullable ItemLeftClickHandler<T, U> leftClickHandler;
    private final @Nullable ItemRightClickHandler<T, U> rightClickHandler;

    private final @Nullable ItemInteractLeftClickHandler<U> interactLeftClickHandler;
    private final @Nullable ItemInteractRightClickHandler<U> interactRightClickHandler;

    @SuppressWarnings("unchecked")
    public ItemImpl(@NotNull InventoryManager inventoryManager, @NotNull ItemInstance<T, U> itemInstance, int id) {
        this.inventoryManager = inventoryManager;

        this.itemInstance = itemInstance;
        this.id = id;

        this.dropHandler = this.handler(ItemDropHandler.class);
        this.inventoryDropHandler = this.handler(ItemInventoryDropHandler.class);

        this.leftClickHandler = this.handler(ItemLeftClickHandler.class);
        this.rightClickHandler = this.handler(ItemRightClickHandler.class);

        this.interactLeftClickHandler = this.handler(ItemInteractLeftClickHandler.class);
        this.interactRightClickHandler = this.handler(ItemInteractRightClickHandler.class);
    }

    private <V> @Nullable V handler(@NotNull Class<V> clazz) {
        if (clazz.isInstance(this.itemInstance)) {
            return clazz.cast(this.itemInstance);
        }

        return null;
    }

    @NotNull
    public ItemBuilder buildItem(@NotNull U player, @Nullable T value) {
        return this.itemInstance.buildItem(player, value);
    }

    @Override
    public @NotNull ItemInstance<T, U> instance() {
        return this.itemInstance;
    }

    @Override
    public @NotNull ItemStack asBukkitItem(@NotNull U player, @Nullable T value) {
        return CraftItemStack.asCraftMirror(this.asMinecraftItem(player, value));
    }

    @Override
    public @NotNull net.minecraft.world.item.ItemStack asMinecraftItem(@NotNull U player, @Nullable T value) {
        return this.inventoryManager.toMinecraftStack(this, player, value);
    }

    @Override
    public boolean isSimilar(@NotNull ItemStack itemStack) {
        return this.isSimilar(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public boolean isSimilar(@NotNull net.minecraft.world.item.ItemStack itemStack) {
        return itemStack.hasTag() && itemStack.getTag().contains("inventory_api_item", Tag.TAG_INT) && itemStack.getTag().getInt("inventory_api_item") == this.id;
    }

    public int id() {
        return this.id;
    }

    public @NotNull Optional<ItemDropHandler<U>> dropHandler() {
        return Optional.ofNullable(this.dropHandler);
    }

    public @NotNull Optional<ItemInventoryDropHandler<T, U>> inventoryDropHandler() {
        return Optional.ofNullable(this.inventoryDropHandler);
    }

    public @NotNull Optional<ItemLeftClickHandler<T, U>> leftClickHandler() {
        return Optional.ofNullable(this.leftClickHandler);
    }

    public @NotNull Optional<ItemRightClickHandler<T, U>> rightClickHandler() {
        return Optional.ofNullable(this.rightClickHandler);
    }

    public @NotNull Optional<ItemInteractLeftClickHandler<U>> interactLeftClickHandler() {
        return Optional.ofNullable(this.interactLeftClickHandler);
    }

    public @NotNull Optional<ItemInteractRightClickHandler<U>> interactRightClickHandler() {
        return Optional.ofNullable(this.interactRightClickHandler);
    }
}
