package com.sebuilder.interpreter;

public record TestCaseSelector(String headName,
                               String testCaseName,
                               int chainIndex
) {

    public static TestCaseSelector.Builder builder() {
        return new Builder();
    }

    private TestCaseSelector(Builder builder) {
        this(builder.getHeadName()
                , builder.getTestCaseName()
                , builder.getChainIndex());
    }

    public TestCase findTestCase(TestCase testCase) {
        return this.findChainHead(testCase)
                .getChains()
                .get(testCaseName);
    }

    public TestCase findChainHead(Suite suite) {
        return this.findChainHead(suite.head());
    }

    public TestCase findChainHead(TestCase testCase) {
        return testCase
                .flattenTestCases()
                .filter(it -> it.getChains().size() > 0 && it.name().equals(headName))
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
            return headName;
        }

        public Builder setHeadName(String headName) {
            this.headName = headName;
            return this;
        }

        public String getTestCaseName() {
            return testCaseName;
        }

        public Builder setTestCaseName(String testCaseName) {
            this.testCaseName = testCaseName;
            return this;
        }

        public int getChainIndex() {
            return chainIndex;
        }

        public Builder setChainIndex(int chainIndex) {
            this.chainIndex = chainIndex;
            return this;
        }
    }
}
