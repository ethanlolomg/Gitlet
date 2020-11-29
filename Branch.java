package gitlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;



/** The branch.
 *  @author Ethan Chang*/
public class Branch implements Dumpable {

    /** dir for branch. */
    public static final File DIR =
            Utils.join(System.getProperty("user.dir"), ".gitlet", "branches");

    /** Return name. */
    public String getName() {
        return name;
    }

    /** name of branch. */
    private String name;

    /** Return head. */
    public Commit getHead() {
        Commit h = Commit.readFrom(Commit.DIR.toString(), this.head);
        return h;
    }

    /**set H.*/
    public void setHead(String h) {
        this.head = h;
    }

    /**the head of the branch. */
    private String head;

    /** the constructor of branch take N and H. */
    public Branch(String n, String h) {
        this.name = n;
        this.head = h;
    }

    /**serialize this branch. */
    public void serialize() {
        try {
            File f = Utils.join(DIR, id());
            ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(this);
            out.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**serialize this branch to active branch. */
    public void serializeToActive() {
        try {
            File[] currActBranch = ActiveBranch.AD.listFiles();
            for (File f: currActBranch) {
                f.delete();
            }
            File f = Utils.join(ActiveBranch.AD, "activeBranch");
            ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(this);
            out.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**Return the branch from PATH and FILENAME. */
    public static Branch readFrom(String path, String fileName) {
        Branch d;
        File inFile = Utils.join(path, fileName);
        try {
            ObjectInputStream inp =
                    new ObjectInputStream(new FileInputStream(inFile));
            d = (Branch) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            d = null;
        }
        return d;
    }

    /**Return the id. */
    public String id() {
        return name;
    }

    @Override
    public void dump() {
        System.out.println(name);
        System.out.println(head);
    }
}
