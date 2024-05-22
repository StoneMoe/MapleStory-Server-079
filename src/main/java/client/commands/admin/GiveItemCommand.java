package client.commands.admin;

import client.MapleClient;
import client.commands.CommandResult;
import client.commands.ICommand;
import client.commands.Tools;
import constants.ServerConstants;
import lombok.extern.slf4j.Slf4j;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;

@Slf4j
public class GiveItemCommand implements ICommand {
    @Override
    public ServerConstants.CommandType getCommandType() {
        return ServerConstants.CommandType.AdminCommand;
    }

    @Override
    public String getCommand() {
        return "give";
    }

    @Override
    public CommandResult execute(MapleClient mapleClient, String[] args) {
        var result = new CommandResult();
        result.setType(ServerConstants.CommandType.AdminCommand);
        result.setCommand(getCommand());

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
            result.setMessage(String.format("Add %d of %s to %s.", quantity, MapleItemInformationProvider.getInstance().getName(itemId), character.getName()));
            result.setSuccess(true);
        }
        catch (Exception e) {
            result.setMessage(String.format("Unknown error occurred. %s", e.getMessage()));
            result.setSuccess(false);
            log.error("Unknown error occurred.", e);
        }

        return result;
    }
}
