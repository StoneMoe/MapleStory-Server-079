package client.commands.player;

import client.MapleClient;
import client.commands.Command;
import client.commands.CommandAbstract;
import client.commands.models.CommandResult;
import constants.ServerConstants;

@Command(command = "save", commandType = ServerConstants.CommandType.PlayerCommand)
public class SaveCommand extends CommandAbstract {
    @Override
    public CommandResult execute(MapleClient mapleClient, String[] args) {
        var result = createResult();

        mapleClient.getPlayer().saveToDB(false, true);
        result.setMessage("Saved.");
        result.setSuccess(true);
        return result;
    }

    @Override
    public String showHelp() {
        return "Save your data to database.";
    }
}
