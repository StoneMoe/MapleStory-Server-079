package client.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.commands.models.CommandResult;
import constants.ServerConstants;
import database.DatabaseConnection;
import io.github.classgraph.ClassGraph;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


@Slf4j
public class CommandProcessor {
    private static final HashMap<ServerConstants.CommandType, HashMap<String, CommandAbstract>> commandMap = new HashMap<>();

    public static int Initialize() {
        Tools.LoadValidItems();
        var commandCount = 0;
        try {
            var scanResult = new ClassGraph().enableAllInfo().scan();
            for (final var classInfo : scanResult.getClassesWithAnnotation(Command.class)) {
                if (classInfo.isAbstract()) {
                    continue;
                }

                var cls = classInfo.loadClass(CommandAbstract.class);
                var instance = cls.getDeclaredConstructor().newInstance();
                var commandType = instance.getCommandType();
                var command = instance.getCommand();

                if (!commandMap.containsKey(commandType)) {
                    commandMap.put(commandType, new HashMap<>());
                }

                commandMap.get(commandType).put(command, instance);
                commandCount++;
            }
        } catch (Exception ex) {
            log.error("Initialize command failed.", ex);
        }

        return commandCount;
    }

    /*
    Try to process a command. If it's a command, then return true. If not a command, return false.
     */
    public static boolean processCommand(final MapleClient mapleClient, final String commandLine) {
        var commandType = ServerConstants.CommandType.getByPrefix(commandLine.charAt(0));
        if (commandType != null && commandMap.containsKey(commandType)) {
            String[] commandArray = commandLine.substring(1).split("\\s");
            var command = commandMap.get(commandType).get(commandArray[0]);

            var character = mapleClient.getPlayer();
            if (character == null) {
                return true;
            }

            CommandResult result;
            if (command == null) {
                var availableCommands = new ArrayList<String>();
                for (var key : commandMap.keySet()) {
                    if (character.getGMLevel() >= key.getGmLevel()) {
                        for (var item : commandMap.get(key).keySet()) {
                            availableCommands.add(String.format("%s%s", key.getPrefix(), item));
                        }
                    }
                }

                character.dropMessage(6, "Available commands: " + String.join(", ", availableCommands));
                return true;
            } else {
                if (!command.preCheck(mapleClient)) {
                    character.dropMessage(6, "You are not able to use this command.");
                    return true;
                }

                result = command.execute(mapleClient, Arrays.copyOfRange(commandArray, 1, commandArray.length));
            }

            if (result.isSuccess()) {
                character.dropMessage(6, result.getMessage());
                logToDatabase(character, commandLine);
            } else {
                character.dropMessage(6, command.showHelp());
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
