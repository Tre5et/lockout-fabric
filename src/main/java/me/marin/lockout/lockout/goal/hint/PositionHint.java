package me.marin.lockout.lockout.goal.hint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.BlockPos;

public abstract class PositionHint implements Hint<BlockPos> {
    @Override
    public BlockPos deserialize(JsonElement data) throws IllegalArgumentException {
        if(data == null || !data.isJsonObject()) throw new IllegalArgumentException("Position data is not a JSON object.");
        JsonObject object = data.getAsJsonObject();

        JsonElement x = object.get("x");
        if(x == null || !x.isJsonPrimitive() || !x.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("Position data x is not a valid integer.");
        JsonElement y = object.get("y");
        if(y == null || !y.isJsonPrimitive() || !y.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("Position data y is not a valid integer.");
        JsonElement z = object.get("z");
        if(z == null || !z.isJsonPrimitive() || !z.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("Position data x is not a valid integer.");

        return new BlockPos(x.getAsInt(), y.getAsInt(), z.getAsInt());
    }

    @Override
    public JsonElement serialize(BlockPos data) {
        JsonObject object = new JsonObject();
        object.add("x", new JsonPrimitive(data.getX()));
        object.add("y", new JsonPrimitive(data.getY()));
        object.add("z", new JsonPrimitive(data.getZ()));
        return object;
    }
}
