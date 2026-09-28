package pe.andes.api.common.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    record Sample(String name, int value) {
    }

    @Test
    void roundTripSerialization() {
        Sample sample = new Sample("andes", 42);
        String json = JsonUtils.toJson(sample);
        Sample parsed = JsonUtils.fromJson(json, Sample.class);
        assertEquals(sample, parsed);
    }

    @Test
    void unknownPropertiesAreIgnored() {
        String json = "{\"name\":\"andes\",\"value\":1,\"extra\":\"ignored\"}";
        Sample parsed = JsonUtils.fromJson(json, Sample.class);
        assertEquals("andes", parsed.name());
    }

    @Test
    void invalidJsonThrows() {
        assertThrows(IllegalStateException.class, () -> JsonUtils.fromJson("not-json", Map.class));
    }
}
