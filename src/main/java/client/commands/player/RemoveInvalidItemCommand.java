package client.commands.player;

import client.MapleClient;
import client.commands.ICommand;
import client.commands.CommandResult;
import client.commands.Tools;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import constants.ServerConstants;
import lombok.extern.slf4j.Slf4j;
import server.MapleInventoryManipulator;

import java.util.ArrayList;

@Slf4j
public class RemoveInvalidItemCommand implements ICommand {
    public RemoveInvalidItemCommand() {
    }

    @Override
    public ServerConstants.CommandType getCommandType() {
        return ServerConstants.CommandType.PlayerCommand;
    }

    @Override
    public String getCommand() {
        return "removeInvalidItem";
    }

    @Override
    public CommandResult execute(MapleClient mapleClient, String[] args) {
        var result = new CommandResult();
        result.setCommand(this.getCommand());
        result.setType(this.getCommandType());

        if (Tools.getValidItemMap().isEmpty())
        {
            result.setMessage("Failed to load valid items. Please contact GM for help.");
            result.setSuccess(false);
            return result;
        }

        var character = mapleClient.getPlayer();
        if (character == null)
        {
            result.setMessage("Failed to load character. Please contact GM for help.");
            result.setSuccess(false);
            return result;
        }

        var remove = false;
        if (args.length >= 1)
        {
            if (args[0].equals("-remove"))
            {
                remove = true;
            }
        }

        var removedItems = new ArrayList<IItem>();
        for (final var inventory: character.getInventorys())
        {
            for (final var item: inventory.list())
            {
                if (!Tools.getValidItemMap().containsKey(item.getItemId()))
                {
                    removedItems.add(item);
                }
            }
        }

        if (remove)
        {
            for (var item: removedItems)
            {
                MapleInventoryManipulator.removeById(mapleClient, MapleInventoryType.getByType(item.getType()), item.getItemId(), item.getQuantity(), false, false);
            }
            result.setMessage(String.format("Successfully removed %d invalid items.", removedItems.size()));
        }
        else
        {
            result.setMessage(String.format("You have %d invalid items. %s", removedItems.size(), removedItems));
        }

        result.setSuccess(true);
        return result;
    }
}
