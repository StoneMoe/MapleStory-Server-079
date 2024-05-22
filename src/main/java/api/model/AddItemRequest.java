package api.model;

import lombok.Data;

@Data
public class AddItemRequest {
    private int characterId;
    private int itemId;
    private short quantity;
}
