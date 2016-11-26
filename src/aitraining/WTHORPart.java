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
	private byte[] movesList;

	public WTHORPart(
			short labelNumber, short playerNumberBlack,
			short playerNumberWhite, byte actualScore,
			byte theoreticalScore, byte[] movesList) {

		this.labelNumber = labelNumber;
		this.playerNumberBlack = playerNumberBlack;
		this.playerNumberWhite = playerNumberWhite;
		this.actualScore = actualScore;
		this.theoreticalScore = theoreticalScore;
		this.movesList = movesList;
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

	public byte[] getMovesList() {
		return movesList;
	}

	public byte getBoardSize() {
		return movesList.length == SHOTS_LIST_SIZE_8 ? WTHORReader.BOARD_SIZE_8 : SHOTS_LIST_SIZE_10;
	}

	public static byte convertMoveTo8Board(int move) {

		byte col = (byte) (move % WTHORReader.BOARD_SIZE_10 - 1);
		byte row = (byte) (move / WTHORReader.BOARD_SIZE_10 - 1);

		return (byte) (col + (row * WTHORReader.BOARD_SIZE_8));
	}
}
