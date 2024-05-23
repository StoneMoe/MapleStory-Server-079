package client.commands.gm;

import client.MapleClient;
import client.commands.Command;
import client.commands.CommandAbstract;
import client.commands.Tools;
import client.commands.models.CommandResult;
import constants.ServerConstants;
import lombok.extern.slf4j.Slf4j;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;

@Slf4j
@Command(command = "giveItem", commandType = ServerConstants.CommandType.GMCommand)
public class GiveItemCommand extends CommandAbstract {
    @Override
    public CommandResult execute(MapleClient mapleClient, String[] args) {
        var result = createResult();
        if (args.length < 3) {
            result.setMessage("No enough arguments.");
            result.setSuccess(false);
            return result;
        }

        var character = Tools.getCharacter(args[0]);
        if (character == null) {
            result.setMessage(String.format("Character %s not found.", args[0]));
            result.setSuccess(false);
            return result;
        }

        try {
            var itemId = Integer.parseInt(args[1]);
            var quantity = Short.parseShort(args[2]);
            MapleInventoryManipulator.addById(character.getClient(), itemId, quantity, (byte) 0);
            result.setMessage(String.format("Give %d of %s to %s.", quantity, MapleItemInformationProvider.getInstance().getName(itemId), character.getName()));
            result.setSuccess(true);
        } catch (Exception e) {
            result.setMessage(String.format("Unknown error occurred. %s", e.getMessage()));
            result.setSuccess(false);
            log.error("Unknown error occurred.", e);
        }

        return result;
    }

    @Override
    public String showHelp() {
        return "Give item to player. Usage: giveItem {PlayerName} {ItemId} {ItemQuantity}";
    }
}
