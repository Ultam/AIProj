package aitraining;

import kernel.SimpleKernelExecutor;
import network.FeedForwardNetwork;
import network.NetworkExecutor;

import java.io.File;
import java.io.IOException;

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
	private static FeedForwardNetwork network = new FeedForwardNetwork(BOARD_SIZE * 2, 20, 20, 20, BOARD_SIZE);
	private static NetworkExecutor executor = new NetworkExecutor(network, new SimpleKernelExecutor());
	private static float[] input = new float[network.getInputCount()];
	private static float[] expected = new float[BOARD_SIZE];

	public static void main(String[] args) {

		System.out.println(WTHORPart.convertMoveTo8Board(0));

		WTHORReader reader = new WTHORReader(new File(args[0]));

		for (int i = 0; i < reader.getRecordsCount(); i++) {

			try {
				WTHORPart part = reader.readPart();
//				if (part.getActualScore() < 30)
//					continue;
				trainFromMoves(part.getMovesList());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void trainFromMoves(byte[] moves) {

		boolean black = true;
		for (int i = 0; i < moves.length; i++) {

			if (moves[i] == 0)
				return;

			System.out.println(trainFromMove(moves[i], black));
			black = !black;
		}
	}

	private static float trainFromMove(byte move, boolean black) {

		move = WTHORPart.convertMoveTo8Board(move);

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
}
