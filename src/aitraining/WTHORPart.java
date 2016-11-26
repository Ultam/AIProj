package aitraining;

/**
 * Created by Nyrmburk on 11/26/2016.
 */
public class WTHORPart {

	public static final byte SHOTS_LIST_SIZE_8 = 60;
	public static final byte SHOTS_LIST_SIZE_10 = 96;

	private short labelNumber;
	private short playerNumberBlack;
	private short playerNumberWhite;
	private byte actualScore;
	private byte theoreticalScore;
	private byte[] movessList;

	public WTHORPart(
			short labelNumber, short playerNumberBlack,
			short playerNumberWhite, byte actualScore,
			byte theoreticalScore, byte[] movessList) {

		this.labelNumber = labelNumber;
		this.playerNumberBlack = playerNumberBlack;
		this.playerNumberWhite = playerNumberWhite;
		this.actualScore = actualScore;
		this.theoreticalScore = theoreticalScore;
		this.movessList = movessList;
	}

	public short getLabelNumber() {
		return labelNumber;
	}

	public short getPlayerNumberBlack() {
		return playerNumberBlack;
	}

	public short getPlayerNumberWhite() {
		return playerNumberWhite;
	}

	public byte getActualScore() {
		return actualScore;
	}

	public byte getTheoreticalScore() {
		return theoreticalScore;
	}

	public byte[] getMovessList() {
		return movessList;
	}

	public byte getBoardSize() {
		return movessList.length == SHOTS_LIST_SIZE_8 ? WTHORReader.BOARD_SIZE_8 : SHOTS_LIST_SIZE_10;
	}
}
