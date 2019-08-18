import java.io.*;

public class JackAnalyzer {

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

            File xmlfile = new File(jackfile.getName().substring(0, jackfile.getName().length() - 5) + ".xml");
            File tokenfile = new File(jackfile.getName().substring(0, jackfile.getName().length() - 5) + "T.xml");

            CompilationEngine cm = new CompilationEngine(jackfile, xmlfile, tokenfile);
            cm.close();
        }
    }
}