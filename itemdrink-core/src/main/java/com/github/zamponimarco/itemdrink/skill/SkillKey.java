package com.github.zamponimarco.itemdrink.skill;

import com.github.zamponimarco.cubescocktail.key.Key;
import com.github.zamponimarco.cubescocktail.libs.annotation.Serializable;
import com.github.zamponimarco.cubescocktail.libs.util.ItemUtils;
import com.github.zamponimarco.cubescocktail.libs.util.MessageUtils;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.github.zamponimarco.itemdrink.item.Item;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class SkillKey implements Key {

    @Serializable(stringValue = true)
    private UUID itemId;
    @Serializable(stringValue = true)
    private UUID skillId;

    public SkillKey(Map<String, Object> map) {
        this.itemId = UUID.fromString((String) map.getOrDefault("itemId", null));
        this.skillId = UUID.fromString((String) map.getOrDefault("skillId", null));
    }

    @Override
    public ItemStack getGUIItem() {
        Item item = ItemDrink.getInstance().getItemManager().getItemById(itemId);
        if (item == null) {
            return null;
        }
        Skill skill = item.getSkillById(skillId);
        if (skill == null) {
            return null;
        }
        return ItemUtils.getNamedItem(
                item.getGUIItem(),
                MessageUtils.color("&6&lItem: &c" + item.getName()),
                Lists.newArrayList(
                        MessageUtils.color("&6&lSkill: &c" + skill.getName())
                )
        );
    }

    @Override
    public Key clone() {
        return new SkillKey(itemId, skillId);
    }

    @Override
    public String getName() {
        Item item = ItemDrink.getInstance().getItemManager().getItemById(itemId);
        if (item == null) {
            return "";
        }
        Skill skill = item.getSkillById(skillId);
        if (skill == null) {
            return "";
        }

        return String.format("%s/%s", item.getName(), skill.getName());
    }
}