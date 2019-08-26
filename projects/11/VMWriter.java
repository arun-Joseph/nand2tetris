import java.io.*;

/**
 * Generates VM code.
 */

public class VMWriter {
    private FileWriter fw;

    /**
     * Prepares the output file for writing.
     * 
     * @param outfile vm file.
     */
    public VMWriter(File outfile) {
        try {
            fw = new FileWriter(outfile);
        } catch (Exception e) {
            System.out.println("File not found");
        }
    }

    /**
     * Writes a push command.
     * 
     * @param segment
     * @param index
     */
    public void writePush(String segment, int index) throws Exception {
        fw.write("push " + segment + " " + index + "\n");
    }

    /**
     * Writes a pop command.
     * 
     * @param segment
     * @param index
     */
    public void writePop(String segment, int index) throws Exception {
        fw.write("pop " + segment + " " + index + "\n");
    }

    /**
     * Writes an arithmetic/logical command.
     * 
     * @param command
     */
    public void writeArithmetic(String command) throws Exception {
        fw.write(command + "\n");
    }

    /**
     * Writes a label command.
     * 
     * @param label
     */
    public void writeLabel(String label) throws Exception {
        fw.write("label " + label + "\n");
    }

    /**
     * Writes a goto command.
     * 
     * @param label
     */
    public void writeGoto(String label) throws Exception {
        fw.write("goto " + label + "\n");
    }

    /**
     * Writes an if-goto command.
     * 
     * @param label
     */
    public void writeIf(String label) throws Exception {
        fw.write("if-goto " + label + "\n");
    }

    /**
     * Writes a call command.
     * 
     * @param name
     * @param nArgs
     */
    public void writeCall(String name, int nArgs) throws Exception {
        fw.write("call " + name + " " + nArgs + "\n");
    }

    /**
     * Writes a function command.
     * 
     * @param name
     * @param nLocals
     */
    public void writeFunction(String name, int nLocals) throws Exception {
        fw.write("function " + name + " " + nLocals + "\n");
    }

    /**
     * Writes a return command.
     */
    public void writeReturn() throws Exception {
        fw.write("return\n");
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
}