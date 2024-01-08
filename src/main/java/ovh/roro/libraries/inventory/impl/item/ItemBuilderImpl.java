package ovh.roro.libraries.inventory.impl.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;
import ovh.roro.libraries.language.api.Translation;

import java.util.Objects;
import java.util.UUID;

@ApiStatus.Internal
public class ItemBuilderImpl implements ItemBuilder {

    private final @NotNull CompoundTag rootTag;

    private @Nullable Translation name;
    private @NotNull Translation @Nullable [] description;

    public ItemBuilderImpl(@NotNull CompoundTag rootTag) {
        this.rootTag = rootTag;
    }

    public ItemBuilderImpl(@NotNull Material material, int amount) {
        this(new CompoundTag());

        this.material(material);
        this.amount(amount);
    }

    @Override
    public @NotNull ItemBuilder material(@NotNull Material material) {
        this.rootTag.putString("id", material.getKey().toString());
        return this;
    }

    @Override
    public @NotNull Material material() {
        NamespacedKey key = Objects.requireNonNull(NamespacedKey.fromString(this.rootTag.getString("id")));
        return Objects.requireNonNull(Registry.MATERIAL.get(key));
    }

    @Override
    public @NotNull ItemBuilder damage(int damage) {
        this.tag().putInt("Damage", damage);
        return this;
    }

    @Override
    public short damage() {
        return this.tag().getShort("Damage");
    }

    @Override
    public @NotNull ItemBuilder amount(int amount) {
        this.rootTag.putByte("Count", (byte) amount);
        return this;
    }

    @Override
    public int amount() {
        return this.rootTag.getByte("Count");
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
        CompoundTag tag = this.tag();

        if (!tag.contains("Enchantments", Tag.TAG_LIST)) {
            tag.put("Enchantments", new ListTag());
        }

        ListTag enchantList = tag.getList("Enchantments", Tag.TAG_COMPOUND);
        CompoundTag enchant = new CompoundTag();

        enchant.putString("id", enchantment.getKey().toString());
        enchant.putShort("lvl", (short) level);

        enchantList.add(enchant);
        return this;
    }

    @Override
    public int enchant(@NotNull Enchantment enchantment) {
        CompoundTag tag = this.tag();

        if (!tag.contains("Enchantments", Tag.TAG_LIST)) {
            return 0;
        }

        ListTag enchantList = tag.getList("Enchantments", Tag.TAG_COMPOUND);

        for (Tag enchant : enchantList) {
            CompoundTag castedEnchant = (CompoundTag) enchant;

            if (castedEnchant.getString("id").equals(enchantment.getKey().toString())) {
                return castedEnchant.getShort("lvl");
            }
        }

        return 0;
    }

    @Override
    public @NotNull ItemBuilder flag(@NotNull ItemFlag @NotNull ... flags) {
        CompoundTag tag = this.tag();
        int hideFlags = tag.getInt("HideFlags");

        for (ItemFlag flag : flags) {
            hideFlags |= (byte) (1 << flag.ordinal());
        }

        tag.putInt("HideFlags", hideFlags);
        return this;
    }

    @Override
    public boolean flag(@NotNull ItemFlag flag) {
        CompoundTag tag = this.tag();
        int hideFlags = tag.getInt("HideFlags");

        return (hideFlags & (1 << flag.ordinal())) != 0;
    }

    @Override
    public @NotNull ItemBuilder unbreakable(boolean unbreakable) {
        if (unbreakable) {
            this.tag().putBoolean("Unbreakable", true);
        } else {
            this.tag().remove("Unbreakable");
        }

        return this;
    }

    @Override
    public boolean unbreakable() {
        return this.tag().getBoolean("Unbreakable");
    }

    @Override
    public @NotNull ItemBuilder glowing(boolean glowing) {
        CompoundTag tag = this.tag();

        if (glowing) {
            if (!tag.contains("Enchantments", Tag.TAG_LIST)) {
                tag.put("Enchantments", new ListTag());
            }

            ListTag enchantList = tag.getList("Enchantments", Tag.TAG_COMPOUND);
            CompoundTag enchant = new CompoundTag();

            enchant.putString("id", "minecraft:just_glowing");
            enchant.putShort("lvl", (short) 1);

            enchantList.add(enchant);
        } else {
            tag.remove("Enchantments");
        }

        return this;
    }

    @Override
    public boolean glowing() {
        return this.tag().contains("Enchantments", Tag.TAG_LIST);
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
        this.material(Material.PLAYER_HEAD);

        CompoundTag skullOwner = new CompoundTag();
        CompoundTag properties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag value = new CompoundTag();

        value.putString("Value", texture);

        textures.add(value);

        properties.put("textures", textures);

        skullOwner.putUUID("Id", UUID.randomUUID());
        skullOwner.put("Properties", properties);

        this.tag().put("SkullOwner", skullOwner);

        return this;
    }

    @Override
    public @NotNull ItemBuilder nbt(@NotNull String key, @NotNull Tag tag) {
        this.rootTag.put(key, tag);
        return this;
    }

    @Override
    public @Nullable Tag nbt(@NotNull String key) {
        return this.rootTag.get(key);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public @NotNull ItemBuilder clone() {
        ItemBuilderImpl builder = new ItemBuilderImpl(this.rootTag.copy());

        builder.name = this.name;
        builder.description = this.description;

        return builder;
    }

    public @NotNull CompoundTag rootTag() {
        return this.rootTag;
    }

    public @NotNull CompoundTag tag() {
        if (!this.rootTag.contains("tag")) {
            CompoundTag tag = new CompoundTag();

            this.rootTag.put("tag", tag);

            return tag;
        }

        return this.rootTag.getCompound("tag");
    }
}
