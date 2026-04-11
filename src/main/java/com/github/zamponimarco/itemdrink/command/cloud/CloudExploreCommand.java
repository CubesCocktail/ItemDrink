package com.github.zamponimarco.itemdrink.command.cloud;

import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.libs.command.AbstractCommand;
import com.github.zamponimarco.itemdrink.gui.CloudCollectionInventoryHolder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class CloudExploreCommand extends AbstractCloudCommand {
    @Override
    protected void execute(String[] strings, CommandSender commandSender) {
        Player player = (Player) commandSender;
        player.openInventory(new CloudCollectionInventoryHolder(CubesCocktail.getInstance(), null, player, 1).
                getInventory());
    }

    @Override
    protected boolean isOnlyPlayer() {
        return true;
    }

    @Override
    protected Permission getPermission() {
        return new Permission("cubescocktail.item.cloud.explore");
    }
}
