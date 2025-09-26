package ovh.roro.libraries.inventory.impl.item;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.key.Key;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.bukkit.Material;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;
import ovh.roro.libraries.language.api.Translation;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@ApiStatus.Internal
public class ItemBuilderImpl implements ItemBuilder {

    private final @NotNull ItemStack delegate;

    private @Nullable Translation name;
    private @NotNull Translation @Nullable [] description;

    public ItemBuilderImpl(@NotNull ItemStack delegate) {
        this.delegate = delegate;
    }

    public ItemBuilderImpl(@NotNull Material material, int amount) {
        this(new ItemStack(CraftMagicNumbers.getItem(material), amount));
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
    public @NotNull ItemBuilder hideTooltip(boolean hide) {
        if (hide) {
            TooltipDisplay display = this.delegate.get(DataComponents.TOOLTIP_DISPLAY);

            if (display == null) {
                display = new TooltipDisplay(true, new LinkedHashSet<>());
            } else {
                display = new TooltipDisplay(true, display.hiddenComponents());
            }

            this.delegate.set(DataComponents.TOOLTIP_DISPLAY, display);
        } else {
            TooltipDisplay display = this.delegate.get(DataComponents.TOOLTIP_DISPLAY);

            if (display != null && display.hideTooltip()) {
                if (display.hiddenComponents().isEmpty()) {
                    this.delegate.remove(DataComponents.TOOLTIP_DISPLAY);
                } else {
                    this.delegate.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, display.hiddenComponents()));
                }
            }
        }

        return this;
    }

    @Override
    public @NotNull ItemBuilder hideComponents(io.papermc.paper.datacomponent.@NotNull DataComponentType... componentTypes) {
        Key[] componentKeys = new Key[componentTypes.length];

        for (int i = 0; i < componentTypes.length; i++) {
            componentKeys[i] = componentTypes[i].key();
        }

        return this.hideComponents(componentKeys);
    }

    @Override
    public @NotNull ItemBuilder hideComponents(@NotNull Key... componentKeys) {
        TooltipDisplay display = this.delegate.get(DataComponents.TOOLTIP_DISPLAY);
        Set<DataComponentType<?>> vanillaTypes = new HashSet<>();

        for (Key componentKey : componentKeys) {
            DataComponentType<?> vanillaType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(PaperAdventure.asVanilla(componentKey));

            if (vanillaType == null) {
                throw new IllegalArgumentException("Component key does not match any component: " + componentKey);
            }

            vanillaTypes.add(vanillaType);
        }

        if (display == null) {
            display = new TooltipDisplay(false, new LinkedHashSet<>());
            display.hiddenComponents().addAll(vanillaTypes);
        } else {
            display = new TooltipDisplay(display.hideTooltip(), new LinkedHashSet<>(display.hiddenComponents()));
            display.hiddenComponents().addAll(vanillaTypes);
        }

        this.delegate.set(DataComponents.TOOLTIP_DISPLAY, display);

        return this;
    }

    @Override
    public @NotNull ItemBuilder hideAllComponents() {
        TooltipDisplay display = this.delegate.get(DataComponents.TOOLTIP_DISPLAY);

        if (display == null) {
            display = new TooltipDisplay(true, new LinkedHashSet<>());

            for (DataComponentType<?> componentType : BuiltInRegistries.DATA_COMPONENT_TYPE) {
                display.hiddenComponents().add(componentType);
            }
        } else {
            display = new TooltipDisplay(display.hideTooltip(), new LinkedHashSet<>());

            for (DataComponentType<?> componentType : BuiltInRegistries.DATA_COMPONENT_TYPE) {
                display.hiddenComponents().add(componentType);
            }
        }

        this.delegate.set(DataComponents.TOOLTIP_DISPLAY, display);

        return this;
    }

    @Override
    public @NotNull ItemBuilder showComponents(io.papermc.paper.datacomponent.@NotNull DataComponentType... componentTypes) {
        Key[] componentKeys = new Key[componentTypes.length];

        for (int i = 0; i < componentTypes.length; i++) {
            componentKeys[i] = componentTypes[i].key();
        }

        return this.showComponents(componentKeys);
    }

    @Override
    public @NotNull ItemBuilder showComponents(@NotNull Key... componentKeys) {
        TooltipDisplay display = this.delegate.get(DataComponents.TOOLTIP_DISPLAY);

        if (display == null) {
            return this;
        }

        Set<DataComponentType<?>> vanillaTypes = new HashSet<>();

        for (Key componentKey : componentKeys) {
            DataComponentType<?> vanillaType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(PaperAdventure.asVanilla(componentKey));

            if (vanillaType == null) {
                throw new IllegalArgumentException("Component key does not match any component: " + componentKey);
            }

            vanillaTypes.add(vanillaType);
        }

        display = new TooltipDisplay(display.hideTooltip(), new LinkedHashSet<>(display.hiddenComponents()));
        display.hiddenComponents().removeAll(vanillaTypes);

        if (display.hiddenComponents().isEmpty() && !display.hideTooltip()) {
            this.delegate.remove(DataComponents.TOOLTIP_DISPLAY);
        } else {
            this.delegate.set(DataComponents.TOOLTIP_DISPLAY, display);
        }

        return this;
    }

    @Override
    public @NotNull ItemBuilder showAllComponents() {
        TooltipDisplay display = this.delegate.get(DataComponents.TOOLTIP_DISPLAY);

        if (display != null) {
            if (display.hideTooltip()) {
                this.delegate.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, new LinkedHashSet<>()));
            } else {
                this.delegate.remove(DataComponents.TOOLTIP_DISPLAY);
            }
        }

        return this;
    }

    @Override
    public boolean isComponentHidden(io.papermc.paper.datacomponent.@NotNull DataComponentType componentType) {
        return this.isComponentHidden(componentType.key());
    }

    @Override
    public boolean isComponentHidden(@NotNull Key componentKey) {
        TooltipDisplay display = this.delegate.get(DataComponents.TOOLTIP_DISPLAY);

        if (display == null) {
            return false;
        }

        DataComponentType<?> vanillaType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(PaperAdventure.asVanilla(componentKey));

        if (vanillaType == null) {
            throw new IllegalArgumentException("Component key does not match any components: " + componentKey);
        }

        return display.hiddenComponents().contains(vanillaType);
    }

    @Override
    public @NotNull ItemBuilder unbreakable(boolean unbreakable) {
        this.delegate.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);

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

        for (Property property : gameProfile.properties().get("textures")) {
            return this.skull(property.value(), Objects.requireNonNull(property.signature()));
        }

        return this;
    }

    @Override
    public @NotNull ItemBuilder skull(@NotNull String texture, @NotNull String signature) {
        Preconditions.checkState(this.delegate.getItem() == Items.PLAYER_HEAD, "ItemBuilder#skull can only be used on skulls");

        GameProfile gameProfile = new GameProfile(
                UUID.randomUUID(),
                "",
                new PropertyMap(ImmutableMultimap.of(
                        "textures",
                        new Property("textures", texture, signature)
                ))
        );

        this.delegate.set(DataComponents.PROFILE, ResolvableProfile.createResolved(gameProfile));

        return this;
    }

    @Override
    public @NotNull ItemBuilder overrideModel(@Nullable Key key) {
        if (key == null) {
            this.delegate.set(DataComponents.ITEM_MODEL, this.delegate.getItem().components().get(DataComponents.ITEM_MODEL));
        } else {
            this.delegate.set(DataComponents.ITEM_MODEL, PaperAdventure.asVanilla(key));
        }

        return this;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public @NotNull ItemBuilder clone() {
        ItemBuilderImpl builder = new ItemBuilderImpl(this.delegate.copy());

        builder.name = this.name;
        builder.description = this.description;

        return builder;
    }

    public @NotNull ItemStack delegate() {
        return this.delegate;
    }
}
