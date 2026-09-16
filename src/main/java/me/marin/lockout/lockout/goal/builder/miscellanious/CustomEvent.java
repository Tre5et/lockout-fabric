package me.marin.lockout.lockout.goal.builder.miscellanious;

import me.marin.lockout.Constants;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.rendering.texture.GenericTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureAnchor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public record CustomEvent(
    String id,
    Supplier<String> name,
    Supplier<TextureExtractor> textureExtractor
) {
    public AcceptanceCondition<CustomEvent> getAcceptanceCondition() {
        return new AcceptanceCondition<>() {
            @Override
            public boolean test(CustomEvent value, ServerPlayer player) {
                return value.equals(CustomEvent.this);
            }

            @Override
            public String getId() {
                return id();
            }

            @Override
            public String getName() {
                return name().get();
            }

            @Override
            public List<TextureExtractor> getExamples() {
                return List.of(textureExtractor().get());
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj == this) return true;
        if(!(obj instanceof CustomEvent event)) return false;
        return Objects.equals(event.id(), this.id());
    }

    public static final CustomEvent GEYSER_LAUNCH = new CustomEvent("GEYSER_LAUNCH", () -> "Get Launched by a Geyser", () -> ItemTextureExtractor.item(Items.POTENT_SULFUR).overlay(
            GenericTextureExtractor.texture(Identifier.fromNamespaceAndPath(Constants.NAMESPACE, "textures/custom/up.png")),
            TextureAnchor.TOP_CENTER,
            12
    ));
}
