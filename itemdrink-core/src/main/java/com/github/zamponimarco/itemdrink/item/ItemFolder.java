package com.github.zamponimarco.itemdrink.item;

import com.github.zamponimarco.cubescocktail.function.Function;
import com.github.zamponimarco.cubescocktail.libs.annotation.Serializable;
import com.github.zamponimarco.cubescocktail.libs.core.Libs;
import com.github.zamponimarco.cubescocktail.libs.util.ItemUtils;
import com.github.zamponimarco.cubescocktail.libs.util.MessageUtils;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class ItemFolder extends AbstractItem {

    private static final String SKILL_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmJiMTI1NmViOWY2NjdjMDVmYjIxZTAyN2FhMWQ1MzU1OGJkYTc0ZTI0MGU0ZmE5ZTEzN2Q4NTFjNDE2ZmU5OCJ9fX0=";
    private static final String NAME_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmEzZmNlNTAzNmY3YWQ0ZjExMTExY2UzMThmOGYxYWVlODU5ZWY0OWRlMTI5M2YxMTYyY2EyZTJkZWEyODFkYiJ9fX0=";
    private static final String FOLDER_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTYzMzBhNGEyMmZmNTU4NzFmYzhjNjE4ZTQyMWEzNzczM2FjMWRjYWI5YzhlMWE0YmI3M2FlNjQ1YTRhNGUifX19";
    protected static int folderCounter = 1;
    @Serializable(headTexture = SKILL_HEAD)
    private List<AbstractItem> items;

    @Serializable(headTexture = NAME_HEAD)
    private String displayName;

    public ItemFolder() {
        super(nextAvailableName());
        this.items = Lists.newArrayList();
        this.displayName = name;
    }

    public ItemFolder(String name, List<AbstractItem> items, String displayName) {
        super(name);
        this.items = items;
        this.displayName = displayName;
        folderCounter++;
    }

    public ItemFolder(Map<String, Object> map) {
        super(map);
        this.items = (List<AbstractItem>) map.getOrDefault("items", Lists.newArrayList());
        this.displayName = (String) map.getOrDefault("displayName", name);
        folderCounter++;
    }

    protected static String nextAvailableName() {
        String name;
        do {
            name = "folder" + folderCounter;
            folderCounter++;
        } while (ItemDrink.getInstance().getItemManager().getAbstractItemByName(name) != null);
        return name;
    }

    @Override
    public ItemStack getGUIItem() {
        int size = getSize();
        List<Component> lore = Lists.newArrayList(
                MessageUtils.color("&6&lContains: &c" + size + " item" + (size == 1 ? "" : "s")),
                MessageUtils.color("&6&lid: &c" + name)
        );
        lore.addAll(Libs.getLocale().getList("gui.item.folder-description"));
        return ItemUtils.getNamedItem(Libs.getVersionWrapper().skullFromValue(FOLDER_HEAD), MessageUtils.color(displayName), lore);
    }

    @Override
    public void changeSkillName(String oldName, String newName) {
        items.forEach(item -> item.changeSkillName(oldName, newName));
    }

    @Override
    public Set<Function> getUsedExecutableSkills() {
        return items.stream().reduce(Sets.newHashSet(), (list, item) -> {
            list.addAll(item.getUsedExecutableSkills());
            return list;
        }, (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        });
    }

    @Override
    public Item getByName(String name) {
        return items.stream().map(item -> item.getByName(name)).filter(Objects::nonNull).findFirst().
                orElse(null);
    }

    @Override
    public Item getById(UUID uuid) {
        return items.stream().map(item -> item.getById(uuid)).filter(Objects::nonNull).findFirst().
                orElse(null);
    }

    @Override
    public int getSize() {
        return items.stream().reduce(0, (size, item) -> size + item.getSize(), Integer::sum);
    }
}
