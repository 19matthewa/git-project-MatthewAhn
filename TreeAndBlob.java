// import java.io.BufferedWriter;
// import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
//import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
//import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreeAndBlob {

     void traverse(String nextExpected, String indexContents, String testFolder){
        git test = new git();
        try{
            String treeContent = Files.readString(Paths.get("git"+File.separator+"objects"+File.separator+nextExpected));//lines are from here (is this right??)
            
            String[] lines = treeContent.split("\n");
            
            for (int i = 0; i < lines.length; i++){
                String[] lineContent = lines[i].split(" ");
                if (indexContents.contains(lines[i])){
                    System.out.print("Index Contains File/Folder: ");
                }
                else{
                    System.out.print("IIndex DOES NOT CONTAIN File/Folder: ");
                }
                System.out.println(lineContent[lineContent.length-1]);
                if (lineContent[0].equals("tree")){ 
                    traverse(lineContent[1], indexContents, testFolder); //traversability is enough to indicate correct hashes
                }
                else{
                    String[] linePath = lineContent[2].split("/");
                    Path pathToActualFile = findByFileName(Paths.get(testFolder), linePath[linePath.length-1]).get(0);
                    String correctHash = test.sha1HashCode(Files.readString(pathToActualFile)); //rely on Hash Function to work
                    if ((lineContent[1]).equals(correctHash)){
                        System.out.print("Correct Hash for the following: ");
                    }
                    else{
                        System.out.print("INCORRECT Hash for the following: ");
                    }
                    System.out.println(lineContent[1] + " " + lineContent[lineContent.length-1]);
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    public List<Path> findByFileName(Path path, String fileName)
            throws IOException {

        List<Path> result;
        try (Stream<Path> pathStream = Files.find(path,Integer.MAX_VALUE,(p, basicFileAttributes) ->p.getFileName().toString().equalsIgnoreCase(fileName))) {
            result = pathStream.collect(Collectors.toList());
        }
        return result;

    }

    public static void main(String[] args) {

        git test = new git();
        TreeAndBlob tester = new TreeAndBlob();
        tree tree = new tree();

        String testFolder = "root";

        test.initializeRepository();

        tester.resetIndexAndObjects();

        tree.generateAllTrees(Paths.get(testFolder));

        String indexContents = "";
        try{
            indexContents = Files.readString(Paths.get("git"+File.separator+"index"));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        try{
            String testFolderHash = "f08b4192afe09b0e63103e79b5c91c1895298dbf"; //must be correct for the test to run
            String treeContent = Files.readString(Paths.get("git"+File.separator+"objects"+File.separator+testFolderHash));

            if (indexContents.contains("tree " + testFolderHash + " " + testFolder)){
                System.out.println("Index Contains root folder: " + testFolder );
            }
            else{
                System.out.println("Index DOES NOT CONTAIN root folder: " + testFolder);
            }
            
            String[] lines = treeContent.split("\n");
            for (int i = 0; i < lines.length; i++){
                
                String[] lineContent = lines[i].split(" ");
                if (indexContents.contains(lines[i])){
                    System.out.print("Index Contains File/Folder: ");
                }
                else{
                    System.out.print("index DOES NOT CONTAIN File/Folder: ");
                }
                System.out.println(lineContent[2]);
                if (lineContent[0].equals("tree")){
                    tester.traverse(lineContent[1], indexContents, testFolder);//traverses if tree
                }
                else{
                    String[] linePath = lineContent[2].split("/");
                    Path pathToActualFile = tester.findByFileName(Paths.get(testFolder), linePath[linePath.length-1]).get(0);
                    String correctHash = test.sha1HashCode(Files.readString(pathToActualFile)); //rely on Hash Function to work
                    if ((lineContent[1]).equals(correctHash)){
                        System.out.print("Correct Hash for the following: ");
                    }
                    else{
                        System.out.print("INCORRECT Hash for the following: ");
                    }
                    System.out.println(lineContent[1] + " " + lineContent[lineContent.length-1]);
                }
            }

        }
        catch (IOException e){
            System.out.println("A file that is supposed to exist doesn't");
            e.printStackTrace();
        }

        tester.resetIndexAndObjects();

        //Old Tester Below

        // // write data, filname and correct hash
        // String data = "hello";
        // String filename = "testFile";
        // // make the file and run blob on it
        // String pathToDirectory = System.getProperty("user.dir") + File.separator + "git" + File.separator;
        // try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathToDirectory + filename))) {
        //     bufferedWriter.write(data);
        //     bufferedWriter.close();
        // } catch (Exception e) {
        //     System.out.print(e);
        // }

        // test.generateAllTrees(Paths.get("test"));

        // //test.blob(Paths.get("git/testFile"));
        // // checks if index and objects match what they should
        // try {
        //     FileReader file = new FileReader(pathToDirectory+ "index");
        //     BufferedReader reader = new BufferedReader(file);

        //     System.out.println("is " + reader.readLine());
        //     System.out.println("C: aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d testfile");

        //     FileReader fileTwo = new FileReader(pathToDirectory+"objects"+File.separator+"aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d");
        //     BufferedReader readerTwo = new BufferedReader(fileTwo);

        //     System.out.println("is " + readerTwo.readLine());
        //     System.out.println("C: hello");
        // } catch (Exception e) {
        //     System.out.println(e);
        // }
    }

    public void resetIndexAndObjects() {
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
        File objectsDir = new File("git" + File.separator + "objects");
        if (objectsDir.exists() && objectsDir.isDirectory()) {
            File[] files = objectsDir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.delete()) {
                        System.out.println("Deleted " + file + " object");
                    } else {
                        System.out.println("Failed to delete "+ file);
                    }
                }
            }
        }
}}
