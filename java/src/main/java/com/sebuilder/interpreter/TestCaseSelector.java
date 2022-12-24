package com.sebuilder.interpreter;

public record TestCaseSelector(String headName,
                               String testCaseName,
                               int chainIndex
) {

    public static TestCaseSelector.Builder builder() {
        return new Builder();
    }

    private TestCaseSelector(final Builder builder) {
        this(builder.getHeadName()
                , builder.getTestCaseName()
                , builder.getChainIndex());
    }

    public TestCase findTestCase(final TestCase testCase) {
        return this.findChainHead(testCase)
                .chains()
                .get(this.testCaseName);
    }

    public TestCase findChainHead(final Suite suite) {
        return this.findChainHead(suite.head());
    }

    public TestCase findChainHead(final TestCase testCase) {
        return testCase
                .flattenTestCases()
                .filter(it -> it.chains().size() > 0 && it.name().equals(this.headName))
                .findFirst()
                .orElse(null);
    }

    public static class Builder {
        private String headName;
        private String testCaseName;
        private int chainIndex;

        public TestCaseSelector build() {
            return new TestCaseSelector(this);
        }

        public String getHeadName() {
            return this.headName;
        }

        public Builder setHeadName(final String headName) {
            this.headName = headName;
            return this;
        }

        public String getTestCaseName() {
            return this.testCaseName;
        }

        public Builder setTestCaseName(final String testCaseName) {
            this.testCaseName = testCaseName;
            return this;
        }

        public int getChainIndex() {
            return this.chainIndex;
        }

        public Builder setChainIndex(final int chainIndex) {
            this.chainIndex = chainIndex;
            return this;
        }
    }
}
