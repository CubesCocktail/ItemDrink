package com.github.zamponimarco.itemdrink.skill;

import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.action.group.ActionGroup;
import com.github.zamponimarco.cubescocktail.annotation.PossibleSources;
import com.github.zamponimarco.cubescocktail.annotation.PossibleTargets;
import com.github.zamponimarco.cubescocktail.cooldown.CooldownOptions;
import com.github.zamponimarco.cubescocktail.cooldown.Cooldownable;
import com.github.zamponimarco.cubescocktail.libs.annotation.Enumerable;
import com.github.zamponimarco.cubescocktail.libs.annotation.Serializable;
import com.github.zamponimarco.cubescocktail.libs.model.ModelPath;
import com.github.zamponimarco.cubescocktail.slot.Slot;
import com.github.zamponimarco.cubescocktail.source.Source;
import com.github.zamponimarco.cubescocktail.trgt.ItemTarget;
import com.github.zamponimarco.cubescocktail.trgt.Target;
import com.github.zamponimarco.cubescocktail.trigger.LeftClickTrigger;
import com.github.zamponimarco.cubescocktail.trigger.Trigger;
import com.github.zamponimarco.cubescocktail.trigger.TriggerListener;
import com.github.zamponimarco.cubescocktail.util.Utils;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.github.zamponimarco.itemdrink.item.Item;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Enumerable.Child
@PossibleTargets("getPossibleTargets")
@PossibleSources("getPossibleSources")
@Enumerable.Displayable(name = "&6&lTriggered Skill", description = "gui.item.skill.triggered.description", headTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1Mjg2ZTNlNmZhMDBlNGE2MGJiODk2NzViOWFhNzVkNmM5Y2RkMWVjODQwZDFiY2MyOTZiNzFjOTJmOWU0MyJ9fX0")
public class TriggeredSkill extends Skill implements TriggerListener, Cooldownable {

    private static final boolean CONSUMABLE_DEFAULT = false;

    private static final String CONSUMABLE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTg0YTY4ZmQ3YjYyOGQzMDk2NjdkYjdhNTU4NTViNTRhYmMyM2YzNTk1YmJlNDMyMTYyMTFiZTVmZTU3MDE0In19fQ==";
    private static final String TRIGGER_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1Mjg2ZTNlNmZhMDBlNGE2MGJiODk2NzViOWFhNzVkNmM5Y2RkMWVjODQwZDFiY2MyOTZiNzFjOTJmOWU0MyJ9fX0=";
    private static final String COOLDOWN_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmZlOGNmZjc1ZjdkNDMzMjYwYWYxZWNiMmY3NzNiNGJjMzgxZDk1MWRlNGUyZWI2NjE0MjM3NzlhNTkwZTcyYiJ9fX0=";

    @Serializable(headTexture = CONSUMABLE_HEAD, description = "gui.item.skill.triggered.consumable")
    @Serializable.Optional(defaultValue = "CONSUMABLE_DEFAULT")
    protected boolean consumable;

    @Serializable(headTexture = COOLDOWN_HEAD, description = "gui.item.skill.triggered.cooldownOptions")
    @Serializable.Number(minValue = 1, scale = 1)
    protected CooldownOptions cooldownOptions;

    @Serializable(headTexture = TRIGGER_HEAD, description = "gui.item.skill.triggered.trigger")
    protected Trigger trigger;

    public TriggeredSkill(ModelPath<Item> path) {
        super(path);
        this.consumable = CONSUMABLE_DEFAULT;
        this.cooldownOptions = new CooldownOptions();
        this.trigger = new LeftClickTrigger();
        trigger.registerListener(this);
        registerKeyed();
    }

    public TriggeredSkill(UUID itemId, UUID id, boolean consumable, List<Slot> allowedSlots, List<ActionGroup> groups,
                          CooldownOptions cooldownOptions, Trigger trigger) {
        super(itemId, id, allowedSlots, groups);
        this.consumable = consumable;
        this.cooldownOptions = cooldownOptions;
        this.trigger = trigger;
        trigger.registerListener(this);
        registerKeyed();
    }

    public TriggeredSkill(Map<String, Object> map) {
        super(map);
        this.consumable = (boolean) map.getOrDefault("consumable", CONSUMABLE_DEFAULT);
        this.cooldownOptions = (CooldownOptions) map.getOrDefault("cooldownOptions", new CooldownOptions());
        this.trigger = (Trigger) map.getOrDefault("trigger", new LeftClickTrigger());
        trigger.registerListener(this);
        registerKeyed();
    }

    public Collection<Class<? extends Target>> getPossibleTargets() {
        Set<Class<? extends Target>> targets = new HashSet<>(trigger.getPossibleTargets());
        targets.add(ItemTarget.class);
        return targets;
    }

    public Collection<Class<? extends Source>> getPossibleSources() {
        return new HashSet<>(trigger.getPossibleSources());
    }

    @Override
    public void onTrigger(Map<String, Object> map) {
        LivingEntity caster = (LivingEntity) map.get("caster");
        ItemStack itemStack = (ItemStack) map.get("item");

        if (itemStack != null) {
            Item item = ItemDrink.getInstance().getItemManager().getItemByItemStack(itemStack);
            if (item != null && item.getSkills().contains(this)) {
                executeTriggers(map, itemStack, caster);
            }
        } else {
            List<ItemStack> items = Utils.getEntityItems(caster);
            IntStream.range(0, items.size()).filter(i -> Objects.nonNull(items.get(i))).forEach(i -> {
                ItemStack equipItem = items.get(i);
                Item item = ItemDrink.getInstance().getItemManager().getItemByItemStack(equipItem);
                if (item != null && item.getId().equals(itemId) && getAllowedSlots().contains(Slot.slots.get(i))) {
                    map.put("item", equipItem);
                    executeTriggers(map, equipItem, caster);
                    map.remove("item");
                }
            });
        }
    }

    private void executeTriggers(Map<String, Object> map, ItemStack itemStack, LivingEntity caster) {
        if (cooldownOptions.getCooldown() > 0) {
            if (CubesCocktail.getInstance().getCooldownManager().getCooldown(caster, getKey()) > 0) {
                if (caster instanceof Player) {
                    cooldownOptions.getBar().switchCooldownContext((Player) caster, getKey(),
                            cooldownOptions.getCooldown());
                }
                return;
            } else {
                CubesCocktail.getInstance().getCooldownManager().addCooldown(caster, getKey(), cooldownOptions.getCooldown(),
                        getCooldownOptions().getBar());
            }
        }
        executeActions(map);
        consumeIfConsumable(itemStack);
    }

    private void consumeIfConsumable(ItemStack item) {
        if (consumable) {
            int amount = item.getAmount();
            item.setAmount(--amount);
        }
    }

    @Override
    public ItemStack getGUIItem() {
        return trigger.getGUIItem();
    }

    @Override
    public void onRemoval() {
        trigger.unregisterListener(this);
        unregisterKeyed();
    }

    @Override
    public Skill clone() {
        return new TriggeredSkill(itemId, id, consumable, allowedSlots.stream().map(Slot::clone).collect(Collectors.toList()),
                groups.stream().map(ActionGroup::clone).collect(Collectors.toList()), cooldownOptions.clone(), trigger.clone());
    }

    @Override
    public String getName() {
        return trigger.getName();
    }
}
