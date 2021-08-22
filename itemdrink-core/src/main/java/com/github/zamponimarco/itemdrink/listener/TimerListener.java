package com.github.zamponimarco.itemdrink.listener;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.slot.EquipmentSlot;
import com.github.zamponimarco.cubescocktail.slot.Slot;
import com.github.zamponimarco.cubescocktail.util.Utils;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.github.zamponimarco.itemdrink.item.Item;
import com.github.zamponimarco.itemdrink.skill.TimedSkill;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

public class TimerListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        CubesCocktail.getInstance().getTimerManager().removeAllTimers(e.getPlayer());
    }

    public void onPlayerDeath(PlayerDeathEvent e) {
        if (!e.getKeepInventory())
            CubesCocktail.getInstance().getTimerManager().removeAllTimers(e.getEntity());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        List<ItemStack> items = Utils.getEntityItems(p);
        IntStream.range(0, items.size()).forEach(i -> addTimers(p, items.get(i), Slot.slots.get(i)));
    }

    @EventHandler
    public void onPlayerChangeHeldItem(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        removeTimers(p, p.getInventory().getItem(e.getPreviousSlot()));
        addTimers(p, p.getInventory().getItem(e.getNewSlot()), new EquipmentSlot(org.bukkit.inventory.EquipmentSlot.HAND));
    }

    @EventHandler
    public void onPlayerSwapItem(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        removeTimers(p, e.getMainHandItem());
        removeTimers(p, e.getOffHandItem());
        addTimers(p, e.getOffHandItem(), new EquipmentSlot(org.bukkit.inventory.EquipmentSlot.OFF_HAND));
        addTimers(p, e.getOffHandItem(), new EquipmentSlot(org.bukkit.inventory.EquipmentSlot.HAND));
    }

    @EventHandler
    public void onPlayerMoveItem(InventoryClickEvent e) {
        HumanEntity entity = e.getWhoClicked();

        if (entity.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        Inventory inventory = e.getView().getBottomInventory();
        int numericSlot = e.getSlot();
        Slot slot = Slot.getSlotFromInventory(inventory, numericSlot);
        if (slot != null) {
            switch (e.getAction()) {
                case PLACE_ONE:
                    if (e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.AIR))
                        addTimers(entity, e.getCursor(), slot);
                    break;
                case PLACE_ALL:
                    addTimers(entity, e.getCursor(), slot);
                    break;
                case SWAP_WITH_CURSOR:
                    addTimers(entity, e.getCursor(), slot);
                    removeTimers(entity, e.getCurrentItem());
                    break;
                case PICKUP_ALL:
                case DROP_ALL_SLOT:
                case MOVE_TO_OTHER_INVENTORY:
                    removeTimers(entity, e.getCurrentItem());
                    break;
                case HOTBAR_SWAP:
                case HOTBAR_MOVE_AND_READD:
                    removeTimers(entity, e.getCurrentItem());
                    Slot newSlot = Slot.getSlotFromInventory(inventory, e.getHotbarButton());
                    if (Objects.nonNull(newSlot))
                        addTimers(entity, e.getInventory().getItem(e.getHotbarButton()), newSlot);
                    break;
                case DROP_ONE_SLOT:
                case PICKUP_HALF:
                    if (e.getCurrentItem() != null && e.getCurrentItem().getAmount() == 1) {
                        removeTimers(entity, e.getCurrentItem());
                    }
                    break;
                case COLLECT_TO_CURSOR:
                    if (ItemDrink.getInstance().getItemManager().isSupremeItem(e.getCurrentItem()))
                        e.setCancelled(true);
                    break;
            }
        }
    }

    @EventHandler
    public void onPlayerDrag(InventoryDragEvent e) {
        HumanEntity entity = e.getWhoClicked();

        if (entity.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        Inventory inventory = e.getView().getBottomInventory();
        ItemStack itemStack = e.getOldCursor();
        Item item = ItemDrink.getInstance().getItemManager().getItemByItemStack(itemStack);
        Set<Integer> slots = e.getInventorySlots();
        if (Objects.nonNull(item)) {
            slots.stream().map(slot -> Slot.getSlotFromInventory(inventory, slot)).filter(Objects::nonNull).forEach(slot ->
                    addTimers(entity, itemStack, slot));
        }
    }

    @EventHandler
    public void onPlayerChangeArmor(PlayerArmorChangeEvent e) {
        Player player = e.getPlayer();
        ItemStack oldItemStack = e.getOldItem();
        ItemStack newItemStack = e.getNewItem();
        removeTimers(player, oldItemStack);
        addTimers(player, newItemStack, new EquipmentSlot(org.bukkit.inventory.EquipmentSlot.valueOf(e.getSlotType().name())));
    }

    public void removeTimers(LivingEntity e, ItemStack itemStack) {
        Item item = ItemDrink.getInstance().getItemManager().getItemByItemStack(itemStack);

        if (Objects.nonNull(item)) {
            item.getSkills().stream().filter(skill -> skill instanceof TimedSkill).map(skill -> (TimedSkill) skill).
                    forEach(skill -> CubesCocktail.getInstance().getTimerManager().removeTimers(e, skill.getKey()));
        }
    }

    public void addTimers(LivingEntity e, ItemStack itemStack, Slot slot) {
        Item item = ItemDrink.getInstance().getItemManager().getItemByItemStack(itemStack);

        if (Objects.nonNull(item)) {
            item.getSkills().stream().filter(skill -> skill instanceof TimedSkill && skill.getAllowedSlots().contains(slot)).
                    map(skill -> (TimedSkill) skill).forEach(skill -> CubesCocktail.getInstance().getTimerManager().addNewTimers(e, skill));
        }
    }

}
