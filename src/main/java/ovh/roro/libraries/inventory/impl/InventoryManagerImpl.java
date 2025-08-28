package ovh.roro.libraries.inventory.impl;

import com.google.common.base.Preconditions;
import io.papermc.paper.adventure.PaperAdventure;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
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
import ovh.roro.libraries.inventory.util.StringUtil;
import ovh.roro.libraries.language.api.LanguageManager;
import ovh.roro.libraries.language.api.Translation;
import ovh.roro.libraries.language.util.LibraryInstanceLoader;

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

    public static final @NotNull LibraryInstanceLoader<InventoryManagerImpl> LOADER = new LibraryInstanceLoader<>(
            "InventoryManager",
            InventoryManagerImpl::new
    );

    private static final @NotNull Int2ObjectMap<MenuType<?>> ROWS_TO_MENU_TYPE = Util.make(new Int2ObjectArrayMap<>(), map -> {
        map.defaultReturnValue(null);

        map.put(1, MenuType.GENERIC_9x1);
        map.put(2, MenuType.GENERIC_9x2);
        map.put(3, MenuType.GENERIC_9x3);
        map.put(4, MenuType.GENERIC_9x4);
        map.put(5, MenuType.GENERIC_9x5);
        map.put(6, MenuType.GENERIC_9x6);
    });

    private final @NotNull CraftServer server;
    private final @NotNull JavaPlugin plugin;
    private final @NotNull LanguageManager languageManager;

    private final @NotNull AtomicInteger itemIdCounter;
    private final @NotNull Int2ObjectMap<Item> itemById;
    private final @NotNull Map<UUID, Deque<InventoryAttachment>> lastInventories;

    private final @NotNull DefaultItemFactoryImpl defaultItemFactory;

    private @MonotonicNonNull Item<PaginationContext, ?> previousItem;
    private @MonotonicNonNull Item<PaginationContext, ?> nextItem;

    private boolean registered;
    private @MonotonicNonNull Function<UUID, InventoryPlayerHolder> playerMapper;

    private InventoryManagerImpl(@NotNull JavaPlugin plugin) {
        this.server = (CraftServer) plugin.getServer();
        this.plugin = plugin;
        this.languageManager = LanguageManager.languageManager();

        this.itemIdCounter = new AtomicInteger(0);
        this.itemById = new Int2ObjectArrayMap<>();
        this.lastInventories = new HashMap<>();

        this.defaultItemFactory = new DefaultItemFactoryImpl(this);
    }

    @Override
    public void register(@NotNull Function<UUID, InventoryPlayerHolder> playerMapper) {
        Preconditions.checkArgument(!this.registered, this.plugin.getName() + "'s InventoryManager already registered");

        this.registered = true;
        this.playerMapper = Objects.requireNonNull(playerMapper);

        this.server.getPluginManager().registerEvents(new ItemDropListener(this), this.plugin);
        this.server.getPluginManager().registerEvents(new ItemInteractListener(this), this.plugin);
        this.server.getPluginManager().registerEvents(new ItemInventoryListener(this), this.plugin);
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

        InventoryWrapper<T, U, V> wrapper = new InventoryWrapper<>(this, player, inventoryImpl, value);
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

        net.kyori.adventure.text.Component title = this.languageManager.translate(player.language(), inventoryImpl.title(player, value));

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
    public boolean isRegisteredInventory(org.bukkit.inventory.@NotNull Inventory inventory) {
        return ((CraftInventory) inventory).getInventory() instanceof InventoryWrapper<?, ?, ?>;
    }

    @Override
    public <T extends InventoryPlayerHolder> @NotNull List<T> getInventoryViewers(@NotNull Inventory<?, ?, T> inventory) {
        List<T> players = new ArrayList<>();

        for (CraftPlayer player : this.server.getOnlinePlayers()) {
            if (player.getHandle().containerMenu instanceof ChestMenu chestMenu &&
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

    @SuppressWarnings("DataFlowIssue")
    @Override
    public @NotNull Optional<@NotNull Item> parseItem(@Nullable net.minecraft.world.item.ItemStack itemStack) {
        if (itemStack == null || !itemStack.has(DataComponents.CUSTOM_DATA)) {
            return Optional.empty();
        }

        CompoundTag tag = itemStack.get(DataComponents.CUSTOM_DATA).copyTag();

        return tag.getInt("inventory_api_item")
                .map(id -> this.itemById.get(id.intValue()));
    }

    @Override
    public @NotNull <T, U extends InventoryPlayerHolder> ClassicInventory<T, U> createInventory(@NotNull ClassicInventoryInstance<T, U> inventoryInstance) {
        return new ClassicInventoryImpl<>(this, inventoryInstance);
    }

    @Override
    public @NotNull <T, U, V extends InventoryPlayerHolder> PageableInventory<T, U, V> createPageableInventory(@NotNull PageableInventoryInstance<T, U, V> inventoryInstance) {
        return new PageableInventoryImpl<>(this, inventoryInstance);
    }

    @Override
    public @NotNull <T, U extends InventoryPlayerHolder> ConfirmationInventory<T, U> createConfirmationInventory(@NotNull ConfirmationInventoryInstance<T, U> inventoryInstance) {
        return new ConfirmationInventoryImpl<>(this, inventoryInstance);
    }

    @Override
    public <T, U extends InventoryPlayerHolder> @NotNull Item<T, U> createItem(@NotNull ItemInstance<T, U> itemInstance) {
        ItemImpl<T, U> item = new ItemImpl<>(this, itemInstance, this.itemIdCounter.incrementAndGet());

        this.itemById.put(item.id(), item);

        return item;
    }

    @Override
    public @NotNull StaticItem createStaticItem(@NotNull StaticItemInstance itemInstance) {
        StaticItemImpl item = new StaticItemImpl(this, itemInstance, this.itemIdCounter.incrementAndGet());

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
        return new ItemBuilderImpl(itemStack);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public @NotNull <T, U extends InventoryPlayerHolder> net.minecraft.world.item.ItemStack toMinecraftStack(@NotNull Item<T, U> item, @NotNull U player, @Nullable T value) {
        return this.toMinecraftStack(player, item.instance().buildItem(player, value), item);
    }

    @Override
    public @NotNull <T, U extends InventoryPlayerHolder> ItemStack toBukkitStack(@NotNull Item<T, U> item, @NotNull U player, @Nullable T value) {
        return CraftItemStack.asCraftMirror(this.toMinecraftStack(item, player, value));
    }

    public @Nullable net.minecraft.world.item.ItemStack toMinecraftStack(@NotNull InventoryPlayerHolder player, @Nullable ItemBuilder builder, @Nullable Item item) {
        if (builder == null || item == null) {
            return null;
        }

        ItemBuilderImpl clonedBuilder = (ItemBuilderImpl) builder.clone();
        net.minecraft.world.item.ItemStack delegate = clonedBuilder.delegate();

        CustomData.update(DataComponents.CUSTOM_DATA, delegate, compoundTag -> {
            compoundTag.putInt("inventory_api_item", ((ItemImpl) item).id());
        });

        Translation name = clonedBuilder.name();
        if (name != null) {
            delegate.set(DataComponents.CUSTOM_NAME, this.removeDefaultItalic(PaperAdventure.asVanilla(this.languageManager.translate(player.language(), name))));
        }

        Translation[] description = clonedBuilder.description();
        if (description != null) {
            List<Component> lore = new ArrayList<>();

            for (Translation translation : description) {
                net.kyori.adventure.text.Component translatedComponent = this.languageManager.translate(player.language(), translation);
                Component vanillaTranslatedComponent = PaperAdventure.asVanilla(translatedComponent);

                Component lastComponent = this.splitAndCollectNewlines(
                        vanillaTranslatedComponent,
                        vanillaTranslatedComponent.getStyle(),
                        null,
                        component -> {
                            lore.add(this.removeDefaultItalic(component));
                        }
                );

                if (lastComponent != null) {
                    lore.add(this.removeDefaultItalic(lastComponent));
                }
            }

            if (!lore.isEmpty()) {
                delegate.set(DataComponents.LORE, new ItemLore(lore));
            }
        }

        return delegate;
    }

    private @Nullable MutableComponent splitAndCollectNewlines(
            @NotNull Component component,
            @NotNull Style componentStyle,
            @Nullable MutableComponent currentComponent,
            @NotNull Consumer<net.minecraft.network.chat.Component> consumer
    ) {
        if (component.getContents() instanceof PlainTextContents contents) {
            String text = contents.text();
            boolean endsWithNewLine = text.endsWith("\n");
            String[] lines = StringUtil.splitNewline(text);

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];

                currentComponent = this.setOrAppend(currentComponent, Component.literal(line).withStyle(componentStyle));

                if (endsWithNewLine || i != lines.length - 1) {
                    consumer.accept(currentComponent);
                    currentComponent = null;
                }
            }
        } else {
            currentComponent = this.setOrAppend(currentComponent, MutableComponent.create(component.getContents()).withStyle(componentStyle));
        }

        for (net.minecraft.network.chat.Component sibling : component.getSiblings()) {
            currentComponent = this.splitAndCollectNewlines(sibling, sibling.getStyle().applyTo(componentStyle), currentComponent, consumer);
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

    private @NotNull Component removeDefaultItalic(@NotNull Component component) {
        return Component.empty()
                .withStyle(style -> style.withItalic(false))
                .append(component);
    }

    @Override
    public @NotNull DefaultItemFactoryImpl defaultItemFactory() {
        return this.defaultItemFactory;
    }

    @NotNull
    Map<UUID, Deque<InventoryAttachment>> lastInventories() {
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
