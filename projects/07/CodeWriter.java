import java.io.*;
import java.util.*;

/**
 * Generates assembly code from parsed VM command.
 */

public class CodeWriter {
    private FileWriter fw;
    private String file;
    private int label;

    /**
     * Opens the output file for parsing.
     * 
     * @param asmfile output file.
     */
    public CodeWriter(File asmfile) {
        try {
            fw = new FileWriter(asmfile);
            file = asmfile.getName();
            file = file.substring(0, file.length() - 4);
        } catch (Exception e) {
            System.out.println("File not found");
        }

        label = 0;
    }

    /**
     * Writes to the output file the assembly code that implements the given
     * arithmetic command.
     * 
     * @param command arithmetic command.
     */
    public void writeArithmetic(String command) throws Exception {
        if (command.equals("add")) {
            fw.write("@SP\n" + "AM=M-1\n" + "D=M\n" + "A=A-1\n" + "M=D+M\n");
        } else if (command.equals("sub")) {
            fw.write("@SP\n" + "AM=M-1\n" + "D=M\n" + "A=A-1\n" + "M=M-D\n");
        } else if (command.equals("neg")) {
            fw.write("@SP\n" + "A=M-1\n" + "M=-M\n");
        } else if (command.equals("eq")) {
            writeLogical("JEQ");
        } else if (command.equals("gt")) {
            writeLogical("JGT");
        } else if (command.equals("lt")) {
            writeLogical("JLT");
        } else if (command.equals("and")) {
            fw.write("@SP\n" + "AM=M-1\n" + "D=M\n" + "A=A-1\n" + "M=D&M\n");
        } else if (command.equals("or")) {
            fw.write("@SP\n" + "AM=M-1\n" + "D=M\n" + "A=A-1\n" + "M=D|M\n");
        } else if (command.equals("not")) {
            fw.write("@SP\n" + "A=M-1\n" + "M=!M\n");
        } else {
            throw new Exception("Arithmetic error");
        }
    }

    /**
     * Writes to the output file the assembly code that implements the given
     * push/pop command.
     * 
     * @param command push/pop command.
     * @param segment stack segment.
     * @param index   location in stack segment.
     */
    public void writePushPop(int command, String segment, int index) throws Exception {
        switch (command) {
        case Parser.C_PUSH:
            if (segment.equals("local")) {
                writePush("LCL", index);
            } else if (segment.equals("argument")) {
                writePush("ARG", index);
            } else if (segment.equals("this")) {
                writePush("THIS", index);
            } else if (segment.equals("that")) {
                writePush("THAT", index);
            } else if (segment.equals("constant")) {
                fw.write("@" + index + "\n" + "D=A\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
            } else if (segment.equals("static")) {
                fw.write("@" + file + "." + index + "\n" + "D=M\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
            } else if (segment.equals("temp")) {
                fw.write("@R" + (index + 5) + "\n" + "D=M\n" + "@SP\n"
                        + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
            } else if (segment.equals("pointer")) {
                switch (index) {
                case 0:
                    fw.write("@THIS\n" + "D=M\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
                    break;

                case 1:
                    fw.write("@THAT\n" + "D=M\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
                    break;

                default:
                    throw new Exception("Push pointer error");
                }
            } else {
                throw new Exception("Push error");
            }
            break;

        case Parser.C_POP:
            if (segment.equals("local")) {
                writePop("LCL", index);
            } else if (segment.equals("argument")) {
                writePop("ARG", index);
            } else if (segment.equals("this")) {
                writePop("THIS", index);
            } else if (segment.equals("that")) {
                writePop("THAT", index);
            } else if (segment.equals("static")) {
                fw.write("@" + file + "." + index + "\n" + "D=A\n" + "@R13\n" + "M=D\n" + "@SP\n" + "AM=M-1\n" + "D=M\n"
                        + "@R13\n" + "A=M\n" + "M=D\n");
            } else if (segment.equals("temp")) {
                fw.write("@R" + (index + 5) + "\n" + "D=A\n" + "@R13\n" + "M=D\n"
                        + "@SP\n" + "AM=M-1\n" + "D=M\n" + "@R13\n" + "A=M\n" + "M=D\n");
            } else if (segment.equals("pointer")) {
                switch (index) {
                case 0:
                    fw.write("@THIS\n" + "D=A\n" + "@R13\n" + "M=D\n" + "@SP\n" + "AM=M-1\n" + "D=M\n" + "@R13\n"
                            + "A=M\n" + "M=D\n");
                    break;

                case 1:
                    fw.write("@THAT\n" + "D=A\n" + "@R13\n" + "M=D\n" + "@SP\n" + "AM=M-1\n" + "D=M\n" + "@R13\n"
                            + "A=M\n" + "M=D\n");
                    break;

                default:
                    throw new Exception("Pop pointer error");
                }
            } else {
                throw new Exception("Pop error");
            }
            break;

        default:
            throw new Exception("Push/Pop error");
        }
    }

    /**
     * Closes the output file.
     */
    public void close() {
        try {
            fw.close();
        } catch (Exception e) {
        }
    }

    /**
     * @return next label.
     */
    private int getLabel() {
        return label++;
    }

    /**
     * Writes to the output file the assembly code that implements the given logical
     * command.
     */
    private void writeLogical(String command) throws Exception {
        int label1 = getLabel();
        int label2 = getLabel();
        fw.write("@SP\n" + "AM=M-1\n" + "D=M\n" + "A=A-1\n" + "D=M-D\n" + "@L" + label1 + "\n" + "D;" + command + "\n"
                + "@SP\n" + "A=M-1\n" + "M=0\n" + "@L" + label2 + "\n" + "0;JMP\n" + "(L" + label1 + ")\n" + "@SP\n"
                + "A=M-1\n" + "M=-1\n" + "(L" + label2 + ")\n");
    }

    /**
     * Writes to the output file the assembly code that implements the given push
     * command.
     * 
     * @param segment stack segment.
     * @param index   location in stack segment.
     */
    private void writePush(String segment, int index) throws Exception {
        fw.write("@" + index + "\n" + "D=A\n" + "@" + segment + "\n" + "A=D+M\n" + "D=M\n" + "@SP\n" + "A=M\n" + "M=D\n"
                + "@SP\n" + "M=M+1\n");
    }

    /**
     * Writes to the output file the assembly code that implements the given pop
     * command.
     * 
     * @param segment stack segment.
     * @param index   location in stack segment.
     */
    private void writePop(String segment, int index) throws Exception {
        fw.write("@" + index + "\n" + "D=A\n" + "@" + segment + "\n" + "D=D+M\n" + "@R13\n" + "M=D\n" + "@SP\n"
                + "AM=M-1\n" + "D=M\n" + "@R13\n" + "A=M\n" + "M=D\n");
    }
}