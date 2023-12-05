import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class demonstrates the use of the Viterbi algorithm to perform part-of-speech tagging
 * using Hidden Markov Models (HMMs) for a given observation sequence.
 *
 * @author lordchariteigirimbabazi
 * CS10, FALL 2023
 */

public class ViterbiTest {

    public static void main(String[] args) {
        Map<String, Map<String, Double>> transitionProbabilities = new HashMap<>();
        Map<String, Map<String, Double>> observationProbabilities = new HashMap<>();

        //HARD-CODED TEST PART
        // Define transition probabilities
        transitionProbabilities.put("#", new HashMap<>());
        transitionProbabilities.get("#").put("NP", 3.0);
        transitionProbabilities.get("#").put("N", 7.0);

        transitionProbabilities.put("N", new HashMap<>());
        transitionProbabilities.get("N").put("CNJ", 2.0);
        transitionProbabilities.get("N").put("V", 8.0);

        transitionProbabilities.put("V", new HashMap<>());
        transitionProbabilities.get("V").put("CNJ", 2.0);
        transitionProbabilities.get("V").put("N", 4.0);
        transitionProbabilities.get("V").put("NP", 4.0);

        transitionProbabilities.put("NP", new HashMap<>());
        transitionProbabilities.get("NP").put("CNJ", 2.0);
        transitionProbabilities.get("NP").put("V", 8.0);

        transitionProbabilities.put("CNJ", new HashMap<>());
        transitionProbabilities.get("CNJ").put("V", 4.0);
        transitionProbabilities.get("CNJ").put("N", 4.0);
        transitionProbabilities.get("CNJ").put("NP", 2.0);

        // Define observation probabilities
        observationProbabilities.put("N", new HashMap<>());
        observationProbabilities.get("N").put("watch", 2.0);
        observationProbabilities.get("N").put("cat", 4.0);
        observationProbabilities.get("N").put("dog", 4.0);

        observationProbabilities.put("V", new HashMap<>());
        observationProbabilities.get("V").put("chase", 3.0);
        observationProbabilities.get("V").put("watch", 6.0);
        observationProbabilities.get("V").put("get", 1.0);

        observationProbabilities.put("NP", new HashMap<>());
        observationProbabilities.get("NP").put("chase", 10.0);

        observationProbabilities.put("CNJ", new HashMap<>());
        observationProbabilities.get("CNJ").put("and", 10.0);


        HMMModel model0 = new HMMModel(transitionProbabilities, observationProbabilities);

        // Define an input observation sequence (sentence)
        ArrayList<String> observation = new ArrayList<>();

        observation.add("dog");
        observation.add("watch");
        observation.add("cat");
        observation.add("and");
        observation.add("chase");
        observation.add("watch");
        observation.add("cat");

        List<String> result = model0.viterbiDecoder(observation);
        System.out.println("Observations are:" + observation);
        System.out.println("Viterbi Decoding Result:" + result);

        // Define an input observation sequence (sentence)
        ArrayList<String> observation1 = new ArrayList<>();

        observation1.add("all");
        observation1.add("dogs");
        observation1.add("and");
        observation1.add("rabbits");
        observation1.add("are");
        observation1.add("dog");
        observation1.add("species");


        List<String> result1 = model0.viterbiDecoder(observation1);
        System.out.println("Observations are:" + observation1);
        System.out.println("Viterbi Decoding Result:" + result1);

    }
}

