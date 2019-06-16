import java.io.*;
import java.util.*;

public class VMTranslator {
    private static Parser parser;
    private static CodeWriter writer;

    private static File[] files;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("File/Folder not specified");
            return;
        }

        if (args[0].endsWith(".vm")) {
            File asmfile = new File(args[0].substring(0, args[0].length() - 3) + ".asm");
            writer = new CodeWriter(asmfile);
            files = new File[] { new File(args[0]) };
        } else {
            File asmfile = new File(args[0] + args[0].substring(args[0].lastIndexOf("/")) + ".asm");
            writer = new CodeWriter(asmfile);
            files = new File(args[0]).listFiles();
        }

        try {
            writer.writeInit();
        } catch (Exception e) {
        }

        for (File vmfile : files) {
            if (!vmfile.getName().endsWith(".vm")) {
                continue;
            }

            parser = new Parser(vmfile);
            writer.setFileName(vmfile.getName().substring(0, vmfile.getName().length() - 3));

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

                    case Parser.C_GOTO:
                        writer.writeGoto(parser.arg1());
                        break;

                    case Parser.C_IF:
                        writer.writeIf(parser.arg1());
                        break;

                    case Parser.C_LABEL:
                        writer.writeLabel(parser.arg1());
                        break;

                    case Parser.C_CALL:
                        writer.writeCall(parser.arg1(), parser.arg2());
                        break;

                    case Parser.C_FUNCTION:
                        writer.writeFunction(parser.arg1(), parser.arg2());
                        break;

                    case Parser.C_RETURN:
                        writer.writeReturn();
                        break;

                    default:
                        throw new Exception("Invalid command type");
                    }

                    lineNo++;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage() + " on line " + lineNo);
            }

            parser.close();
        }

        writer.close();
    }
}