import java.io.*;
import java.util.*;

public class VMTranslator {
    private static Parser parser;
    private static CodeWriter writer;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("File not specified");
            return;
        }

        File vmfile = new File(args[0]);
        parser = new Parser(vmfile);

        String asm = args[0].substring(0, args[0].length() - 3) + ".asm";
        File asmfile = new File(asm);
        writer = new CodeWriter(asmfile);

        int command, lineNo = 1;
        try {
            while (parser.hasMoreCommands()) {
                parser.advance();
                command = parser.commandType();

                switch (command) {
                case Parser.C_NULL:
                    break;

                case Parser.C_ARITHMETIC:
                    writer.writeArithmetic(parser.arg1());
                    break;

                case Parser.C_PUSH:
                case Parser.C_POP:
                    writer.writePushPop(command, parser.arg1(), parser.arg2());
                    break;

                default:
                    throw new Exception("Invalid command Type");
                }

                lineNo++;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + " on line " + lineNo);
        }

        parser.close();
        writer.close();
    }
}