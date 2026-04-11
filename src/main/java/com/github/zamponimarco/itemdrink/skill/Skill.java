package com.github.zamponimarco.itemdrink.skill;

import com.github.zamponimarco.cubescocktail.action.Action;
import com.github.zamponimarco.cubescocktail.action.args.ActionArgument;
import com.github.zamponimarco.cubescocktail.action.group.ActionGroup;
import com.github.zamponimarco.cubescocktail.key.Key;
import com.github.zamponimarco.cubescocktail.key.Keyed;
import com.github.zamponimarco.cubescocktail.libs.annotation.Enumerable;
import com.github.zamponimarco.cubescocktail.libs.annotation.Serializable;
import com.github.zamponimarco.cubescocktail.libs.model.Model;
import com.github.zamponimarco.cubescocktail.libs.model.ModelPath;
import com.github.zamponimarco.cubescocktail.slot.EquipmentSlot;
import com.github.zamponimarco.cubescocktail.slot.Slot;
import com.github.zamponimarco.cubescocktail.source.CasterSource;
import com.github.zamponimarco.cubescocktail.trgt.ItemTarget;
import com.github.zamponimarco.itemdrink.item.Item;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Enumerable.Parent(classArray = {TriggeredSkill.class, TimedSkill.class})
public abstract class Skill implements Model, Cloneable, Keyed {

    public static final List<Slot> DEFAULT_SLOTS = Arrays.stream(org.bukkit.inventory.EquipmentSlot.values()).
            map(EquipmentSlot::new).collect(Collectors.toList());
    private static final String SLOTS_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGQ5YjY4OTE1YjE0NzJkODllNWUzYTliYTZjOTM1YWFlNjAzZDEyYzE0NTRmMzgyMjgyNWY0M2RmZThhMmNhYyJ9fX0=";
    private static final String GROUPS_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y0ZDVhNGFiYjY0ZGIxMWI0NTcxZTc0N2M1OGU0MDMwMThmNjQ5YzE4MTZjNjUwOWY5YTNmN2E3ODIxYjQ4ZSJ9fX0=";

    @Serializable(stringValue = true)
    protected UUID itemId;

    @Serializable(stringValue = true)
    protected UUID id;

    @Serializable(headTexture = SLOTS_HEAD, description = "gui.item.skill.slots")
    protected List<Slot> allowedSlots;
    @Serializable(headTexture = GROUPS_HEAD, description = "gui.item.skill.groups")
    protected List<ActionGroup> groups;

    public Skill(ModelPath<Item> path) {
        this(path.getRoot().getId(), UUID.randomUUID(), new ArrayList<>(DEFAULT_SLOTS),
                Lists.newArrayList());
    }

    public Skill(UUID itemId, UUID id, List<Slot> allowedSlots, List<ActionGroup> groups) {
        this.itemId = itemId;
        this.id = id;
        this.allowedSlots = allowedSlots;
        this.groups = groups;
    }

    public Skill(Map<String, Object> map) {
        this.itemId = UUID.fromString((String) map.getOrDefault("itemId", UUID.randomUUID().toString()));
        this.id = UUID.fromString((String) map.getOrDefault("id", UUID.randomUUID().toString()));
        this.allowedSlots = (List<Slot>) map.getOrDefault("allowedSlots", Lists.newArrayList());
        this.groups = (List<ActionGroup>) map.getOrDefault("groups", Lists.newArrayList());
        legacyTransition(map);
    }

    private void legacyTransition(Map<String, Object> map) {
        List<Action> onItemActions = (List<Action>) map.get("onItemActions");
        if (onItemActions != null && !onItemActions.isEmpty()) {
            groups.add(new ActionGroup(new CasterSource(), new ItemTarget(), onItemActions));
        }
    }

    public void executeActions(ActionArgument args) {
        groups.forEach(group -> group.executeGroup(args));
    }

    @Override
    public abstract Skill clone();


    @Override
    public Key getKey() {
        return new SkillKey(itemId, id);
    }

    public abstract String getName();
}
