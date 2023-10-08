package ovh.roro.libraries.inventory.impl.listener;

import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.annotation.ItemMovable;
import ovh.roro.libraries.inventory.api.event.item.click.ItemLeftClickHandler;
import ovh.roro.libraries.inventory.api.event.item.click.ItemRightClickHandler;
import ovh.roro.libraries.inventory.api.event.item.drop.ItemInventoryDropHandler;
import ovh.roro.libraries.inventory.api.instance.ItemInstance;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.slot.Slot;
import ovh.roro.libraries.inventory.impl.InventoryImpl;
import ovh.roro.libraries.inventory.impl.InventoryManagerImpl;
import ovh.roro.libraries.inventory.impl.InventoryWrapper;
import ovh.roro.libraries.inventory.impl.item.ItemImpl;
import ovh.roro.libraries.inventory.impl.slot.AttachedSlotImpl;
import ovh.roro.libraries.inventory.impl.slot.DynamicSlotImpl;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ItemInventoryListener implements Listener {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger("InventoryManager");

    private final @NotNull InventoryManagerImpl inventoryManager;

    public ItemInventoryListener(@NotNull InventoryManagerImpl inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        InventoryPlayerHolder mappedPlayer = Objects.requireNonNull(this.inventoryManager.playerMapper().apply(player.getUniqueId()));

        if (event.getInventory().getHolder() instanceof Player) {
            this.handlePlayerInventoryClick(event, mappedPlayer);
        } else if (((CraftInventory) event.getClickedInventory()).getInventory() instanceof InventoryWrapper wrapper) {
            this.handleInventoryWrapperClick(event, mappedPlayer, wrapper);
        } else {
            this.handleInventoryClick(event, mappedPlayer);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (((CraftInventory) event.getInventory()).getInventory() instanceof InventoryWrapper) {
            // Only check slots if the top inventory is an InventoryWrapper
            for (Integer slot : event.getNewItems().keySet()) {
                if (((CraftInventory) event.getView().getInventory(slot)).getInventory() instanceof InventoryWrapper) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        for (ItemStack itemStack : event.getNewItems().values()) {
            if (this.inventoryManager.parseItem(itemStack).isPresent()) {
                event.setCancelled(true);
                return;
            }
        }

        if (this.inventoryManager.parseItem(event.getCursor()).isPresent()) {
            event.setCancelled(true);
        }
    }

    private void handlePlayerInventoryClick(@NotNull InventoryClickEvent event, @NotNull InventoryPlayerHolder player) {
        if (event.getSlotType() == InventoryType.SlotType.ARMOR || event.getSlotType() == InventoryType.SlotType.CRAFTING) {
            if (event.getAction() != InventoryAction.HOTBAR_SWAP) {
                ItemStack cursorItem = player.bukkitPlayer().getItemOnCursor();

                this.inventoryManager.parseItem(cursorItem).ifPresent(item -> {
                    event.setCancelled(true);
                });
            } else {
                ItemStack hotbarItem = player.bukkitPlayer().getInventory().getItem(event.getHotbarButton());

                this.inventoryManager.parseItem(hotbarItem).ifPresent(item -> {
                    event.setCancelled(true);

                    ((ItemImpl) item).leftClickHandler().ifPresent(handler -> {
                        try {
                            ((ItemLeftClickHandler) handler).onLeftClick(player, false, null);
                        } catch (Throwable throwable) {
                            ItemInventoryListener.LOGGER.error("An exception occurred while handling player inventory click for item {}", item.instance().getClass().getSimpleName(), throwable);
                        }
                    });
                });
            }
        } else {
            this.handleInventoryClick(event, player);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void handleInventoryClick(@NotNull InventoryClickEvent event, @NotNull InventoryPlayerHolder player) {
        if (event.getClick() == ClickType.NUMBER_KEY) {
            Optional<Item> hotbarItem = this.inventoryManager.parseItem(player.bukkitPlayer().getInventory().getItem(event.getHotbarButton()));
            Optional<Item> slotItem = this.inventoryManager.parseItem(event.getClickedInventory().getItem(event.getSlot()));

            if (slotItem.isPresent()) {
                this.handleHotbarSwap(event, player, slotItem.get(), null, -1);
            } else if (hotbarItem.isPresent()) {
                this.handleHotbarSwap(event, player, hotbarItem.get(), null, -1);
            }

            return;
        }

        if (event.getClick().isShiftClick() && ((CraftInventory) event.getInventory()).getInventory() instanceof InventoryWrapper) {
            Inventory inventory = event.getInventory();
            int targetSlot = this.firstPossibleSlot(inventory, event.getCurrentItem());

            // Found a possible slot, cancel the event
            if (targetSlot != -1) {
                event.setCancelled(true);
            }
        }

        Optional<Item> clickedItem = this.inventoryManager.parseItem(event.getCurrentItem());

        if (clickedItem.isPresent()) {
            ItemImpl item = (ItemImpl) clickedItem.get();
            ItemInstance instance = item.instance();

            if (!instance.getClass().isAnnotationPresent(ItemMovable.class)) {
                event.setCancelled(true);
            }

            if (event.getClick() == ClickType.DOUBLE_CLICK) {
                event.setCancelled(true);
                return;
            }

            if (event.isLeftClick()) {
                item.leftClickHandler().ifPresent(handler -> {
                    try {
                        ((ItemLeftClickHandler) handler).onLeftClick(player, event.isShiftClick(), null);
                    } catch (Throwable throwable) {
                        ItemInventoryListener.LOGGER.error("An exception occurred while handling inventory click for item {}", instance.getClass().getSimpleName(), throwable);
                    }
                });
            } else if (event.isRightClick()) {
                item.rightClickHandler().ifPresent(handler -> {
                    try {
                        ((ItemRightClickHandler) handler).onRightClick(player, event.isShiftClick(), null);
                    } catch (Throwable throwable) {
                        ItemInventoryListener.LOGGER.error("An exception occurred while handling inventory click for item {}", instance.getClass().getSimpleName(), throwable);
                    }
                });
            } else if (event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) {
                item.inventoryDropHandler().ifPresent(handler -> {
                    try {
                        ((ItemInventoryDropHandler) handler).onDrop(player, null);
                    } catch (Throwable throwable) {
                        ItemInventoryListener.LOGGER.error("An exception occurred while handling inventory click for item {}", instance.getClass().getSimpleName(), throwable);
                    }
                });
            }
        }
    }

    private void handleInventoryWrapperClick(@NotNull InventoryClickEvent event, @NotNull InventoryPlayerHolder player, @NotNull InventoryWrapper wrapper) {
        InventoryImpl inventory = wrapper.inventory();
        Slot slot = inventory.inventoryContent().slot(event.getSlot());
        Item item = slot.item();

        if (event.getClick() == ClickType.NUMBER_KEY) {
            Optional<Item> hotbarItem = this.inventoryManager.parseItem(player.bukkitPlayer().getInventory().getItem(event.getHotbarButton()));

            if (item != null) {
                this.handleHotbarSwap(event, player, item, wrapper, event.getSlot());
            } else if (hotbarItem.isPresent()) {
                this.handleHotbarSwap(event, player, hotbarItem.get(), null, -1);
            } else {
                event.setCancelled(true);
            }

            return;
        }

        if (item == null) {
            if (!event.getCursor().isEmpty()) {
                event.setCancelled(true);
            }

            return;
        }

        ItemInstance instance = item.instance();
        Object value = null;

        if (slot instanceof AttachedSlotImpl attachedSlot) {
            value = Optional.ofNullable(attachedSlot.valueMapper())
                    .map(function -> function.apply(player, wrapper.value()))
                    .orElse(null);
        } else if (slot instanceof DynamicSlotImpl) {
            value = wrapper.value();
        }

        Object finalValue = value;

        if (!instance.getClass().isAnnotationPresent(ItemMovable.class)) {
            event.setCancelled(true);
        }

        if (event.getClick() == ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            return;
        }

        if (event.isLeftClick()) {
            ((ItemImpl) item).leftClickHandler().ifPresent(handler -> {
                try {
                    ((ItemLeftClickHandler) handler).onLeftClick(player, event.isShiftClick(), finalValue);
                } catch (Throwable throwable) {
                    ItemInventoryListener.LOGGER.error("An exception occurred while handling inventory click for item {}", instance.getClass().getSimpleName(), throwable);
                }
            });
        } else if (event.isRightClick()) {
            ((ItemImpl) item).rightClickHandler().ifPresent(handler -> {
                try {
                    ((ItemRightClickHandler) handler).onRightClick(player, event.isShiftClick(), finalValue);
                } catch (Throwable throwable) {
                    ItemInventoryListener.LOGGER.error("An exception occurred while handling inventory click for item {}", instance.getClass().getSimpleName(), throwable);
                }
            });
        } else if (event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) {
            ((ItemImpl) item).inventoryDropHandler().ifPresent(handler -> {
                try {
                    ((ItemInventoryDropHandler) handler).onDrop(player, finalValue);
                } catch (Throwable throwable) {
                    ItemInventoryListener.LOGGER.error("An exception occurred while handling inventory click for item {}", instance.getClass().getSimpleName(), throwable);
                }
            });
        }
    }

    private void handleHotbarSwap(@NotNull InventoryClickEvent event, @NotNull InventoryPlayerHolder player, @NotNull Item item, @Nullable InventoryWrapper wrapper, int slotIndex) {
        if (!item.instance().getClass().isAnnotationPresent(ItemMovable.class)) {
            event.setCancelled(true);
        }

        ((ItemImpl) item).leftClickHandler().ifPresent(handler -> {
            try {
                if (wrapper == null) {
                    ((ItemLeftClickHandler) handler).onLeftClick(player, false, null);
                } else {
                    InventoryImpl inventory = wrapper.inventory();
                    Slot slot = inventory.inventoryContent().slot(slotIndex);

                    if (slot instanceof AttachedSlotImpl attachedSlot) {
                        ((ItemLeftClickHandler) handler).onLeftClick(
                                player,
                                false,
                                Optional.ofNullable(attachedSlot.valueMapper())
                                        .map(function -> function.apply(player, wrapper.value()))
                                        .orElse(null)
                        );
                    } else if (slot instanceof DynamicSlotImpl) {
                        ((ItemLeftClickHandler) handler).onLeftClick(
                                player,
                                false,
                                wrapper.value()
                        );
                    } else {
                        ((ItemLeftClickHandler) handler).onLeftClick(
                                player,
                                false,
                                null
                        );
                    }
                }
            } catch (Throwable throwable) {
                ItemInventoryListener.LOGGER.error("An exception occurred while handling hotbar swap for item {}", item.instance().getClass().getSimpleName(), throwable);
            }
        });
    }

    private int firstPossibleSlot(@NotNull Inventory inventory, @NotNull ItemStack itemStack) {
        ItemStack[] contents = inventory.getStorageContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack slotItem = contents[i];

            // If the slot is empty OR if the item in the slot matches the provided item and its amount is lower than its max stack size
            if (slotItem == null || slotItem.getAmount() < slotItem.getMaxStackSize() && slotItem.isSimilar(itemStack)) {
                return i;
            }
        }

        return -1;
    }
}
