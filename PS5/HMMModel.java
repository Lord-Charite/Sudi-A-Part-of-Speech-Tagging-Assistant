import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This class represents a Hidden Markov Model (HMM) for a part-of-speech tagging system.
 * @author lordchariteigirimbabazi
 * CS10, FALL 2023
 */

public class HMMModel {
    Map<String, Map<String, Double>> transitionProbabilities; // transition probability maps
    Map<String, Map<String, Double>> observationProbabilities; // observation probability maps

    String initialState = "#";

    public HMMModel() {
        this.transitionProbabilities = new HashMap<>();
        this.observationProbabilities = new HashMap<>();
    }

    // Constructor for HMMModel that takes probabilities as parameters for hard-coded Test
    public HMMModel(Map<String, Map<String, Double>> transitionProbabilities,
                    Map<String, Map<String, Double>> observationProbabilities) {
        this.transitionProbabilities = transitionProbabilities;
        this.observationProbabilities = observationProbabilities;
    }

    /**
     * Viterbi decoding method to find the most likely sequence of part-of-speech tags for an input sequence.
     *
     * @param observations An ArrayList of words in the input sequence.
     * @return             A List of part-of-speech tags representing the most likely sequence.
     */
    public List<String> viterbiDecoder(List<String> observations) {
        // Create a list to store backtraces at each time step
        ArrayList<Map<String, String>> backtraceList = new ArrayList<>();
        // Initialize the set of possible states with the initial state
        Set<String> possibleStates = new HashSet<>();

        double unseenWordPenalty = -100.0;

        possibleStates.add(initialState);

        // Initialize the current scores map with the initial state having a score of 0.0
        Map<String, Double> stateScores = new HashMap<>();
        stateScores.put(initialState, 0.0);

        for (String observation : observations) {
            // Initialize the set of next possible states and scores
            Set<String> nextStates = new HashSet<>();
            Map<String, Double> nextScores = new HashMap<>();
            Map<String, String> backtrace = new HashMap<>();

            for (String currentState : possibleStates) {
                // Get the transition probabilities for the current state
                Map<String, Double> currentStateTransitions = transitionProbabilities.get(currentState);

                if (currentStateTransitions != null) {
                    // Iterate over possible next states
                    for (String nextState : currentStateTransitions.keySet()) {
                        nextStates.add(nextState);

                        // Calculate the score for the next state
                        double score = stateScores.get(currentState) +
                                currentStateTransitions.get(nextState) +
                                (observationProbabilities.get(nextState).getOrDefault(observation, unseenWordPenalty));

                        // Update nextScores and backtrace if a higher score is found
                        if (!nextScores.containsKey(nextState) || score > nextScores.get(nextState)) {
                            nextScores.put(nextState, score);
                            backtrace.put(nextState, currentState);
                        }
                    }
                }
            }

            // Store the backtrace for the current time step
            backtraceList.add(backtrace);
            possibleStates = nextStates;
            stateScores = nextScores;
        }

        // Find the best end state with the highest score
        String bestEndState = null;
        double maxEndStateScore = Double.NEGATIVE_INFINITY;

        for (String state : stateScores.keySet()) {
            double score = stateScores.get(state);
            if (score > maxEndStateScore) {
                maxEndStateScore = score;
                bestEndState = state;
            }
        }

        // Initialize the current state with the best end state
        String currentState = bestEndState;
        List<String> bestPath = new ArrayList<>();

        // Reconstruct the best path by following backtraces
        for (int i = backtraceList.size() - 1; i >= 0; i--) {
            bestPath.add(0, currentState);
            currentState = backtraceList.get(i).get(currentState);
        }


        return bestPath;
    }

    /**
     * Train the Hidden Markov Model using training data provided as lists of sentences and
     * corresponding tags.
     *
     * @param sentencesList The list of sentences, where each sentence is represented as a List of words.
     * @param tagsList      The list of corresponding part-of-speech tags for each sentence.
     */
    public void trainModel(List<ArrayList<String>> sentencesList, List<ArrayList<String>> tagsList) {
        if (sentencesList.size() != tagsList.size()) {
            throw new IllegalArgumentException("Mismatch between sentences and tags lists.");
        }

        trainTransitionLogProbabilities(tagsList);
        trainObservationLogProbabilities(sentencesList, tagsList);

    }

    /**
     * Train the observation probabilities of words given part-of-speech tags based on a list of sentences and tags.
     *
     * @param sentencesList The list of sentences, where each sentence is represented as a List of words.
     * @param tagsList      The list of part-of-speech tags for the corresponding sentences.
     */
    public void trainObservationLogProbabilities(List<ArrayList<String>> sentencesList, List<ArrayList<String>> tagsList) {
        if (sentencesList.size() != tagsList.size()) {
            throw new IllegalArgumentException("Mismatch between sentences and tags lists.");
        }
        Map<String, Double> wordObservations;
        for (int i = 0; i < sentencesList.size(); i++) {
            ArrayList<String> sentences = sentencesList.get(i);
            ArrayList<String> tags = tagsList.get(i);

            // Ensure the number of words in the sentence matches the number of tags
            if (sentences.size() != tags.size()) {
                throw new IllegalArgumentException("Mismatch between sentence length and tags.");
            }

            for (int j = 0; j < sentences.size(); j++) {
                String word = sentences.get(j);
                String tag = tags.get(j);

                // Initialize the observation probabilities map for the current tag if not already present
                if (!observationProbabilities.containsKey(tag)) {
                    observationProbabilities.put(tag, new HashMap<>());
                }

                wordObservations = observationProbabilities.get(tag);
                // Update the count of observations of words for the current tag
                if (wordObservations.containsKey(word)) {
                    wordObservations.put(word, wordObservations.get(word) + 1.0);
                } else {
                    wordObservations.put(word, 1.0);
                }

            }

        }

        // Normalize and convert observation probabilities to log probabilities
        normalizeToLogProbabilities(observationProbabilities);
    }

    /**
     * Train the transition probabilities between part-of-speech tags based on a list of tags.
     *
     * @param tagsList The list of part-of-speech tags for sentences, represented as a list of lists.
     */
    public void trainTransitionLogProbabilities(List<ArrayList<String>> tagsList) {

        for (ArrayList<String> tags : tagsList) {

            String previousTag = initialState;

            for (String currentTag : tags) {
                // Initialize the transition probabilities map for the current tag if not already present
                if (!transitionProbabilities.containsKey(previousTag)) {
                    transitionProbabilities.put(previousTag, new HashMap<>());
                }

                Map<String, Double> tagTransitions = transitionProbabilities.get(previousTag);

                // Update the count of transitions from the previous tag to the current tag
                if (tagTransitions.containsKey(currentTag)) {
                    tagTransitions.put(currentTag, tagTransitions.get(currentTag) + 1.0);
                } else {
                    tagTransitions.put(currentTag, 1.0);
                }

                previousTag = currentTag; // Update the previous tag
            }
        }

        // Normalize and convert transition probabilities to log probabilities
        normalizeToLogProbabilities(transitionProbabilities);
    }

    /**
     * Helper method to normalize probabilities and convert them to log probabilities.
     *
     * @param probabilities The map of probabilities to be normalized and converted.
     */
    private void normalizeToLogProbabilities(Map<String, Map<String, Double>> probabilities) {


        for (String tag : probabilities.keySet()) {
            double total = 0.0;
            Map<String, Double> tagProbabilities = probabilities.get(tag);
            // Calculate the total count for the current tag
            for (double count : tagProbabilities.values()) {
                total += count;
            }

            // Normalize probabilities and convert to log probabilities
            for (String key : tagProbabilities.keySet()) {
                double probability = tagProbabilities.get(key) / total;
                tagProbabilities.put(key, Math.log(probability));
            }
        }
    }

    /**
     * Load data from a file into a list of strings.
     *
     * @param filename     The path to the file.
     * @return             A list of strings containing the data from the file.
     * @throws IOException if there is an issue reading the file.
     */
    public List<ArrayList<String>> loadFromFile(String filename) throws IOException {
        List<ArrayList<String>> data = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line;
        while ((line = br.readLine()) != null) {

            if (!line.isEmpty()) {
                String[] words = line.toLowerCase().split(" ");
                ArrayList<String> sentenceWords = new ArrayList<>(Arrays.asList(words));
                sentenceWords.remove(sentenceWords.size()-1); //remove the dot from file format
                data.add(sentenceWords);

            }
        }

        br.close(); // Close the file after reading

        return data;
    }

    /**
     * Evaluate the performance of the HMM on a pair of test files.
     *
     * @param testSentencesFile The file containing test sentences.
     * @param testTagsFile      The file containing the correct part-of-speech tags for the test sentences.
     */
    public void evaluatePerformance(String testSentencesFile, String testTagsFile) {
        try {
            List<ArrayList<String>> testSentences = loadFromFile(testSentencesFile);
            List<ArrayList<String>> testTags = loadFromFile(testTagsFile);

            if (testSentences.size() != testTags.size()) {
                throw new IllegalArgumentException("Mismatch between sentences and tags lists.");
            }

            int correctTags = 0;
            int totalTags = 0;

            for (int i = 0; i < testSentences.size(); i++) {
                ArrayList<String> sentenceTokens = testSentences.get(i);
                ArrayList<String> correctTagsList = testTags.get(i);

                // Use the Viterbi decoder to predict tags for the sentence
                List<String> predictedTags = viterbiDecoder(sentenceTokens);
                // Update evaluation metrics
                int sentenceTags = correctTagsList.size();
                for (int j = 0; j < sentenceTags; j++) {
                    totalTags++;
                    if (correctTagsList.get(j).equals(predictedTags.get(j))) {
                        correctTags++;
                    }
                }
            }

            // Calculate and display evaluation metrics
            double accuracy = (double) correctTags / totalTags;
            System.out.println("\033[34mAccuracy: " + accuracy*100 + " %\033[0m");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Console-based evaluation method to provide tags for an input sentence.
     */
    public void consoleTest() {
        List<String> userInput = new ArrayList<>();
        Scanner in = new Scanner(System.in);

        System.out.println("Welcome to the Part-of-Speech Tagging Console!");
        System.out.println("Enter a sentence, and we will provide tags for it.");
        while (true) {
            System.out.println("Enter a new sentence to see its tags");
            System.out.println("Type 'q' to exit.");
            System.out.print("Enter a sentence: ");
            String sentence = in.nextLine();

            if (sentence.equalsIgnoreCase("q")) { // Check if the user wants to exit
                break;
            }

            String[] words = sentence.toLowerCase().split(" "); // Tokenize and preprocess the input sentence
            userInput.addAll(Arrays.asList(words));

            List<String> predictedTags = viterbiDecoder(userInput); // Use the Viterbi decoder to predict tags for the user'EditorOnes input

            System.out.println("Predicted tags for your input: " + predictedTags);  // Display the predicted tags

            userInput.clear();// Clear the userInput for the next input
        }

        in.close();

        System.out.println("\n\033[34mConsole Test over! \nThank you for testing our Part-of-Speech Tagging Model!\033[0m");
    }


    public static void main(String[] args) throws IOException {

        HMMModel model = new HMMModel();
        String sentencesFile = "PS5/brown-train-sentences.txt";
        String tagsFile = "PS5/brown-train-tags.txt";
        String sentencesFile1 = "PS5/brown-test-sentences.txt";
        String tagsFile1 = "PS5/brown-test-tags.txt";
        List<ArrayList<String>> sentencesWords = model.loadFromFile(sentencesFile);
        List<ArrayList<String>> tagsWords = model.loadFromFile(tagsFile);
        model.trainModel(sentencesWords, tagsWords);
        System.out.println("Model performance based on current trained Data: ");
        model.evaluatePerformance(sentencesFile1, tagsFile1);
        System.out.println("""

                \033[32mLET'S TEST THE MODEL BY GETTING INPUTS FROM YOU\033[0m
                """);
        model.consoleTest();
    }
}
