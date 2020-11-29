package gitlet;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/** The commands.
 *  @author Ethan Chang*/
public class Commands {

    /**cwd. */
    public static final File CWD = new File(
            System.getProperty("user.dir"));

    /**gitletFiles. */
    public static final File GITFILE = new
            File(CWD + "/.gitlet");

    /**init. */
    public static void init() {
        if (GITFILE.exists()) {
            System.err.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
            return;
        }
        File repoDir = Utils.join(CWD, ".gitlet");
        repoDir.mkdir();
        Commit.DIR.mkdir();
        Commit.STAGING_ADD.mkdirs();
        Commit.STAGING_RM.mkdir();
        Blob.DIR.mkdir();
        Branch.DIR.mkdir();
        ActiveBranch.AD.mkdir();
        Commit initial = new Commit("initial commit", null);
        Branch masterBranch = new Branch("master", initial.id());
        initial.serialize();
        masterBranch.serialize();
        masterBranch.serializeToActive();
    }

    /**delete the remove stage if exist
     *  adn return without staging
     * if file has already been added => replace it or do nothing
     * if the file has not been add, stage it
     *             but add a file named FILENAME,
     *             and content/object to be addBlob. */
    public static void add(String fileName) {
        File file = new File(CWD + "/" + fileName);
        if (!file.exists()) {
            System.err.println("File does not exist.");
            return;
        }
        Blob addBlob = new Blob(fileName);
        File addBlobPath = new File(Commit.STAGING_ADD + "/" + fileName);
        String content = Utils.readContentsAsString(file);
        addBlob.setContent(content);
        addBlob.serializeToBlobs();

        File rmBlobPath = new File(Commit.STAGING_RM + "/" + fileName);
        if (rmBlobPath.exists()) {
            rmBlobPath.delete();
            return;
        }
        Commit head = Branch.readFrom(
                ActiveBranch.AD.toString(), "activeBranch").getHead();
        if (head.getFiles().containsKey(fileName)) {
            String oldBlobID = head.getFiles().get(fileName);
            String oldContent = Blob.readFromStage(Blob.DIR.toString(),
                    oldBlobID).getContent();
            if (oldContent.equals(content)) {
                return;
            }
        }
        if (addBlobPath.exists()) {
            addBlobPath.delete();
            addBlob.serializeToStageAdd();
        } else {
            addBlob.serializeToStageAdd();
        }
    }

    /** return all the files in staging add as a hashMap<filename, blobId>
     * read from staging add, finding the
     * id of blob from the name of the file. */
    public static HashMap<String, String> stageAddFileHM() {
        List<String> listFs = Utils.plainFilenamesIn(Commit.STAGING_ADD);
        HashMap<String, String> allAddFiles = new HashMap<>();
        if (!listFs.isEmpty()) {
            for (String f : listFs) {
                String bID = Blob.readFromStage(Commit.STAGING_ADD.toString(),
                        f).id();
                allAddFiles.put(f, bID);
            }
        }
        return allAddFiles;
    }

    /** clean the DIR. */
    public static void cleanDir(String dir) {
        List<String> listFiles = Utils.plainFilenamesIn(dir);
        File file;
        for (String f: listFiles) {
            file = new File(dir + "/" + f);
            file.delete();
        }
    }

    /** return all the files in staging remove
     * as a hashMap<filename, blobId>. */
    public static HashMap<String, String> stageRemoveFileHM() {
        List<String> listFs = Utils.plainFilenamesIn(Commit.STAGING_RM);
        HashMap<String, String> allRemoveFiles = new HashMap<>();
        String blobID;
        if (!listFs.isEmpty()) {
            for (String f : listFs) {
                blobID = Blob.readFromStage(
                        Commit.STAGING_RM.toString(), f).id();
                allRemoveFiles.put(f, blobID);
            }
        }
        return allRemoveFiles;
    }

    /** update according to current, commit, update according to stage-add,
     * update according to stage-rm, create a new
     * commit with the same files, take MESSAGE. */
    public static void commit(String message) {
        if (message.length() < 1) {
            System.out.println("Please enter a commit message.");
            return;
        }
        Branch activeBranch = Branch.readFrom(
                ActiveBranch.AD.toString(), "activeBranch");
        Commit currhead = activeBranch.getHead();
        Commit newCommit = new Commit(message, currhead.id());
        HashMap<String, String> newFiles = new HashMap<>();
        HashMap<String, String> headFiles = currhead.getFiles();
        HashMap<String, String> allStageAddFiles = stageAddFileHM();
        HashMap<String, String> allStageRmFiles = stageRemoveFileHM();
        for (String key: headFiles.keySet()) {
            newFiles.put(key, currhead.getFiles().get(key));
        }
        for (String key: allStageAddFiles.keySet()) {
            newFiles.put(key, allStageAddFiles.get(key));
        }
        for (String key: allStageRmFiles.keySet()) {
            if (newFiles.containsKey(key)) {
                newFiles.remove(key);
            }
        }
        cleanDir(Commit.STAGING_ADD.toString());
        cleanDir(Commit.STAGING_RM.toString());
        if (newFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        newCommit.setFiles(newFiles);
        newCommit.serialize();
        activeBranch.setHead(newCommit.id());
        activeBranch.serializeToActive();
        activeBranch.serialize();
    }

    /** remove from add stage regardless take FILENAME.*/
    public static void remove(String fileName) {
        File commitPath = new File(Commit.STAGING_ADD + "/" + fileName);
        File cwdFile = new File(CWD + "/" + fileName);
        Branch activeBranch = Branch.readFrom(
                ActiveBranch.AD.toString(), "activeBranch");
        if (!activeBranch.getHead().getFiles().containsKey(fileName)
                && !commitPath.exists()) {
            System.err.println("No reason to remove the file.");
            return;
        }
        if (commitPath.exists()) {
            commitPath.delete();
            return;
        }
        if (activeBranch.getHead().getFiles().containsKey(fileName)) {
            if (cwdFile.exists()) {
                cwdFile.delete();
            }
            Blob removeBlob = new Blob(fileName);
            removeBlob.serializeToStageRm();
        }
    }

    /**log. */
    public static void log() {
        Branch activeBranch = Branch.readFrom(
                ActiveBranch.AD.toString(), "activeBranch");
        Commit curr = activeBranch.getHead();
        boolean lumberjack = true;
        while (lumberjack) {
            System.out.println("===");
            System.out.println("commit " + curr.id());
            if (curr.getMergeParentId() != null) {
                System.out.println("Merge: "
                        + curr.getParentId().substring(0, 7)
                        + " " + curr.getMergeParentId().substring(0, 7));
            }
            System.out.println("Date: " + curr.getTimestamp());
            System.out.println(curr.getMessage());
            System.out.println("");
            if (curr.getTimestamp().equals("Thu Jan 1 00:00:00 1970 -0800")) {
                lumberjack = false;
            } else {
                curr = Commit.readFrom(Commit.DIR.toString(),
                        curr.getParentId());
            }
        }
    }

    /** read commit from all commit dir. */
    public static void globalLog() {
        List<String> listFs = Utils.plainFilenamesIn(Commit.DIR);
        if (!listFs.isEmpty()) {
            for (String f : listFs) {

                Commit hCommit = Commit.readFrom(Commit.DIR.toString(), f);
                System.out.println("===");
                System.out.println("commit " + hCommit.id());
                System.out.println("Date: " + hCommit.getTimestamp());
                System.out.println(hCommit.getMessage());
                System.out.println("");
            }
        }
    }

    /** read commit from all commit dir take COMMITMESSAGE. */
    public static void find(String commitMessage) {
        boolean foundOne = false;
        List<String> listFs = Utils.plainFilenamesIn(Commit.DIR);
        if (!listFs.isEmpty()) {
            for (String f : listFs) {

                Commit hCommit = Commit.readFrom(Commit.DIR.toString(), f);
                if (hCommit.getMessage().equals(commitMessage)) {
                    System.out.println(hCommit.id());
                    foundOne = true;
                }
            }
        }
        if (!foundOne) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**Staged files, used in printing out non staged files. */
    public static void status() {
        if (!GITFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        File[] listBranches = Branch.DIR.listFiles();
        Arrays.sort(listBranches);
        File[] listStageAdd = Commit.STAGING_ADD.listFiles();
        Arrays.sort(listStageAdd);
        File[] listStageRm = Commit.STAGING_RM.listFiles();
        Arrays.sort(listStageRm);
        Branch activeBranch = Branch.readFrom(
                ActiveBranch.AD.toString(), "activeBranch");
        String abName = null;
        if (activeBranch != null) {
            abName = activeBranch.getName();
        }
        ArrayList<String> stagedAddFs = new ArrayList<>();
        ArrayList<String> stagedRmFs = new ArrayList<>();
        System.out.println("=== Branches ===");
        if (listBranches != null) {
            String bName;
            for (File b : listBranches) {
                bName = b.getName();
                if (bName.equals(abName)) {
                    bName = "*" + bName;
                }
                System.out.println(bName);
            }
        } else {
            System.out.println("*master");
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        if (listBranches != null) {
            for (File sa : listStageAdd) {
                System.out.println(sa.getName());
                stagedAddFs.add(sa.getName());
            }
        }
        System.out.println("");
        System.out.println("=== Removed Files ===");
        if (listBranches != null) {
            for (File sd : listStageRm) {
                System.out.println(sd.getName());
                stagedRmFs.add(sd.getName());
            }
        }
        int counter = 0;
        int k = 0;
        counter ++
        for (int i = 0; k > i; i++) {

        }
        System.out.println("");
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println("");
        System.out.println("=== Untracked Files ===");
        if (!allUntracked().isEmpty()) {
            for (String f: allUntracked()) {
                System.out.println(f);
            }
        }
        System.out.println("");
    }

    /**return all untracks. */
    public static Set<String> allUntracked() {
        List<String> cwdFs = Utils.plainFilenamesIn(CWD);
        Set<String> untrackedFs = new HashSet<>();
        Set<String> headCommitKeys =
                Branch.readFrom(ActiveBranch.AD.toString(),
                "activeBranch").getHead().getFiles().keySet();
        HashSet<String> trackedFiles = new HashSet<>();
        for (File f : Commit.STAGING_ADD.listFiles()) {
            trackedFiles.add(f.getName());
        }
        for (String fileName: headCommitKeys) {
            trackedFiles.add(fileName);
        }
        for (String f: cwdFs) {
            if (!trackedFiles.contains(f)) {
                untrackedFs.add(f);
            }
        }
        return untrackedFs;
    }

    /**Update file with CURR and FILENAME. */
    public static void updateFile(Commit curr, String fileName) {
        String blobId = curr.getFiles().get(fileName);
        Blob readBlob =
                Blob.readFromStage(Blob.DIR.toString(), blobId);
        String content = readBlob.getContent();
        File oldFile = new File(CWD + "/" + fileName);
        Utils.writeContents(oldFile, content);
    }

    /**Return abrev-file name with COMMITID. */
    public static String abreNameFile(String commitId) {
        List<String> listFiles =
                Utils.plainFilenamesIn(Commit.DIR);
        for (String f: listFiles) {
            if (f.substring(0, 8).equals(commitId)) {
                return f;
            }
        }
        return null;
    }

    /**Case 1: just file name Case 2: commit
     * id and file name Case3: branch name take FILENAME, COMMITID,
     * and BRANCHNAME. */
    public static void checkout(String fileName,
                                String commitId, String branchName) {
        Branch activeBranch = Branch.readFrom(
                ActiveBranch.AD.toString(), "activeBranch");
        Commit head = activeBranch.getHead();
        if (fileName != null && commitId == null && branchName == null) {
            if (!head.getFiles().containsKey(fileName)) {
                System.err.println("File does not exist in that commit.");
                return;
            }
            updateFile(head, fileName);
        } else if (fileName != null && commitId != null && branchName == null) {
            if (commitId.length() == 8) {
                commitId = abreNameFile(commitId);
            }
            File commitFile = new File(Commit.DIR + "/" + commitId);
            if (!commitFile.exists()) {
                System.err.println("No commit with that id exists.");
                return;
            }
            Commit curr = Commit.readFrom(Commit.DIR.toString(), commitId);
            if (!curr.getFiles().containsKey(fileName)) {
                System.err.println("File does not exist in that commit.");
                return;
            }
            updateFile(curr, fileName);
        } else if (fileName == null && commitId == null && branchName != null) {
            File branchFile = new File(Branch.DIR + "/" + branchName);
            if (!branchFile.exists()) {
                System.err.println("No such branch exists.");
                return;
            }
            List<String> cListFiles = Utils.plainFilenamesIn(CWD);
            Branch givenb = Branch.readFrom(Branch.DIR.toString(), branchName);
            Commit headGivenb = givenb.getHead();
            Set<String> bListFiles = headGivenb.getFiles().keySet();
            if (givenb.getName().equals(activeBranch.getName())) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            Set<String> untracks = allUntracked();
            for (String f: untracks) {
                if (bListFiles.contains(f)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    return;
                }
            }
            for (String f: bListFiles) {
                updateFile(headGivenb, f);
            }
            checkoutHelper(cListFiles, bListFiles);
            File currB = new File(ActiveBranch.AD.toString(),
                    "activeBranch");
            currB.delete();
            givenb.serializeToActive();
        }
    }

    /** checkout helper that take CLISTFILES and BLISTFILES. */
    public static void checkoutHelper(List<String> cListFiles,
                                      Set<String> bListFiles) {
        File dFile;
        for (String f: cListFiles) {
            if (!bListFiles.contains(f)) {
                dFile = new File(CWD + "/" + f);
                dFile.delete();
            }
        }
    }

    /** update a new branch with BRANCHNAME. */
    public static void branch(String branchName) {
        String headId = Branch.readFrom(
                ActiveBranch.AD.toString(), "activeBranch").getHead().id();
        File[] listBranches = Branch.DIR.listFiles();
        for (File b: listBranches) {
            if (b.getName().equals(branchName)) {
                System.err.println("A branch with that name already exists.");
                return;
            }
        }
        Branch newBranch = new Branch(branchName, headId);
        newBranch.serialize();
    }

    /**remove the branch with BRANCHNAME. */
    public static void removeBranch(String branchName) {
        List<String> listBs = Utils.plainFilenamesIn(Branch.DIR);
        String abName = Branch.readFrom(
                ActiveBranch.AD.toString(), "activeBranch").getName();
        if (!listBs.contains(branchName)) {
            System.err.println("A branch with that name does not exist.");
            return;
        }
        if (abName.equals(branchName)) {
            System.err.println("Cannot remove the current branch.");
            return;
        }
        File rmBranch = new File(Branch.DIR, branchName);
        rmBranch.delete();
    }

    /** Checks out all the files tracked by the given commit take COMMITID. */
    public static void reset(String commitId) {
        File commit = new File(Commit.DIR.toString(), commitId);
        if (!commit.exists()) {
            System.err.println("No commit with that id exists.");
            return;
        }
        Branch activeBranch = Branch.readFrom(
                ActiveBranch.AD.toString(), "activeBranch");
        Commit headAB = activeBranch.getHead();
        Set<String> currTracks = headAB.getFiles().keySet();
        Commit givenCom = Commit.readFrom(Commit.DIR.toString(), commitId);
        Set<String> files = givenCom.getFiles().keySet();
        Set<String> untracks = allUntracked();
        for (String f: untracks) {
            if (files.contains(f)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }
        for (String name: files) {
            checkout(name, commitId, null);
        }
        File delFs;
        for (String f: currTracks) {
            if (!files.contains(f)) {
                delFs = new File(CWD + "/" + f);
                if (delFs.exists()) {
                    delFs.delete();
                }
            }
        }
        cleanDir(Commit.STAGING_ADD.toString());
        cleanDir(Commit.STAGING_RM.toString());
        activeBranch.setHead(commitId);
        activeBranch.serialize();
        activeBranch.serializeToActive();
    }

    /** mod given, !mod curr
     * mod curr, !mod given
     * !present split, !present given, present curr
     * !present at split, present at given, !present at curr
     * present at split; !mod at curr; !present at given
     * present at split, !mod at given, !tracked at curr
     * mod curr, mod given, !mod curr given
     * take BRANCHNAME.*/
    public static void merge(String branchName) {
        if (Utils.plainFilenamesIn(Commit.STAGING_ADD).size() > 0
                || Utils.plainFilenamesIn(Commit.STAGING_RM).size() > 0) {
            System.err.println("You have uncommitted changes.");
            return;
        }
        Branch curr = Branch.readFrom(
                ActiveBranch.AD.toString(), "activeBranch");
        if (branchName.equals(curr.getName())) {
            System.err.println("Cannot merge a branch with itself.");
            return;
        }
        Branch mergeB = Branch.readFrom(Branch.DIR.toString(), branchName);
        if (mergeB == null) {
            System.err.println("A branch with that name does not exist.");
            return;
        }
        Commit givenCommit = mergeB.getHead();
        Commit currCommit = curr.getHead();
        Commit splitCom;
        try {
            splitCom = givenCommit.getLatestAncestor(currCommit);
            if (splitCom == null) {
                checkout(null, null, branchName);
                System.out.println("Current branch fast-forwarded.");
                return;
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            return;
        }
        Set<String> untrackedfiles = allUntracked();
        for (String fileName: untrackedfiles) {
            if (givenCommit.getFiles().containsKey(fileName)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }
        mergeHelper(givenCommit, splitCom, currCommit, branchName, curr);
    }

    /**helper for merge take, GC, SC, CC, BRANCHNAME, CURR. */
    public static void mergeHelper(Commit gc, Commit sc,
                                   Commit cc, String branchName, Branch curr) {
        Set<String> filesProcesses = new HashSet<>();
        filesProcesses.addAll(gc.getFiles().keySet());
        filesProcesses.addAll(cc.getFiles().keySet());
        filesProcesses.addAll(sc.getFiles().keySet());
        for (String f: filesProcesses) {
            if (gc.isTracked(f) && sc.isModified(gc, f)
                    && !sc.isModified(cc, f)) {
                checkout(f, gc.id(), null);
                add(f);
            } else if (!sc.isModified(gc, f) && sc.isModified(cc, f)) {
                continue;
            } else if (!sc.isTracked(f) && !gc.isTracked(f)
                    && cc.isTracked(f)) {
                continue;
            } else if (!sc.isTracked(f) && gc.isTracked(f)
                    && !cc.isTracked(f)) {
                checkout(f, gc.id(), null);
                add(f);
            } else if (sc.isTracked(f) && !sc.isModified(cc, f)
                    && !gc.isTracked(f)) {
                remove(f);
            } else if (sc.isTracked(f)
                    && !sc.isModified(gc, f)
                    && !cc.isTracked(f)) {
                continue;
            } else if (sc.isModified(gc, f) && sc.isModified(cc, f)) {
                if (!gc.isModified(cc, f)) {
                    continue;
                } else {
                    File cf = new File(Blob.DIR + "/" + cc.getFiles().get(f));
                    File gf = new File(Blob.DIR + "/" + gc.getFiles().get(f));
                    String currContent;
                    String givenContent;
                    if (cf.exists()) {
                        currContent = Blob.readFromStage(Blob.DIR.toString(),
                                cc.getFiles().get(f)).getContent();
                    } else {
                        currContent = "";
                    }
                    if (gf.exists()) {
                        givenContent = Blob.readFromStage(Blob.DIR.toString(),
                                gc.getFiles().get(f)).getContent();
                    } else {
                        givenContent = "";
                    }
                    System.out.println("Encountered a merge conflict.");
                    String content = "<<<<<<< HEAD\n" + currContent
                            + "=======\n" + givenContent + ">>>>>>>\n";
                    Utils.writeContents(Utils.join(f), content);
                    add(f);
                }
            }
        }
        commit("Merged " + branchName + " into " + curr.getName() + ".");
        Branch b2 = Branch.readFrom(ActiveBranch.AD.toString(), "activeBranch");
        Commit headActiveB2 = b2.getHead();
        headActiveB2.setMergeParent(gc);
    }
}

