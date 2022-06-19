package com.github.zamponimarco.itemdrink.gui;

import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.database.NamedModel;
import com.github.zamponimarco.cubescocktail.function.AbstractFunction;
import com.github.zamponimarco.cubescocktail.libs.core.Libs;
import com.github.zamponimarco.cubescocktail.libs.gui.PluginInventoryHolder;
import com.github.zamponimarco.cubescocktail.libs.util.ItemUtils;
import com.github.zamponimarco.cubescocktail.libs.util.MessageUtils;
import com.github.zamponimarco.cubescocktail.util.CompressUtils;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.github.zamponimarco.itemdrink.item.AbstractItem;
import com.github.zamponimarco.itemdrink.item.Item;
import com.github.zamponimarco.itemdrink.item.ItemFolder;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CloudCollectionInventoryHolder extends PluginInventoryHolder {

    protected static final String NEXT_PAGE_ITEM = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTdiMDNiNzFkM2Y4NjIyMGVmMTIyZjk4MzFhNzI2ZWIyYjI4MzMxOWM3YjYyZTdkY2QyZDY0ZDk2ODIifX19==";
    protected static final String PREVIOUS_PAGE_ITEM = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDgzNDhhYTc3ZjlmYjJiOTFlZWY2NjJiNWM4MWI1Y2EzMzVkZGVlMWI5MDVmM2E4YjkyMDk1ZDBhMWYxNDEifX19==";
    protected static final int MODELS_NUMBER = 50;

    private final Player player;
    private final AtomicBoolean isLoaded = new AtomicBoolean();
    private int page;
    private Map<AbstractItem, Set<AbstractFunction>> items = new HashMap<>();

    public CloudCollectionInventoryHolder(JavaPlugin plugin, PluginInventoryHolder parent, Player player, int page) {
        super(plugin, parent);
        this.player = player;
        this.page = page;
    }

    @Override
    protected void initializeInventory() {
        this.inventory = Bukkit.createInventory(this, 54,
                MessageUtils.color("&6&lPublic Items"));
        items.clear();
        isLoaded.set(false);
        fetchItems();

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (isLoaded.get()) {
                    placeItems(items);
                    placeCollectionOnlyItems();
                } else {
                    this.cancel();
                }
            }
        };

        runnable.runTaskTimer(CubesCocktail.getInstance(), 0, 1);
        registerClickConsumer(53, getBackItem(), getBackConsumer());
        fillInventoryWith(Material.GRAY_STAINED_GLASS_PANE);
    }

    private void placeItems(Map<AbstractItem, Set<AbstractFunction>> items) {
        AtomicInteger index = new AtomicInteger();
        items.forEach((item, set) -> registerClickConsumer(index.getAndIncrement(), getItemRepresentation(item),
                e -> {
                    set.forEach(skill -> CubesCocktail.getInstance().getFunctionManager().addFunction(skill));
                    ItemDrink.getInstance().getItemManager().addItem(item);
                    player.sendMessage(MessageUtils.color("&aThe item has been succesfully imported."));
                }));
    }

    private ItemStack getItemRepresentation(AbstractItem model) {
        if (model instanceof Item) {
            Item item = (Item) model;
            ItemStack cloned = item.getItem().getWrapped().clone();
            List<Component> lore = cloned.getItemMeta() == null ? null : cloned.getItemMeta().lore();
            lore = lore == null ? Lists.newArrayList() : lore;
            lore.add(MessageUtils.color(""));
            lore.add(MessageUtils.color("&6&lName: &c" + item.getName()));
            cloned.setAmount(1);
            return ItemUtils.getNamedItem(cloned, cloned.getItemMeta().displayName(), lore);
        } else if (model instanceof ItemFolder) {
            ItemFolder item = (ItemFolder) model;
            ItemStack cloned = item.getGUIItem().clone();
            int size = item.getSize();
            return ItemUtils.getNamedItem(cloned, cloned.getItemMeta().displayName(),
                    Lists.newArrayList(
                            MessageUtils.color("&6&lContains &c" + size + " &6&litem" + (size == 1 ? "" : "s")),
                            MessageUtils.color("&6&l- Left click &eto import")
                    ));
        }
        return null;
    }

    private void fetchItems() {
        Bukkit.getScheduler().runTaskAsynchronously(CubesCocktail.getInstance(), () -> {
            URL url;
            try {
                url = new URL("http://188.34.166.204:3000/items?limit=" + MODELS_NUMBER + "&skip=" + (page - 1) *
                        MODELS_NUMBER + "&approved=true");
                URLConnection con = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) con;
                http.setRequestMethod("GET");
                http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                http.setDoInput(true);
                http.connect();
                try (InputStream is = http.getInputStream()) {
                    Reader reader = new InputStreamReader(is);
                    Gson gson = new GsonBuilder().create();
                    final TypeAdapter<JsonArray> jsonObjectTypeAdapter = gson.getAdapter(JsonArray.class);
                    JsonReader jsonReader = gson.newJsonReader(reader);
                    final JsonArray incomingJsonObject = jsonObjectTypeAdapter.read(jsonReader);
                    incomingJsonObject.forEach(elm -> {
                        try {
                            Set<AbstractFunction> set = StreamSupport.stream(elm.getAsJsonObject().
                                    getAsJsonArray("skills").spliterator(), false).map(jsonSkill ->
                                    (AbstractFunction) NamedModel.fromSerializedString(new String(CompressUtils.
                                            decompress(Base64.getDecoder().decode(jsonSkill.getAsJsonObject().get("skill").
                                                    getAsString())), Charset.defaultCharset()))).collect(Collectors.toSet());
                            AbstractItem item = (AbstractItem) NamedModel.fromSerializedString(
                                    new String(CompressUtils.decompress(Base64.getDecoder().decode(elm.getAsJsonObject().
                                            get("item").getAsString())), Charset.defaultCharset()));
                            items.put(item, set);
                        } catch (Exception e) {
                            System.out.println("Newer item");
                        }
                    });
                    jsonReader.close();
                }
                http.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        isLoaded.set(true);
    }

    protected void placeCollectionOnlyItems() {
        if (!items.isEmpty()) {
            this.registerClickConsumer(52, ItemUtils.getNamedItem(Libs.getVersionWrapper().skullFromValue(NEXT_PAGE_ITEM),
                    MessageUtils.color("&6&lNext page"), new ArrayList<>()), (e) -> {
                ++this.page;
                e.getWhoClicked().openInventory(this.getInventory());
            });
        }
        if (this.page != 1) {
            this.registerClickConsumer(51, ItemUtils.getNamedItem(Libs.getVersionWrapper().skullFromValue(PREVIOUS_PAGE_ITEM),
                    MessageUtils.color("&6&lPrevious page"), new ArrayList<>()), (e) -> {
                --this.page;
                e.getWhoClicked().openInventory(this.getInventory());
            });
        }
    }

}
