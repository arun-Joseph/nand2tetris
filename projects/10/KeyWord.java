public enum KeyWord {
    CLASS("class"), METHOD("method"), FUNCTION("function"), CONSTRUCTOR("constructor"), INT("int"), CHAR("char"),
    BOOLEAN("boolean"), VOID("void"), VAR("var"), STATIC("static"), FIELD("field"), LET("let"), DO("do"), IF("if"),
    ELSE("else"), WHILE("while"), RETURN("return"), TRUE("true"), FALSE("false"), NULL("null"), THIS("this");

    private final String keyword;

    private KeyWord(String keyword) {
        this.keyword = keyword;
    }

    public String abbr() {
        return this.keyword;
    }
}