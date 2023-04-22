package com.github.zamponimarco.itemdrink.item;

import com.github.zamponimarco.cubescocktail.function.Function;
import com.github.zamponimarco.cubescocktail.libs.model.NamedModel;
import com.github.zamponimarco.cubescocktail.libs.util.ItemUtils;
import com.github.zamponimarco.cubescocktail.libs.util.MessageUtils;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractItem extends NamedModel {

    public AbstractItem(String name) {
        super(name);
    }

    public AbstractItem(Map<String, Object> map) {
        super(map);
    }

    protected ItemStack getCorruptedItem() {
        return ItemUtils.getNamedItem(new ItemStack(Material.BARRIER), MessageUtils.color("&4&lCorrupted"), Lists.
                newArrayList(MessageUtils.color("&cThe display item is corrupted"),
                        MessageUtils.color("&cTry to set it again.")));
    }

    abstract public void changeSkillName(String oldName, String newName);

    abstract public Set<Function> getUsedExecutableSkills();

    abstract public Item getByName(String name);

    abstract public Item getById(UUID uuid);

    abstract public int getSize();

    @Override
    protected boolean isAlreadyPresent(String name) {
        return ItemDrink.getInstance().getItemManager().getAbstractItemByName(name) != null;
    }
}
