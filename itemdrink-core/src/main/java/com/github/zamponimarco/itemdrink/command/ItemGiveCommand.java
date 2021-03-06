package com.github.zamponimarco.itemdrink.command;

import com.github.zamponimarco.cubescocktail.libs.command.AbstractCommand;
import com.github.zamponimarco.cubescocktail.libs.core.Libs;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.github.zamponimarco.itemdrink.item.Item;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

public class ItemGiveCommand extends AbstractCommand {

    @Override
    protected void execute(String[] arguments, CommandSender sender) {
        if (arguments.length < 2) {
            return;
        }

        Player p = Bukkit.getPlayer(arguments[0]);
        if (p == null) {
            sender.sendMessage(Libs.getLocale().get("messages.command.player-not-found"));
            return;
        }

        Item item = ItemDrink.getInstance().getItemManager().getItemByName(arguments[1]);
        if (item == null) {
            sender.sendMessage(Libs.getLocale().get("messages.command.item-not-found"));
            return;
        }

        ItemStack toAdd = item.getUsableItem();
        int itemCount = 0;
        int remaining = 1;
        if (arguments.length > 2) {
            try {
                int amount = Integer.parseInt(arguments[2]);
                itemCount = amount >> 6;
                remaining = amount % 64;
            } catch (NumberFormatException ignored) {
            }
        }

        for (int i = 0; i < itemCount; i++) {
            toAdd.setAmount(64);
            p.getInventory().addItem(toAdd);
        }
        toAdd.setAmount(remaining);
        p.getInventory().addItem(toAdd);

        sender.sendMessage(Libs.getLocale().get("messages.command.player-item-received"));
    }

    @Override
    protected boolean isOnlyPlayer() {
        return false;
    }

    @Override
    protected Permission getPermission() {
        return new Permission("cubescocktail.item.give");
    }
}
