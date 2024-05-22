package api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;

import api.model.ItemData;
import client.MapleCharacter;
import handling.channel.ChannelServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Tools {
    private static Map<Integer, ItemData> itemMap = new HashMap<>();

    public static Map<Integer, ItemData> getItemMap() {
        if (itemMap.isEmpty())
        {
            try
            {
                var jsonStream = Tools.class.getClassLoader().getResourceAsStream("items.json");
                var mapper = new JsonMapper();
                var items = mapper.readValue(jsonStream, new TypeReference<ArrayList<ItemData>>(){});
                for (var item: items)
                {
                    itemMap.put(item.getId(), item);
                }
            }
            catch (Exception ex)
            {
                log.error("Failed to load items.json", ex);
            }
        }

        return itemMap;
    }

    /**
     * 将查询字符串转换为键值对映射
     * @param query 查询字符串
     * @return 映射表
     */
    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            }
        }
        return result;
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
}
