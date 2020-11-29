package gitlet;

import java.io.File;

/** The active branch.
 *  @author Ethan Chang*/
public class ActiveBranch implements Dumpable {

    /** Active Branch dir.*/
    public static final File AD = Utils.join(
            System.getProperty("user.dir"), ".gitlet", "activeBranch");

    /** activeBranchID. */
    private String activeBranchId;

    /** constructors take BRANCH. */
    public ActiveBranch(String branch) {
        this.activeBranchId = branch;
    }

    @Override
    public void dump() {
        System.out.println(activeBranchId);
    }
}
