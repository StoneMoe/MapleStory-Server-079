package client.commands.gm;

import client.MapleClient;
import client.commands.Command;
import client.commands.CommandAbstract;
import client.commands.models.CommandResult;
import constants.ServerConstants;
import handling.channel.ChannelServer;
import tools.MaplePacketCreator;

@Command(command = "setExpRate", commandType = ServerConstants.CommandType.GMCommand)
public class SetExpRateCommand extends CommandAbstract {

    @Override
    public CommandResult execute(MapleClient mapleClient, String[] args) {
        var result = createResult();
        if (args.length == 0) {
            result.setMessage("Rate missing.");
            result.setSuccess(false);
            return result;
        }

        try {
            var expRate = Integer.parseInt(args[0]);
            for (final var channel : ChannelServer.getAllInstances()) {
                channel.setExpRate(expRate);
                channel.broadcastMessage(MaplePacketCreator.serverNotice(6, String.format("The exp rate set to %d", expRate)));
            }

            result.setMessage("Success.");
            result.setSuccess(true);
        } catch (Exception ex) {
            result.setMessage(String.format("Unknown exception. %s", ex.getMessage()));
            result.setSuccess(false);
        }

        return result;
    }

    @Override
    public String showHelp() {
        return "Usage: !setExpRate 30";
    }
}
