import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

public class Commit implements GitInterface{
    public void stage(String filePath){
        System.out.println("stage is in tree.java");
    }

    public String commit(String author, String message){
        return commitRoot(author,message);
    }

    public void checkout(String commitHash){
    }

    public String commitRoot(String author, String note) {
        //resetIndex();
        git git = new git();
        tree tree = new tree();

        String treeHash = tree.neededHashingFromRoot(Paths.get("root"));

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
            writer.write("message: " + note);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String rootContents = getContents(commmitFile);
        String newRootHashCode = git.sha1HashCode(rootContents);
    
        String thePath = "git"+File.separator+"objects"+File.separator+newRootHashCode;
        try {
            Files.write(Paths.get(thePath),rootContents.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateHead(newRootHashCode);

        return newRootHashCode;
    }


    public void updateHead(String newRootHashCode) {
        File head = new File("git" + File.separator + "HEAD");
        FileWriter writer;
        try {
            writer = new FileWriter(head, false);
            writer.write(newRootHashCode);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetIndex() {
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

    public String getContents(File commmitFile) {
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