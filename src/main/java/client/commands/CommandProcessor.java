package client.commands;

import client.MapleCharacter;
import client.MapleClient;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

import database.DatabaseConnection;
import io.github.classgraph.ClassGraph;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CommandProcessor {
    private static final HashMap<Character, HashMap<String, ICommand>> commandMap = new HashMap<>();

    public static int Initialize()
    {
        Tools.LoadValidItems();
        var commandCount = 0;
        try
        {
            var scanResult = new ClassGraph().enableAllInfo().scan();
            for (final var classInfo: scanResult.getClassesImplementing(ICommand.class))
            {
                var cls = classInfo.loadClass(ICommand.class);
                var instance = cls.getDeclaredConstructor().newInstance();
                var prefix = instance.getCommandType().getPrefix();
                var command = instance.getCommand();

                if (!commandMap.containsKey(prefix)) {
                    commandMap.put(prefix, new HashMap<>());
                }

                commandMap.get(prefix).put(command, instance);
                commandCount++;
            }
        }
        catch (Exception ex) {
            log.error("Initialize command failed.", ex);
        }

        return commandCount;
    }

    /*
    Try to process a command. If it's a command, then return true. If not a command, return false.
     */
    public static boolean processCommand(final MapleClient mapleClient, final String commandLine)
    {
        var prefix = commandLine.charAt(0);
        if (commandMap.containsKey(prefix)) {
            String[] commandArray = commandLine.substring(1).split("\\s");
            var command = commandMap.get(prefix).get(commandArray[0]);

            var character = mapleClient.getPlayer();
            if (character == null) {
                return true;
            }

            CommandResult result;
            if (command == null)
            {
                log.error("Unable to find command: {}", prefix);
                result = new CommandResult();
                result.setMessage("Available commands: " + String.join(", ", commandMap.get(prefix).keySet()));
                result.setCommand(null);
                result.setSuccess(false);
                result.setType(null);
            }
            else {
                result = command.execute(mapleClient, Arrays.copyOfRange(commandArray, 1, commandArray.length));
            }

            if (result.isSuccess()) {
                character.dropMessage(6, result.getMessage());
                logToDatabase(character, commandLine);
            }
            else {
                character.dropMessage(-2, String.format("Error: %s", result.getMessage()));
            }
            return true;
        }

        return false;
    }

    private static void logToDatabase(final MapleCharacter character, final String command) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO gmlog (cid, name, command, mapid, ip) VALUES (?, ?, ?, ?, ?)")) {
            ps.setInt(1, character.getId());
            ps.setString(2, character.getName());
            ps.setString(3, command);
            ps.setInt(4, character.getMap().getId());
            ps.setString(5, character.getClient().getSessionIPAddress());
            ps.executeUpdate();
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
