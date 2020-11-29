package gitlet;

import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Ethan Chang
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {
    }

    /**test for init.*/
    @Test
    public void testInit() {
        Main.main("init");
    }

    /**test for log.*/
    @Test
    public void testLog() {
        Main.main("init");
        Main.main("log");
    }

    /**test fro status.*/
    @Test
    public void testStatus() {
        Main.main("init");
        Main.main("status");
    }

}


