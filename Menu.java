package ie.atu.sw;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

/**
 * Starts the menu so the user is able to select an option
 * Adds functionality to the options in <i>MenuOptions</i>.
 * Extends the <i>MenuTemplate</i> for some core features
 *
 * @see ie.atu.sw.MenuTemplate
 * @see MenuOptions
 */
public class Menu extends MenuTemplate {
    private boolean isRunning = true;
    private Scanner sc = new Scanner(System.in);
    private int lineNum;
    private int page;
    private String inputFileName;
    private String outputFileName;
    private String stopWordsFile = "./google-1000.txt";
    private String dictionaryFile = "./dictionary.csv";
    private Map<String, String> indexer = new ConcurrentSkipListMap<>();
    private Map<String, String> dictionaryList = new ConcurrentSkipListMap<>();
    private Set<String> stopWordsList = new ConcurrentSkipListSet<>();
    private Map<String, List<Integer>> wordsCount = new TreeMap<>();

    /**
     * Checks if output file name has been set
     *
     * @return
     */
    private boolean inputFileLoaded() {
        if (inputFileName != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if output file name has been set
     *
     * @return
     */
    private boolean outputFileNameSet() {
        if (outputFileName != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Starts the menu and lets the user choose from the displayed options
     * Displays menu information from
     * @see MenuOptions
     * @throws InterruptedException
     * @throws IOException from configure Stop Words and Dictionary files
     */
    public void start() throws InterruptedException, IOException {
        while (isRunning) {
            MenuOptions.show();
            int choice = sc.nextInt();
            //TODO: Try to add error handling for non-numeric character input
            switch (choice) {
                case 1 -> loadTextFile();
                case 2 -> configureDictionary(dictionaryFile);
                case 3 -> configureStopWords(stopWordsFile);
                case 4 -> setOutputFileName();
                case 5 -> runIndexer(inputFileName);
                case 6 -> {
                    //TODO: Make sure threads are closed. Shut down gracefully
                    System.out.println("[INFO] Shutting down...please wait...");
                    isRunning = false;
                }
                default -> System.out.println("Error invalid input. Please enter [1-6].");
            }
        }
    }

    /**
     * Starts creating the final index and creates it under the file name provided by the user
     */
    private void runIndexer(String file) throws IOException {
        if (requirementsMet()) {
            System.out.println("[INFO] Creating index");
            parseBook(file);
            writeToTextFile(indexer, outputFileName);
            System.out.println("[INFO] Index file " + outputFileName + " created successfully!");
        }
        System.out.println("[ERROR] Please ensure all requirements are met");
        System.out.println("[INFO] Input file set: " + inputFileLoaded());
        System.out.println("[INFO] Output file name set: " + outputFileNameSet());
    }

    /**
     * Checks that all requirements are met before creating index
     * @return
     */
    private boolean requirementsMet() {
        if (inputFileLoaded() &&
                outputFileNameSet()) {
            return true;
        }
        return false;
    }

    /**
     * Allows the user to set an output file name.
     * This must be set by the user so the output file is created.
     * If left null no file will be created.
     */
    private String setOutputFileName() {
        System.out.println("[INFO] Please enter a file name for your index");
        Scanner outFile = new Scanner(System.in);
        outputFileName = outFile.nextLine();

        System.out.println("Your index file name is " + outputFileName);
        return outputFileName;
    }

    /**
     * Uses stop words from <i>stopWordsFile</i>
     * Loads stop words into a List. These words are <b>not</b> included
     * in the final index
     *
     * <b>Big O:</b> O(n) Standard Loop
     *
     * @param file
     */
    private void configureStopWords(String file) throws IOException {
        Files.lines(Path.of(file))
                .forEach(line -> Thread.startVirtualThread(() -> processStopWords(line)));
        System.out.println("[INFO] Common Words configured");
    }

    /**
     * Processes dictionary to be uses when searching
     * text file selected by the user.
     * Uses <b>definitions</b> from selected dictionary.
     * <b>Big O:</b> Standard Loop
     * @param file
     */
    private void configureDictionary(String file) throws IOException {
        Files.lines(Path.of(file))
                .forEach(line -> Thread.startVirtualThread(() -> processDictionary(line)));
        System.out.println("[INFO] Dictionary configured");
    }

    /**
     * Users enters file path for text file to be processed
     */
    private String loadTextFile() {
        Scanner sc2 = new Scanner(System.in);

        System.out.println("[INFO] Enter file path to load text from");
        String fileName = sc2.nextLine();
        File file = new File(fileName);
        inputFileName = file.toString();
        System.out.println("[INFO] Text file " + inputFileName + " loaded successfully.");

        return inputFileName.toString();
    }

    /**
     * Splits line into words and adds to Stop Words List.
     * <b>Big O:</b> O(1) - Standard Loop
     */
    private void processStopWords(String line) {
        Arrays.stream(line.split(" ")).forEach(w -> stopWordsList.add(w));
    }

    /**
     * Splits lines from Dictionary CSV into word and definition
     * Adds to Dictionary List.
     * <b>Big O:</b> O(1) - Standard Loop
     */
    private void processDictionary(String line) {
        //TODO: Figure out why this isn't working
        // potential to have one process method if can shorthand
        //Arrays.stream(line.split(",")).forEach((key, val) -> dictionaryList.put(key, val));
        String[] str = line.split(",");
        String key = str[0];
        String val = str[1];
        for (String s : str) {
            dictionaryList.put(key, val);
        }
    }

    /**
     * Parses the specified file selected by the user line by line
     * Sends each line to be processed
     * Keeps track of page count
     * <b>Big O:</b> O(n) - Standard Loop
     *
     * @param file
     * @throws IOException
     */
    private void parseBook(String file) throws IOException {
        System.out.println("[INFO] Processing book and adding to index");
        Files.lines(Path.of(file))
                .forEach(line -> {
                    lineNum++;
                    if (lineNum % 40 == 0) {
                        page++;
                        //TODO: Add page count
                    }
                    processLineFromBook(line);
                });
    }

    /**
     * Processes each line from the user selected file
     * Calls <i>addToList</i> for validation
     * <b>Big O:</b> O(n) for each loop
     *
     * @param line
     */
    private void processLineFromBook(String line) {
        Arrays.stream(line.split(" ")).forEach(w -> {
            try {
                addToLists(w);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Checks if word is on the <i>stopWordsList</i>. Breaks if the word is on <i>stopWordsList</i>.
     * Adds the word to the indexer if:
     * <ol>
     *     <li>it is not in <i>stopWordsList</i></li>
     *     <li>it is not in <i>indexer</i></li>
     *     <li>it is in <i>dictionaryList</i></li>
     * </ol>
     * Counts the word if it meets the above conditions
     * <b>Big O:</b> Average O(1) for Maps
     * @param word
     */
    private void addToLists(String word) throws Exception {
        if (stopWordsList.contains(word)) return;

        if (!indexer.containsKey(word) && dictionaryList.containsKey(word)) {
            //TODO: countWord method used here
            //countWord(word);
            indexer.put(word.toLowerCase(), dictionaryList.get(word));
        }
    }

    /**
     * Writes the <i>indexer</i> map to a text file.
     * Uses the <i>outputFileName</i> submitted by the user as the file name.
     * Uses <i>BufferedWriter</i> to write words and definitions to file
     * <b>Big O:</b> O(n) Worst case scenario one foreach loop
     * @param index
     * @param outputFileName
     */
    private void writeToTextFile(Map index, String outputFileName) {
        //TODO: Maybe add:
        // The top n most frequent/ infrequent words.

        Map<String, String> temp = new TreeMap<>(indexer);

        System.out.println("[INFO] Writing list to text file");
        File file = new File(outputFileName);
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file));

            bw.write("### INDEX OF " + inputFileName + "###" + "\n" + "\n");
            bw.write("Number of unique words: " + index.size() + "\n" + "\n");
            bw.write("WORD" + "\t" + "\t" + "DEFINITION" + "\n");
            bw.write("-----------------------------------------");
            for (Map.Entry<String, String> word : temp.entrySet()) {
                bw.write(word.getKey() + "\t" + "\t" + word.getValue() + "\n");
            }

            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Keeps track of the number of times a word appears in the submitted text.
     * <b>Big O:</b> O(1) Get and Put actions on an Array are constant time operations
     * @param word
     * @throws Exception
     */
    private void countWord(String word) throws Exception {
        // TODO: Add word count as column to exported file
        int count = 1;
        List<Integer> list;
        if (wordsCount.containsKey(word)) {
            list = wordsCount.get(word);
        } else {
            list = new ArrayList<>();
        }
        list.add(count);
        wordsCount.put(word, list);
    }
}
