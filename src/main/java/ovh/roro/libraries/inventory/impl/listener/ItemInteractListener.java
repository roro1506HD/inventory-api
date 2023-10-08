package ovh.roro.libraries.inventory.impl.listener;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.annotation.ItemInteractionSettings;
import ovh.roro.libraries.inventory.api.event.item.interact.ItemInteractLeftClickHandler;
import ovh.roro.libraries.inventory.api.event.item.interact.ItemInteractRightClickHandler;
import ovh.roro.libraries.inventory.impl.InventoryManagerImpl;
import ovh.roro.libraries.inventory.impl.item.ItemImpl;

import java.util.Objects;

public class ItemInteractListener implements Listener {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger("InventoryManager");

    private final @NotNull InventoryManagerImpl inventoryManager;

    public ItemInteractListener(@NotNull InventoryManagerImpl inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR || event.getItem() == null) {
            return;
        }

        InventoryPlayerHolder player = Objects.requireNonNull(this.inventoryManager.playerMapper().apply(event.getPlayer().getUniqueId()));

        this.inventoryManager.parseItem(event.getItem()).ifPresent(item -> {
            ItemInteractionSettings interactionSettings = item.instance().getClass().getAnnotation(ItemInteractionSettings.class);
            Action action = event.getAction();

            if (interactionSettings != null) {
                if (!interactionSettings.leftClickAir() && action == Action.LEFT_CLICK_AIR) {
                    return;
                }

                if (!interactionSettings.leftClickBlock() && action == Action.LEFT_CLICK_BLOCK) {
                    return;
                }

                if (!interactionSettings.rightClickAir() && action == Action.RIGHT_CLICK_AIR) {
                    return;
                }

                if (!interactionSettings.rightClickBlock() && action == Action.RIGHT_CLICK_BLOCK) {
                    return;
                }
            }

            event.setCancelled(true);

            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                ((ItemImpl) item).interactRightClickHandler().ifPresent(handler -> {
                    try {
                        ((ItemInteractRightClickHandler) handler).onInteractRightClick(player, event.getClickedBlock());
                    } catch (Throwable throwable) {
                        ItemInteractListener.LOGGER.error("An exception occurred while handling player interaction for item {}", item.instance().getClass().getSimpleName(), throwable);
                    }
                });
            } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                ((ItemImpl) item).interactLeftClickHandler().ifPresent(handler -> {
                    try {
                        ((ItemInteractLeftClickHandler) handler).onInteractLeftClick(player, event.getClickedBlock());
                    } catch (Throwable throwable) {
                        ItemInteractListener.LOGGER.error("An exception occurred while handling player interaction for item {}", item.instance().getClass().getSimpleName(), throwable);
                    }
                });
            }
        });
    }
}
