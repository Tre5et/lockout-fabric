package me.marin.lockout.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import me.marin.lockout.Constants;
import me.marin.lockout.Lockout;
import me.marin.lockout.lockout.goal.difficulty.GameDifficulty;
import me.marin.lockout.network.Network;
import me.marin.lockout.network.ServerConfigPayload;
import me.marin.lockout.registries.LockoutRegistryKeys;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ServerConfig {
    public static final StreamCodec<ByteBuf, ServerConfig> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            c -> c.boardSize,
            ByteBufCodecs.INT,
            c -> c.startTime,
            ByteBufCodecs.BOOL,
            c -> c.giveCompasses,
            HintMode.CODEC,
            c -> c.hintMode,
            Identifier.STREAM_CODEC,
            c -> c.difficulty,
            ServerConfig::new
    );

    private static final Path CONFIG_PATH = new File("./config/lockout/server.json").toPath();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    private static ServerConfig instance = new ServerConfig();

    @Getter
    private int boardSize = 5;
    @Getter
    private int startTime = 60;
    @Getter
    private boolean giveCompasses = false;
    @Getter
    private HintMode hintMode = HintMode.MAP;
    @Getter
    private Identifier difficulty = Constants.id("random");

    public ServerConfig(int boardSize, int startTime, boolean giveCompasses, HintMode hintMode, Identifier difficulty) {
        this.boardSize = boardSize;
        this.startTime = startTime;
        this.giveCompasses = giveCompasses;
        this.hintMode = hintMode;
        this.difficulty = difficulty;
    }

    public ServerConfig() {
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
        save();
        broadcast();
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
        save();
        broadcast();
    }

    public void setGiveCompasses(boolean giveCompasses) {
        this.giveCompasses = giveCompasses;
        save();
        broadcast();
    }

    public void setHintMode(HintMode hintMode) {
        this.hintMode = hintMode;
        save();
        broadcast();
    }

    public Optional<Holder.Reference<GameDifficulty>> getDifficulty(RegistryAccess registryAccess) {
        return registryAccess.lookup(LockoutRegistryKeys.DIFFICULTY).flatMap(r -> r.get(this.difficulty));
    }

    public void setDifficulty(Holder.Reference<GameDifficulty> difficulty) {
        this.difficulty = difficulty.key().identifier();
        save();
        broadcast();
    }

    public void broadcast() {
        Network.broadcast(new ServerConfigPayload(this));
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            createConfigDir();
            loadDefaultConfig();
            save();
        } else {
            try {
                String s = Files.readString(CONFIG_PATH);
                instance = GSON.fromJson(s, ServerConfig.class);
                save(); // saves "new" config values (from updates)
            } catch (Exception e) {
                Lockout.log("Invalid config file, using default values.");
                loadDefaultConfig();
            }
        }
    }

    public static void loadDefaultConfig() {
        instance = new ServerConfig();
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

    public enum HintMode {
        TEXT(null),
        COMPASS(Items.COMPASS),
        MAP(Items.FILLED_MAP);

        public static final StreamCodec<ByteBuf, HintMode> CODEC = ByteBufCodecs.fromCodec(Codec.stringResolver(HintMode::name, HintMode::valueOf));

        @Getter
        private final Item itemType;

        HintMode(Item itemType) {
            this.itemType = itemType;
        }
    }
}
