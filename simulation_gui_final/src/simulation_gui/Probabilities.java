package simulation_gui;

import java.util.Random;

public class Probabilities {
	
    public static void main(String[] args) {
        // Define probabilities
        double[] probabilities = {0.3, 0.7}; // Example probabilities for each path
        String[] paths = {"path1", "path2"}; // Corresponding paths
        int[] counts = new int[probabilities.length]; // Array to store counts
        String[] chosenPaths = new String[10]; // Array to store the chosen paths

        // Run the path selection process 100 times
        Random random = new Random();
        for (int run = 0; run < 10; run++) {
            // Generate a random number between 0 and 1
            double ran = random.nextDouble();

            // Initialize variable for cumulative probability
            double cumulativeProbability = 0.0;

            // Determine the chosen path based on probabilities
            for (int i = 0; i < probabilities.length; i++) {
                cumulativeProbability += probabilities[i];
                System.out.println("cumulativeProbability" + cumulativeProbability);
                System.out.println("ran" + ran);
                if (ran < cumulativeProbability) {
                    counts[i]++;
                    chosenPaths[run] = paths[i]; // Store the chosen path
                    break;
                }
            }
        }

        // Output the counts for each probability
        for (int i = 0; i < probabilities.length; i++) {
            System.out.println("Probability " + (i + 1) + ": " + counts[i] + " occurrences");
        }

        // Output the chosen paths for each run
        for (int run = 0; run < chosenPaths.length; run++) {
            System.out.println("Run " + (run + 1) + ": " + chosenPaths[run]);
        }
    }
}
