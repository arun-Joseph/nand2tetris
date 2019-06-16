import java.io.*;
import java.util.*;

/**
 * Handles the parsing of a single .vm file.
 */

public class Parser {
    private Scanner sc;
    private String command;
    private int argType;
    private String arg1;
    private int arg2;

    public static final int C_NULL = 0;
    public static final int C_ARITHMETIC = 1;
    public static final int C_PUSH = 2;
    public static final int C_POP = 3;
    public static final int C_GOTO = 4;
    public static final int C_IF = 5;
    public static final int C_LABEL = 6;
    public static final int C_CALL = 7;
    public static final int C_FUNCTION = 8;
    public static final int C_RETURN = 9;

    /**
     * Opens the input file for parsing.
     * 
     * @param vmfile input file.
     */
    public Parser(File vmfile) {
        try {
            sc = new Scanner(vmfile);
        } catch (Exception e) {
            System.out.println("File not found");
        }
    }

    /**
     * @return true if more commands are in input file.
     */
    public boolean hasMoreCommands() {
        return sc.hasNextLine();
    }

    /**
     * Reads the next command from the input file.
     */
    public void advance() throws Exception {
        command = sc.nextLine();
        if (command.indexOf('/') != -1) {
            command = command.substring(0, command.indexOf('/'));
        }
        command = command.trim();

        if (command.length() == 0) {
            argType = C_NULL;
            return;
        }

        String[] args = command.split(" ");
        if (args.length > 3) {
            throw new Exception("Invalid number of arguments");
        }

        if ((args[0].equals("add") || args[0].equals("sub") || args[0].equals("neg") || args[0].equals("eq")
                || args[0].equals("gt") || args[0].equals("lt") || args[0].equals("and") || args[0].equals("or")
                || args[0].equals("not")) && args.length == 1) {
            argType = C_ARITHMETIC;
            arg1 = args[0];
        } else if ((args[0].equals("push")) && args.length == 3) {
            argType = C_PUSH;
            arg1 = args[1];
        } else if ((args[0].equals("pop")) && args.length == 3) {
            argType = C_POP;
            arg1 = args[1];
        } else if ((args[0].equals("goto")) && args.length == 2) {
            argType = C_GOTO;
            arg1 = args[1];
        } else if ((args[0].equals("if-goto")) && args.length == 2) {
            argType = C_IF;
            arg1 = args[1];
        } else if ((args[0].equals("label")) && args.length == 2) {
            argType = C_LABEL;
            arg1 = args[1];
        } else if ((args[0].equals("call")) && args.length == 3) {
            argType = C_CALL;
            arg1 = args[1];
        } else if ((args[0].equals("function")) && args.length == 3) {
            argType = C_FUNCTION;
            arg1 = args[1];
        } else if ((args[0].equals("return")) && args.length == 1) {
            argType = C_RETURN;
        } else {
            throw new Exception("Invalid argument type");
        }
        arg1.trim();

        if (args[0].equals("push") || args[0].equals("pop") || args[0].equals("call") || args[0].equals("function")) {
            try {
                arg2 = Integer.parseInt(args[2]);
            } catch (Exception e) {
                throw new Exception("Invalid index");
            }
        }
    }

    /**
     * @return constant representing the type of the current command.
     */
    public int commandType() {
        return argType;
    }

    /**
     * @return the first argument of the current command.
     */
    public String arg1() {
        return arg1;
    }

    /**
     * @return the second argument of the current command.
     */
    public int arg2() {
        return arg2;
    }

    /**
     * Closes the input file.
     */
    public void close() {
        sc.close();
    }
}