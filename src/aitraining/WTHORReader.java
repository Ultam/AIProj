package aitraining;

import java.io.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Nyrmburk on 11/25/2016.
 */
public class WTHORReader {

	public static final byte BOARD_SIZE_8 = 8;
	public static final byte BOARD_SIZE_10 = 10;

	private File file;
	InputStream in;

	private Date dateCreated;
	private int tournamentYear;

	private int recordsCount;
	private short playerCount;
	private short partyYear;
	private byte boardSize;
	private byte typeParts;
	private byte depth;
	private byte reserved;

	public WTHORReader(File file) {
		this.file = file;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			readHeader();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readHeader() throws IOException {

		int year = in.read() * 100 + in.read();
		int month = in.read();
		int day = in.read();

		Calendar calendar = new Calendar.Builder().setDate(year, month, day).build();
		dateCreated = calendar.getTime();

		recordsCount = in.read() | in.read() << 8 | in.read() << 16 | in.read() << 24;

		playerCount = (short) (in.read() | in.read() << 8);
		partyYear = (short) (in.read() | in.read() << 8);
		boardSize = (byte) in.read();
		boardSize = getBoardSize() == 0 ? BOARD_SIZE_8 : getBoardSize(); // if 0, change to 8
		typeParts = (byte) in.read();
		depth = (byte) in.read();
		reserved = (byte) in.read();
	}

	public WTHORPart readPart() throws IOException {

		short labelNumber = (short) (in.read() | in.read() << 8);
		short playerNumberBlack = (short) (in.read() | in.read() << 8);
		short playerNumberWhite = (short) (in.read() | in.read() << 8);
		byte actualScore = (byte) in.read();
		byte theoreticalScore = (byte) in.read();
		byte listSize = boardSize == BOARD_SIZE_8 ? WTHORPart.SHOTS_LIST_SIZE_8 : WTHORPart.SHOTS_LIST_SIZE_10;
		byte[] movesList = new byte[listSize];
		in.read(movesList);

		return new WTHORPart(labelNumber, playerNumberBlack,
				playerNumberWhite, actualScore,
				theoreticalScore, movesList);
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public int getTournamentYear() {
		return tournamentYear;
	}

	public int getRecordsCount() {
		return recordsCount;
	}

	public short getPlayerCount() {
		return playerCount;
	}

	public short getPartyYear() {
		return partyYear;
	}

	public byte getBoardSize() {
		return boardSize;
	}

	public byte getTypeParts() {
		return typeParts;
	}

	public byte getDepth() {
		return depth;
	}

	public byte getReserved() {
		return reserved;
	}
}
