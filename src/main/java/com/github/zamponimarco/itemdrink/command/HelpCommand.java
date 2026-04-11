package com.github.zamponimarco.itemdrink.command;

import com.github.zamponimarco.cubescocktail.libs.command.AbstractCommand;
import com.github.zamponimarco.cubescocktail.libs.util.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public class HelpCommand extends AbstractCommand {

    @Override
    protected void execute(String[] arguments, CommandSender sender) {
        sender.sendMessage(MessageUtils.color("        &c&lItem&6&lDrink &cHelp\n" +
                "&2/cc item help &7Show help message.\n" +
                "&2/cc item list &7Show the items GUI.\n" +
                "&2/cc item get [name] &7Get the item with the given name.\n" +
                "&2/cc item give [player] [name] <amount> &7Give the item with the given name to the player\n" +
                "&2/cc item backup &7To create a local backup of your files.\n" +
                "&2/cc item cloud help &7Show cloud help message.\n" +
                "&2For further help: &7https://discord.gg/TzREkc9"));
    }

    @Override
    protected boolean isOnlyPlayer() {
        return false;
    }

    @Override
    protected Permission getPermission() {
        return new Permission("cubescocktail.item.help");
    }
}
