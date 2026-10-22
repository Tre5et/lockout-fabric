package me.marin.lockout.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import me.marin.lockout.Lockout;
import me.marin.lockout.server.ServerConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientConfig {
    private static final Path CONFIG_PATH = new File("./config/lockout/client.json").toPath();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    private static ClientConfig instance = new ClientConfig();

    @Getter
    private BoardPosition boardPosition = BoardPosition.RIGHT;
    @Getter
    @Setter
    private transient ServerConfig serverConfig = ServerConfig.getInstance();

    public void setBoardPosition(BoardPosition boardPosition) {
        this.boardPosition = boardPosition;
        save();
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            createConfigDir();
            loadDefaultConfig();
            save();
        } else {
            try {
                String s = Files.readString(CONFIG_PATH);
                instance = GSON.fromJson(s, ClientConfig.class);
                save(); // saves "new" config values (from updates)
            } catch (Exception e) {
                Lockout.log("Invalid config file, using default values.");
                loadDefaultConfig();
            }
        }
    }

    public static void loadDefaultConfig() {
        instance = new ClientConfig();
    }

    private static void createConfigDir() {
        try {
            Files.createDirectories(Path.of("./config/lockout"));
        } catch (Exception e) {
            Lockout.error(e);
        }
    }

    public static void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(instance));
        } catch (Exception e) {
            Lockout.error(e);
        }
    }

    public enum BoardPosition {
        LEFT,
        RIGHT;

        public static BoardPosition match(String boardPosition) {
            return switch (boardPosition) {
                case "left" -> LEFT;
                case "right" -> RIGHT;
                default -> null;
            };
        }
    }
}
