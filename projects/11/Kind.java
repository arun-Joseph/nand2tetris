public enum Kind {
    STATIC("static"), FIELD("field"), ARG("argument"), VAR("local"), NONE("");

    private String kind;

    private Kind(String kind) {
        this.kind = kind;
    }

    public String abbr() {
        return this.kind;
    }
}