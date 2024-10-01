// import java.io.BufferedWriter;
// import java.io.BufferedReader;
import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
//import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class initializeRepositoryTester {

    static void traverse(String nextExpected, String indexContents, String testFolder){
        try{
            String treeContent = Files.readString(Paths.get("git"+File.separator+"objects"+File.separator+nextExpected));
            
            String[] lines = treeContent.split("\n");
            
            for (int i = 0; i < lines.length; i++){
                String[] lineContent = lines[i].split(" ");
                if (indexContents.contains(lines[i])){
                    System.out.print("Index Contains File/Folder: ");
                }
                else{
                    System.out.print("Index DOES NOT CONTAIN File/Folder: ");
                }
                System.out.println(lineContent[2]);
                if (lineContent[0].equals("tree")){ 
                    traverse(lineContent[1], indexContents, testFolder); //traversability is enough to indicate correct hashes
                }
                else{
                    Path pathToActualFile = findByFileName(Paths.get(testFolder), lineContent[2]).get(0);
                    String correctHash = git.sha1HashCode(Files.readString(pathToActualFile)); //rely on Hash Function to work
                    if (lineContent[1].equals(correctHash)){
                        System.out.print("Correct Hash for the following: ");
                    }
                    else{
                        System.out.print("INCORRECT Hash for the following: ");
                    }
                    System.out.println(lineContent[1] + " " + lineContent[2]);
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    public static List<Path> findByFileName(Path path, String fileName)
            throws IOException {

        List<Path> result;
        try (Stream<Path> pathStream = Files.find(path,
                Integer.MAX_VALUE,
                (p, basicFileAttributes) ->
                        p.getFileName().toString().equalsIgnoreCase(fileName))
        ) {
            result = pathStream.collect(Collectors.toList());
        }
        return result;

    }

    public static void main(String[] args) {

        git test = new git();

        String testFolder = "testFolder";

        test.initializeRepository();

        test.deleteReopsitory();

        test.initializeRepository();

        test.generateAllTrees(Paths.get(testFolder));

        String indexContents = "";
        try{
            indexContents = Files.readString(Paths.get("git"+File.separator+"index"));
        }
        catch (IOException e){
            e.printStackTrace();
        }

        try{
            String testFolderHash = "4bd82b4983fdad505b2f1ef2a310ae64e4dbaa41"; //must be correct for the test to run
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
                    System.out.print("Index DOES NOT CONTAIN File/Folder: ");
                }
                System.out.println(lineContent[2]);
                if (lineContent[0].equals("tree")){
                    traverse(lineContent[1], indexContents, testFolder);
                }
                else{
                    Path pathToActualFile = findByFileName(Paths.get(testFolder), lineContent[2]).get(0);
                    String correctHash = git.sha1HashCode(Files.readString(pathToActualFile)); //rely on Hash Function to work
                    if (lineContent[1].equals(correctHash)){
                        System.out.print("Correct Hash for the following: ");
                    }
                    else{
                        System.out.print("INCORRECT Hash for the following: ");
                    }
                    System.out.println(lineContent[1] + " " + lineContent[2]);
                }
            }


        }
        catch (IOException e){
            System.out.println("A file that is supposed to exist doesn't");
            e.printStackTrace();
        }

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
}
