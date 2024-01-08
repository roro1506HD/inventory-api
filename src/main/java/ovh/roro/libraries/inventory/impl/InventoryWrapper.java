package ovh.roro.libraries.inventory.impl;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.instance.InventoryInstance;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.item.StaticItem;
import ovh.roro.libraries.inventory.api.slot.Slot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

@ApiStatus.Internal
public class InventoryWrapper<T, U extends InventoryInstance<T, V>, V extends InventoryPlayerHolder> implements Container {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger("InventoryAPI - Wrapper");

    private final @NotNull InventoryManagerImpl inventoryManager;
    private final @NotNull V player;

    private final @NotNull NonNullList<ItemStack> itemsCache;
    private final long @NotNull [] hash;
    private final @NotNull InventoryImpl<T, U, V> inventory;
    private final @Nullable T value;

    private boolean viewing;
    private boolean softClose;

    public InventoryWrapper(@NotNull InventoryManagerImpl inventoryManager, @NotNull V player, @NotNull InventoryImpl<T, U, V> inventory, @Nullable T value) {
        this.inventoryManager = inventoryManager;
        this.player = player;

        this.itemsCache = NonNullList.withSize(inventory.rows() * 9, ItemStack.EMPTY);
        this.hash = new long[this.itemsCache.size()];
        this.inventory = inventory;
        this.value = value;
    }

    void updateInventory() {
        Arrays.fill(this.hash, 0L);

        this.inventory.updateInventory(this.player, this.value);
    }

    public @Nullable T value() {
        return this.value;
    }

    public @NotNull InventoryImpl<T, U, V> inventory() {
        return this.inventory;
    }

    @Override
    public int getContainerSize() {
        return this.hash.length;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.itemsCache) {
            if (stack != null && !stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ItemStack getItem(int i) {
        Slot slot = this.inventory.inventoryContent().slot(i);

        if (slot.hash() != this.hash[i]) {
            try {
                ItemStack stack = this.inventoryManager.toMinecraftStack(this.player, slot.createItem(this.player, this.value), slot.item());

                this.itemsCache.set(i, Objects.requireNonNullElse(stack, ItemStack.EMPTY));
                this.hash[i] = slot.hash();
            } catch (Exception ex) {
                InventoryWrapper.LOGGER.error("An error occurred while creating minecraft stack", ex);
            }
        }

        return this.itemsCache.get(i);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return null;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return null;
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        this.inventory.inventoryContent().item(slot, this.inventoryManager.createStaticItem(() -> this.inventoryManager.fromLegacy(itemStack)));
    }

    @Override
    public int getMaxStackSize() {
        return this.inventory.maxStackSize();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player entityHuman) {
        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void clearContent() {
        for (int i = 0; i < this.hash.length; i++) {
            this.inventory.inventoryContent().item(i, (Item) null);
        }
    }

    @Override
    public List<ItemStack> getContents() {
        int size = this.hash.length;
        List<ItemStack> items = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            items.add(this.getItem(i));
        }

        return items;
    }

    @Override
    public void onOpen(CraftHumanEntity human) {
        if (!human.getUniqueId().equals(this.player.bukkitPlayer().getUniqueId())) {
            throw new IllegalStateException();
        }

        this.viewing = true;

        this.inventory.openHandler().ifPresent(handler -> {
            handler.onOpen(this.player, this.value);
        });
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onClose(CraftHumanEntity human) {
        if (!human.getUniqueId().equals(this.player.bukkitPlayer().getUniqueId())) {
            throw new IllegalStateException();
        }

        this.viewing = false;

        if (this.softClose) {
            return;
        }

        Deque<InventoryAttachment> queue = this.inventoryManager.lastInventories().get(human.getUniqueId());

        if (queue != null && !queue.isEmpty() && queue.getLast().inventory().equals(this.inventory)) {
            this.inventoryManager.lastInventories().remove(human.getUniqueId());
        }

        this.inventory.closeHandler().ifPresent(handler -> {
            handler.onClose(this.player, this.value);
        });
    }

    @Override
    public List<HumanEntity> getViewers() {
        if (!this.viewing) {
            return List.of();
        }

        return List.of(this.player.bukkitPlayer());
    }

    @Override
    public InventoryHolder getOwner() {
        return null;
    }

    @Override
    public void setMaxStackSize(int i) {
    }

    @Override
    public Location getLocation() {
        return null;
    }

    public void softClose() {
        this.softClose = true;
    }

    public @NotNull V player() {
        return this.player;
    }
}
