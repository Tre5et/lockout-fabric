package me.marin.lockout.generator;

import me.marin.lockout.Lockout;
import me.marin.lockout.lockout.goal.builder.GoalBuilder;
import me.marin.lockout.lockout.goal.difficulty.DifficultyCheckFunction;
import me.marin.lockout.lockout.goal.difficulty.GameDifficulty;
import me.marin.lockout.lockout.goal.requirements.GoalRequirementContext;
import me.marin.lockout.server.game.ServerLockoutBoard;
import me.marin.lockout.server.goal.ServerGoal;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;

import java.util.*;

public class BoardGenerator {
    private final Random RANDOM = new Random();

    private final List<GoalBuilder<?,?>> registeredGoals;
    private final GoalRequirementContext context;

    public BoardGenerator(List<GoalBuilder<?,?>> registeredGoals, GoalRequirementContext context) {
        this.registeredGoals = registeredGoals;
        this.context = context;
    }

    public ServerLockoutBoard generateBoard(int size, GameDifficulty difficulty, Registry<GoalBuilder<?,?>> registry) {
        ArrayList<ServerGoal<?>> board = new ArrayList<>();
        ArrayList<GoalBuilder<?,?>> availableGoals = new ArrayList<>(registeredGoals.stream().filter(GoalBuilder::isEnabled).toList());
        Collections.shuffle(availableGoals);
        int goalCount = size * size;
        RandomSource random = RandomSource.create();

        // Populate initial board with goals respecting the difficulty setting.
        while(board.size() < goalCount) {
            try {
                generateDifficultyValidatedGoal(registry, board, difficulty, random, 100);
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
            double totalDifficulty = board.stream().mapToDouble(ServerGoal::getDifficulty).average().getAsDouble();
            DifficultyCheckFunction.DifficultyStatus status = difficulty.getTotal().check((float) totalDifficulty);
            if(status.isAllowed()) {
                Lockout.log("Board generation finished. (difficulty: " + totalDifficulty + ")");
                Collections.shuffle(board);
                return new ServerLockoutBoard(board);
            }
            ServerGoal<?> replaced;
            if(status == DifficultyCheckFunction.DifficultyStatus.TOO_HARD) {
                Lockout.log("Board too hard (difficulty: " + totalDifficulty + "), replacing hardest goal.");
                replaced = board.stream().max(Comparator.comparing(ServerGoal::getDifficulty)).get();
            } else {
                Lockout.log("Board too easy (difficulty: " + totalDifficulty + "), replacing easiest goal.");
                replaced = board.stream().min(Comparator.comparing(ServerGoal::getDifficulty)).get();
            }
            board.remove(replaced);
            try {
                generateDifficultyValidatedGoal(registry, board, difficulty, random, 100);
            } catch (NoSuchElementException e) {
                Lockout.log("Board generation failed: Failed to replace goal to make correct difficulty: " + e.getMessage());
                return null;
            }
        }

        Lockout.log("Board generation failed: Failed to coerce board into acceptable difficulty in " + maxAdjustmentAttempts + " attempts.");
        return null;
    }

    private void generateDifficultyValidatedGoal(Registry<GoalBuilder<?,?>> goalRegistry, ArrayList<ServerGoal<?>> currentGoals, GameDifficulty difficulty, RandomSource random, int maxAttempts) throws NoSuchElementException {
        for(int i = 0; i < maxAttempts; i++) {
            if(goalRegistry.size() <= currentGoals.size()) throw new NoSuchElementException("No more goals available.");
            Optional<Holder.Reference<GoalBuilder<?,?>>> optionalBuilder = goalRegistry.getRandom(random);
            if(optionalBuilder.isEmpty()) continue;
            Holder.Reference<GoalBuilder<?,?>> builder = optionalBuilder.get();
            if(!builder.value().isEnabled()) continue;
            if(currentGoals.stream().anyMatch(g -> g.getBuildData().id().equals(builder.key()))) continue;
            if(!builder.value().getGroups().stream().allMatch(g -> g.value().canAdd(builder.key(), currentGoals, goalRegistry))) {
                // Group does not permit this goal to be added.
                continue;
            }
            var goal = builder.value().buildGeneratedServer(context, builder.key());
            if(goal.isEmpty()) continue;
            if(!difficulty.getIndividual().check(goal.get().getDifficulty()).isAllowed()) continue;
            Lockout.log("Generated goal in " + i + " attempts.");
            currentGoals.add(goal.get());
            return;
        }
        throw new NoSuchElementException("Failed to find a goal in " + maxAttempts + " attempts.");
    }
}
