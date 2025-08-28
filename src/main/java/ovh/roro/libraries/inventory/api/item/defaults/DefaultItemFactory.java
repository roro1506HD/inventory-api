package ovh.roro.libraries.inventory.api.item.defaults;

import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.item.StaticItem;

@ApiStatus.NonExtendable
@ApiStatus.Internal
public interface DefaultItemFactory {

    @NotNull StaticItem separator(@NotNull Material material);

    @NotNull StaticItem separator(@NotNull Key key);

    @NotNull Item<Object, ?> back();

}
