package ReversiRandom;

import kernel.SimpleKernelExecutor;
import network.FeedForwardNetwork;
import network.NetworkExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

class NeuralNetworkAI {

	private static final int BOARD_SIDE = 8;
	private static final int BOARD_SIZE = 64;

    public Socket s;
	public BufferedReader sin;
	public PrintWriter sout;
    Random generator = new Random();

    double t1, t2;
    double turnLength = 0;
    int me;
    int boardState;
    int state[][] = new int[8][8]; // state[0][0] is the bottom left corner of the board (on the GUI)
    int turn = -1;
    int round;

    ArrayList<Integer> validMoves = new ArrayList<Integer>();

	// create a network with 3 boards worth of info: own pieces, opponent pieces, and valid spots.
	// There are 3 hidden layers with 256 nodes each.
	// The output is one boards worth of outputs; each representing a best guess.
	// The output node with the highest valid value becomes the selected action.
	private FeedForwardNetwork network = new FeedForwardNetwork(BOARD_SIZE * 3, 20, 20, 20, BOARD_SIZE);
	private NetworkExecutor executor = new NetworkExecutor(network, new SimpleKernelExecutor());
	float[] input = new float[network.getInputCount()];
	float[] expected = new float[BOARD_SIZE];

    // main function that (1) establishes a connection with the server, and then plays whenever it is this player's turn
    public NeuralNetworkAI(int _me, String host) {
        me = _me;
        initClient(host);

        int myMove;
        
        while (true) {
            //System.out.println("Read");
            readMessage();
            if (me == 1) {						//TODO: Fix so it won't re-update every time - check if round has changed
            	turnLength = t1/(40-round/2);
            }
            else {
           		turnLength = t2/(40-round/2);
            }
            if (turn == me) {
                //System.out.println("Move");
                validMoves = getValidMoves(round, state, me);
                
                myMove = move();
                //myMove = generator.nextInt(numValidMoves);        // select a move randomly
                
                //String sel = validMoves.get(myMove) / 8 + "\n" + validMoves.get(myMove) % 8;
                String sel = myMove / 8 + "\n" + myMove % 8;
                
                //System.out.println("Selection: " + myMove / 8 + ", " + myMove % 8);
                
                sout.println(sel);
            }
        }
    }
    
    // You should modify this function
    // validMoves is a list of valid locations that you could place your "stone" on this turn
    // Note that "state" is a global variable 2D list that shows the state of the game
    private int move() {
    	
        // just move randomly for now
        //int myMove = generator.nextInt(validMoves.size());
        int myMove = -1;
        ArrayList<GameState> bottomLevel = new ArrayList<GameState>();
        GameState rootState = new GameState(this.state, -1, me, null);
        bottomLevel.add(rootState);
        
        int startRound = round;
        
        for (int i = 0; i < 3; i++) {
        	ArrayList<GameState> bottomLevelTemp = new ArrayList<GameState>();
        	for (GameState currState : bottomLevel) {
        		expandTree(currState, bottomLevelTemp, startRound);
        	}
        	bottomLevel = bottomLevelTemp;
        	startRound += 2;
        }
        
        //run search
        double alpha = -Double.MAX_VALUE;
        double beta = Double.MAX_VALUE;
        maximinSearch(rootState, alpha, beta);
    	for (GameState gs : rootState.children) {
    		if (gs.heuristic == rootState.heuristic) {
    			myMove = gs.move;
    			break;
    		}
    	}

		setOwnInput(input);
		setOpponentInput(input);
		setValidSpaces(input);

		int index = 0;
		for (int i = 0; i < 3; i++) {
			for (int x = 0; x < BOARD_SIDE; x++) {
				for (int y = 0; y < BOARD_SIDE; y++) {
					System.out.printf("%.0f ",input[index++]);
				}
				System.out.println();
			}
			System.out.println();
		}

		expected[myMove] = 1;
		float error = 0;
		for (int i = 0; i < 500; i++) {
			 error = executor.train(input, expected);
		}
		System.out.println("error is: " + error);
		expected[myMove] = 0;
        
        return myMove;
    }

	private void setOwnInput(float[] input) {
		int i = 0;
		for (int x = 0; x < BOARD_SIDE; x++) {
			for (int y = 0; y < BOARD_SIDE; y++) {

				input[i] = state[x][y] == me ? 1 : 0;
				i++;
			}
		}
	}

	private void setOpponentInput(float[] input) {

		int i = BOARD_SIZE;
		for (int x = 0; x < BOARD_SIDE; x++) {
			for (int y = 0; y < BOARD_SIDE; y++) {

				input[i] = (state[x][y] != me) && (state[x][y] != 0) ? 1 : 0;
				i++;
			}
		}
	}

	private void setValidSpaces(float[] input) {

		int offset = BOARD_SIZE + BOARD_SIZE;

		// clear old values
		for (int i = 0; i < BOARD_SIZE; i++)
			input[offset + i] = 0;

		for (int i = 0; i < validMoves.size(); i++)
			input[offset + validMoves.get(i)] = 1;
	}
    
    double maximinSearch(GameState root, double alpha, double beta) {		//add alpha/beta values for pruning
    	if (root.children.get(0).children.size() == 0) {		//base case is pennultimate level of tree
    		root.calculateHeuristic(me);
    	}
    	else {
    		if (root.me == this.me) {
    			double max = -Double.MAX_VALUE;
    			for (GameState child : root.children) {
    				max = Math.max(max, maximinSearch(child, alpha, beta));
    				alpha = Math.max(alpha, max);
    				if (beta <= alpha)
    					break;
    			}
    			root.heuristic = max;
    		}
    		else {
    			double min = Double.MAX_VALUE;
    			for (GameState child : root.children) {
    				min = Math.min(min, maximinSearch(child, alpha, beta));
    				beta = Math.min(beta, min);
    				if (beta <= alpha)
    					break;
    			}
    			root.heuristic = min;
    		}
    	}
    	return root.heuristic;
    }
    
    private void expandTree(GameState currState, ArrayList<GameState> bottomLevelTemp, int startRound) {
    	ArrayList<Integer> moves = getValidMoves(startRound, currState.state, me);
    	if (moves.size() == 0) {
    		moves.add(-1);
    	}
        for (Integer move : moves) {
        	GameState child = new GameState(this.state, move, me, currState);
        	currState.children.add(child);
        	
        	ArrayList<Integer> childMoves = getValidMoves(startRound+1, child.state, 3-me);
        	if (childMoves.size() == 0 && move != -1) {
        		childMoves.add(-1);
        	}
        	for(Integer childMove : childMoves) {
        		GameState grandChild = new GameState(child.state, childMove, 3-me, child);
        		child.children.add(grandChild);
        		bottomLevelTemp.add(grandChild);
        	}
        }
    }
    
    // generates the set of valid moves for the player; returns a list of valid moves (validMoves)
    private ArrayList<Integer> getValidMoves(int round, int state[][], int player) {
        int i, j;
        
        ArrayList<Integer> valMoves = new ArrayList<Integer>();
        if (round < 4) {
            if (state[3][3] == 0) {
                valMoves.add(3*8 + 3);
            }
            if (state[3][4] == 0) {
            	valMoves.add(3*8 + 4);
            }
            if (state[4][3] == 0) {
            	valMoves.add(4*8 + 3);
            }
            if (state[4][4] == 0) {
            	valMoves.add(4*8 + 4);
            }
            //System.out.println("Valid Moves:");
        }
        else {
            //System.out.println("Valid Moves:");
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    if (state[i][j] == 0) {
                        if (couldBe(state, i, j, player)) {
                            valMoves.add(i*8 + j);
                            //System.out.println(i + ", " + j);
                        }
                    }
                }
            }
        }
        return valMoves;
        
        //if (round > 3) {
        //    System.out.println("checking out");
        //    System.exit(1);
        //}
    }
    
    private boolean checkDirection(int state[][], int row, int col, int incx, int incy, int player) {
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
            if (player == 1) {
                if (sequence[i] == 2)
                    count ++;
                else {
                    if ((sequence[i] == 1) && (count > 0))
                        return true;
                    break;
                }
            }
            else {
                if (sequence[i] == 1)
                    count ++;
                else {
                    if ((sequence[i] == 2) && (count > 0))
                        return true;
                    break;
                }
            }
        }
        
        return false;
    }
    
    private boolean couldBe(int state[][], int row, int col, int player) {
        int incx, incy;
        
        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    continue;
            
                if (checkDirection(state, row, col, incx, incy, player))
                    return true;
            }
        }
        
        return false;
    }
    
    public void readMessage() {
        int i, j;
        try {
            //System.out.println("Ready to read again");
            turn = Integer.parseInt(sin.readLine());
            
            if (turn == -999) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
                
                System.exit(1);
            }
            
            //System.out.println("Turn: " + turn);
            round = Integer.parseInt(sin.readLine());
            t1 = Double.parseDouble(sin.readLine());
            //System.out.println(t1);
            t2 = Double.parseDouble(sin.readLine());
            //System.out.println(t2);
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    state[i][j] = Integer.parseInt(sin.readLine());
                }
            }
            sin.readLine();
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
        
        //System.out.println("Turn: " + turn);
        //System.out.println("Round: " + round);
        //System.out.println();
    }
    
    public void initClient(String host) {
        int portNumber = 3333+me;
        
        try {
			s = new Socket(host, portNumber);
            sout = new PrintWriter(s.getOutputStream(), true);
			sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            sin.readLine();
            //System.out.println(info);
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    
    // compile on your machine: javac *.java
    // call: java RandomGuy [ipaddress] [player_number]
    //   ipaddress is the ipaddress on the computer the server was launched on.  Enter "localhost" if it is on the same computer
    //   player_number is 1 (for the black player) and 2 (for the white player)
    public static void main(String args[]) {
        new NeuralNetworkAI(Integer.parseInt(args[1]), args[0]);
    }
    
}
