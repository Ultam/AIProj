package aitraining;

import kernel.SimpleKernelExecutor;
import network.FeedForwardNetwork;
import network.NetworkExecutor;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

/**
 * Created by Nyrmburk on 11/25/2016.
 */
public class ReversiAI {

	private static final int BOARD_SIDE = 8;
	private static final int BOARD_SIZE = 64;

	// create a network with 3 boards worth of info: own pieces, opponent pieces, and valid spots.
	// There are 3 hidden layers with 256 nodes each.
	// The output is one boards worth of outputs; each representing a best guess.
	// The output node with the highest valid value becomes the selected action.
	private static FeedForwardNetwork network;
	private static NetworkExecutor executor;
	private static float[] input;
	private static float[] expected;

	public static void main(String[] args) {

		File networkFile = new File(args[0]);
		File trainingDir = new File(args[1]);

		if (!networkFile.exists()) {
			network = new FeedForwardNetwork(BOARD_SIZE * 2, 64, 64, 64, 64, BOARD_SIZE);
		} else {
			try {
				network = FeedForwardNetwork.load(networkFile);
				System.out.println("loaded: " + networkFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		executor = new NetworkExecutor(network, new SimpleKernelExecutor());
		input = new float[network.getInputCount()];
		expected = new float[BOARD_SIZE];

		File[] files = trainingDir.listFiles((dir, name) -> name.endsWith(".wtb"));

		File errorFile = new File("error.csv");
		PrintStream errorOut = null;
		try {
			errorFile.createNewFile();
			errorOut = new PrintStream(new BufferedOutputStream(new FileOutputStream(errorFile)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (File file : files) {
			System.out.println(file);

			WTHORReader reader = new WTHORReader(file);

			for (int i = 0; i < reader.getRecordsCount(); i++) {

				try {
					WTHORPart part = reader.readPart();
//					if (part.getActualScore() < 40)
//						continue;
					trainFromMoves(part.getMovesList(), errorOut);
//					System.out.println(getAccuracyOfMoves(part.getMovesList()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			FeedForwardNetwork.save(networkFile, network);
			System.out.println("saved: " + networkFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static float getAccuracyOfMoves(byte[] moves) {

		Arrays.fill(input, 0f);

		float error = 0;
		boolean black = true;
		for (int i = 0; i < moves.length; i++) {

			if (moves[i] == 0)
				break;

			error += getAccuracyOfMove(moves[i], black) ? 1f / 60 : 0;
			black = !black;
		}

		return error;
	}

	private static boolean getAccuracyOfMove(byte move, boolean black) {

		move = WTHORPart.convertMoveToIndex8(move);

		int offset = black ? 0 : BOARD_SIZE;
		input[move + offset] = 1;

		float[] output = executor.forward(input);

		int bestIndex = 0;
		float best = 0;
		for (int i = 0; i < output.length; i++) {
			if (output[i] > best) {
				best = output[i];
				bestIndex = i;
			}
		}

		return move == bestIndex;
	}

	public static void trainFromMoves(byte[] moves, PrintStream errorOut) {

		Arrays.fill(input, 0f);

		boolean black = true;
		for (int i = 0; i < moves.length; i++) {

			if (moves[i] == 0)
				break;

			errorOut.println(trainFromMove(moves[i], black));
			black = !black;
		}
	}

	private static float trainFromMove(byte move, boolean black) {

		move = WTHORPart.convertMoveToIndex8(move);

		int offset = black ? 0 : BOARD_SIZE;
		input[move + offset] = 1;

		expected[move] = 1;
		float error = executor.train(input, expected);
		expected[move] = 0;

		return error;
	}

//	private static void setValidSpaces(float[] input) {
//
//		int offset = BOARD_SIZE + BOARD_SIZE;
//
//		// clear old values
//		for (int i = 0; i < BOARD_SIZE; i++)
//			input[offset + i] = 0;
//
//		for (int i = 0; i < validMoves.size(); i++)
//			input[offset + validMoves.get(i)] = 1;
//	}

	public static BufferedImage inputsToImage(float[] inputs) {

		BufferedImage image = new BufferedImage(8, 16, BufferedImage.TYPE_BYTE_GRAY);

		int i = 0;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				image.setRGB(x, y, Color.HSBtoRGB(1, 0, 1 - inputs[i++]));
			}
		}

		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				image.setRGB(x, y + 8, Color.HSBtoRGB(1, 0, 1 - inputs[i++]));
			}
		}

		return image;
	}
}
