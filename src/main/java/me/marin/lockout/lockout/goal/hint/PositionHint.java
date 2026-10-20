package me.marin.lockout.lockout.goal.hint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import oshi.util.tuples.Pair;

public abstract class PositionHint implements Hint<Pair<BlockPos, Identifier>> {
    @Override
    public Pair<BlockPos,Identifier> deserialize(JsonElement data) throws IllegalArgumentException {
        if(data == null || !data.isJsonObject()) throw new IllegalArgumentException("Position data is not a JSON object.");
        JsonObject object = data.getAsJsonObject();

        JsonElement x = object.get("x");
        if(x == null || !x.isJsonPrimitive() || !x.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("Position data x is not a valid integer.");
        JsonElement y = object.get("y");
        if(y == null || !y.isJsonPrimitive() || !y.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("Position data y is not a valid integer.");
        JsonElement z = object.get("z");
        if(z == null || !z.isJsonPrimitive() || !z.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("Position data x is not a valid integer.");

        JsonElement level = object.get("level");
        if(level == null || !level.isJsonPrimitive() || !level.getAsJsonPrimitive().isString()) throw new IllegalArgumentException("Position data level is not a valid string.");

        return new Pair<>(new BlockPos(x.getAsInt(), y.getAsInt(), z.getAsInt()), Identifier.parse(level.getAsString()));
    }

    @Override
    public JsonElement serialize(Pair<BlockPos, Identifier> data) {
        JsonObject object = new JsonObject();
        object.add("x", new JsonPrimitive(data.getA().getX()));
        object.add("y", new JsonPrimitive(data.getA().getY()));
        object.add("z", new JsonPrimitive(data.getA().getZ()));
        object.add("level", new JsonPrimitive(data.getB().toString()));
        return object;
    }
}
