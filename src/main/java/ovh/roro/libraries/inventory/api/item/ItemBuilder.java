package ovh.roro.libraries.inventory.api.item;

import net.minecraft.nbt.Tag;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.language.api.Translation;

@ApiStatus.NonExtendable
public interface ItemBuilder {

    static @NotNull ItemBuilder of(@NotNull Material material) {
        return InventoryManager.inventoryManager().createItemBuilder(material);
    }

    static @NotNull ItemBuilder fromLegacy(@NotNull ItemStack itemStack) {
        return InventoryManager.inventoryManager().fromLegacy(itemStack);
    }

    @Contract("_ -> this")
    @NotNull ItemBuilder material(@NotNull Material material);

    @NotNull Material material();

    @Contract("_ -> this")
    @NotNull ItemBuilder damage(int damage);

    short damage();

    @Contract("_ -> this")
    @NotNull ItemBuilder amount(int amount);

    int amount();

    @Contract("_ -> this")
    @NotNull ItemBuilder name(@Nullable Translation translation);

    @Nullable Translation name();

    @Contract("_ -> this")
    @NotNull ItemBuilder description(@NotNull Translation @Nullable ... translations);

    @Nullable Translation @NotNull [] description();

    @Contract("_, _ -> this")
    @NotNull ItemBuilder enchant(@NotNull Enchantment enchantment, int level);

    int enchant(@NotNull Enchantment enchantment);

    @Contract("_ -> this")
    @NotNull ItemBuilder flag(@NotNull ItemFlag @NotNull ... flags);

    boolean flag(@NotNull ItemFlag flag);

    @Contract("_ -> this")
    @NotNull ItemBuilder unbreakable(boolean unbreakable);

    boolean unbreakable();

    @Contract("_ -> this")
    @NotNull ItemBuilder glowing(boolean glowing);

    boolean glowing();

    @Contract("_ -> this")
    @NotNull ItemBuilder skull(@NotNull Player player);

    @Contract("_, _ -> this")
    @NotNull ItemBuilder skull(@NotNull String texture, @NotNull String signature);

    @Contract("_, _ -> this")
    @NotNull ItemBuilder nbt(@NotNull String key, @NotNull Tag tag);

    @Nullable Tag nbt(@NotNull String key);

    @NotNull ItemBuilder clone();

}
