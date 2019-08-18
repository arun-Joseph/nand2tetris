import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the compiler's input.
 */

public class JackTokenizer {
    private Scanner sc;
    private FileWriter fw;

    private String currentToken;
    private int pointer;
    private int maxPointer;
    private TokenType currentTokenType;
    private ArrayList<String> tokens;

    private String keyWordRegex;
    private String symbolRegex;
    private String identifierRegex;
    private String intConstRegex;
    private String stringConstRegex;
    private Pattern tokenPattern;

    /**
     * Opens the input .jack file and gets ready to tokenize it.
     * 
     * @param infile
     * @param outfile
     */
    public JackTokenizer(File infile, File outfile) {
        try {
            sc = new Scanner(infile);
            fw = new FileWriter(outfile);

            String file = "";
            String line;

            while (sc.hasNext()) {
                line = sc.nextLine().trim();

                int pos = line.indexOf("//");
                if (pos != -1) {
                    line = line.substring(0, pos).trim();
                }

                if (line.length() > 0) {
                    file += line + "\n";
                }
            }

            while (true) {
                int beginComments = file.indexOf("/*");
                int endComments = file.indexOf("*/");

                if (beginComments == -1) {
                    break;
                }

                if (endComments == -1) {
                    file = file.substring(0, beginComments - 1);
                } else {
                    file = file.substring(0, beginComments) + file.substring(endComments + 2);
                }
            }

            keyWordRegex = "";
            for (KeyWord keyword : KeyWord.values()) {
                keyWordRegex += keyword.abbr() + "|";
            }

            symbolRegex = "[\\{\\}\\(\\)\\}\\[\\]\\.\\,\\;\\+\\-\\*\\/\\&\\|\\<\\>\\=\\~]";
            identifierRegex = "[a-zA-Z_][a-zA-Z0-9_]*";
            intConstRegex = "[0-9]+";
            stringConstRegex = "\"[^\"\n]*\"";
            tokenPattern = Pattern.compile(
                    keyWordRegex + symbolRegex + "|" + identifierRegex + "|" + intConstRegex + "|" + stringConstRegex);

            Matcher matcher = tokenPattern.matcher(file);
            pointer = 0;
            maxPointer = 0;
            tokens = new ArrayList<>();

            while (matcher.find()) {
                tokens.add(matcher.group());
            }

            fw.write("<tokens>\n");
        } catch (Exception e) {
            System.out.println("File not found");
        }

    }

    /**
     * @return true if there are more tokens.
     */
    public boolean hasMoreTokens() {
        return pointer < tokens.size();
    }

    /**
     * Gets the next token from the input, and makes it the current token.
     */
    public void advance() throws Exception {
        String parsedToken;

        currentToken = tokens.get(pointer);
        pointer++;

        if (currentToken.matches(keyWordRegex)) {
            currentTokenType = TokenType.KEYWORD;
            parsedToken = keyWord().abbr();
        } else if (currentToken.matches(symbolRegex)) {
            currentTokenType = TokenType.SYMBOL;
            parsedToken = String.valueOf(symbol());
            if (symbol() == '<') {
                parsedToken = "&lt;";
            } else if (symbol() == '>') {
                parsedToken = "&gt;";
            } else if (symbol() == '&') {
                parsedToken = "&amp;";
            }
        } else if (currentToken.matches(identifierRegex)) {
            currentTokenType = TokenType.IDENTIFIER;
            parsedToken = identifier();
        } else if (currentToken.matches(intConstRegex)) {
            currentTokenType = TokenType.INT_CONST;
            parsedToken = String.valueOf(intVal());
        } else if (currentToken.matches(stringConstRegex)) {
            currentTokenType = TokenType.STRING_CONST;
            parsedToken = stringVal();
        } else {
            throw new Exception("Invalid token type");
        }

        if (pointer > maxPointer) {
            fw.write("<" + currentTokenType.abbr() + "> ");
            fw.write(parsedToken);
            fw.write(" </" + currentTokenType.abbr() + ">\n");

            maxPointer++;
        }
    }

    /**
     * Sets the pointer to the previous token.
     */
    public void retrace() {
        if (pointer > 0) {
            pointer--;
        }
    }

    /**
     * @return the type of the current token.
     */
    public TokenType tokenType() {
        return currentTokenType;
    }

    /**
     * @return the keyword which is the current token.
     */
    public KeyWord keyWord() {
        for (KeyWord keyword : KeyWord.values()) {
            if (keyword.abbr().equals(currentToken)) {
                return keyword;
            }
        }
        return null;
    }

    /**
     * @return the character which is the current token.
     */
    public char symbol() {
        return currentToken.charAt(0);
    }

    /**
     * @return the identifier which is the current token.
     */
    public String identifier() {
        return currentToken;
    }

    /**
     * @return the integer value of the current token.
     */
    public int intVal() {
        return Integer.parseInt(currentToken);
    }

    /**
     * @return the string value of the current token, without the two enclosing
     *         double quotes.
     */
    public String stringVal() {
        return currentToken.substring(1, currentToken.length() - 1);
    }

    /**
     * Closes the FileWriter
     */
    public void close() throws Exception {
        fw.write("</tokens>\n");
        fw.close();
    }
}