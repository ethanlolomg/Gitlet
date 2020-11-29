package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author EthanChang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  java gitlet.Main add text.txt
     *  java.gitlet.Main commit "message" */
    public static void main(String... args) {
        if (args.length < 1) {
            System.err.println("Please enter a command");
            return;
        }
        if (args[0].equals("add")) {
            if (!correctNumArgs(args.length, 2)) {
                return;
            }
            Commands.add(args[1]);
            return;
        } else if (args[0].equals("init")) {
            if (!correctNumArgs(args.length, 1)) {
                return;
            }
            Commands.init();
            return;
        } else if (args[0].equals("commit")) {
            if (!correctNumArgs(args.length, 2)) {
                return;
            }
            Commands.commit(args[1]);
            return;
        } else if (args[0].equals("rm")) {
            if (!correctNumArgs(args.length, 2)) {
                return;
            }
            Commands.remove(args[1]);
            return;
        } else if (args[0].equals("checkout")) {
            String fileName = null;
            String commitId = null;
            String branchName = null;
            if (args[1].equals("--") && args.length == 3) {
                fileName = args[2];
                Commands.checkout(fileName, null, null);
            } else if (!args[1].equals("--") && args.length == 2) {
                branchName = args[1];
                Commands.checkout(null, null, branchName);
            } else if (args[2].equals("--") && args.length == 4) {
                commitId = args[1];
                fileName = args[3];
                Commands.checkout(fileName, commitId, null);
            } else {
                System.out.println("Incorrect operands");
            }
            return;
        } else if (args[0].equals("global-log")) {
            if (!correctNumArgs(args.length, 1)) {
                return;
            }
            Commands.globalLog();
            return;
        } else {
            main2(args);
        }
    }

    /**Main version two take ARGS.*/
    public static void main2(String[] args) {
        if (args[0].equals("find")) {
            if (!correctNumArgs(args.length, 2)) {
                return;
            }
            String commitMsg = "";
            commitMsg += args[1];
            Commands.find(commitMsg);
            return;
        } else if (args[0].equals("status")) {
            if (!correctNumArgs(args.length, 1)) {
                return;
            }
            Commands.status();
            return;
        } else if (args[0].equals("log")) {
            if (!correctNumArgs(args.length, 1)) {
                return;
            }
            Commands.log();
            return;
        } else if (args[0].equals("branch")) {
            if (!correctNumArgs(args.length, 2)) {
                return;
            }
            String bName = args[1];
            Commands.branch(bName);
            return;
        } else if (args[0].equals("rm-branch")) {
            if (!correctNumArgs(args.length, 2)) {
                return;
            }
            String bName = args[1];
            Commands.removeBranch(bName);
            return;
        } else if (args[0].equals("reset")) {
            if (!correctNumArgs(args.length, 2)) {
                return;
            }
            String commitId = args[1];
            Commands.reset(commitId);
            return;
        } else {
            main3(args);
        }
    }
    /**Main version three take ARGS.*/
    public static void main3(String[] args) {
        if (args[0].equals("merge")) {
            if (!correctNumArgs(args.length, 2)) {
                return;
            }
            String bName = args[1];
            Commands.merge(bName);
            return;
        } else if (args[0].equals("add-remote")) {
            if (!correctNumArgs(args.length, 3)) {
                return;
            }
            RemoteCommands.addRemote(args[1], args[2]);
            return;
        } else if (args[0].equals("rm-remote")) {
            if (!correctNumArgs(args.length, 2)) {
                return;
            }
            RemoteCommands.rmRemote(args[1]);
            return;
        } else if (args[0].equals("push")) {
            if (!correctNumArgs(args.length, 3)) {
                return;
            }
            RemoteCommands.push(args[1], args[2]);
            return;
        } else if (args[0].equals("fetch")) {
            if (!correctNumArgs(args.length, 3)) {
                return;
            }
            RemoteCommands.fetch(args[1], args[2]);
            return;
        } else if (args[0].equals("pull")) {
            if (!correctNumArgs(args.length, 3)) {
                return;
            }
            RemoteCommands.pull(args[1], args[2]);
            return;
        } else {
            System.out.println("No command with that name exists");
            return;
        }
    }

    /**take ACTUAL adn REQUIRED to return boolean.*/
    public static boolean correctNumArgs(int actual, int required) {
        if (required == actual) {
            return true;
        } else {
            System.err.println("Incorrect operands");
            return false;
        }
    }

}
