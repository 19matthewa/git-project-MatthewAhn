import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class tree {
 public static void generateAllTrees(Path path) {
        try{
            String rootName = git.sha1HashCode(listAllFiles(path, null, ""));
            Files.write(Paths.get("git"+File.separator+"index"), ("tree " + rootName + " " + path.toFile().getPath() + "\n").getBytes(), StandardOpenOption.APPEND);
            
        }
        catch (IOException e){
            e.printStackTrace();
        }
        
        
    }
    public static String neededHashingFromRoot(Path path) {
        String hash = "";
        try{
            String rootName = git.sha1HashCode(listAllFiles(path, null, ""));
            Files.write(Paths.get("git"+File.separator+"index"), ("tree " + rootName + " " + path.toFile().getPath() + "\n").getBytes(), StandardOpenOption.APPEND);
            hash = rootName;
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return hash;
        
    }

    //https://www.geeksforgeeks.org/list-all-files-from-a-directory-recursively-in-java/
    private static String listAllFiles(Path currentPath, List<Path> allFiles, String content) throws IOException 
    {
        int zip = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath)) 
        {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    File[] listOfFiles = entry.toFile().listFiles();
                    String curContent = "";
                    if(listOfFiles != null) {
                        for (int i = 0; i < listOfFiles.length; i++) {
                            if (listOfFiles[i].isFile()) {
                                git.blob(Paths.get(listOfFiles[i].getPath()),zip);
                                if (curContent.equals("")){
                                    curContent += "blob " + git.sha1HashCode(Files.readString(Paths.get(listOfFiles[i].getPath()))) + " " + listOfFiles[i].getPath();
                                }
                                 else{curContent += "\n"+ "blob " + git.sha1HashCode(Files.readString(Paths.get(listOfFiles[i].getPath()))) + " " + listOfFiles[i].getPath();}
                            }
                            else if (listOfFiles[i].isDirectory()){
                                String hashCode = git.sha1HashCode(listAllFiles(Paths.get(listOfFiles[i].getPath()), allFiles, ""));
                                if (curContent.equals("")){
                                    curContent += "tree " + hashCode + " " + listOfFiles[i].getPath();
                                }
                                else{
                                curContent += "\n" +"tree " + hashCode + " " + listOfFiles[i].getPath();
                            }
                                Files.write(Paths.get("git"+File.separator+"index"), ("tree " + hashCode + " " + listOfFiles[i].getPath() + "\n").getBytes(), StandardOpenOption.APPEND);
                            }
                        }
                    }
                    
                    String curTreeName = git.sha1HashCode(curContent);//NEED TO CHECK IF THIS HASH IS ALREADY IN OBJECTS, does below
                    if (!commit.inObjects(curTreeName)){

                        Files.write(Paths.get("git"+File.separator+"objects"+File.separator+curTreeName), curContent.getBytes());//puts cur content into objects (for sub trees of a tree [this has an extra linie])

                        if (content.equals("")){
                            content += "tree " + curTreeName + " " + entry.toFile().getPath() ;//adds the tree to contents
                        }
                        else{
                            content += "\n" + "tree " + curTreeName + " " + entry.toFile().getPath();//adds the tree to contents
                        }
                        Files.write(Paths.get("git"+File.separator+"index"), ("tree " + curTreeName + " " + entry.toFile().getPath() + "\n").getBytes(), StandardOpenOption.APPEND);//puts cur content into index
                    }
                    
                } else {
                    if (content.equals("")){
                        content += "blob " + git.sha1HashCode(Files.readString(entry)) + " " + entry.toFile().getPath();//adds the blob to contents
                    }
                    else{
                        content += "\n" + "blob " + git.sha1HashCode(Files.readString(entry)) + " " + entry.toFile().getPath();//adds the blob to contents
                    }
                    git.blob(entry,zip);
                }
                
            }

            String hashedFinalTreeName = git.sha1HashCode(content);//NEED TO CHECK IS THIS HASH IS ALAREADY IN OBJECTS//contents is what actually shows up in the files in objecta folder (fro final tree [this has an extra line])
            if (!commit.inObjects(hashedFinalTreeName)){
                Files.write(Paths.get("git"+File.separator+"objects"+File.separator+hashedFinalTreeName), content.getBytes());
                // Files.write(Paths.get("git"+File.separator+"index"), ("tree " + hashedFinalTreeName + " " + currentPath.toFile().getName() + "\n").getBytes(), StandardOpenOption.APPEND);
            }
            return content;
        }
    }   
}
