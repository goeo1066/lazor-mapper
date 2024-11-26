package com.github.goeo1066.lazormapper.composers.upsert;

public record LazorUpsertSpec<S>(
        boolean doUpdate
) {
    public static <S> Builder<S> builder() {
        return new Builder<>();
    }

    public static <S> LazorUpsertSpec<S> ofDefault() {
        return new LazorUpsertSpec<>(true);
    }

    public static class Builder<S> {
        public boolean doUpdate = true;

        public Builder<S> doUpdate(boolean doUpdate) {
            this.doUpdate = doUpdate;
            return this;
        }

        public LazorUpsertSpec<S> build() {
            return new LazorUpsertSpec<>(doUpdate);
        }
    }
}
