package com.github.goeo1066.lazormapper.composers;


public record LazorSelectSpec(String whereClause, String orderByClause, Long limit, Long offset) {

    public static Builder builder() {
        return new Builder();
    }

    public LazorSelectSpec withoutPaging() {
        return new LazorSelectSpec(whereClause, orderByClause, null, null);
    }

    public static class Builder {
        private String whereClause;
        private String orderByClause;
        private Long limit;
        private Long offset;

        public Builder whereClause(String whereClause) {
            this.whereClause = whereClause;
            return this;
        }

        public Builder orderByClause(String orderByClause) {
            this.orderByClause = orderByClause;
            return this;
        }

        public Builder limit(Long limit) {
            this.limit = limit;
            return this;
        }

        public Builder limit(Integer limit) {
            this.limit = limit == null ? null : limit.longValue();
            return this;
        }

        public Builder offset(Long offset) {
            return this;
        }

        public Builder offset(Integer offset) {
            this.offset = offset == null ? null : offset.longValue();
            return this;
        }

        public LazorSelectSpec build() {
            return new LazorSelectSpec(whereClause, orderByClause, limit, offset);
        }
    }
}
