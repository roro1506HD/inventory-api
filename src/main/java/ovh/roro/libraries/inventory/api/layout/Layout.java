package ovh.roro.libraries.inventory.api.layout;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.impl.layout.CornersLayoutImpl;
import ovh.roro.libraries.inventory.impl.layout.OutlineLayoutImpl;

@ApiStatus.OverrideOnly
public interface Layout {

    @NotNull Layout CORNERS = CornersLayoutImpl.INSTANCE;
    @NotNull Layout OUTLINE = OutlineLayoutImpl.INSTANCE;

    int @NotNull [] slots(int inventorySize);

}
