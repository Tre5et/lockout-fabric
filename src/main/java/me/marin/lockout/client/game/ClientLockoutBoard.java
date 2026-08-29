package me.marin.lockout.client.game;

import me.marin.lockout.client.LockoutClient;
import me.marin.lockout.client.goal.ClientGoal;
import me.marin.lockout.game.LockoutBoard;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.Optional;

public class ClientLockoutBoard extends LockoutBoard<ClientGoal> {
    public ClientLockoutBoard(List<ClientGoal> goals) {
        super(goals);
    }

    public void extractGoals(GuiGraphicsExtractor extractor, Font font, int x, int y, int slotSize, int slotPadding, AdditionExtractor backgroundExtractor, AdditionExtractor foregroundExtractor) {
        for(int xIndex = 0; xIndex < getSize(); xIndex++) {
            for(int yIndex = 0; yIndex < getSize(); yIndex++) {
                int goalX = x + yIndex * (slotSize + slotPadding);
                int goalY = y + xIndex * (slotSize + slotPadding);
                int goalIndex = xIndex * getSize() + yIndex;
                ClientGoal goal = getGoals().get(goalIndex);
                backgroundExtractor.extract(goalX, goalY, goalIndex, Optional.ofNullable(goal));
                if(goal != null) goal.extractTexture(extractor, font, goalX, goalY, slotSize, slotSize, LockoutClient.CURRENT_TICK);
                foregroundExtractor.extract(goalX, goalY, goalIndex, Optional.ofNullable(goal));
            }
        }
    }

    public Optional<Integer> getHoveredIndex(int mouseX, int mouseY, int boardX, int boardY, int slotSize, int slotPadding) {
        int relativeX = mouseX - boardX;
        if(relativeX < 0 || relativeX > getSize() * (slotSize + slotPadding) - slotPadding) return Optional.empty();
        if(relativeX % (slotSize + slotPadding) >= slotSize) return Optional.empty();
        int xIndex = relativeX / (slotSize + slotPadding);

        int relativeY = mouseY - boardY;
        if(relativeY < 0 || relativeY > getSize() * (slotSize + slotPadding) - slotPadding) return Optional.empty();
        if(relativeY % (slotSize + slotPadding) >= slotSize) return Optional.empty();
        int yIndex = relativeY / (slotSize + slotPadding);

        return Optional.of(yIndex * getSize() + xIndex);
    }

    public Optional<ClientGoal> getHoveredGoal(int mouseX, int mouseY, int boardX, int boardY, int slotSize, int slotPadding) {
        int relativeX = mouseX - boardX;
        if(relativeX < 0 || relativeX > getSize() * (slotSize + slotPadding) - slotPadding) return Optional.empty();
        if(relativeX % (slotSize + slotPadding) >= slotSize) return Optional.empty();
        int xIndex = relativeX / (slotSize + slotPadding);

        int relativeY = mouseY - boardY;
        if(relativeY < 0 || relativeY > getSize() * (slotSize + slotPadding) - slotPadding) return Optional.empty();
        if(relativeY % (slotSize + slotPadding) >= slotSize) return Optional.empty();
        int yIndex = relativeY / (slotSize + slotPadding);

        return Optional.ofNullable(getGoals().get(yIndex * getSize() + xIndex));
    }

    public interface AdditionExtractor {
        void extract(int x, int y, int goalIndex, Optional<ClientGoal> goal);
    }
}
