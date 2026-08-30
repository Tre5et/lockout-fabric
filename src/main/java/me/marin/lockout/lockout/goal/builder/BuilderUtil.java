package me.marin.lockout.lockout.goal.builder;

import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BuilderUtil {
    public static String idToName(String id) {
        return Arrays.stream(id.split("_"))
                .map(p -> p.substring(0, 1).toUpperCase() + p.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public static String identifierToName(Identifier identifier) {
        String id = Arrays.stream(identifier.getPath().split("/")).toList().getLast();
        int dotIndex = id.lastIndexOf(".");
        if(dotIndex != -1) id = id.substring(0, dotIndex);
        return idToName(id);
    }

    public static String identifierToId(Identifier identifier) {
        String id = Arrays.stream(identifier.getPath().split("/")).toList().getLast();
        int dotIndex = id.lastIndexOf(".");
        if(dotIndex != -1) id = id.substring(0, dotIndex);
        return id.toUpperCase();
    }
}
