package com.github.zamponimarco.itemdrink.command;

import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.libs.command.AbstractCommand;
import com.github.zamponimarco.cubescocktail.libs.model.ModelPath;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.github.zamponimarco.itemdrink.gui.ItemCollectionInventoryHolder;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class ItemListCommand extends AbstractCommand {

    @SneakyThrows
    @Override
    protected void execute(String[] arguments, CommandSender sender) {
        Player p = (Player) sender;
        p.openInventory(new ItemCollectionInventoryHolder(CubesCocktail.getInstance(), null,
                new ModelPath<>(ItemDrink.getInstance().getItemManager(), null), ItemDrink.getInstance().
                getItemManager().getClass().getDeclaredField("items"), 1, o -> true).getInventory());
    }

    @Override
    protected boolean isOnlyPlayer() {
        return true;
    }

    @Override
    protected Permission getPermission() {
        return new Permission("cubescocktail.item.list");
    }
}
