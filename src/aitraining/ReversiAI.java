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

	private FeedForwardNetwork network = new FeedForwardNetwork(BOARD_SIZE * 3, 20, 20, 20, BOARD_SIZE);
	private NetworkExecutor executor = new NetworkExecutor(network, new SimpleKernelExecutor());
	float[] input = new float[network.getInputCount()];
	float[] expected = new float[BOARD_SIZE];

	public static void main(String[] args) {

		WTHORReader reader = new WTHORReader(new File(args[0]));

		for (int i = 0; i < reader.getRecordsCount(); i++) {

			try {
				reader.readPart();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
