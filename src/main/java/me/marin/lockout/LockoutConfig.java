package me.marin.lockout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class LockoutConfig {

    private static final Path CONFIG_PATH = new File("./config/lockout.json").toPath();

    @Getter
    private static LockoutConfig instance;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @SerializedName("boardSize")
    public int boardSize = 5;

    @SerializedName("boardPosition")
    public BoardPosition boardPosition = BoardPosition.RIGHT;

    @SerializedName("startTime")
    public int startTime = 60;

    @SerializedName("giveCompasses")
    public boolean giveCompasses = false;

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            createConfigDir();
            loadDefaultConfig();
            save();
        } else {
            try {
                String s = Files.readString(CONFIG_PATH);
                instance = GSON.fromJson(s, LockoutConfig.class);
                save(); // saves "new" config values (from updates)
            } catch (Exception e) {
                Lockout.log("Invalid config file, using default values.");
                loadDefaultConfig();
            }
        }
    }

    public static void loadDefaultConfig() {
        instance = new LockoutConfig();
        instance.boardSize = 5;
        instance.boardPosition = BoardPosition.RIGHT;
        instance.startTime = 60;
        instance.giveCompasses = false;
    }

    private static void createConfigDir() {
        try {
            Files.createDirectories(Path.of("./config"));
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
        @SerializedName("left")
        LEFT,
        @SerializedName("right")
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
