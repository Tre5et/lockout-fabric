package me.marin.lockout.lockout.goal.progress;

import me.marin.lockout.client.goal.progress.ClientGoalProgress;
import me.marin.lockout.client.goal.progress.SimpleClientGoalProgress;
import me.marin.lockout.client.goal.progress.TargetFloatClientGoalProgress;
import me.marin.lockout.client.goal.progress.TargetNumberClientGoalProgress;
import me.marin.lockout.lockout.goal.acceptance.AcceptanceCondition;
import me.marin.lockout.lockout.goal.acceptance.AnyAcceptanceCondition;
import me.marin.lockout.lockout.goal.rendering.texture.CycleTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.ItemCountTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.StackingTextureExtractor;
import me.marin.lockout.lockout.goal.rendering.texture.TextureExtractor;
import me.marin.lockout.server.goal.progress.ServerGoalProgress;
import me.marin.lockout.server.goal.progress.SimpleServerGoalProgress;
import me.marin.lockout.server.goal.progress.TargetFloatServerGoalProgress;
import me.marin.lockout.server.goal.progress.UniqueServerGoalProgress;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface GoalProgressSupplier<T,U,E> {
    ClientGoalProgress<E> getClient(T data);

    ServerGoalProgress<U,E> getServer(T data);

    String getStaticId();

    String getId(T data);

    String getName(T data);

    TextureExtractor getTextureExtractor(T data);

    default TextureExtractor applyFinalTextureExtractor(TextureExtractor extractor, T data) {
        return extractor;
    }

    default CustomStaticIdGoalProgressSupplier<T,U,E> withStaticId(String id) {
        return new CustomStaticIdGoalProgressSupplier<>(this, id);
    }

    default <M> MappedGoalProgressSupplier<T,U,E,M> map(Function<M,U> mapper) {
        return new MappedGoalProgressSupplier<>(this, mapper);
    }

    default <M> MappedCreationGoalProgressSupplier<T,U,E,M> mapCreation(Function<M,T> mapper) {
        return new MappedCreationGoalProgressSupplier<>(this, mapper);
    }

    class MappedGoalProgressSupplier<T,U,E,M> implements GoalProgressSupplier<T,M,E> {
        private final GoalProgressSupplier<T,U,E> original;
        private final Function<M,U> mapper;

        public MappedGoalProgressSupplier(GoalProgressSupplier<T, U, E> original, Function<M, U> mapper) {
            this.original = original;
            this.mapper = mapper;
        }

        @Override
        public ClientGoalProgress<E> getClient(T data) {
            return original.getClient(data);
        }

        @Override
        public ServerGoalProgress<M, E> getServer(T data) {
            return original.getServer(data).map(mapper);
        }

        @Override
        public String getStaticId() {
            return original.getStaticId();
        }

        @Override
        public String getId(T data) {
            return original.getId(data);
        }

        @Override
        public String getName(T data) {
            return original.getName(data);
        }

        @Override
        public TextureExtractor getTextureExtractor(T data) {
            return original.getTextureExtractor(data);
        }

        @Override
        public TextureExtractor applyFinalTextureExtractor(TextureExtractor extractor, T data) {
            return original.applyFinalTextureExtractor(extractor, data);
        }
    }

    class MappedCreationGoalProgressSupplier<T,U,E,M> implements GoalProgressSupplier<M,U,E> {
        private final GoalProgressSupplier<T,U,E> original;
        private final Function<M,T> mapper;

        public MappedCreationGoalProgressSupplier(GoalProgressSupplier<T, U, E> original, Function<M, T> mapper) {
            this.original = original;
            this.mapper = mapper;
        }

        @Override
        public ClientGoalProgress<E> getClient(M data) {
            return original.getClient(mapper.apply(data));
        }

        @Override
        public ServerGoalProgress<U, E> getServer(M data) {
            return original.getServer(mapper.apply(data));
        }

        @Override
        public String getStaticId() {
            return original.getStaticId();
        }

        @Override
        public String getId(M data) {
            return original.getId(mapper.apply(data));
        }

        @Override
        public String getName(M data) {
            return original.getName(mapper.apply(data));
        }

        @Override
        public TextureExtractor getTextureExtractor(M data) {
            return original.getTextureExtractor(mapper.apply(data));
        }

        @Override
        public TextureExtractor applyFinalTextureExtractor(TextureExtractor extractor, M data) {
            return original.applyFinalTextureExtractor(extractor, mapper.apply(data));
        }
    }

    class CustomStaticIdGoalProgressSupplier<T,U,E> implements GoalProgressSupplier<T,U,E> {
        private final GoalProgressSupplier<T,U,E> original;
        private final String id;

        public CustomStaticIdGoalProgressSupplier(GoalProgressSupplier<T, U, E> original, String id) {
            this.original = original;
            this.id = id;
        }


        @Override
        public ClientGoalProgress<E> getClient(T data) {
            return original.getClient(data);
        }

        @Override
        public ServerGoalProgress<U, E> getServer(T data) {
            return original.getServer(data);
        }

        @Override
        public String getStaticId() {
            return id;
        }

        @Override
        public String getId(T data) {
            return original.getId(data);
        }

        @Override
        public String getName(T data) {
            return original.getName(data);
        }

        @Override
        public TextureExtractor getTextureExtractor(T data) {
            return original.getTextureExtractor(data);
        }
    }

    static <T,U> GoalProgressSupplier<T,U,Boolean> simple(Function<T, AcceptanceCondition<U>> condition) {
        return new GoalProgressSupplier<>() {
            @Override
            public ClientGoalProgress<Boolean> getClient(T data) {
                return new SimpleClientGoalProgress();
            }

            @Override
            public ServerGoalProgress<U, Boolean> getServer(T data) {
                return new SimpleServerGoalProgress<>(condition.apply(data)::test);
            }

            @Override
            public String getStaticId() {
                return condition.apply(null).getId();
            }

            @Override
            public String getId(T data) {
                return condition.apply(data).getId();
            }

            @Override
            public String getName(T data) {
                return condition.apply(data).getName();
            }

            @Override
            public TextureExtractor getTextureExtractor(T option) {
                return new CycleTextureExtractor(condition.apply(option).getExamples());
            }
        };
    }

    static <U> GoalProgressSupplier<Void,U,Boolean> simple(Supplier<AcceptanceCondition<U>> condition) {
        return simple(_ -> condition.get());
    }

    static <U,E> GoalProgressSupplier<Integer,U,Integer> unique(String title, Function<Integer, AcceptanceCondition<U>> condition, Function<U,E> toElement) {
        return new GoalProgressSupplier<>() {
            @Override
            public ClientGoalProgress<Integer> getClient(Integer data) {
                return new TargetNumberClientGoalProgress(title, data);
            }

            @Override
            public ServerGoalProgress<U, Integer> getServer(Integer data) {
                return new UniqueServerGoalProgress<>(data, condition.apply(data)::test, toElement);
            }

            @Override
            public String getStaticId() {
                return "UNIQUE_" + condition.apply(null).getId();
            }

            @Override
            public String getId(Integer data) {
                return data + "_UNIQUE_" + condition.apply(data).getId();
            }

            @Override
            public String getName(Integer data) {
                return data + " Unique " + condition.apply(data).getName();
            }

            @Override
            public TextureExtractor getTextureExtractor(Integer data) {
                return new CycleTextureExtractor(condition.apply(data).getExamples());
            }

            @Override
            public TextureExtractor applyFinalTextureExtractor(TextureExtractor extractor, Integer data) {
                return new StackingTextureExtractor(List.of(
                        extractor,
                        new ItemCountTextureExtractor(Component.literal(data.toString()))
                ), 0);
            }
        };
    }

    static <U> GoalProgressSupplier<Integer,U,Integer> unique(String title, Function<Integer, AcceptanceCondition<U>> condition) {
        return unique(title, condition, u -> u);
    }

    static <U> GoalProgressSupplier<Number,U,Number> total(String title, Function<Number, AcceptanceCondition<U>> condition, Function<U, Number> toNumber, Function<Number, String> numberToString) {
        return new GoalProgressSupplier<>() {
            @Override
            public ClientGoalProgress<Number> getClient(Number data) {
                return new TargetFloatClientGoalProgress(title, data, numberToString);
            }

            @Override
            public ServerGoalProgress<U, Number> getServer(Number data) {
                AcceptanceCondition<U> resolvedCondition = condition.apply(data);
                return new TargetFloatServerGoalProgress<>(data, u -> resolvedCondition.test(u) ? toNumber.apply(u) : 0);
            }

            @Override
            public String getStaticId() {
                return "TOTAL_" + condition.apply(null).getId();
            }

            @Override
            public String getId(Number data) {
                return "_TOTAL_" + numberToString.apply(data) + "_" + condition.apply(data).getId();
            }

            @Override
            public String getName(Number data) {
                return numberToString.apply(data) + " " + condition.apply(data).getName();
            }

            @Override
            public TextureExtractor getTextureExtractor(Number data) {
                return new CycleTextureExtractor(condition.apply(data).getExamples());
            }

            @Override
            public TextureExtractor applyFinalTextureExtractor(TextureExtractor extractor, Number data) {
                return new StackingTextureExtractor(List.of(
                        extractor,
                        new ItemCountTextureExtractor(Component.literal(numberToString.apply(data)))
                ), 0);
            }
        };
    }

    static <U> GoalProgressSupplier<Integer, U, Number> countTotal(String title, Function<Integer, AcceptanceCondition<U>> condition) {
        return GoalProgressSupplier.total(title, n -> condition.apply(n == null ? null : n.intValue()), _ -> 1, n -> String.valueOf(n.intValue())).mapCreation(i -> i);
    }

    static GoalProgressSupplier<Float, Float, Number> rawTotal(String title, Supplier<String> name, Supplier<TextureExtractor> baseTexture) {
        return GoalProgressSupplier.<Float>total(title, _ -> new AnyAcceptanceCondition<>(
                "",
                name,
                () -> List.of(baseTexture.get())
        ), f -> f, f -> String.valueOf(Math.round(f.floatValue()))).mapCreation(f -> f);
    }
}
