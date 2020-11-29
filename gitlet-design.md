# Gitlet Design Document

**Name**: Ethan Chang

## Classes and Data Structures

###Commit
It represent a commit node for a commit with the metadata of a commit.
####Fields
* String message - a string of message from the commit command.
* String timestamp - time of the commit was made.
* String parent - the previous commit of this commit.
* HashMap files - contains the files/blobs that are changed/ made <String, String> filename => id

###Commands
It contains all the command methods that are being used for gitlet.
The commands are being called in the Main.  

###Blob
####Fields
* String id - how this Blob would be refer found.
* String fileName - the name of the file that is being blobed.
* String content - the screenshot of the file that was added.

###CommitTree
It organize the branches and commit by putting them into a tree structure. 
####Fields
* String id - how this CommitTree would be found.
* HashMap branches - it contains the branch key and the head of a branch. <String, String> branchName to IDOfHEAD

## Algorithms
###Commit Class

###Commands Class
1. init - Initialize gitlet repo in the current directory. Create .gitlet (staging and commits). 
Commit 0 (with a head pointer and master branch)
2. add - Create new blob first for the file added or modified (only if it is different).
Put the blob into the staged for addition with the name of the file
3. commit - Cloning the head commit as the next commit (with new metadata). The new commit point to the parents and the staged files.
Advanced the head and master. After commit a file move the staged file to the log
4. rm - Similar to add, but save the put the blob in the remove staging area.
5. log - shows commits
6. global-log - shows global logs
7. find - find
8. status - it shows the following status of gitlet: branches, Staged file, Removed file, Modification not staged for commit, and untracked files.
9. checkout - allow the user to move the head to a previous commit.
10. branch - create a new branch off the current branch.
11. rm-branch - remove a branch that is given into the argument. If the given branch does not exist it would throw and error.
12. reset - reset the status of the state.
13. merge - combine two branches into one. You can only merge two branches together if they do not have a conflicting change, or it would throw an error.
###Blob Class
###CommitTree Class
## Persistence
### .gitlet
* logs - it contains the commitTree for this repository. The CommitTrees that are bring serialized 
contains the order of commit (id) and location of the head of each branch.
* commits - it contain the data for all the commits with their meta-data, with pointers to blobs. It is directory with a bunch of unsorted serialized commit objects.
*  blobs - it contains all serialized blobs instances, and can be located with their id.


