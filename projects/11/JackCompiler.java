import java.io.*;

public class JackCompiler {

    private static File[] files;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("File/Folder not specified");
            return;
        }

        if (args[0].endsWith(".jack")) {
            files = new File[] { new File(args[0]) };
        } else {
            files = new File(args[0]).listFiles();
        }

        for (File jackfile : files) {
            if (!jackfile.getName().endsWith(".jack")) {
                continue;
            }

            File vmfile = new File(
                    jackfile.getAbsolutePath().substring(0, jackfile.getAbsolutePath().length() - 5) + ".vm");
            CompilationEngine cm = new CompilationEngine(jackfile, vmfile);
            cm.close();
        }
    }
}