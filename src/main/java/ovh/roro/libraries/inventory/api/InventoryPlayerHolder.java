package ovh.roro.libraries.inventory.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.language.api.LanguagePlayerHolder;

public interface InventoryPlayerHolder extends LanguagePlayerHolder {

    @NotNull Player bukkitPlayer();

}
