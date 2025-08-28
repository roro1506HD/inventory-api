package ovh.roro.libraries.inventory.impl.item.defaults;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ovh.roro.libraries.inventory.api.InventoryManager;
import ovh.roro.libraries.inventory.api.item.Item;
import ovh.roro.libraries.inventory.api.item.StaticItem;
import ovh.roro.libraries.inventory.api.item.defaults.DefaultItemFactory;
import ovh.roro.libraries.inventory.impl.InventoryManagerImpl;

@ApiStatus.Internal
public class DefaultItemFactoryImpl implements DefaultItemFactory {

    private final @NotNull InventoryManager inventoryManager;

    private final @NotNull LoadingCache<Key, StaticItem> separatorsCache;
    private final @NotNull Item<Object, ?> backItem;

    public DefaultItemFactoryImpl(@NotNull InventoryManagerImpl inventoryManager) {
        this.inventoryManager = inventoryManager;

        this.separatorsCache = this.createSeparatorsCache();
        this.backItem = inventoryManager.createItem(new BackItem(inventoryManager));
    }

    private @NotNull LoadingCache<Key, StaticItem> createSeparatorsCache() {
        return CacheBuilder.newBuilder()
                .build(new CacheLoader<>() {
                    @Override
                    public StaticItem load(Key key) throws Exception {
                        return DefaultItemFactoryImpl.this.inventoryManager.createStaticItem(
                                () -> DefaultItemFactoryImpl.this.inventoryManager.createItemBuilder(Material.PAPER)
                                        .overrideModel(key)
                                        .hideTooltip(true)
                        );
                    }
                });
    }

    @Override
    public @NotNull StaticItem separator(@NotNull Material material) {
        return this.separator(material.key());
    }

    @Override
    public @NotNull StaticItem separator(@NotNull Key key) {
        return this.separatorsCache.getUnchecked(key);
    }

    @Override
    public @NotNull Item<Object, ?> back() {
        return this.backItem;
    }
}
