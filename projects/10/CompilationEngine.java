import java.io.*;

/**
 * Generates the compiler's output.
 */

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private FileWriter fw;
    private int spaces;

    /**
     * Creates a new compilation engine.
     * 
     * @param infile    input file.
     * @param outfile   output file.
     * @param tokenfile token file.
     */
    public CompilationEngine(File infile, File outfile, File tokenfile) {
        try {
            tokenizer = new JackTokenizer(infile, tokenfile);
            fw = new FileWriter(outfile);
        } catch (Exception e) {
            System.out.println("File not found");
        }

        try {
            compileClass();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Compiles a complete class.
     */
    public void compileClass() throws Exception {
        compile("<class>");
        incSpaces();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.CLASS) {
            error("class");
        }
        compileKeyWord();
        ;

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("className");
        }
        compileIdentifier();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '{') {
            error("{");
        }
        compileSymbol();

        compileClassVarDec();

        compileSubroutineDec();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '}') {
            error("}");
        }
        compileSymbol();

        decSpaces();
        compile("</class>");
    }

    /**
     * Compiles a static variable declaration, or a field declaration.
     */
    public void compileClassVarDec() throws Exception {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.CONSTRUCTOR
                || tokenizer.keyWord() == KeyWord.FUNCTION || tokenizer.keyWord() == KeyWord.METHOD)) {
            tokenizer.retrace();
            return;
        } else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '}') {
            return;
        }
        tokenizer.retrace();

        compile("<classVarDec>");
        incSpaces();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD
                || (tokenizer.keyWord() != KeyWord.STATIC && tokenizer.keyWord() != KeyWord.FIELD)) {
            error("(static | field)");
        }
        compileKeyWord();

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.INT
                || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN)) {
            compileKeyWord();
        } else if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            compileIdentifier();
        } else {
            error("type");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("varName");
        }
        compileIdentifier();

        while (true) {
            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ';') {
                compileSymbol();
                break;
            } else if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ',') {
                error(",");
            }
            compileSymbol();

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("varName");
            }
            compileIdentifier();
        }

        decSpaces();
        compile("</classVarDec>");

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

        compile("<subroutineDec>");
        incSpaces();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || (tokenizer.keyWord() != KeyWord.CONSTRUCTOR
                && tokenizer.keyWord() != KeyWord.FUNCTION && tokenizer.keyWord() != KeyWord.METHOD)) {
            error("(constructor | function | method");
        }
        compileKeyWord();

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD
                && (tokenizer.keyWord() == KeyWord.VOID || tokenizer.keyWord() == KeyWord.INT
                        || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN)) {
            compileKeyWord();
        } else if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            compileIdentifier();
        } else {
            error("(void | type)");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("subroutineName");
        }
        compileIdentifier();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '(') {
            error("{");
        }
        compileSymbol();

        compileParameterList();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
            error("}");
        }
        compileSymbol();

        compileSubroutineBody();

        decSpaces();
        compile("</subroutineDec>");

        compileSubroutineDec();
    }

    /**
     * Compiles a parameter list.
     */
    public void compileParameterList() throws Exception {
        compile("<parameterList>");
        incSpaces();

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')') {
            decSpaces();
            compile("</parameterList>");
            tokenizer.retrace();
            return;
        }
        tokenizer.retrace();

        while (true) {
            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.INT
                    || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN)) {
                compileKeyWord();
            } else if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
                compileIdentifier();
            } else {
                error("type");
            }

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("varName");
            }
            compileIdentifier();

            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')') {
                tokenizer.retrace();
                break;
            } else if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ',') {
                error(",");
            }
            compileSymbol();
        }

        decSpaces();
        compile("</parameterList>");
    }

    /**
     * Compiles a subroutine's body.
     */
    public void compileSubroutineBody() throws Exception {
        compile("<subroutineBody>");
        incSpaces();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '{') {
            error("{");
        }
        compileSymbol();

        compileVarDec();

        compileStatements();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '}') {
            error("}");
        }
        compileSymbol();

        decSpaces();
        compile("</subroutineBody>");
    }

    /**
     * Compiles a var declaration.
     */
    public void compileVarDec() throws Exception {
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.VAR) {
            tokenizer.retrace();
            return;
        }
        compile("<varDec>");
        incSpaces();
        compileKeyWord();

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.INT
                || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN)) {
            compileKeyWord();
        } else if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            compileIdentifier();
        } else {
            error("type");
        }

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("varName");
        }
        compileIdentifier();

        while (true) {
            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ';') {
                compileSymbol();
                break;
            } else if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ',') {
                error(",");
            }
            compileSymbol();

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("varName");
            }
            compileIdentifier();
        }

        decSpaces();
        compile("</varDec>");

        compileVarDec();
    }

    /**
     * Compiles a sequence of statements.
     */
    public void compileStatements() throws Exception {
        compile("<statements>");
        incSpaces();

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

        decSpaces();
        compile("</statements>");
    }

    /**
     * Compiles a let statement.
     */
    public void compileLet() throws Exception {
        compile("<letStatement>");
        incSpaces();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.LET) {
            error("let");
        }
        compileKeyWord();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("varName");
        }
        compileIdentifier();

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '[') {
            compileSymbol();

            compileExpression();

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ']') {
                error("]");
            }
            compileSymbol();

            tokenizer.advance();
        }
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '=') {
            error("=");
        }
        compileSymbol();

        compileExpression();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ';') {
            error(";");
        }
        compileSymbol();

        decSpaces();
        compile("</letStatement>");
    }

    /**
     * Compiles an if statement.
     */
    public void compileIf() throws Exception {
        compile("<ifStatement>");
        incSpaces();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.IF) {
            error("if");
        }
        compileKeyWord();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '(') {
            error("(");
        }
        compileSymbol();

        compileExpression();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
            error(")");
        }
        compileSymbol();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '{') {
            error("{");
        }
        compileSymbol();

        compileStatements();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '}') {
            error("}");
        }
        compileSymbol();

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.ELSE) {
            compileKeyWord();

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '{') {
                error("{");
            }
            compileSymbol();

            compileStatements();

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '}') {
                error("}");
            }
            compileSymbol();
        } else {
            tokenizer.retrace();
        }

        decSpaces();
        compile("</ifStatement>");
    }

    /**
     * Compiles a while statement.
     */
    public void compileWhile() throws Exception {
        compile("<whileStatement>");
        incSpaces();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.WHILE) {
            error("while");
        }
        compileKeyWord();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '(') {
            error("(");
        }
        compileSymbol();

        compileExpression();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
            error(")");
        }
        compileSymbol();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '{') {
            error("{");
        }
        compileSymbol();

        compileStatements();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '}') {
            error("}");
        }
        compileSymbol();

        decSpaces();
        compile("</whileStatement>");
    }

    /**
     * Compiles a do statement.
     */
    public void compileDo() throws Exception {
        compile("<doStatement>");
        incSpaces();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.DO) {
            error("do");
        }
        compileKeyWord();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
            error("(className | varName | subroutineName)");
        }
        compileIdentifier();

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '.') {
            compileSymbol();

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                error("subroutineName");
            }
            compileIdentifier();

            tokenizer.advance();
        }
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '(') {
            error("(");
        }
        compileSymbol();

        compileExpressionList();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
            error(")");
        }
        compileSymbol();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ';') {
            error(";");
        }
        compileSymbol();

        decSpaces();
        compile("</doStatement>");
    }

    /**
     * Compiles a return statement.
     */
    public void compileReturn() throws Exception {
        compile("<returnStatement>");
        incSpaces();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.RETURN) {
            error("return");
        }
        compileKeyWord();

        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ';') {
            tokenizer.retrace();

            compileExpression();

            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ';') {
                error(";");
            }
        }
        compileSymbol();

        decSpaces();
        compile("</returnStatement>");
    }

    /**
     * Compiles an expression.
     */
    public void compileExpression() throws Exception {
        compile("<expression>");
        incSpaces();

        compileTerm();

        while (true) {
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL
                    || !("+-*/&|<>=".contains(String.valueOf(tokenizer.symbol())))) {
                tokenizer.retrace();
                break;
            }
            compileSymbol();

            compileTerm();
        }

        decSpaces();
        compile("</expression>");
    }

    /**
     * Compiles a term.
     */
    public void compileTerm() throws Exception {
        compile("<term>");
        incSpaces();

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.INT_CONST) {
            compileIntegerConstant();
        } else if (tokenizer.tokenType() == TokenType.STRING_CONST) {
            compileStringConstant();
        } else if (tokenizer.tokenType() == TokenType.KEYWORD
                && (tokenizer.keyWord() == KeyWord.TRUE || tokenizer.keyWord() == KeyWord.FALSE
                        || tokenizer.keyWord() == KeyWord.NULL || tokenizer.keyWord() == KeyWord.THIS)) {
            compileKeyWord();
        } else if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
            compileIdentifier();

            tokenizer.advance();
            if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '[') {
                compileSymbol();

                compileExpression();

                tokenizer.advance();
                if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ']') {
                    error("]");
                }
                compileSymbol();
            } else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '.') {
                compileSymbol();

                tokenizer.advance();
                if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
                    error("subroutineName");
                }
                compileIdentifier();

                tokenizer.advance();
                if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '(') {
                    error("(");
                }
                compileSymbol();

                compileExpressionList();

                tokenizer.advance();
                if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
                    error(")");
                }
                compileSymbol();
            } else if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(') {
                compileSymbol();

                compileExpressionList();

                tokenizer.advance();
                if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
                    error(")");
                }
                compileSymbol();
            } else {
                tokenizer.retrace();
            }
        } else if (tokenizer.tokenType() == TokenType.SYMBOL) {
            if (tokenizer.symbol() == '(') {
                compileSymbol();

                compileExpression();

                tokenizer.advance();
                if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')') {
                    error(")");
                }
                compileSymbol();
            } else if (tokenizer.symbol() == '-' || tokenizer.symbol() == '~') {
                compileSymbol();

                compileTerm();
            } else {
                error("('(' | '-' | '~')");
            }
        } else {
            error("(integerConstant | stringConstant | keywordConstant | varName)");
        }

        decSpaces();
        compile("</term>");
    }

    /**
     * Compiles a comma-separated list of expressions.
     */
    public void compileExpressionList() throws Exception {
        compile("<expressionList>");
        incSpaces();

        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')') {
            tokenizer.retrace();
            decSpaces();
            compile("</expressionList>");
            return;
        }
        tokenizer.retrace();

        compileExpression();

        while (true) {
            tokenizer.advance();
            if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ',') {
                tokenizer.retrace();
                break;
            }
            compileSymbol();

            compileExpression();
        }

        decSpaces();
        compile("</expressionList>");
    }

    /**
     * Increments spaces.
     */
    private void incSpaces() {
        spaces += 2;
    }

    /**
     * Decrements spaces.
     */
    private void decSpaces() {
        spaces -= 2;
    }

    /**
     * Prints the compiled statement.
     */
    private void compile(String tags) throws Exception {
        String tab = "";
        for (int i = 0; i < spaces; i++) {
            tab += " ";
        }
        fw.write(tab + tags + "\n");
    }

    /**
     * Prints the keyword.
     */
    private void compileKeyWord() throws Exception {
        compile("<keyword> " + tokenizer.keyWord().abbr() + " </keyword>");
    }

    /**
     * Prints the symbol.
     */
    private void compileSymbol() throws Exception {
        if (tokenizer.symbol() == '<') {
            compile("<symbol> &lt; </symbol>");
        } else if (tokenizer.symbol() == '>') {
            compile("<symbol> &gt; </symbol>");
        } else if (tokenizer.symbol() == '&') {
            compile("<symbol> &amp; </symbol>");
        } else {
            compile("<symbol> " + tokenizer.symbol() + " </symbol>");
        }
    }

    /**
     * Prints the identifier.
     */
    private void compileIdentifier() throws Exception {
        compile("<identifier> " + tokenizer.identifier() + " </identifier>");
    }

    /**
     * Prints the integer constant.
     */
    private void compileIntegerConstant() throws Exception {
        compile("<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
    }

    /**
     * Prints the string constant.
     */
    private void compileStringConstant() throws Exception {
        compile("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>");
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
            fw.close();
        } catch (Exception e) {
        }
    }
}