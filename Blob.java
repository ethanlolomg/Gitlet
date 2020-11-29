package gitlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/** The blob.
 *  @author Ethan Chang*/
public class Blob implements Dumpable {
    /**In blobs, different than staging the file name = blob id,
     * the blob content = serialized blob object. */
    public static final File DIR = Utils.join(
            System.getProperty("user.dir"), ".gitlet", "blobs");

    /** filename.*/
    private String fileName;

    /** setter for C. */
    public void setContent(String c) {
        this.content = c;
    }

    /**content.*/
    private String content;

    /** constructor for blob take FNAME.*/
    public Blob(String fname) {
        this.fileName = fname;
        File f = new File(Commands.CWD + "/" + fname);
        if (f.exists()) {
            this.content = Utils.readContentsAsString(f);
        } else {
            this.content = "";
        }
    }

    /** Return id. */
    public String id() {
        return Utils.sha1(fileName, content);
    }

    /** Return blob from PATH and FILENAME.*/
    public static Blob readFromStage(String path, String fileName) {
        Blob d;
        File inFile = Utils.join(path, fileName);
        try {
            ObjectInputStream inp =
                    new ObjectInputStream(new FileInputStream(inFile));
            d = (Blob) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            d = null;
        }
        return d;
    }

    /**Return name of file. */
    public String getName() {
        return this.fileName;
    }

    /** Return content. */
    public String getContent() {
        return this.content;
    }

    /**serialize this blob. */
    public void serializeToBlobs() {
        File f = Utils.join(DIR, id());
        Utils.writeObject(f, this);
    }

    /**serialize this blob to add. */
    public void serializeToStageAdd() {
        File f = Utils.join(Commit.STAGING_ADD, fileName);
        Utils.writeObject(f, this);
    }

    /**serialize this blob to remove. */
    public void serializeToStageRm() {
        File f = Utils.join(Commit.STAGING_RM, fileName);
        Utils.writeObject(f, this);
    }

    @Override
    public void dump() {
        System.out.println("blob");
        System.out.println("ID: [" + id() + "]");
        System.out.println("fileName: [" + fileName + "]");
    }

}
