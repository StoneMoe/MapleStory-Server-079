package client.commands;

import client.MapleCharacter;
import client.commands.model.ItemData;
import client.commands.player.RemoveInvalidItemCommand;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import handling.channel.ChannelServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Tools {
    @Getter
    private static final Map<Integer, ItemData> validItemMap = new HashMap<>();

    public static void LoadValidItems() {
        try
        {
            var jsonStream = RemoveInvalidItemCommand.class.getClassLoader().getResourceAsStream("validItems.json");
            var mapper = new JsonMapper();
            var items = mapper.readValue(jsonStream, new TypeReference<ArrayList<ItemData>>(){});
            for (var item: items)
            {
                validItemMap.put(item.getId(), item);
            }
            log.info("Loaded {} valid items", items.size());
        }
        catch (Exception ex)
        {
            log.error("Failed to load validItems.json", ex);
        }
    }

    public static MapleCharacter getCharacter(Integer characterId)
    {
        MapleCharacter character = null;
        for (final var channel: ChannelServer.getAllInstances())
        {
            character = channel.getPlayerStorage().getCharacterById(characterId);
            if (character != null)
            {
                break;
            }
        }

        return character;
    }

    public static MapleCharacter getCharacter(String characterName)
    {
        MapleCharacter character = null;
        for (final var channel: ChannelServer.getAllInstances())
        {
            character = channel.getPlayerStorage().getCharacterByName(characterName);
            if (character != null)
            {
                break;
            }
        }

        return character;
    }
}
