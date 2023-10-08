package ovh.roro.libraries.inventory.impl.confirmation.item;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.context.ConfirmationContext;
import ovh.roro.libraries.inventory.api.event.item.click.ItemClickHandler;
import ovh.roro.libraries.inventory.api.instance.ItemInstance;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;
import ovh.roro.libraries.language.api.Translation;

@ApiStatus.Internal
@SuppressWarnings("rawtypes")
public class ConfirmItem implements ItemInstance<ConfirmationContext, InventoryPlayerHolder>, ItemClickHandler<ConfirmationContext, InventoryPlayerHolder> {

    @Override
    public @NotNull ItemBuilder buildItem(@NotNull InventoryPlayerHolder player, @Nullable ConfirmationContext value) {
        return ItemBuilder.of(Material.SLIME_BALL)
                .name(Translation.translation("core.item.confirmation.confirm.name"));
    }

    @Override
    public void onClick(@NotNull InventoryPlayerHolder player, boolean isShiftClick, @Nullable ConfirmationContext value) {
        Preconditions.checkNotNull(value);

        //noinspection unchecked
        value.confirmationCallback().accept(player);
    }
}
