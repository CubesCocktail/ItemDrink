package com.github.zamponimarco.itemdrink.manager;

import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.libs.model.ModelManager;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.github.zamponimarco.itemdrink.item.AbstractItem;
import com.github.zamponimarco.itemdrink.item.Item;
import com.github.zamponimarco.itemdrink.item.ItemFolder;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ItemManager extends ModelManager<AbstractItem> {

    private final static NamespacedKey key = new NamespacedKey(CubesCocktail.getInstance(), "item-id");

    private List<AbstractItem> items;

    public ItemManager(Class<AbstractItem> classObject, String databaseType, JavaPlugin plugin) {
        super(classObject, databaseType, plugin, ImmutableMap.of("name", "item",
                "addon", ItemDrink.getInstance()));
        items = database.loadObjects();
    }

    public static NamespacedKey getKey() {
        return key;
    }

    public boolean isSupremeItem(ItemStack i) {
        return i != null && i.getItemMeta() != null && i.getItemMeta().getPersistentDataContainer().has(key,
                PersistentDataType.STRING);
    }

    public Item getItemById(UUID id) {
        return items.stream().map(abstractItem -> abstractItem.getById(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public Item getItemByName(String name) {
        return items.stream().map(abstractItem -> abstractItem.getByName(name)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public Item getItemByItemStack(ItemStack item) {
        if (isSupremeItem(item)) {
            String s = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (s != null)
                return getItemById(UUID.fromString(s));
        }
        return null;
    }

    public ItemFolder getFolderByName(String name) {
        return (ItemFolder) items.stream().filter(abstractItem -> abstractItem instanceof ItemFolder &&
                abstractItem.getName().equals(name)).findFirst().orElse(null);
    }

    public AbstractItem getAbstractItemByName(String name) {
        Item item = getItemByName(name);
        if (item != null) {
            return item;
        }
        return getFolderByName(name);
    }

    public void addItem(AbstractItem item) {
        items.add(item);
        saveModel(item);
    }
}
