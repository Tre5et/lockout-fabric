package me.marin.lockout.generator;

import me.marin.lockout.Lockout;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.difficulty.DifficultyCheckFunction;
import me.marin.lockout.lockout.goal.difficulty.GameDifficulty;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContext;
import me.marin.lockout.server.game.ServerLockoutBoard;
import me.marin.lockout.server.goal.ServerGoal;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class BoardGenerator {
    private final Random RANDOM = new Random();

    private final List<GoalBuilder<?,?>> registeredGoals;
    private final GoalRequirementContext context;

    public BoardGenerator(List<GoalBuilder<?,?>> registeredGoals, GoalRequirementContext context) {
        this.registeredGoals = registeredGoals;
        this.context = context;
    }

    public ServerLockoutBoard generateBoard(int size, GameDifficulty difficulty) {
        ArrayList<Pair<GoalBuilder<?,?>, ServerGoal<?>>> board = new ArrayList<>();
        ArrayList<GoalBuilder<?,?>> availableGoals = new ArrayList<>(registeredGoals.stream().filter(GoalBuilder::isEnabled).toList());
        Collections.shuffle(availableGoals);
        int goalCount = size * size;

        // Populate initial board with goals respecting the difficulty setting.
        while(board.size() < goalCount) {
            try {
                generateDifficultyValidatedGoal(availableGoals, board, difficulty, 100);
            } catch (NoSuchElementException e) {
                Lockout.log("Board generation failed: Failed to populate initial board after "+ board.size() + " goals: " + e.getMessage());
                return null;
            }
        }
        Lockout.log("Generated initial board.");

        // Check if total board difficulty conforms with difficulty requirement,
        // replace most / least difficult goals if not.
        int maxAdjustmentAttempts = 100;
        for(int i = 0; i < maxAdjustmentAttempts; i++) {
            double totalDifficulty = board.stream().mapToDouble(g -> g.getB().getDifficulty()).average().getAsDouble();
            DifficultyCheckFunction.DifficultyStatus status = difficulty.getTotal().check((float) totalDifficulty);
            if(status.isAllowed()) {
                Lockout.log("Board generation finished. (difficulty: " + totalDifficulty + ")");
                Collections.shuffle(board);
                return new ServerLockoutBoard(board.stream().map(Pair::getB).collect(Collectors.toUnmodifiableList()));
            }
            Pair<GoalBuilder<?,?>, ServerGoal<?>> replaced;
            if(status == DifficultyCheckFunction.DifficultyStatus.TOO_HARD) {
                Lockout.log("Board too hard (difficulty: " + totalDifficulty + "), replacing hardest goal.");
                replaced = board.stream().max(Comparator.comparing(g -> g.getB().getDifficulty())).get();
            } else {
                Lockout.log("Board too easy (difficulty: " + totalDifficulty + "), replacing easiest goal.");
                replaced = board.stream().min(Comparator.comparing(g -> g.getB().getDifficulty())).get();
            }
            board.remove(replaced);
            try {
                generateDifficultyValidatedGoal(availableGoals, board, difficulty, 100);
            } catch (NoSuchElementException e) {
                Lockout.log("Board generation failed: Failed to replace goal to make correct difficulty: " + e.getMessage());
                return null;
            }
        }

        Lockout.log("Board generation failed: Failed to coerce board into acceptable difficulty in " + maxAdjustmentAttempts + " attempts.");
        return null;
    }

    private void generateDifficultyValidatedGoal(ArrayList<GoalBuilder<?,?>> availableGoals, ArrayList<Pair<GoalBuilder<?,?>, ServerGoal<?>>> currentGoals, GameDifficulty difficulty, int maxAttempts) throws NoSuchElementException {
        for(int i = 0; i < maxAttempts; i++) {
            if(availableGoals.isEmpty()) throw new NoSuchElementException("No more goals available.");
            int index = RANDOM.nextInt(availableGoals.size());
            GoalBuilder<?,?> builder = availableGoals.get(index);
            if(!builder.isEnabled()) continue;
            if(!builder.getGroups().stream().allMatch(g -> g.canAdd(builder, currentGoals.stream().map(Pair::getA).collect(Collectors.toUnmodifiableList())))) {
                // Group does not permit this goal to be added.
                continue;
            }
            var goal = builder.buildGeneratedServer(context);
            if(goal.isEmpty()) continue;
            if(!difficulty.getIndividual().check(goal.get().getDifficulty()).isAllowed()) continue;
            availableGoals.remove(index);
            currentGoals.add(new Pair<>(builder, goal.get()));
            Lockout.log("Generated goal in " + i + " attempts.");
            return;
        }
        throw new NoSuchElementException("Failed to find a goal in " + maxAttempts + " attempts.");
    }
}
