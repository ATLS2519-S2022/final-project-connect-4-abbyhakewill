/* *****************************************************************************
 * 
 * Title:            Connect4
 * Files:            Connect4.java, Connect4Game.java, Connect4Board.java,
 * 					 Arbitrator.java, TimeUpException.java, PlayerThread.java, Player.java
 * 					 HumanPlayer.java, RandomPlayer.java, AlphaBetaPlayer.java, GreedyPlayer.java, MinimaxPlayer.java
 * Semester:         Spring 2022
 * 
 * Author:           Abby Hakewill abha5846@colorado.edu
 * 
 * Description:		 An application that allows humans and AI agents to play Connect 4
 * 					 and a Connect 4 variant where the game goes on until the board is
 * 				     completely full and the winner is the player with the most Connect 4s.
 * 
 * Written:       	 4/26/2022
 * 
 * Credits:        	Apoorva Kanekal, Github, Recitation, DZone.com
 **************************************************************************** */
/**
 * Player that only looks one move into the future. Also incorporates a simple 
 * heuristic evaluation function to analyze which move has the highest 
 * calculated score within a smaller range of options. This will provide us with
 * the "local optimum" as opposed to exploring all possible options in-depth.
 * 
 * @author abby hakewill
 *
 */

public class GreedyPlayer implements Player {
	private int id;
	private int enemyId;
	private int cols;
	
	private Move[] possibleMoves;
	
	public String name() {
		return "GreedyBot"; //names enemy player
	}
	
//discerns player ids, sets the amount of time for each move, and sets the size of the board
	public void init(int id, int msecPerMove, int rows, int cols) {
		this.id = id;
		enemyId = 3-id; //opponent's id
		this.cols = cols;
	}

//tells the current state of the connect 4 board, tells me where my opponent's recent move was, and includes (arb)
//which tells the game which move i'm going to make
	public void calcMove(Connect4Board board, int oppMoveCol, Arbitrator arb) throws TimeUpException {
		if (board.isFull())
			throw new Error ("Error: The board is full");
		
		possibleMoves = new Move[cols];
		
		for(int c = 0; c < cols; c++) {
			if(board.isValidMove(c)) {
				board.move(c,  id); //temporarily makes my proposed move
				int moveValue = evaluateBoard(board, id, enemyId);
				possibleMoves[c] = new Move(c, moveValue);
				board.unmove(c, id); //undoes the move that i tested before
			}
		}
		Move bestMove = null;

		for (int i = 0; i < possibleMoves.length; i++) {
			if(bestMove == null)
				bestMove = possibleMoves[i];

			else if (possibleMoves[i] != null && bestMove.compareTo(possibleMoves[i]) < 0)
				bestMove = possibleMoves[i];
		}
		arb.setMove(bestMove.column);
	}
	
//begin heuristic evaluation by evaluating the enemy score in comparison to my score
	private int evaluateBoard(Connect4Board board, int myId, int enemyId) {
		int myScore = calcScore(board, myId);
		int enemyScore = calcScore(board, enemyId);
		return myScore - enemyScore;
	}


	private int calcScore(Connect4Board board, int id) {
		final int rows = board.numRows();
		final int cols = board.numCols();
		int score = 0;

		//horizontal check
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c <= cols - 4; c++) {
				if(board.get(r, c + 0) != id) continue;
				if(board.get(r, c + 1) != id) continue;
				if(board.get(r, c + 2) != id) continue;
				if(board.get(r, c + 3) != id) continue;
				score++;
			}
		}

		//vertical check
		for (int c = 0; c < cols; c++) {
			for (int r = 0; r <= rows - 4; r++) {
				if(board.get(r + 0, c) != id) continue;
				if(board.get(r + 1, c) != id) continue;
				if(board.get(r + 2, c) != id) continue;
				if(board.get(r + 3, c) != id) continue;
				score++;
			}
		}

		//diagonal check
		for (int c = 0; c <= cols - 4; c++) {
			for (int r = 0; r <= rows - 4; r++) {
				if(board.get(r + 0, c + 0) != id) continue;
				if(board.get(r + 1, c + 1) != id) continue;
				if(board.get(r + 2, c + 2) != id) continue;
				if(board.get(r + 3, c + 3) != id) continue;
				score++;
			}
		}

		//other diagonal check
		for (int c = 0; c <= cols - 4; c++) {
			for (int r = rows -1; r >= 3; r--) {
				if (board.get(r - 0, c + 0) != id) continue;
				if (board.get(r - 1, c + 1) != id) continue;
				if (board.get(r - 2, c + 2) != id) continue;
				if (board.get(r - 3, c + 3) != id) continue;
				score++;
			}
		}
		return score;

	}

//implements comparable interface and compareTo() to compare the values of each move
	private class Move implements Comparable<Move>{
		private int column;
		private int value;

		public Move(int column, int value) {
			this.column = column;
			this.value = value;
		}

		public int compareTo(Move other) {
			return Integer.compare(this.value, other.value);
		}
	
	}
}
