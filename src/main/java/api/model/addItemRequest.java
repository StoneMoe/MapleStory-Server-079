package api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class addItemRequest {
    private int characterId;
    private int itemId;
    private short quantity;
}
