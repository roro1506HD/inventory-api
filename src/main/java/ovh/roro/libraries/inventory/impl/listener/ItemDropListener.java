package ovh.roro.libraries.inventory.impl.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.annotation.ItemDroppable;
import ovh.roro.libraries.inventory.api.event.item.drop.ItemDropHandler;
import ovh.roro.libraries.inventory.impl.InventoryManagerImpl;
import ovh.roro.libraries.inventory.impl.item.ItemImpl;

import java.util.Objects;

public class ItemDropListener implements Listener {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger("InventoryManager");

    private final @NotNull InventoryManagerImpl inventoryManager;

    public ItemDropListener(@NotNull InventoryManagerImpl inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
        InventoryPlayerHolder player = Objects.requireNonNull(this.inventoryManager.playerMapper().apply(event.getPlayer().getUniqueId()));

        this.inventoryManager.parseItem(event.getItemDrop().getItemStack()).ifPresent(item -> {
            if (!item.instance().getClass().isAnnotationPresent(ItemDroppable.class)) {
                event.setCancelled(true);
            }

            ((ItemImpl) item).dropHandler().ifPresent(handler -> {
                try {
                    ((ItemDropHandler) handler).onDrop(player);
                } catch (Throwable throwable) {
                    ItemDropListener.LOGGER.error("An exception occurred while handling player drop for item {}", item.instance().getClass().getSimpleName(), throwable);
                }
            });
        });
    }
}
