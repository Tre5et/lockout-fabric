package me.marin.lockout.lockout.goal.rendering.name;

public interface NameExtractorProvider<T> {
    NameExtractor get(T option);

    static Simple simple(NameExtractor extractor) {
        return new Simple(extractor);
    }

    class Simple implements NameExtractorProvider<Void> {
        private final NameExtractor extractor;

        public Simple(NameExtractor extractor) {
            this.extractor = extractor;
        }

        @Override
        public NameExtractor get(Void option) {
            return extractor;
        }
    }
}
