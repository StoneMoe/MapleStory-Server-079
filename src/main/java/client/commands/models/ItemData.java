package client.commands.models;

import lombok.Data;

@Data
public class ItemData {
    private Integer id;
    private String name;
    private String type;
    private String description;
}
