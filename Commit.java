package gitlet;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Date;


/** The commit.
 *  @author Ethan Chang*/
public class Commit implements Dumpable {

    /**The Dir for commit. */
    public static final File DIR = Utils.join(
            System.getProperty("user.dir"), ".gitlet", "commits");
    /**The Dir for commit add stage. */
    public static final File STAGING_ADD = Utils.join(
            System.getProperty("user.dir"), ".gitlet", "staging", "add");
    /** In staging add, file name = changed file name
     *  (not hash), content = serialized blob object. */
    public static final File STAGING_RM = Utils.join(
            System.getProperty("user.dir"), ".gitlet", "staging", "remove");

    /**Return Message.*/
    public String getMessage() {
        return message;
    }

    /**message. */
    private String message;

    /** Return all files. */
    public HashMap<String, String> getFiles() {
        return files;
    }
    /** files setter take SETFILES.*/
    public void setFiles(HashMap<String, String> setFiles) {
        this.files = setFiles;
    }

    /**File name map to blob id.*/
    private HashMap<String, String> files;

    /** Return parentId. */
    public String getParentId() {
        return parentId;
    }

    /**Return parent. */
    public Commit getParent() {
        if (parentId == null) {
            return null;
        } else {
            return Commit.readFrom(Commit.DIR.toString(), parentId);
        }
    }

    /** parentId.*/
    private String parentId;

    /** Return Merge parent. */
    public Commit getMergeParent() {
        if (mergeParentId == null) {
            return null;
        } else {
            return Commit.readFrom(Commit.DIR.toString(), mergeParentId);
        }
    }

    /**Return merge Parent Id. */
    public String getMergeParentId() {
        return mergeParentId;
    }

    /**use MERGEPARENT to set. */
    public void setMergeParent(Commit mergeParent) {
        this.mergeParentId = mergeParent.id();
    }

    /** Merge Parent id. */
    private String mergeParentId;

    /**Return time stamp. */
    public String getTimestamp() {
        return timestamp;
    }

    /**Time stamp. */
    private String timestamp;

    /** Commit constructor take MES and PID. */
    public Commit(String mes, String pId) {
        this.message = mes;
        this.parentId = pId;
        if (this.parentId == null) {
            this.timestamp = "Thu Jan 1 00:00:00 1970 -0800";
        } else {
            this.timestamp = new SimpleDateFormat("E MMM d HH:mm:ss yyyy").
                    format(new Date());
            this.timestamp += " -0800";
        }
        this.files = new HashMap<>();
    }

    /**Return the commit read from PATH and FILENAME. */
    public static Commit readFrom(String path, String fileName) {
        Commit d;
        File inFile = Utils.join(path, fileName);
        try {
            ObjectInputStream inp =
                    new ObjectInputStream(new FileInputStream(inFile));
            d = (Commit) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            d = null;
        }
        return d;
    }

    /**serialize this. */
    public void serialize() {
        File f = Utils.join(DIR, id());
        Utils.writeObject(f, this);
    }

    /**Return id. */
    public String id() {
        if (parentId == null) {
            parentId = "null";
        }
        return Utils.sha1(message, timestamp, parentId);
    }

    /** Return latest ancestor from this and OTHER. */
    public Commit getLatestAncestor(Commit other) {
        ArrayList<String> thisParents = getAncestors();
        ArrayList<String> otherParents = other.getAncestors();
        if (otherParents.contains(this.id())) {
            throw Utils.error("Given branch is an "
                    + "ancestor of the current branch.");
        } else if (thisParents.contains(other.id())) {
            return null;
        }  else {
            for (String t: thisParents) {
                if (otherParents.contains(t)) {
                    return Commit.readFrom(Commit.DIR.toString(), t);
                }
            }
        }
        return null;
    }

    /**Return the ancestors. */
    public ArrayList<String> getAncestors() {
        Queue<Commit> frontier = new LinkedList<>();
        ArrayList<String> tParents = new ArrayList<>();
        frontier.add(this);
        while (!frontier.isEmpty()) {
            Commit curr = frontier.poll();
            if (!tParents.contains(curr.id())) {
                tParents.add(curr.id());
                Commit m = curr.getMergeParent();
                Commit p = curr.getParent();
                if (m != null) {
                    frontier.add(m);
                }
                if (p != null) {
                    frontier.add(p);
                }
            }
        }
        return tParents;
    }

    /**Return whether the OTHER and FILENAME is modified. */
    public boolean isModified(Commit other, String filename) {
        if (other.isTracked(filename) && this.isTracked(filename)) {
            return !this.files.get(filename).equals(
                    other.getFiles().get(filename));
        } else if (!other.isTracked(filename)
                && !this.isTracked(filename)) {
            return false;
        }
        return true;
    }

    /**Return string of commit. */
    public String toString() {
        String out = message + "\n";
        for (String f: this.files.keySet()) {
            out += f + "\n";
        }
        return out;
    }

    /**Return whether FILENAME is tracked. */
    public boolean isTracked(String filename) {
        return this.files.containsKey(filename);
    }

    @Override
    public void dump() {
        System.out.println("commit");
        System.out.println("ID: [" + id() + "]");
        System.out.println("message: [" + message + "]");
        System.out.println("timestamp: [" + timestamp + "]");
        for (String file: files.keySet()) {
            System.out.println("file: [" + file + "] "
                    + "[" + files.get(file) + "]");
        }
    }
}
