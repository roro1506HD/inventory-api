package ovh.roro.libraries.inventory.api.slot;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.impl.slot.AttachedSlotImpl;
import ovh.roro.libraries.inventory.impl.slot.DynamicSlotImpl;
import ovh.roro.libraries.inventory.impl.slot.StaticSlotImpl;

@ApiStatus.NonExtendable
public interface SlotType {

    @NotNull SlotType DYNAMIC = DynamicSlotImpl.TYPE;
    @NotNull SlotType ATTACHED = AttachedSlotImpl.TYPE;
    @NotNull SlotType STATIC = StaticSlotImpl.TYPE;

    @SuppressWarnings("rawtypes")
    @NotNull Slot createSlot();

}
