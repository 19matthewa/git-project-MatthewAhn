public class GitInterfaceTester {
    public static void main(String[]args){
        tree treeThing = new tree();
        Commit commitThing = new Commit();
        treeThing.stage("root");
        commitThing.commitRoot("Matthew", "sos");
        TreeAndBlob reset = new TreeAndBlob();
        // reset.resetIndexAndObjects();
    }
}