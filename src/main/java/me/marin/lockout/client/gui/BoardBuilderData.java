package me.marin.lockout.client.gui;

import lombok.Getter;
import lombok.Setter;
import me.marin.lockout.client.game.ClientLockoutBoard;
import me.marin.lockout.client.goal.ClientGoal;

import java.util.Collections;
import java.util.List;

import static me.marin.lockout.Constants.MAX_BOARD_SIZE;
import static me.marin.lockout.Constants.MIN_BOARD_SIZE;

/**
 * Stores information about BoardBuilderScreen independently of the GUI.
 * All important states are saved here (including filled goals, search query in the search bar etc.)
 */
public class BoardBuilderData extends ClientLockoutBoard {
    private static final int DEFAULT_SIZE = 5;

    public static final BoardBuilderData INSTANCE = new BoardBuilderData();

    @Getter @Setter
    private String title = "";

    /**
     * Index of the goal that is currently being modified (searching goal or editing data)
     */
    @Setter @Getter
    private Integer modifyingIdx = null;

    @Setter @Getter
    private String search = "";

    private BoardBuilderData() {
        super(Collections.nCopies(DEFAULT_SIZE * DEFAULT_SIZE, null));
    }

    public void clear() {
        Collections.fill(getGoals(), null);
        modifyingIdx = null;
    }

    public ClientGoal getModifyingGoal() {
        return getGoals().get(modifyingIdx);
    }

    /**
     * Increases the board size by 1 by adding a column to the right and a row to the bottom of the board.
     */
    public void incrementSize() {
        int size = getSize();
        if (size >= MAX_BOARD_SIZE) return;
        int modifyingRow = modifyingIdx == null ? 0 : modifyingIdx / size;
        int modifyingColumn = modifyingIdx == null ? 0 : modifyingIdx % size;

        size += 1;

        // add column to the right (without bottom right corner)
        for (int i = 0; i < size - 1; i++) {
            getGoals().add((size * i) + (size - 1), null);
        }

        if (modifyingIdx != null) {
            modifyingIdx = modifyingRow * size + modifyingColumn;
        }

        // add row to the bottom (including bottom right corner)
        getGoals().addAll(Collections.nCopies(size, null));
    }

    /**
     * Decreases the board size by 1 by removing the rightmost column and the bottommost row.
     * Any goals in the removed slots are voided.
     */
    public void decrementSize() {
        int size = getSize();
        if (size <= MIN_BOARD_SIZE) return;
        int modifyingRow = modifyingIdx == null ? 0 : modifyingIdx / size;
        int modifyingColumn = modifyingIdx == null ? 0 : modifyingIdx % size;

        size -= 1;

        // remove the bottommost row
        for (int i = 0; i < size + 1; i++) {
            getGoals().removeLast();
        }

        // remove the rightmost column
        for (int i = size - 1; i >= 0; i--) {
            getGoals().remove((size + 1) * i + size);
        }

        if (modifyingIdx != null) {
            if (modifyingRow >= size) {
                modifyingRow -= 1;
            }
            if (modifyingColumn >= size) {
                modifyingColumn -= 1;
            }
            modifyingIdx = modifyingRow * size + modifyingColumn;
        }
    }

    public void setGoal(ClientGoal goal) {
        getGoals().set(modifyingIdx, goal);
    }

    public void setBoard(String title, List<ClientGoal> goals) {
        this.title = title;
        this.modifyingIdx = null;

        this.getGoals().clear();
        this.getGoals().addAll(goals);
        int size = getSize();
        if(getGoals().size() < size * size) {
            int targetSize = (size+1) * (size+1);
            getGoals().addAll(Collections.nCopies(targetSize - getGoals().size(), null));
        }
    }

    public boolean isValid() {
        int size = getSize();
        return getGoals().stream().distinct().count() == (long) size * size;
    }

}
