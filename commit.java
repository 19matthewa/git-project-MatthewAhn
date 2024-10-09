import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

public class commit {
    public static String commitRoot(String author, String note) {
        resetIndex();
        String root = "root";
        String treeHash = tree.neededHashingFromRoot(Paths.get(root));

        String parent = "";
        File showParent = new File("git" + File.separator + "HEAD");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(showParent));
            try {
                parent = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        LocalDate date = LocalDate.now();

        String fileName = "newCommit";

        File commmitFile = new File(fileName);
        try {
            FileWriter clearing = new FileWriter(commmitFile, false);
            clearing.write("");
            clearing.close();

            FileWriter writer = new FileWriter(commmitFile, true);
            writer.write("tree: " + treeHash + "\n");
            writer.write("parent: " + parent + "\n");
            writer.write("author: " + author + "\n");
            writer.write("date: " + date + "\n");
            writer.write("method: " + note);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String rootContents = getContents(commmitFile);
        String newRootHashCode = git.sha1HashCode(rootContents);
        updateHead(newRootHashCode);

        return newRootHashCode;
    }

    public static void checkout(String commitHash){
        //update working directory to commit state
    }

    public static Boolean inObjects(String hashCode) {
        return Files.exists(Paths.get("git" + File.separator + "objects" + File.separator + hashCode));
    }

    public static void updateHead(String newRootHashCode) {
        File head = new File("git" + File.separator + "HEAD");
        FileWriter writer;
        try {
            writer = new FileWriter(head, false);
            writer.write(newRootHashCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void resetIndex() {
        File indexFile = new File("git" + File.separator + "index");
        if (indexFile.exists()) {
            try {
                FileWriter writer = new FileWriter(indexFile, false); // Overwrite mode
                writer.write("");
                writer.close();
                System.out.println("Index file reset to empty.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getContents(File commmitFile) {
        StringBuffer contents = new StringBuffer();
        try {
            FileReader contentReader = new FileReader(commmitFile);
            BufferedReader bufferReader = new BufferedReader(contentReader);

            char character = (char) bufferReader.read();

            while (character != (char) -1) {
                contents.append(character);
                character = (char) bufferReader.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contents.toString();
    }
}