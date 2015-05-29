import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Saquib
 */
public class NaiveBayes {

    List<String> wordListForSpam = new ArrayList<>();
    List<String> wordListForHam = new ArrayList<>();
    List<Integer> wordCountForSpam = new ArrayList<>();
    List<Integer> wordCountForHam = new ArrayList<>();
    List<String> vocabularyList = new ArrayList<>();
    int numberOfSpam;
    int numberOfHam;
    boolean stopListEnable;
    List<String> stopWords;

    private static String SPAMFOLDERNAME;
    private static String HAMFOLDERNAME;
    private static String SPAMTEST;
    private static String HAMTEST;
    private static String STOPWORDS;

    public NaiveBayes(boolean stopListEnable) throws FileNotFoundException {
        wordListForSpam = new ArrayList<>();
        wordListForHam = new ArrayList<>();
        wordCountForSpam = new ArrayList<>();
        wordCountForHam = new ArrayList<>();
        vocabularyList = new ArrayList<>();
        this.numberOfHam = 0;
        this.numberOfSpam = 0;

        this.stopListEnable = stopListEnable;
        if (stopListEnable) {
            //Populate the stop words
            File stopWordsFile = new File(STOPWORDS);
            stopWords = readStopFile(stopWordsFile);
        }
    }

    /**
     * trains the Naive Bayes Algorithm on the training data generates the
     * different data member values
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void trainNaiveBayes() throws FileNotFoundException, IOException {

        float totalWordsinSpam = 0, totalWordsinHam = 0;

        //TRAINING THE ALGO ON SPAM RECORDS
        File folder = new File(SPAMFOLDERNAME);
        File[] listOfFiles = folder.listFiles();
        List<String> words;
        int i;

        this.numberOfSpam = listOfFiles.length;

        //CALCULATE ALL COUNTS FROM SPAM FILES
        for (File spamFile : listOfFiles) {
            //READ FROM SPAM FILE
            words = readFile(spamFile);
            //COMPUTE THE COUNTS FOR NAIVE BAYES CALCULATION
            for (String word : words) {
                if (word.length() == 0) {
                    continue;
                } else {
                    totalWordsinSpam = totalWordsinSpam + 1;
                }

                if (wordListForSpam.contains(word)) {
                    i = wordListForSpam.indexOf(word);
                    wordCountForSpam.set(i, wordCountForSpam.get(i) + 1);
                } else {
                    wordCountForSpam.add(1);
                    wordListForSpam.add(word);
                }
            }
        }

        vocabularyList.addAll(wordListForSpam);

        //READ ALL THE FILES FROM THE HAM Folder 
        //TODO:PUT THE URL FOR HAM FOLDER
        folder = new File(HAMFOLDERNAME);
        listOfFiles = folder.listFiles();

        this.numberOfHam = listOfFiles.length;

        //CALCULATE ALL COUNTS FROM HAM FILES
        for (File hamFile : listOfFiles) {
            //READ FROM HAM FILE
            words = readFile(hamFile);
            //COMPUTE THE COUNTS FOR NAIVE BAYES CALCULATION
            for (String word : words) {
                //TOTAL COUNT OF WORDS IN HAM FILES
                if (word.length() == 0) {
                    continue;
                } else {
                    totalWordsinHam = totalWordsinHam + 1;
                }

                //ADD THE WORD TO THE VOCABULARY LIST
                if (!vocabularyList.contains(word)) {
                    vocabularyList.add(word);
                }

                //UPDATE THE HAMCOUNT AND HAMWORDLIST
                if (wordListForHam.contains(word)) {
                    i = wordListForHam.indexOf(word);
                    wordCountForHam.set(i, wordCountForHam.get(i) + 1);
                } else {
                    wordCountForHam.add(1);
                    wordListForHam.add(word);
                }
            }
        }
    }

    /*
     Tests if the test file is SPAM or HAM. If it is SPAM, returns True. Else return False.
     */
    public boolean IsSpam(File testFile) throws FileNotFoundException {
        List<String> words = readFile(testFile);

        double valueSpam = 0;
        double valueHam = 0;
        int i;

        double countSpamWords = 0;
        double countHamWords = 0;

        for (Integer count : this.wordCountForSpam) {
            countSpamWords = countSpamWords + count;
        }

        //Check if the file is SPAM
        for (String eachWord : words) {
            if (this.wordListForSpam.contains(eachWord)) {
                i = this.wordListForSpam.indexOf(eachWord);
                valueSpam += Math.log(this.wordCountForSpam.get(i) + 1.0) - Math.log(countSpamWords + this.vocabularyList.size());
            } else {
                valueSpam += Math.log(1.0) - Math.log(countSpamWords + this.vocabularyList.size());
            }
        }

        for (Integer count : this.wordCountForHam) {
            countHamWords = countHamWords + count;
        }

        //Check if the file is HAM
        for (String eachWord : words) {
            if (this.wordListForHam.contains(eachWord)) {
                i = this.wordListForHam.indexOf(eachWord);
                valueHam += Math.log(this.wordCountForHam.get(i) + 1.0) - Math.log(countHamWords + this.vocabularyList.size());
            } else {
                valueHam += Math.log(1.0) - Math.log(countHamWords + this.vocabularyList.size());
            }
        }
        return valueSpam > valueHam;
    }

    /**
     * Read from the file specified and returns a list of all the individual
     * words in the document.
     *
     * @param spamFile
     * @return list of all valid words in the file
     * @throws FileNotFoundException
     */
    public List<String> readFile(File spamFile) throws FileNotFoundException {

        Scanner scanner = new Scanner(spamFile);
        List<String> words;
        words = new ArrayList<>();
        List<String> temp;

        while (scanner.hasNextLine()) {
            temp = Arrays.asList(scanner.nextLine().split(" "));
            for (String t : temp) {
                t = t.toLowerCase();
                if (!t.matches("[a-z']+") || (stopListEnable && stopWords.contains(t))) {
                    continue;
                } else {
                    words.add(t.toLowerCase());
                }
            }
        }

        return words;
    }

    public List<String> readStopFile(File spamFile) throws FileNotFoundException {

        Scanner scanner = new Scanner(spamFile);
        List<String> words;
        words = new ArrayList<>();
        String temp = new String();

        while (scanner.hasNextLine()) {
            temp = scanner.nextLine();
            words.add(temp.toLowerCase());
        }

        return words;
    }

    /**
     * Uses the isSpam method to find out the class of the text file in the test
     * folder(for HAM AND SPAM)
     *
     * @return accuracy over the spam and ham records returnValue[0] is over
     * Spam and returnValue[1] is over Ham
     * @throws FileNotFoundException
     */
    public double[] returnAccuracy() throws FileNotFoundException {
        File folder = new File(SPAMTEST);
        File[] listOfFiles = folder.listFiles();
        double percentage = 0;
        double[] returnValue = new double[2];

        //CALCULATE THE ACCURACY OF SPAM
        for (File spamFile : listOfFiles) {
            if (IsSpam(spamFile)) {
                percentage++;
            }
        }

        percentage = percentage * 100 / listOfFiles.length;
        returnValue[0] = percentage;

        //CALCULATE THE ACCURACY OF HAM
        percentage = 0;
        folder = new File(HAMTEST);
        listOfFiles = folder.listFiles();
        for (File hamFile : listOfFiles) {
            if (!IsSpam(hamFile)) {
                percentage++;
            }
        }

        percentage = percentage * 100 / listOfFiles.length;
        returnValue[1] = percentage;

        return returnValue;
    }

    public static void main(String[] args) throws IOException {

        
        //Set the folder names to the static variables
        NaiveBayes.SPAMFOLDERNAME = args[0];
        NaiveBayes.HAMFOLDERNAME = args[1];
        NaiveBayes.SPAMTEST = args[2];
        NaiveBayes.HAMTEST = args[3];
        NaiveBayes.STOPWORDS = args[4];
        
        LogisticRegression.SPAMFOLDERNAME = NaiveBayes.SPAMFOLDERNAME;
        LogisticRegression.HAMFOLDERNAME =  NaiveBayes.HAMFOLDERNAME;
        LogisticRegression.SPAMTEST = NaiveBayes.SPAMTEST;
        LogisticRegression.HAMTEST = NaiveBayes.HAMTEST;
        LogisticRegression.STOPWORDS = NaiveBayes.STOPWORDS;
        
        
        
        //NAIVE BAYES without StopList
        NaiveBayes NBWithoutStopList = new NaiveBayes(false);
        NBWithoutStopList.trainNaiveBayes();
        //CALCULATE THE ACCURACY OVER THE SPAM TEST RECORDS AND HAM TEST RECORDS
        double[] value = NBWithoutStopList.returnAccuracy();
        System.out.println("NAIVE BAYES WITH STOP WORDS INCLUDED:");
        System.out.println("THE ACCURACY OF NAIVE BAYES OVER SPAM RECORDS IS: " + value[0]);
        System.out.println("THE ACCURACY OF NAIVE BAYES OVER HAM RECORDS IS: " + value[1]);

        //Naive Bayes with stopList
        NaiveBayes NBWithStopList = new NaiveBayes(true);
        //TRAIN THE NAIVE BAYES
        NBWithStopList.trainNaiveBayes();
        //CALCULATE THE ACCURACY OVER THE SPAM TEST RECORDS AND HAM TEST RECORDS
        value = NBWithStopList.returnAccuracy();
        System.out.println("NAIVE BAYES WITH STOP WORDS REMOVED:");
        System.out.println("THE ACCURACY OF NAIVE BAYES OVER SPAM RECORDS IS: " + value[0]);
        System.out.println("THE ACCURACY OF NAIVE BAYES OVER HAM RECORDS IS: " + value[1]);

        LogisticRegression lrWithST = new LogisticRegression(NBWithoutStopList.vocabularyList, false);
        lrWithST.runLogisticRegression();

        LogisticRegression lrWithoutST = new LogisticRegression(NBWithStopList.vocabularyList, true);
        lrWithoutST.runLogisticRegression();

    }

}
