package com.github.zamponimarco.itemdrink.command;

import com.github.zamponimarco.cubescocktail.libs.command.AbstractCommand;
import com.github.zamponimarco.cubescocktail.libs.core.Libs;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.github.zamponimarco.itemdrink.item.Item;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class ItemGetCommand extends AbstractCommand {

    @Override
    protected void execute(String[] arguments, CommandSender sender) {
        if (arguments.length < 1) {
            return;
        }

        Item item = ItemDrink.getInstance().getItemManager().getItemByName(arguments[0]);
        if (item == null) {
            sender.sendMessage(Libs.getLocale().get("messages.command.item-not-found"));
            return;
        }
        ((Player) sender).getInventory().addItem(item.getUsableItem());
        sender.sendMessage(Libs.getLocale().get("messages.command.item-received"));
    }

    @Override
    protected boolean isOnlyPlayer() {
        return true;
    }

    @Override
    protected Permission getPermission() {
        return new Permission("cubescocktail.item.get");
    }
}
