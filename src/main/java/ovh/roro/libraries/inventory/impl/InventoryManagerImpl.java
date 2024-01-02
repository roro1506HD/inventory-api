package ovh.roro.libraries.inventory.impl;

import com.google.common.base.Preconditions;
import io.papermc.paper.adventure.PaperAdventure;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.roro.libraries.inventory.api.ClassicInventory;
import ovh.roro.libraries.inventory.api.ConfirmationInventory;
import ovh.roro.libraries.inventory.api.Inventory;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.inventory.api.InventoryPlayerHolder;
import ovh.roro.libraries.inventory.api.PageableInventory;
import ovh.roro.libraries.inventory.api.context.PaginationContext;
import ovh.roro.libraries.inventory.api.instance.ClassicInventoryInstance;
import ovh.roro.libraries.inventory.api.instance.ConfirmationInventoryInstance;
import ovh.roro.libraries.inventory.api.instance.InventoryInstance;
import ovh.roro.libraries.inventory.api.instance.ItemInstance;
import ovh.roro.libraries.inventory.api.instance.PageableInventoryInstance;
import ovh.roro.libraries.inventory.api.instance.StaticItemInstance;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.item.ItemBuilder;
import ovh.roro.libraries.inventory.api.item.StaticItem;
import ovh.roro.libraries.inventory.impl.classic.ClassicInventoryImpl;
import ovh.roro.libraries.inventory.impl.confirmation.ConfirmationInventoryImpl;
import ovh.roro.libraries.inventory.impl.item.ItemBuilderImpl;
import ovh.roro.libraries.inventory.impl.item.ItemImpl;
import ovh.roro.libraries.inventory.impl.item.StaticItemImpl;
import ovh.roro.libraries.inventory.impl.item.defaults.DefaultItemFactoryImpl;
import ovh.roro.libraries.inventory.impl.listener.ItemDropListener;
import ovh.roro.libraries.inventory.impl.listener.ItemInteractListener;
import ovh.roro.libraries.inventory.impl.listener.ItemInventoryListener;
import ovh.roro.libraries.inventory.impl.pageable.PageableInventoryImpl;
import ovh.roro.libraries.inventory.impl.pageable.item.NextItem;
import ovh.roro.libraries.inventory.impl.pageable.item.PreviousItem;
import ovh.roro.libraries.language.api.LanguageManager;
import ovh.roro.libraries.language.api.Translation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

@ApiStatus.Internal
@SuppressWarnings("rawtypes")
public class InventoryManagerImpl implements InventoryManager {

    public static final @NotNull InventoryManagerImpl INSTANCE = new InventoryManagerImpl();

    private static final @NotNull Int2ObjectMap<MenuType<?>> ROWS_TO_MENU_TYPE = Util.make(new Int2ObjectArrayMap<>(), map -> {
        map.defaultReturnValue(null);

        map.put(1, MenuType.GENERIC_9x1);
        map.put(2, MenuType.GENERIC_9x2);
        map.put(3, MenuType.GENERIC_9x3);
        map.put(4, MenuType.GENERIC_9x4);
        map.put(5, MenuType.GENERIC_9x5);
        map.put(6, MenuType.GENERIC_9x6);
    });

    private final @NotNull Server server;

    private final @NotNull AtomicInteger itemIdCounter;
    private final @NotNull Int2ObjectMap<Item> itemById;
    private final @NotNull Map<UUID, Deque<InventoryAttachment>> lastInventories;

    private final @NotNull DefaultItemFactoryImpl defaultItemFactory;

    private @MonotonicNonNull Item<PaginationContext, ?> previousItem;
    private @MonotonicNonNull Item<PaginationContext, ?> nextItem;

    private boolean registered;
    private @MonotonicNonNull Function<UUID, InventoryPlayerHolder> playerMapper;

    private InventoryManagerImpl() {
        this.server = Bukkit.getServer();

        this.itemIdCounter = new AtomicInteger(0);
        this.itemById = new Int2ObjectArrayMap<>();
        this.lastInventories = new HashMap<>();

        this.defaultItemFactory = new DefaultItemFactoryImpl(this);
    }

    @Override
    public void register(@NotNull JavaPlugin plugin, @NotNull Function<UUID, InventoryPlayerHolder> playerMapper) {
        Preconditions.checkArgument(!this.registered, "InventoryManager already registered");

        this.registered = true;
        this.playerMapper = Objects.requireNonNull(playerMapper);

        this.server.getPluginManager().registerEvents(new ItemDropListener(this), plugin);
        this.server.getPluginManager().registerEvents(new ItemInteractListener(this), plugin);
        this.server.getPluginManager().registerEvents(new ItemInventoryListener(this), plugin);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, U extends InventoryInstance<T, V>, V extends InventoryPlayerHolder> void openInventory(@NotNull Inventory<T, U, V> inventory, @NotNull V player, @Nullable T value) {
        InventoryImpl<T, U, V> inventoryImpl = (InventoryImpl<T, U, V>) inventory;
        int rows = inventoryImpl.rows();
        MenuType<?> menuType = InventoryManagerImpl.ROWS_TO_MENU_TYPE.get(rows);

        if (menuType == null) {
            throw new IllegalArgumentException("Cannot open inventory of " + rows + " rows");
        }

        InventoryWrapper<T, U, V> wrapper = new InventoryWrapper<>(player, inventoryImpl, value);
        ServerPlayer serverPlayer = ((CraftPlayer) player.bukkitPlayer()).getHandle();

        wrapper.inventory().ensureIsBuilt();
        wrapper.inventory().updateInventory(player, value);

        this.lastInventories.computeIfAbsent(serverPlayer.getUUID(), uuid -> new ArrayDeque<>()).add(new InventoryAttachment<>(inventory, value));

        int containerCounter = serverPlayer.nextContainerCounter();
        AbstractContainerMenu menu = CraftEventFactory.callInventoryOpenEvent(
                serverPlayer,
                new ChestMenu(
                        menuType,
                        containerCounter,
                        serverPlayer.getInventory(),
                        wrapper,
                        rows
                )
        );

        if (menu == null) {
            return;
        }

        //noinspection OverrideOnly
        net.kyori.adventure.text.Component title = LanguageManager.languageManager().translate(player.language(), inventoryImpl.title(player, value));

        serverPlayer.connection.send(
                new ClientboundOpenScreenPacket(
                        containerCounter,
                        menuType,
                        PaperAdventure.asVanilla(title)
                )
        );

        serverPlayer.containerMenu = menu;

        serverPlayer.initMenu(menu);
    }

    @Override
    public void openPreviousInventory(@NotNull InventoryPlayerHolder player) {
        this.openPreviousInventory(player, 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void openPreviousInventory(@NotNull InventoryPlayerHolder player, int inventoriesToSkip) {
        Deque<InventoryAttachment> queue = this.lastInventories.get(player.bukkitPlayer().getUniqueId());

        if (queue == null) {
            return;
        }

        ServerPlayer serverPlayer = ((CraftPlayer) player.bukkitPlayer()).getHandle();

        if (!queue.isEmpty() &&
                serverPlayer.containerMenu instanceof ChestMenu chestMenu &&
                chestMenu.getContainer() instanceof InventoryWrapper<?, ?, ?> wrapper &&
                wrapper.inventory().equals(queue.getLast().inventory())
        ) {
            queue.pollLast();
        }

        for (int i = 0; i < inventoriesToSkip; i++) {
            queue.pollLast();
        }

        if (queue.isEmpty()) {
            player.bukkitPlayer().closeInventory();
        } else {
            InventoryAttachment attachment = queue.removeLast();

            this.openInventory(attachment.inventory(), player, attachment.attachment());
        }
    }

    @Override
    public boolean hasPreviousInventory(@NotNull InventoryPlayerHolder player) {
        Deque<InventoryAttachment> queue = this.lastInventories.get(player.bukkitPlayer().getUniqueId());

        return queue != null && queue.size() > 1;
    }

    @Override
    public void updateInventory(@NotNull InventoryPlayerHolder player) {
        AbstractContainerMenu containerMenu = ((CraftPlayer) player.bukkitPlayer()).getHandle().containerMenu;

        if (!(containerMenu instanceof ChestMenu chestMenu)) {
            return;
        }

        Container container = chestMenu.getContainer();

        if (container instanceof InventoryWrapper wrapper) {
            wrapper.updateInventory();
        }
    }

    @Override
    public void softCloseInventory(@NotNull InventoryPlayerHolder player) {
        AbstractContainerMenu containerMenu = ((CraftPlayer) player.bukkitPlayer()).getHandle().containerMenu;

        if (!(containerMenu instanceof ChestMenu chestMenu)) {
            return;
        }

        Container container = chestMenu.getContainer();

        if (container instanceof InventoryWrapper wrapper) {
            wrapper.softClose();
            player.bukkitPlayer().closeInventory();
        }
    }

    @Override
    public <T extends InventoryPlayerHolder> @NotNull List<T> getInventoryViewers(@NotNull Inventory<?, ?, T> inventory) {
        List<T> players = new ArrayList<>();

        for (Player player : this.server.getOnlinePlayers()) {
            if (((CraftPlayer) player).getHandle().containerMenu instanceof ChestMenu chestMenu &&
                    chestMenu.getContainer() instanceof InventoryWrapper<?, ?, ?> wrapper && wrapper.inventory() == inventory) {
                //noinspection unchecked
                players.add((T) wrapper.player());
            }
        }

        return players;
    }

    @Override
    public @NotNull Optional<@NotNull Item> parseItem(@Nullable ItemStack itemStack) {
        return this.parseItem(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public @NotNull Optional<@NotNull Item> parseItem(@Nullable net.minecraft.world.item.ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTag() || !itemStack.getTag().contains("inventory_api_item", Tag.TAG_INT)) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.itemById.get(itemStack.getTag().getInt("inventory_api_item")));
    }

    @Override
    public @NotNull <T, U extends InventoryPlayerHolder> ClassicInventory<T, U> createInventory(@NotNull ClassicInventoryInstance<T, U> inventoryInstance) {
        return new ClassicInventoryImpl<>(inventoryInstance);
    }

    @Override
    public @NotNull <T, U, V extends InventoryPlayerHolder> PageableInventory<T, U, V> createPageableInventory(@NotNull PageableInventoryInstance<T, U, V> inventoryInstance) {
        return new PageableInventoryImpl<>(this, inventoryInstance);
    }

    @Override
    public @NotNull <T, U extends InventoryPlayerHolder> ConfirmationInventory<T, U> createConfirmationInventory(@NotNull ConfirmationInventoryInstance<T, U> inventoryInstance) {
        return new ConfirmationInventoryImpl<>(inventoryInstance);
    }

    @Override
    public <T, U extends InventoryPlayerHolder> @NotNull Item<T, U> createItem(@NotNull ItemInstance<T, U> itemInstance) {
        ItemImpl<T, U> item = new ItemImpl<>(itemInstance, this.itemIdCounter.incrementAndGet());

        this.itemById.put(item.id(), item);

        return item;
    }

    @Override
    public @NotNull StaticItem createStaticItem(@NotNull StaticItemInstance itemInstance) {
        StaticItemImpl item = new StaticItemImpl(itemInstance, this.itemIdCounter.incrementAndGet());

        this.itemById.put(item.id(), item);

        return item;
    }

    @Override
    public @NotNull ItemBuilder createItemBuilder(@NotNull Material material) {
        return this.createItemBuilder(material, 1);
    }

    @Override
    public @NotNull ItemBuilder createItemBuilder(@NotNull Material material, int amount) {
        return new ItemBuilderImpl(material, amount);
    }

    @Override
    public @NotNull ItemBuilder fromLegacy(@NotNull ItemStack itemStack) {
        return this.fromLegacy(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public @NotNull ItemBuilder fromLegacy(@NotNull net.minecraft.world.item.ItemStack itemStack) {
        return new ItemBuilderImpl(itemStack.save(new CompoundTag()));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public @NotNull <T, U extends InventoryPlayerHolder> net.minecraft.world.item.ItemStack toMinecraftStack(@NotNull Item<T, U> item, @NotNull U player, @Nullable T value) {
        return this.toMinecraftStack(player, item.instance().buildItem(player, value), item);
    }

    public @Nullable net.minecraft.world.item.ItemStack toMinecraftStack(@NotNull InventoryPlayerHolder player, @Nullable ItemBuilder builder, @Nullable Item item) {
        if (builder == null || item == null) {
            return null;
        }

        ItemBuilderImpl clonedBuilder = (ItemBuilderImpl) builder.clone();
        CompoundTag display = clonedBuilder.tag().getCompound("display");
        boolean changedDisplay = false;

        clonedBuilder.tag().putInt("inventory_api_item", ((ItemImpl) item).id());

        Translation name = clonedBuilder.name();
        if (name != null) {
            display.putString("Name", this.removeDefaultItalicAndSerialize(PaperAdventure.asVanilla(LanguageManager.languageManager().translate(player.language(), name))));
            changedDisplay = true;
        }

        Translation[] description = clonedBuilder.description();
        if (description != null) {
            ListTag lore = new ListTag();

            for (Translation translation : description) {
                net.kyori.adventure.text.Component translatedComponent = LanguageManager.languageManager().translate(player.language(), translation);

                Component lastComponent = this.splitAndCollectNewlines(PaperAdventure.asVanilla(translatedComponent), null, component -> {
                    lore.add(StringTag.valueOf(this.removeDefaultItalicAndSerialize(component)));
                });

                if (lastComponent != null) {
                    lore.add(StringTag.valueOf(this.removeDefaultItalicAndSerialize(lastComponent)));
                }
            }

            if (!lore.isEmpty()) {
                display.put("Lore", lore);
                changedDisplay = true;
            }
        }

        if (changedDisplay) {
            clonedBuilder.tag().put("display", display);
        }

        return net.minecraft.world.item.ItemStack.of(clonedBuilder.rootTag());
    }

    private @Nullable MutableComponent splitAndCollectNewlines(
            @NotNull Component component,
            @Nullable MutableComponent currentComponent,
            @NotNull Consumer<net.minecraft.network.chat.Component> consumer
    ) {
        if (component.getContents() instanceof LiteralContents literalContents) {
            String text = literalContents.text();
            boolean endsWithNewLine = text.endsWith("\n");
            String[] lines = text.split("\n");

            if (lines.length > 0) {
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];

                    currentComponent = this.setOrAppend(currentComponent, Component.literal(line).withStyle(component.getStyle()));

                    if (endsWithNewLine || i != lines.length - 1) {
                        consumer.accept(currentComponent);
                        currentComponent = null;
                    }
                }
            } else if (endsWithNewLine) {
                if (currentComponent == null) {
                    consumer.accept(Component.empty());
                } else {
                    consumer.accept(currentComponent);
                }

                currentComponent = null;
            }
        } else {
            currentComponent = this.setOrAppend(currentComponent, MutableComponent.create(component.getContents()).withStyle(component.getStyle()));
        }

        for (net.minecraft.network.chat.Component sibling : component.getSiblings()) {
            currentComponent = this.splitAndCollectNewlines(sibling, currentComponent, consumer);
        }

        return currentComponent;
    }

    private @NotNull MutableComponent setOrAppend(
            @Nullable MutableComponent currentComponent,
            @NotNull MutableComponent toAppend
    ) {
        if (currentComponent == null) {
            return toAppend;
        }

        return currentComponent.append(toAppend);
    }

    private @NotNull String removeDefaultItalicAndSerialize(@NotNull Component component) {
        return Component.Serializer.toJson(
                Component.empty()
                        .withStyle(style -> style.withItalic(false))
                        .append(component)
        );
    }

    @Override
    public @NotNull DefaultItemFactoryImpl defaultItemFactory() {
        return this.defaultItemFactory;
    }

    public @NotNull Map<UUID, Deque<InventoryAttachment>> lastInventories() {
        return this.lastInventories;
    }

    public @NotNull Item<PaginationContext, ?> previousItem() {
        if (this.previousItem == null) {
            this.previousItem = this.createItem(new PreviousItem(this));
        }

        return this.previousItem;
    }

    public @NotNull Item<PaginationContext, ?> nextItem() {
        if (this.nextItem == null) {
            this.nextItem = this.createItem(new NextItem(this));
        }

        return this.nextItem;
    }

    public @NotNull Function<UUID, InventoryPlayerHolder> playerMapper() {
        return this.playerMapper;
    }
}
