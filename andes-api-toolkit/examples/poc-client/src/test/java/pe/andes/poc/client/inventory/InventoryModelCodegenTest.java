package pe.andes.poc.client.inventory;

import org.junit.jupiter.api.Test;
import pe.andes.api.common.util.JsonUtils;
import pe.andes.poc.client.generated.inventory.model.StockLevel;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Proves that andes-api-client's infrastructure (Jackson-based (de)serialization via
 * {@code JsonUtils}) works transparently with models GENERATED from a second, differently
 * shaped external contract ({@code contracts/openapi-client-b.yaml}), without any custom code.
 */
class InventoryModelCodegenTest {

    @Test
    void deserializesGeneratedStockLevelModel() {
        String json = """
                {"sku":"SKU-1","warehouse":"LIM-01","quantityAvailable":42}
                """;

        StockLevel stockLevel = JsonUtils.fromJson(json, StockLevel.class);

        assertEquals("SKU-1", stockLevel.getSku());
        assertEquals("LIM-01", stockLevel.getWarehouse());
        assertEquals(42, stockLevel.getQuantityAvailable());
    }
}
