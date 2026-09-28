package pe.edu.galaxy.training.java.logs.util;

import java.util.UUID;

public final class TraceIdGenerator {
    private TraceIdGenerator() {}

    public static String generate() {
    //return UUID.randomUUID().toString().replace("-", "");
        return UUID.randomUUID().toString();
    }
}
