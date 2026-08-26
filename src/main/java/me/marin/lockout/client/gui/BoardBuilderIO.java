package me.marin.lockout.client.gui;

import com.google.gson.*;
import me.marin.lockout.lockout.goal.Goal;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BoardBuilderIO {

    public static final Path DIRECTORY = Minecraft.getInstance().gameDirectory.toPath().resolve("lockout-boards");
    public static final String FILE_EXTENSION = ".json";

    public static final BoardBuilderIO INSTANCE = new BoardBuilderIO();

    public BoardBuilderIO() {
        if (!Files.exists(DIRECTORY)) {
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void saveBoard(String name, List<Goal<?>> goals) throws IOException {
        Path boardPath = DIRECTORY.resolve(name + FILE_EXTENSION);
        Files.createFile(boardPath);
        JsonArray data = new JsonArray();
        for(Goal<?> goal : goals) {
            data.add(goal.serialize(List.of(), false));
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String jsonString = gson.toJson(data);
        Files.writeString(boardPath, jsonString);
    }

    public List<String> getSavedBoards() throws IOException {
        return Files.list(DIRECTORY).map(p -> StringUtils.removeEnd(p.getFileName().toString(), FILE_EXTENSION)).toList();
    }

    public Path getBoardPath(String name) {
        return DIRECTORY.resolve(name + FILE_EXTENSION);
    }

    public List<Goal<?>> readBoard(String name) throws IOException {
        JsonElement read = JsonParser.parseString(Files.readString(getBoardPath(name)));
        if(!read.isJsonArray()) throw new IOException("Saved board is not an array.");
        List<Goal<?>> goals = new ArrayList<>();
        for(JsonElement element : read.getAsJsonArray()) {
            goals.add(Goal.deserialize(element, List.of()));
        }
        return goals;
    }

    /**
     * Changes the board name if necessary. If board name isn't specified or the name already exists, append (1), (2),
     * or whatever first available name is.
     * @param boardName Name of the board
     * @return Changed name of the board
     */
    public String getSuitableName(String boardName) throws IOException {
        boolean exists = false;
        List<String> savedBoards = getSavedBoards();
        for (String savedBoard : savedBoards) {
            if (savedBoard.equals(boardName)) {
                exists = true;
                break;
            }
        }
        if (!exists) return boardName;

        int num = 1;
        while (savedBoards.contains(boardName + " (" + num + ")")) {
            num++;
        }
        return boardName + " (" + num + ")";
    }


}
