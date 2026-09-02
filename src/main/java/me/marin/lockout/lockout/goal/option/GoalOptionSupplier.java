package me.marin.lockout.lockout.goal.option;

import com.google.gson.reflect.TypeToken;
import me.marin.lockout.client.goal.option.ClientGoalOptionGenerator;
import me.marin.lockout.client.goal.option.IntegerClientGoalOptionGenerator;
import me.marin.lockout.client.goal.option.ListClientGoalOptionGenerator;

import java.util.List;
import java.util.function.Function;

public interface GoalOptionSupplier<T>  {
    ClientGoalOptionGenerator<T> getClient();

    GoalOptionGenerator<T> get();

    String getStaticId();

    GoalOptionSupplier<Void> NONE = new GoalOptionSupplier<>() {
        @Override
        public ClientGoalOptionGenerator<Void> getClient() {return null;}

        @Override
        public GoalOptionGenerator<Void> get() {return null;}

        @Override
        public String getStaticId() {
            return "";
        }
    };

    static <T> GoalOptionSupplier<T> list(String title, List<T> values, TypeToken<T> type, String collectionName, Function<T,String> elementToString) {
        return new GoalOptionSupplier<>() {
            @Override
            public ClientGoalOptionGenerator<T> getClient() {
                return new ListClientGoalOptionGenerator<>(title, values, type, elementToString);
            }

            @Override
            public GoalOptionGenerator<T> get() {
                return new ListGoalOptionGenerator<>(values, type);
            }

            @Override
            public String getStaticId() {
                return collectionName.toUpperCase();
            }
        };
    }

    static GoalOptionSupplier<Integer> integer(String title, int min, int max, int step) {
        return new GoalOptionSupplier<>() {
            @Override
            public ClientGoalOptionGenerator<Integer> getClient() {
                return new IntegerClientGoalOptionGenerator(title, min, max, step, (max-min+1)/step);
            }

            @Override
            public GoalOptionGenerator<Integer> get() {
                return new IntegerGoalOptionGenerator(min, max, step, (max-min+1)/step);
            }

            @Override
            public String getStaticId() {
                return min + "_" + max + "_" + step;
            }
        };
    }
}
