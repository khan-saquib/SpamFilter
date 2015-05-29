import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Saquib
 */
public class LogisticRegression {

    public static String SPAMFOLDERNAME ;
    public static String HAMFOLDERNAME;
    public static String SPAMTEST ;
    public static String HAMTEST;
    public static String STOPWORDS;
    
    //data members
    List<String> vocabularyList;
    List<Double> weights;
    List<List<Integer>> COUNT;
    int noOfSpam;
    boolean stopListEnable;
    List<String> stopWords;
    
    
    
    
    public LogisticRegression(List<String> vocabularyList, boolean stopListEnable) throws FileNotFoundException
    {
        vocabularyList.add(0,"");
        this.vocabularyList = vocabularyList;
        
        weights = new ArrayList<>();
        //Initialise the weights
        for(int i= 0;i<vocabularyList.size();i++)
        {
            weights.add(0.0);
        }
        COUNT = new ArrayList<>();
        noOfSpam = 0;
        
        
        this.stopListEnable = stopListEnable;
        if(stopListEnable)
        {
            //Populate the stop words
            File stopWordsFile = new File(STOPWORDS);
            stopWords = readFile(stopWordsFile);
        }
        else
            stopWords = null;
        
        
    }
    
    
    
    public void runLogisticRegression() throws FileNotFoundException
    {
        
        File spamFolder = new File(SPAMFOLDERNAME);
        File[] listOfSpamFiles = spamFolder.listFiles();
        File hamFolder = new File(HAMFOLDERNAME);
        File[] listOfHamFiles = hamFolder.listFiles();
        
        
        
        
        /**
         * READ EACH SPAM RECORD AND SET THE COUNT FOR EACH WORD IN THE COUNTSPAM 
         * MATRIX(INDEX DECIDED BY VOCABULARY LIST INDEX FOR THAT WORD)
         */
        populateCOUNTForSPAM(listOfSpamFiles);
        
        /**
         * READ EACH HAM RECORD AND SET THE COUNT FOR EACH WORD IN THE COUNTHAM 
         * MATRIX(INDEX DECIDED BY VOCABULARY LIST INDEX FOR THAT WORD)
         */
        populateCOUNTForHAM(listOfHamFiles);
        
        
        double eenta = 0.01;
        double lemda;
        int noOfIterations=10;
        
        for(lemda = 0.1; lemda <= 4.0; lemda+=0.4)
        {
            //Approximate the weights from the learning data
            populateWeightsFromAllTrainingData(1,0, listOfSpamFiles.length + listOfHamFiles.length,eenta, lemda, noOfIterations);
            double[] value = testLogisticRegression();
            System.out.println("STOP LIST ENABLED:" + this.stopListEnable  + " lemda = "+ lemda);
            System.out.println("THE ACCURACY OF LOGISTIC REGRESSION OVER SPAM RECORDS IS: " + value[0]);
            System.out.println("THE ACCURACY OF LOGISTIC REGRESSIOn OVER HAM RECORDS IS: " + value[1]);
   
        }   
        
        
    }

    
    
    public double[] testLogisticRegression() throws FileNotFoundException
    {
        File folder = new File(SPAMTEST);
        File[] listOfFiles = folder.listFiles();
        double percentage=0;
        double[] returnValue = new double[2];
        
        
        //CALCULATE THE ACCURACY OF SPAM
        for(File spamFile : listOfFiles)
        {
            if(IsSpam(spamFile))
               percentage++;
        }
        
        percentage = percentage*100/listOfFiles.length;
        returnValue[0] = percentage;
        
        
        //CALCULATE THE ACCURACY OF HAM
        percentage = 0;
        folder = new File(HAMTEST);
        listOfFiles = folder.listFiles();
        for(File hamFile : listOfFiles)
        {
            if(!IsSpam(hamFile))
               percentage++;
        }
        
        percentage = percentage*100/listOfFiles.length;
        returnValue[1] = percentage;
        
        return returnValue;
    }
    
    
    
    /*
    Tests if the test file is SPAM or HAM. If it is SPAM, returns True. Else return False.
    */
    public boolean IsSpam(File testFile) throws FileNotFoundException
    {
        List<String> words = readFile(testFile);
        
        List<Integer> countOfVocabularyWords = new ArrayList<>();
        
        for(int count=0; count<vocabularyList.size();count++)
        {
            countOfVocabularyWords.add(0);
        }
        
        int i;
        
        //Calculate count of the words in the file
        for(String eachWord: words)
        {
            if(this.vocabularyList.contains(eachWord))
            {
                i = this.vocabularyList.indexOf(eachWord);
                countOfVocabularyWords.set(i, countOfVocabularyWords.get(i) + 1);
            }
        }
        
        
        double temp = weights.get(0);
        for(int count = 1; count<vocabularyList.size() ;count++)
        {
            temp = temp + weights.get(count)*countOfVocabularyWords.get(count);
        }
        
        double result = 1.0/(1.0+Math.exp(-temp));
        
        
        return result > 0.5;
    }
     
    
    
    /**READ EACH SPAM RECORD AND SET THE COUNT FOR EACH WORD IN THE 
 COUNT MATRIX(INDEX DECIDED BY VOCABULARY LIST INDEX 
 FOR THAT WORD
     * @param listOfSpamFiles 
     */
    private void populateCOUNTForSPAM(File[] listOfSpamFiles) throws FileNotFoundException {
        
        //Initialise temporary words
        List<String> words;
        int countOfFile = -1;
        int i;
        
        for(File spamFile : listOfSpamFiles)
        {
            //SET THE COUNT OF THE FILE
            countOfFile++;
            //READ FROM SPAM FILE
            words = readFile(spamFile);
            
            //CREATE A ROW IN THE COUNT Matrix for this file
            List<Integer> countSpam = new ArrayList<>();
            int size = vocabularyList.size();
            countSpam.add(1);
            for(int counter = 1;counter< size; counter++)
                countSpam.add(0);
            COUNT.add(countSpam);
            
            //COMPUTE THE COUNTS FOR TO BE USED IN GRADIENT ASCENT CALCULATION FOR ALL SPAM RECORDS
            for(String word : words)
            {
                if(word.length()==0 || (stopListEnable && stopWords.contains(word)))
                    continue;
                if(vocabularyList.contains(word))
                {   
                    i = vocabularyList.indexOf(word);
                    COUNT.get(countOfFile).set(i,COUNT.get(countOfFile).get(i) + 1);
                }
                else
                {
                    //WORD NOT FOUND IN THE VOCABULARY LIST. LIST INCOMPLETE.
                    System.out.println("ERROR:LR MODULE \nWORD:"
                            + word + ":FROM SPAM EMAILS NOT FOUND IN VOCABULARY LIST");
                    System.exit(-1);
                }
            }
        }
        this.noOfSpam = COUNT.size();
        
        
        
    }

    
    /**
      * READ EACH HAM RECORD AND SET THE COUNT FOR EACH WORD IN THE 
     * COUNTHAM MATRIX(INDEX DECIDED BY VOCABULARY LIST INDEX 
     * FOR THAT WORD
     * @param listOfFiles 
     */
    private void populateCOUNTForHAM(File[] listOfFiles) throws FileNotFoundException {
        //Initialise temporary words
        List<String> words;
        int countOfFile = noOfSpam-1;
        int i;
        
        for(File hamFile : listOfFiles)
        {
            //SET THE COUNT OF THE FILE
            countOfFile++;
            //READ FROM Ham FILE
            words = readFile(hamFile);
            
            //CREATE A ROW IN THE COUNTHAM Matrix for this file
            List<Integer> countHam = new ArrayList<>();
            int size = vocabularyList.size();
            countHam.add(1);
            for(int counter = 1;counter< size; counter++)
                countHam.add(0);
            
            COUNT.add(countHam);
            
            //COMPUTE THE COUNTS FOR TO BE USED IN GRADIENT ASCENT CALCULATION FOR ALL HAM RECORDS
            for(String word : words)
            {
                if(word.length()==0 || (stopListEnable && stopWords.contains(word)))
                    continue;
                if(vocabularyList.contains(word))
                {   
                    i = vocabularyList.indexOf(word);
                    COUNT.get(countOfFile).set(i,COUNT.get(countOfFile).get(i) + 1);
                }
                else
                {
                    //WORD NOT FOUND IN THE VOCABULARY LIST. LIST INCOMPLETE.
                    System.out.println("ERROR:LR MODULE \nWORD:"
                            + word + ":FROM HAM EMAILS NOT FOUND IN VOCABULARY LIST");
                    System.exit(-1);
                }
            }
        }

    }
    
    /**
     * Populate the weights on the basis of ALL the records
     * 
     * 
     * @param CLASS
     * @param totalNoOfFiles
     * @param eenta
     * @param lemda 
     */
    private void populateWeightsFromAllTrainingData(int ClassForSpam, int ClassForHam , int totalNoOfFiles, double eenta, double lemda, int noOfIterations) 
    {
        
        //temp variables
        double delta;
        List<Double> hTheta;
        double newWeight;
        
        for(Double weight: weights)
        {
            weight = 0.0;
        }
    
        //HARD LIMIT ON THE ITERATIONS
        for(int j=0;j<noOfIterations;j++)
        {   
            hTheta = new ArrayList<>();
            
            for(int counter = 0;counter<totalNoOfFiles;counter++)
            {
                hTheta.add(calculatehThetaFromCOUNT(counter)); 
            }
            
            
            for(int i=0;i<vocabularyList.size();i++)
            {
                delta = 0.0;
                for(int k=0;k<totalNoOfFiles;k++)
                {
                    if(k<=noOfSpam)
                        delta = delta + (((double)ClassForSpam - (double)hTheta.get(k))*(double)COUNT.get(k).get(i));
                    else
                        delta = delta + (((double)ClassForHam - (double)hTheta.get(k))*(double)COUNT.get(k).get(i));
                    
                }
                delta = eenta*delta;
                newWeight = weights.get(i) + delta - (eenta*lemda*weights.get(i));
                weights.set(i, newWeight);
            }
//            System.out.println();
//            for(int counter1 = 0; counter1 < weights.size(); counter1++)
//                System.out.print(weights.get(counter1));
        }
        
    }

    
    
    
    /*
    private void populateWeightsFroHam(int CLASS, int totalNoOfFiles, double eenta, double lemda, int noOfIterations) {
        
        //temp variables
        double delta = 0.0;
        List<Double> hTheta;
        double newWeight;
              
        //HARD LIMIT ON THE ITERATIONS
        for(int j=0;j<noOfIterations;j++)
        {   
            hTheta = new ArrayList<>();
            for(int counter = 0;counter<totalNoOfFiles;counter++)
            {
                hTheta.add(calculatehThetaForEachFileFromHam(counter)); 
            }
            
            //TODO: Correct the COUNT counter as Ham records now come after spam
            for(int i=0;i<vocabularyList.size();i++)
            {
                delta = 0;
                for(int k=0;k<totalNoOfFiles;k++)
                {
                    delta = delta + (((double)CLASS - (double)hTheta.get(k))*(double)COUNTHAM.get(k).get(i));
                }
                delta = eenta*delta;
                newWeight = weights.get(i) + delta - (eenta*lemda*weights.get(i));
                weights.set(i, newWeight);
            }
            
        }
        
    }
    */

    private double calculatehThetaFromCOUNT(int fileIndex) 
    {
        double temp = weights.get(0);
        for(int count = 1; count<vocabularyList.size() ;count++)
        {
            temp = temp + (double)weights.get(count)*(double)COUNT.get(fileIndex).get(count);
        }
        
        return 1.0/(1.0+Math.exp(-temp));
    }
    
    /*
    private double calculatehThetaForEachFileFromHam(int fileIndex) 
    {
        double temp = weights.get(0);
        for(int count = 1; count<vocabularyList.size() ;count++)
        {
            temp = temp + (double)weights.get(count)*(double)COUNT.get(fileIndex).get(count);
        }
        
        return 1.0/(1.0+Math.exp(-temp));
    }
    */
    
         /**
     * Read from the file specified and returns a list of all the individual words in the document.
     * @param file
     * @return list of all valid words in the file
     * @throws FileNotFoundException 
     */
    private List<String> readFile(File file) throws FileNotFoundException 
    {
        
        Scanner scanner = new Scanner(file);
        List<String> words;
        words = new ArrayList<>();
        List<String> temp;
            
            while(scanner.hasNextLine())
            {
                temp = Arrays.asList(scanner.nextLine().split(" "));
                for(String t: temp)
                {
                    if(t.matches("[a-zA-Z']+") && t.length()>1) 
                    {
                        words.add(t.toLowerCase());
                    }
                }
            }
    
            return words;
    }
    
    
    
    
    
}
