1. Complie both the java files(Compile LogisticRegression.java before NaiveBayes.java).

2. Execute the NaiveBayes.java file. It has a main method that executes both Naive Bayes classification as well as Logistic Regression classification.

3. The 5 arguments you need to pass to the void static main method are:
	1.Path to the Spam Folder inlcuding the folder name For Training Examples
	2.Path to Ham folder inlcuding the folder name for training examples
	3.Path to the Spam folder inlcuding the folder name for Test Examples
	4.Path to the Ham folder inlcuding the folder namefor Test Examples.
	5.Path to the Stop Words file inlcuding the file name
Ex- Keep the unzipped dataset into the same folder as the NaiveBayes.java file. Keep stop words file in the same folder as the java program.

java NaiveBayes hw2/train/spam hw2/train/ham hw2/test/spam hw2/test/ham StopWords.txt
Generic Format:
java NaiveBayes <TRAIN_SPAM_FOLDER> <TRAIN_HAM_FOLDER> <TEST_SPAM_FOLDER> <TRAIN_HAM_FOLDER> <StopWords_File>

4. There is no need to specify the count of files anywhere in the program. It only needs the path to the folder having the files.

5. The program takes 3 minutes 44 seconds to generate accuracy for all the values of lamda I am checking it on.