package me.marin.lockout.lockout.goal.difficulty;

public interface DifficultyProbabilityFunction {
    float getProbability(float difficulty);

    default DifficultyProbabilityFunction scale(float factor) {
        return d -> Math.clamp(this.getProbability(d) * factor, 0, 1);
    }

    default DifficultyProbabilityFunction allowRange(float min, float max) {
        return d -> (d >= min && d <= max) ? this.getProbability(d) : 0;
    }

    static DifficultyProbabilityFunction random() {
        return _ -> 1;
    }

    static DifficultyProbabilityFunction absoluteDistance(float target, float steepness) {
        return d -> Math.clamp(1 - Math.abs(target - d) * steepness, 0, 1);
    }

    static DifficultyProbabilityFunction gauss(float target, float deviation) {
        return d -> {
            double factor = 1.0 / (deviation * Math.sqrt(2.0 * Math.PI));
            double exponent = -0.5 * Math.pow((d - target) / deviation, 2.0);
            return (float) (factor * Math.exp(exponent));
        };
    }
}
