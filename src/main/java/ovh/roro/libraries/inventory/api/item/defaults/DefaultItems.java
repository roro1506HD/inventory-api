package ovh.roro.libraries.inventory.api.item.defaults;

import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.item.StaticItem;

@ApiStatus.NonExtendable
public interface DefaultItems {

    static @NotNull StaticItem separator(@NotNull Material material) {
        return InventoryManager.inventoryManager().defaultItemFactory().separator(material);
    }

    static @NotNull StaticItem separator(@NotNull Key key) {
        return InventoryManager.inventoryManager().defaultItemFactory().separator(key);
    }

    static @NotNull Item<Object, ?> back() {
        return InventoryManager.inventoryManager().defaultItemFactory().back();
    }
}
