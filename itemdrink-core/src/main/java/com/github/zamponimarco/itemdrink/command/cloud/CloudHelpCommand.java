package com.github.zamponimarco.itemdrink.command.cloud;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public class CloudHelpCommand extends AbstractCloudCommand {
    @Override
    protected void execute(String[] strings, CommandSender commandSender) {
        printHelpMessage(commandSender);
    }

    @Override
    protected boolean isOnlyPlayer() {
        return false;
    }

    @Override
    protected Permission getPermission() {
        return new Permission("cubescocktail.item.cloud.help");
    }
}
