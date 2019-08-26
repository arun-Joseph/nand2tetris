import java.io.*;

/**
 * Generates the compiler's output.
 */

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private VMWriter writer;
    private SymbolTable symbolTable;

    private String className;
    private KeyWord funcType;
    private String funcName;
    private String retType;
    private int classLabel;

    /**
     * Creates a new compilation engine.
     * 
     * @param infile  input file.
     * @param outfile output file.
     */
    public CompilationEngine(File infile, File outfile) {
        try {
            tokenizer = new JackTokenizer(infile);
            writer = new VMWriter(outfile);
        } catch (Exception e) {
            System.out.println("File not found");
        }

        try {
            symbolTable = new SymbolTable();
            classLabel = 0;
            compileClass();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Compiles a complete class.
     */
    public void compileClass() throws Exception {
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.CLASS) {
            error("class");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("className");
        }
        className = tokenizer.identifier();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '{') {
            error("{");
        }

        compileClassVarDec();

        compileSubroutineDec();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '}') {
            error("}");
        }
    }

    /**
     * Compiles a static variable declaration, or a field declaration.
     */
    public void compileClassVarDec() throws Exception {
        String name, type = "";
        Kind kind = Kind.NONE;

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.CONSTRUCTOR
                || tokenizer.keyWord() == KeyWord.FUNCTION || tokenizer.keyWord() == KeyWord.METHOD)) {
            tokenizer.retrace();
            return;
        } else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '}') {
            return;
        }
        tokenizer.retrace();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD
                || (tokenizer.keyWord() != KeyWord.STATIC && tokenizer.keyWord() != KeyWord.FIELD)) {
            error("(static | field)");
        }
        if (tokenizer.keyWord() == KeyWord.STATIC) {
            kind = Kind.STATIC;
        } else if (tokenizer.keyWord() == KeyWord.FIELD) {
            kind = Kind.FIELD;
        }

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.INT
                || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN)) {
            type = tokenizer.keyWord().abbr();
        } else if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            type = tokenizer.identifier();
        } else {
            error("type");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("varName");
        }
        name = tokenizer.identifier();
        symbolTable.define(name, type, kind);

        while (true) {
            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ';') {
                break;
            } else if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ',') {
                error(",");
            }

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("varName");
            }
            name = tokenizer.identifier();
            symbolTable.define(name, type, kind);
        }

        compileClassVarDec();
    }

    /**
     * Compiles a complete method, function, or constructor.
     */
    public void compileSubroutineDec() throws Exception {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '}') {
            tokenizer.retrace();
            return;
        }
        tokenizer.retrace();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || (tokenizer.keyWord() != KeyWord.CONSTRUCTOR
                && tokenizer.keyWord() != KeyWord.FUNCTION && tokenizer.keyWord() != KeyWord.METHOD)) {
            error("(constructor | function | method");
        }
        funcType = tokenizer.keyWord();

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD
                && (tokenizer.keyWord() == KeyWord.VOID || tokenizer.keyWord() == KeyWord.INT
                        || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN)) {
            retType = tokenizer.keyWord().abbr();
        } else if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            retType = tokenizer.identifier();
        } else {
            error("(void | type)");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("subroutineName");
        }
        funcName = tokenizer.identifier();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '(') {
            error("{");
        }

        compileParameterList();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
            error("}");
        }

        compileSubroutineBody();

        compileSubroutineDec();
    }

    /**
     * Compiles a parameter list.
     */
    public void compileParameterList() throws Exception {
        String name, type = "";

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')') {
            tokenizer.retrace();
            return;
        }
        tokenizer.retrace();

        while (true) {
            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.INT
                    || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN)) {
                type = tokenizer.keyWord().abbr();
            } else if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
                type = tokenizer.identifier();
            } else {
                error("type");
            }

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("varName");
            }
            name = tokenizer.identifier();
            symbolTable.define(name, type, Kind.ARG);

            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')') {
                tokenizer.retrace();
                break;
            } else if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ',') {
                error(",");
            }
        }
    }

    /**
     * Compiles a subroutine's body.
     */
    public void compileSubroutineBody() throws Exception {
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '{') {
            error("{");
        }

        compileVarDec();

        writer.writeFunction(className + "." + funcName, symbolTable.VarCount(Kind.VAR));
        if (funcType == KeyWord.CONSTRUCTOR) {
            writer.writePush("constant", symbolTable.VarCount(Kind.FIELD));
            writer.writeCall("Memory.alloc", 1);
            writer.writePop("pointer", 0);
        } else if (funcType == KeyWord.METHOD) {
            writer.writePush("argument", 0);
            writer.writePop("pointer", 0);
        }

        compileStatements();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '}') {
            error("}");
        }
    }

    /**
     * Compiles a var declaration.
     */
    public void compileVarDec() throws Exception {
        String name, type = "";

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.VAR) {
            tokenizer.retrace();
            return;
        }

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.INT
                || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN)) {
            type = tokenizer.keyWord().abbr();
        } else if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            type = tokenizer.identifier();
        } else {
            error("type");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("varName");
        }
        name = tokenizer.identifier();
        symbolTable.define(name, type, Kind.VAR);

        while (true) {
            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ';') {
                break;
            } else if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ',') {
                error(",");
            }

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("varName");
            }
            name = tokenizer.identifier();
            symbolTable.define(name, type, Kind.VAR);
        }

        compileVarDec();
    }

    /**
     * Compiles a sequence of statements.
     */
    public void compileStatements() throws Exception {
        while (true) {
            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '}') {
                tokenizer.retrace();
                break;
            } else if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.LET) {
                tokenizer.retrace();
                compileLet();
            } else if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.IF) {
                tokenizer.retrace();
                compileIf();
            } else if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.WHILE) {
                tokenizer.retrace();
                compileWhile();
            } else if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.DO) {
                tokenizer.retrace();
                compileDo();
            } else if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.RETURN) {
                tokenizer.retrace();
                compileReturn();
            } else {
                error("(letStatement | ifStatement | whileStatement | doStatement | returnStatement)");
            }
        }
    }

    /**
     * Compiles a let statement.
     */
    public void compileLet() throws Exception {
        String name;
        Kind kind;
        boolean isArray = false;

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.LET) {
            error("let");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("varName");
        }
        name = tokenizer.identifier();
        kind = symbolTable.KindOf(name);
        if (kind == Kind.NONE) {
            error("identifier");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '[') {
            writer.writePush(kind.abbr(), symbolTable.IndexOf(name));

            compileExpression();
            writer.writeArithmetic("add");

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ']') {
                error("]");
            }
            isArray = true;

            tokenizer.advance();
        }
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '=') {
            error("=");
        }

        compileExpression();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ';') {
            error(";");
        }

        if (isArray) {
            writer.writePop("temp", 0);
            writer.writePop("pointer", 1);
            writer.writePush("temp", 0);
            writer.writePop("that", 0);
        } else {
            writer.writePop(kind.abbr(), symbolTable.IndexOf(name));
        }
    }

    /**
     * Compiles an if statement.
     */
    public void compileIf() throws Exception {
        String label = getLabel();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.IF) {
            error("if");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '(') {
            error("(");
        }

        compileExpression();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
            error(")");
        }

        writer.writeArithmetic("not");
        writer.writeIf(label);

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '{') {
            error("{");
        }

        compileStatements();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '}') {
            error("}");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.ELSE) {
            String label2 = getLabel();
            writer.writeGoto(label2);
            writer.writeLabel(label);

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '{') {
                error("{");
            }

            compileStatements();

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '}') {
                error("}");
            }

            writer.writeLabel(label2);
        } else {
            writer.writeLabel(label);
            tokenizer.retrace();
        }
    }

    /**
     * Compiles a while statement.
     */
    public void compileWhile() throws Exception {
        String label1 = getLabel();
        String label2 = getLabel();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.WHILE) {
            error("while");
        }
        writer.writeLabel(label1);

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '(') {
            error("(");
        }

        compileExpression();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
            error(")");
        }

        writer.writeArithmetic("not");
        writer.writeIf(label2);

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '{') {
            error("{");
        }

        compileStatements();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '}') {
            error("}");
        }

        writer.writeGoto(label1);
        writer.writeLabel(label2);
    }

    /**
     * Compiles a do statement.
     */
    public void compileDo() throws Exception {
        String name;

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.DO) {
            error("do");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("(className | varName | subroutineName)");
        }
        name = tokenizer.identifier();

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '.') {
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("subroutineName");
            }
            String func = tokenizer.identifier();
            String type = symbolTable.TypeOf(name);
            int paramCount = 0;
            if (type.equals("")) {
                type = name;
            } else {
                writer.writePush(symbolTable.KindOf(name).abbr(), symbolTable.IndexOf(name));
                paramCount++;
            }

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '(') {
                error("(");
            }

            paramCount += compileExpressionList();
            writer.writeCall(type + "." + func, paramCount);

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
                error(")");
            }
        } else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(') {
            int paramCount = compileExpressionList();
            writer.writePush("pointer", 0);
            writer.writeCall(className + "." + name, paramCount + 1);

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
                error(")");
            }
        } else {
            Kind kind = symbolTable.KindOf(name);
            if (kind == Kind.NONE) {
                error("identifier");
            }
            writer.writePush(kind.abbr(), symbolTable.IndexOf(name));
            tokenizer.retrace();
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ';') {
            error(";");
        }

        writer.writePop("temp", 0);
    }

    /**
     * Compiles a return statement.
     */
    public void compileReturn() throws Exception {
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.RETURN) {
            error("return");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ';') {
            tokenizer.retrace();

            compileExpression();

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ';') {
                error(";");
            }
        } else {
            writer.writePush("constant", 0);
        }
        writer.writeReturn();
    }

    /**
     * Compiles an expression.
     */
    public void compileExpression() throws Exception {
        char op;

        compileTerm();

        while (true) {
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL
                    || !("+-*/&|<>=".contains(String.valueOf(tokenizer.symbol())))) {
                tokenizer.retrace();
                break;
            }
            op = tokenizer.symbol();

            compileTerm();

            switch (op) {
            case '+':
                writer.writeArithmetic("add");
                break;
            case '-':
                writer.writeArithmetic("sub");
                break;
            case '*':
                writer.writeCall("Math.multiply", 2);
                break;
            case '/':
                writer.writeCall("Math.divide", 2);
                break;
            case '&':
                writer.writeArithmetic("and");
                break;
            case '|':
                writer.writeArithmetic("or");
                break;
            case '<':
                writer.writeArithmetic("lt");
                break;
            case '>':
                writer.writeArithmetic("gt");
                break;
            case '=':
                writer.writeArithmetic("eq");
                break;
            }
        }
    }

    /**
     * Compiles a term.
     */
    public void compileTerm() throws Exception {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.INT_CONST) {
            writer.writePush("constant", tokenizer.intVal());
        } else if (tokenizer.tokenType() == TokenType.STRING_CONST) {
            String str = tokenizer.stringVal();
            int length = str.length();

            writer.writePush("constant", length);
            writer.writeCall("String.new", 1);

            for (int i = 0; i < length; i++) {
                writer.writePush("constant", (int) str.charAt(i));
                writer.writeCall("String.appendChar", 2);
            }
        } else if (tokenizer.tokenType() == TokenType.KEYWORD
                && (tokenizer.keyWord() == KeyWord.TRUE || tokenizer.keyWord() == KeyWord.FALSE
                        || tokenizer.keyWord() == KeyWord.NULL || tokenizer.keyWord() == KeyWord.THIS)) {
            if (tokenizer.keyWord() == KeyWord.TRUE) {
                writer.writePush("constant", 0);
                writer.writeArithmetic("not");
            } else if (tokenizer.keyWord() == KeyWord.FALSE || tokenizer.keyWord() == KeyWord.NULL) {
                writer.writePush("constant", 0);
            } else if (tokenizer.keyWord() == KeyWord.THIS) {
                writer.writePush("pointer", 0);
            }
        } else if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            String name = tokenizer.identifier();

            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '[') {
                Kind kind = symbolTable.KindOf(name);
                if (kind == Kind.NONE) {
                    error("identifier");
                }
                writer.writePush(kind.abbr(), symbolTable.IndexOf(name));

                compileExpression();
                writer.writeArithmetic("add");
                writer.writePop("pointer", 1);
                writer.writePush("that", 0);

                tokenizer.advance();
                if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ']') {
                    error("]");
                }
            } else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '.') {
                tokenizer.advance();
                if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                    error("subroutineName");
                }
                String func = tokenizer.identifier();
                String type = symbolTable.TypeOf(name);
                int paramCount = 0;
                if (type.equals("")) {
                    type = name;
                } else {
                    writer.writePush(symbolTable.KindOf(name).abbr(), symbolTable.IndexOf(name));
                    paramCount++;
                }

                tokenizer.advance();
                if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '(') {
                    error("(");
                }

                paramCount += compileExpressionList();
                writer.writeCall(type + "." + func, paramCount);

                tokenizer.advance();
                if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
                    error(")");
                }
            } else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(') {
                int paramCount = compileExpressionList();
                writer.writePush("pointer", 0);
                writer.writeCall(className + "." + name, paramCount + 1);

                tokenizer.advance();
                if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
                    error(")");
                }
            } else {
                Kind kind = symbolTable.KindOf(name);
                if (kind == Kind.NONE) {
                    error("identifier");
                }
                writer.writePush(kind.abbr(), symbolTable.IndexOf(name));
                tokenizer.retrace();
            }
        } else if (tokenizer.tokenType() == TokenType.SYMBOL) {
            if (tokenizer.symbol() == '(') {
                compileExpression();

                tokenizer.advance();
                if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
                    error(")");
                }
            } else if (tokenizer.symbol() == '-' || tokenizer.symbol() == '~') {
                char ch = tokenizer.symbol();

                compileTerm();

                switch (ch) {
                case '-':
                    writer.writeArithmetic("neg");
                    break;
                case '~':
                    writer.writeArithmetic("not");
                    break;
                }
            } else {
                error("('(' | '-' | '~')");
            }
        } else {
            error("(integerConstant | stringConstant | keywordConstant | varName)");
        }
    }

    /**
     * Compiles a comma-separated list of expressions.
     */
    public int compileExpressionList() throws Exception {
        int paramCount = 0;

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')') {
            tokenizer.retrace();
            return 0;
        }
        tokenizer.retrace();

        compileExpression();
        paramCount++;

        while (true) {
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ',') {
                tokenizer.retrace();
                break;
            }

            compileExpression();
            paramCount++;
        }

        return paramCount;
    }

    /**
     * @return the next label.
     */
    private String getLabel() {
        return "L" + (classLabel++);
    }

    /**
     * Throws an exception.
     * 
     * @param message
     */
    private void error(String message) throws Exception {
        throw new Exception(message);
    }

    /**
     * Closes the files.
     */
    public void close() {
        try {
            tokenizer.close();
            writer.close();
        } catch (Exception e) {
        }
    }
}