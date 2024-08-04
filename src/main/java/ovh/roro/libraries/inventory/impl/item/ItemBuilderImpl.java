package ovh.roro.libraries.inventory.impl.item;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.bukkit.Material;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;
import ovh.roro.libraries.language.api.Translation;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@ApiStatus.Internal
public class ItemBuilderImpl implements ItemBuilder {

    private final @NotNull ItemStack delegate;
    private final @NotNull Object2BooleanMap<ItemFlag> flags;

    private @Nullable Translation name;
    private @NotNull Translation @Nullable [] description;

    public ItemBuilderImpl(@NotNull ItemStack delegate) {
        this(delegate, new Object2BooleanArrayMap<>());
    }

    public ItemBuilderImpl(@NotNull Material material, int amount) {
        this(new ItemStack(CraftMagicNumbers.getItem(material), amount));
    }

    private ItemBuilderImpl(@NotNull ItemStack delegate, @NotNull Object2BooleanMap<ItemFlag> flags) {
        this.delegate = delegate;
        this.flags = flags;
    }

    @Override
    public @NotNull Material material() {
        return CraftMagicNumbers.getMaterial(this.delegate.getItem());
    }

    @Override
    public @NotNull ItemBuilder damage(int damage) {
        this.delegate.setDamageValue(damage);
        return this;
    }

    @Override
    public int damage() {
        return this.delegate.getDamageValue();
    }

    @Override
    public @NotNull ItemBuilder amount(int amount) {
        this.delegate.setCount(amount);
        return this;
    }

    @Override
    public int amount() {
        return this.delegate.getCount();
    }

    @Override
    public @NotNull ItemBuilder name(@Nullable Translation translation) {
        this.name = translation;
        return this;
    }

    @Override
    public @Nullable Translation name() {
        return this.name;
    }

    @Override
    public @NotNull ItemBuilder description(@NotNull Translation @Nullable ... translations) {
        this.description = translations;
        return this;
    }

    @Override
    public @NotNull Translation @Nullable [] description() {
        return this.description;
    }

    @Override
    public @NotNull ItemBuilder enchant(@NotNull Enchantment enchantment, int level) {
        this.delegate.enchant(CraftEnchantment.bukkitToMinecraftHolder(enchantment), level);
        return this;
    }

    @Override
    public int enchant(@NotNull Enchantment enchantment) {
        return this.delegate.getEnchantments().getLevel(CraftEnchantment.bukkitToMinecraftHolder(enchantment));
    }

    @Override
    public @NotNull ItemBuilder removeEnchant(@NotNull Enchantment enchantment) {
        EnchantmentHelper.updateEnchantments(this.delegate, mutable -> {
            mutable.set(CraftEnchantment.bukkitToMinecraftHolder(enchantment), 0); // Level 0 removes the enchantment
        });

        return this;
    }

    @Override
    public @NotNull ItemBuilder flags(@NotNull ItemFlag @NotNull ... flags) {
        for (ItemFlag flag : flags) {
            this.flags.put(flag, true);
        }

        return this;
    }

    @Override
    public boolean flag(@NotNull ItemFlag flag) {
        if (this.flags.containsKey(flag)) {
            return this.flags.getBoolean(flag);
        }

        return switch (flag) {
            case HIDE_ENCHANTS -> {
                ItemEnchantments enchantments = this.delegate.get(DataComponents.ENCHANTMENTS);

                yield enchantments != null && !enchantments.showInTooltip;
            }
            case HIDE_ATTRIBUTES -> {
                ItemAttributeModifiers currentModifiers = this.delegate.get(DataComponents.ATTRIBUTE_MODIFIERS);

                yield currentModifiers != null && !currentModifiers.showInTooltip();
            }
            case HIDE_UNBREAKABLE -> {
                Unbreakable unbreakable = this.delegate.get(DataComponents.UNBREAKABLE);

                yield unbreakable != null && !unbreakable.showInTooltip();
            }
            case HIDE_DESTROYS -> {
                AdventureModePredicate predicate = this.delegate.get(DataComponents.CAN_BREAK);

                yield predicate != null && !predicate.showInTooltip();
            }
            case HIDE_PLACED_ON -> {
                AdventureModePredicate predicate = this.delegate.get(DataComponents.CAN_PLACE_ON);

                yield predicate != null && !predicate.showInTooltip();
            }
            case HIDE_ADDITIONAL_TOOLTIP -> this.delegate.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP);
            case HIDE_DYE -> {
                DyedItemColor color = this.delegate.get(DataComponents.DYED_COLOR);

                yield color != null && !color.showInTooltip();
            }
            case HIDE_ARMOR_TRIM -> {
                ArmorTrim trim = this.delegate.get(DataComponents.TRIM);

                yield trim != null && !trim.showInTooltip;
            }
            case HIDE_STORED_ENCHANTS -> {
                ItemEnchantments enchantments = this.delegate.get(DataComponents.STORED_ENCHANTMENTS);

                yield enchantments != null && enchantments.showInTooltip;
            }
        };
    }

    @Override
    public @NotNull ItemBuilder removeFlags(@NotNull ItemFlag @NotNull ... flags) {
        for (ItemFlag flag : flags) {
            this.flags.put(flag, false);
        }

        return this;
    }

    @Override
    public @NotNull ItemBuilder hideTooltip(boolean hide) {
        if (hide) {
            this.delegate.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
        } else {
            this.delegate.remove(DataComponents.HIDE_TOOLTIP);
        }

        return this;
    }

    @Override
    public @NotNull ItemBuilder unbreakable(boolean unbreakable) {
        this.delegate.set(DataComponents.UNBREAKABLE, new Unbreakable(true));

        return this;
    }

    @Override
    public boolean unbreakable() {
        return this.delegate.has(DataComponents.UNBREAKABLE);
    }

    @Override
    public @NotNull ItemBuilder glowing(boolean glowing) {
        this.delegate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glowing);

        return this;
    }

    @Override
    public boolean glowing() {
        Boolean glint = this.delegate.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        if (glint != null) {
            return glint;
        }

        return this.delegate.getItem().isFoil(this.delegate);
    }

    @Override
    public @NotNull ItemBuilder skull(@NotNull Player player) {
        GameProfile gameProfile = ((CraftPlayer) player).getProfile();

        for (Property property : gameProfile.getProperties().get("textures")) {
            return this.skull(property.value(), Objects.requireNonNull(property.signature()));
        }

        return this;
    }

    @Override
    public @NotNull ItemBuilder skull(@NotNull String texture, @NotNull String signature) {
        Preconditions.checkState(this.delegate.getItem() == Items.PLAYER_HEAD, "ItemBuilder#skull can only be used on skulls");

        PropertyMap properties = new PropertyMap();

        properties.put("textures", new Property("textures", texture, signature));

        this.delegate.set(
                DataComponents.PROFILE,
                new ResolvableProfile(
                        Optional.empty(),
                        Optional.of(UUID.randomUUID()),
                        properties
                )
        );

        return this;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public @NotNull ItemBuilder clone() {
        ItemBuilderImpl builder = new ItemBuilderImpl(
                this.delegate.copy(),
                new Object2BooleanArrayMap<>(this.flags)
        );

        builder.name = this.name;
        builder.description = this.description;

        return builder;
    }

    public @NotNull ItemStack delegate() {
        return this.delegate;
    }

    public @NotNull Object2BooleanMap<ItemFlag> flags() {
        return this.flags;
    }
}
