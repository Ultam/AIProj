package ReversiRandom;

import java.util.ArrayList;

public class GameState {
	public int[][] state;
	public int move;
	public int me;
	public int p1Count;
	public int p2Count;
	public int p1Edges;
	public int p2Edges;
	public int depth;			//beginning node is depth 0
	public int opponentMoves;
	public double heuristic;
	public ArrayList<GameState> children;
	public GameState parent;
	public int[] moveCoords;
	
	public GameState (int[][] state, int move, int me, GameState parent) {
		this.state = new int[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				this.state[i][j] = state[i][j];
			}
		}
		moveCoords = new int[]{move/8, move%8};
		this.move = move;
		this.me = me;
		this.parent = parent;
		if (parent != null)
			this.depth = parent.depth+1;
		else
			this.depth = 0;
		this.heuristic = Double.MIN_VALUE;
		
		if (move != -1) {
			int r = move / 8;
			int c = move % 8;
			this.state[r][c] = me;
			changeColors(r, c, me-1);
			//calculateTileCounts();			
		}
		
		this.children = new ArrayList<GameState>();
	}
	
	void calculateTileCounts() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (state[i][j] == 1) {
					p1Count++;
					if (i == 0 || i == 7)
						p1Edges++;
					if (j == 0 || j == 7)
						p1Edges++;
				}
				else if (state[i][j] == 2) {
					p2Count++;
					if (i == 0 || i == 7)
						p2Edges++;
					if (j == 0 || j == 7)
						p2Edges++;
				}
			}
		}
	}
	
	public int getNumChildren() {
		return children.size();
	}
	
	public int currTurn() {		//return 0 if my turn, 1 if not my turn
		return depth % 2;
	}
	
	public double getHeuristic() {
		if (this.heuristic == Double.MIN_VALUE){}
			//calculateHeuristic();
		
		return heuristic;
	}
	
	void calculateHeuristic(int aiMe) {
		calculateTileCounts();
		int endGameBonus = 0;
		int countDiff = p1Count - p2Count;
		int edgeDiff = p1Edges - p2Edges;

		if (aiMe != this.me){
			if (aiMe == 1)
				endGameBonus = 1000 * countDiff;
			else
				endGameBonus = -1000 * countDiff;
		}
		
		if (me == 2) {
			countDiff *= -1;
			edgeDiff *= -1;
		}
		
		this.heuristic = countDiff + 3*edgeDiff - this.children.size() + endGameBonus + calculateStability(); //rethink multipliers and such
	}
	
	int calculateStability() {
		int stability = 0;
		for(int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (state[i][j] != 0) {
					int owner = state[i][j];
					int numVulnerable = 0;
					if (i > 0){
						if (j > 0 && state[i-1][j-1] == 3-owner) {
							numVulnerable += checkVulnerability(i, j, 1, 1);
						}
						if (state[i-1][j] == 3-owner) {
							numVulnerable += checkVulnerability(i, j, 1, 0);
						}
						if (j < 7 && state[i-1][j+1] == 3-owner) {
							numVulnerable += checkVulnerability(i, j, 1, -1);
						}
					}
					if (j > 0 && state[i][j-1] == 3-owner) {
						numVulnerable += checkVulnerability(i, j, 0, 1);
					}
					if (j < 7 && state[i][j+1] == 3-owner) {
						numVulnerable += checkVulnerability(i, j, 0, -1);
					}
					if (i < 7) {
						if (j > 0 && state[i+1][j-1] == 3-owner) {
							numVulnerable += checkVulnerability(i, j, -1, 1);
						}
						if (state[i+1][j] == 3-owner) {
							numVulnerable += checkVulnerability(i, j, -1, 0);
						}
						if (j < 7 && state[i+1][j+1] == 3-owner) {
							numVulnerable += checkVulnerability(i, j, -1, -1);
						}
					}
					if (owner != me) {
						numVulnerable *= -1;
					}
					stability += numVulnerable;
				}
			}
		}
		return stability;
	}
	
	int checkVulnerability (int x, int y, int directionX, int directionY) {
		int numVulnerable = 0;
		if (x+directionX >= 0 && x+directionX < 8 && y+directionY >= 0 && y+directionY < 8) {
			if (state[x+directionX][y+directionY] == 0) {
				numVulnerable++;
			}
			else if (state[x+directionX][y+directionY] == state[x][y]) {
				numVulnerable += checkVulnerability(x+directionX, y+directionY, directionX, directionY);
				if (numVulnerable > 0)
					numVulnerable++;
			}
		}
		return numVulnerable;
	}
	
    public void flipTiles(int row, int col, int incx, int incy, int turn) {
        int sequence[] = new int[7];
        int seqLen;
        int i, r, c;
        
        seqLen = 0;
        for (i = 1; i < 8; i++) {
            r = row+incy*i;
            c = col+incx*i;
        
            if ((r < 0) || (r > 7) || (c < 0) || (c > 7))
                break;
        
            sequence[seqLen] = state[r][c];
            seqLen++;
        }
        
        int count = 0;
        for (i = 0; i < seqLen; i++) {
            if (turn == 0) {
                if (sequence[i] == 2)
                    count ++;
                else {
                    if ((sequence[i] == 1) && (count > 0))
                        count = 20;
                    break;
                }
            }
            else {
                if (sequence[i] == 1)
                    count ++;
                else {
                    if ((sequence[i] == 2) && (count > 0))
                        count = 20;
                    break;
                }
            }
        }
        
        if (count > 10) {
            if (turn == 0) {
                i = 1;
                r = row+incy*i;
                c = col+incx*i;
                while (state[r][c] == 2) {
                    state[r][c] = 1;
                    i++;
                    r = row+incy*i;
                    c = col+incx*i;
                }
            }
            else {
                i = 1;
                r = row+incy*i;
                c = col+incx*i;
                while (state[r][c] == 1) {
                    state[r][c] = 2;
                    i++;
                    r = row+incy*i;
                    c = col+incx*i;
                }
            }
        }
    }
    
    public void changeColors(int row, int col, int turn) {
        int incx, incy;
        
        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    continue;
            
                flipTiles(row, col, incx, incy, turn);
            }
        }
    }
}