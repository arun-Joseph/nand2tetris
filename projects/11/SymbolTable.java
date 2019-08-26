import java.io.*;
import java.util.HashMap;

public class SymbolTable {
    private HashMap<String, Symbol> classSymbols;
    private HashMap<String, Symbol> subroutineSymbols;

    private int fieldCount;
    private int staticCount;
    private int argCount;
    private int varCount;

    /**
     * Creates a new symbol table.
     */
    public SymbolTable() {
        classSymbols = new HashMap<>();
        subroutineSymbols = new HashMap<>();

        fieldCount = 0;
        staticCount = 0;
        argCount = 0;
        varCount = 0;
    }

    /**
     * Starts a subroutine scope.
     */
    public void startSubroutine() {
        subroutineSymbols.clear();

        argCount = 0;
        varCount = 0;
    }

    /**
     * Defines a new identifier.
     * 
     * @param name
     * @param type
     * @param kind
     */
    public void define(String name, String type, Kind kind) {
        if (kind == Kind.STATIC) {
            classSymbols.put(name, new Symbol(type, kind, staticCount++));
        } else if (kind == Kind.FIELD) {
            classSymbols.put(name, new Symbol(type, kind, fieldCount++));
        } else if (kind == Kind.ARG) {
            subroutineSymbols.put(name, new Symbol(type, kind, argCount++));
        } else if (kind == Kind.VAR) {
            subroutineSymbols.put(name, new Symbol(type, kind, varCount++));

        }
    }

    /**
     * @param kind
     * @return the number of variables of the given kind.
     */
    public int VarCount(Kind kind) {
        if (kind == Kind.STATIC) {
            return staticCount;
        } else if (kind == Kind.FIELD) {
            return fieldCount;
        } else if (kind == Kind.ARG) {
            return argCount;
        } else if (kind == Kind.VAR) {
            return varCount;
        }

        return 0;
    }

    /**
     * @param name
     * @return the kind of the identifier.
     */
    public Kind KindOf(String name) {
        if (subroutineSymbols.containsKey(name)) {
            return subroutineSymbols.get(name).getKind();
        } else if (classSymbols.containsKey(name)) {
            return classSymbols.get(name).getKind();
        }

        return Kind.NONE;
    }

    /**
     * @param name
     * @return the type of the identifier.
     */
    public String TypeOf(String name) {
        if (subroutineSymbols.containsKey(name)) {
            return subroutineSymbols.get(name).getType();
        } else if (classSymbols.containsKey(name)) {
            return classSymbols.get(name).getType();
        }

        return "";
    }

    /**
     * @param name
     * @return the index assigned to the identifier.
     */
    public int IndexOf(String name) {
        if (subroutineSymbols.containsKey(name)) {
            return subroutineSymbols.get(name).getNum();
        } else if (classSymbols.containsKey(name)) {
            return classSymbols.get(name).getNum();
        }

        return 0;
    }
}