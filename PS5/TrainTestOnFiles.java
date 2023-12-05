import java.io.IOException;
import java.util.*;

/**
 * This class demonstrates training an HMMModel using training data and performing Viterbi decoding on a sample observation sequence.
 *
 * @author lordchariteigirimbabazi
 * CS10, FALL 2023
 */
public class TrainTestOnFiles {

    public static void main(String[] args) throws IOException {

        //TRAINING
        HMMModel model1 = new HMMModel();
        String sentencesFile = "PS5/simple-train-sentences.txt";
        String tagsFile = "PS5/simple-train-tags.txt";

        List<ArrayList<String>> sentencesWords = model1.loadFromFile(sentencesFile);
        List<ArrayList<String>> tagsWords = model1.loadFromFile(tagsFile);
        model1.trainModel(sentencesWords, tagsWords);
        List<String> obs = new ArrayList<>();
        obs.add("you");
        obs.add("are");
        obs.add("beautiful");
        obs.add("work");
        System.out.println("The observations are: " + obs);
        System.out.println("The corresponding tags from viterbi are: ");
        System.out.println(model1.viterbiDecoder(obs));//viterbi test

        List<String> obs1 = new ArrayList<>();
        obs1.add("the");
        obs1.add("car");
        obs1.add("is");
        obs1.add("beautiful");
        System.out.println("\nThe observations are:" + obs1);
        System.out.println("The corresponding tags from viterbi are: ");
        System.out.println(model1.viterbiDecoder(obs1)); //viterbi test


        //TESTING
        String sentencesFile1 = "PS5/simple-test-sentences.txt";
        String tagsFile1 = "PS5/simple-test-tags.txt";
        System.out.println("Performance is: ");
        model1.evaluatePerformance(sentencesFile1,tagsFile1);

    }
}