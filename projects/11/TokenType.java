public enum TokenType {
    KEYWORD("keyword"), SYMBOL("symbol"), IDENTIFIER("identifier"), INT_CONST("integerConstant"),
    STRING_CONST("stringConstant");

    private String tokenType;

    private TokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String abbr() {
        return this.tokenType;
    }
}