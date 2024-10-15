import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.io.FileWriter;

public class tree extends GitInterfaceTester{
    public void stage(String filePath){
        resetIndex();
        neededHashingFromRoot(Paths.get(filePath));
    }

 public void generateAllTrees(Path path) {
    git git = new git();
        try{
            String rootName = git.sha1HashCode(listAllFiles(path, null, ""));
            Files.write(Paths.get("git"+File.separator+"index"), ("tree " + rootName + " " + path.toFile().getPath() + "\n").getBytes(), StandardOpenOption.APPEND);
            
        }
        catch (IOException e){
            e.printStackTrace();
        }
        
        
    }
    public String neededHashingFromRoot(Path path) {
        String hash = "";
        git git = new git();
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
    private String listAllFiles(Path currentPath, List<Path> allFiles, String content) throws IOException 
    {
        git git = new git();
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
                    if (!inObjects(curTreeName)){

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
            if (!inObjects(hashedFinalTreeName)){
                Files.write(Paths.get("git"+File.separator+"objects"+File.separator+hashedFinalTreeName), content.getBytes());
                // Files.write(Paths.get("git"+File.separator+"index"), ("tree " + hashedFinalTreeName + " " + currentPath.toFile().getName() + "\n").getBytes(), StandardOpenOption.APPEND);
            }
            return content;
        }
    }   

    public Boolean inObjects(String hashCode) {
        return Files.exists(Paths.get("git" + File.separator + "objects" + File.separator + hashCode));
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
}

