package com.github.zamponimarco.itemdrink.item;

import com.github.zamponimarco.cubescocktail.function.Function;
import com.github.zamponimarco.cubescocktail.libs.annotation.Serializable;
import com.github.zamponimarco.cubescocktail.libs.core.Libs;
import com.github.zamponimarco.cubescocktail.libs.model.wrapper.ItemStackWrapper;
import com.github.zamponimarco.cubescocktail.libs.util.ItemUtils;
import com.github.zamponimarco.cubescocktail.libs.util.MessageUtils;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.github.zamponimarco.itemdrink.manager.ItemManager;
import com.github.zamponimarco.itemdrink.skill.Skill;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class Item extends AbstractItem implements Cloneable {

    private static final String SKILL_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmJiMTI1NmViOWY2NjdjMDVmYjIxZTAyN2FhMWQ1MzU1OGJkYTc0ZTI0MGU0ZmE5ZTEzN2Q4NTFjNDE2ZmU5OCJ9fX0=";
    protected static int itemCounter = 1;
    @Serializable(stringValue = true)
    private UUID id;
    @Serializable(description = "gui.item.item", displayItem = "getUsableItem",
            additionalDescription = {"gui.additional-tooltips.item"})
    private ItemStackWrapper item;
    @Serializable(headTexture = SKILL_HEAD, description = "gui.item.skill-set")
    private List<Skill> skills;

    public Item() {
        super(nextAvailableName());
        this.id = UUID.randomUUID();
        this.item = new ItemStackWrapper(true);
        this.skills = Lists.newArrayList();
    }

    public Item(UUID id, String name, ItemStackWrapper item, List<Skill> skills) {
        super(name);
        this.id = id;
        this.item = item;
        this.skills = skills;
        itemCounter++;
    }

    public Item(Map<String, Object> map) {
        super(map);
        this.id = UUID.fromString((String) map.get("id"));
        this.item = (ItemStackWrapper) map.getOrDefault("item", new ItemStackWrapper());
        this.skills = (List<Skill>) map.getOrDefault("skills", Lists.newArrayList());
        itemCounter++;
    }

    protected static String nextAvailableName() {
        String name;
        do {
            name = "item" + itemCounter;
            itemCounter++;
        } while (ItemDrink.getInstance().getItemManager().getAbstractItemByName(name) != null);
        return name;
    }

    public ItemStack getUsableItem() {
        if (item == null) {
            return getCorruptedItem();
        }
        ItemStack item = this.item.getWrapped().clone();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemManager.getKey(), PersistentDataType.STRING, id.toString());
        item.setItemMeta(meta);
        item.setAmount(1);
        return item;
    }

    @Override
    public ItemStack getGUIItem() {
        if (item == null) {
            return getCorruptedItem();
        }
        List<Component> lore = item.getWrapped().getItemMeta() == null ? null : item.getWrapped().getItemMeta().lore();
        lore = lore == null ? Lists.newArrayList() : lore;
        lore.add(MessageUtils.color("&6&lName: &c" + name));
        lore.addAll(Libs.getLocale().getList("gui.item.description"));
        ItemStack itemStack = item.getWrapped().clone();
        itemStack.setAmount(1);
        return ItemUtils.getNamedItem(itemStack, item.getWrapped().getItemMeta().displayName(), lore);
    }

    public void changeSkillName(String oldName, String newName) {
        skills.forEach(skill -> skill.getGroups().forEach(group -> group.getActions().
                forEach(action -> action.changeSkillName(oldName, newName))));
        ItemDrink.getInstance().getItemManager().saveModel(this);
    }

    public Set<Function> getUsedExecutableSkills() {
        /*
        return skillSet.stream().reduce(Sets.newHashSet(), (list, skill) -> {
            list.addAll(skill.getUsedSavedSkills());
            return list;
        }, (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        });
         */
        return Sets.newHashSet();
    }

    public Skill getSkillById(UUID skillId) {
        return skills.stream().filter(skill -> skill.getId().equals(skillId)).findFirst().orElse(null);
    }

    @Override
    public Item getByName(String name) {
        return this.name.equals(name) ? this : null;
    }

    @Override
    public Item getById(UUID uuid) {
        return this.id.equals(uuid) ? this : null;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public Item clone() {
        UUID newItemId = UUID.randomUUID();
        Item newItem = new Item(newItemId, nextAvailableName(), item.clone(), skills.stream().map(Skill::clone).
                collect(Collectors.toList()));
        newItem.skills.forEach(skill -> skill.setItemId(newItemId));
        return newItem;
    }
}
