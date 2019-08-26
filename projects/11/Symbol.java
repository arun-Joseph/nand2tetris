import java.io.*;

public class Symbol {
    private String type;
    private Kind kind;
    private int num;

    public Symbol(String type, Kind kind, int num) {
        this.type = type;
        this.kind = kind;
        this.num = num;
    }

    public String getType() {
        return type;
    }

    public Kind getKind() {
        return kind;
    }

    public int getNum() {
        return num;
    }
}