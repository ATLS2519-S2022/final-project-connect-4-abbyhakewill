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
 * Written:       	 4/29/2022
 * 
 * Credits:        	Apoorva Kanekal, Github, Kenzie Braun, Baeldung.com, Yosenspace.com, StackExchange, Lecture Slides
 **************************************************************************** */
/**
 * Player that utilizes the minimax algorithm concepts to look at an evaluate
 * future moves. Also incorporates iterative deepening search.
 * 
 * @author abby hakewill
 *
 */

import java.util.ArrayList;

public class MinimaxPlayer {
	private int id;
	private int enemyId;
	private int cols;
	
	public String name() {
		return "miniMax";
	}
//tells the current state of the connect 4 board
	public void init(int id, int msecPerMove, int rows, int cols) {
		this.id = id; //my player id
		this.enemyId = 3 - id; //opponent's player id
		this.cols = cols;
	}
	
	public void calcMove(Connect4Board board, int oppMoveCol, Arbitrator arb) throws TimeUpException{
		if(board.isFull())
			throw new Error ("Error: the board is full!");

		GameTree root = new GameTree(-1, board);

		for(int i = 0; i < cols; i++) {
			if(!board.isColumnFull(i)){
				board.move(i, id);
				root.addChild(i, new Connect4Board(board));
				board.unmove(i, id);
			}
		}

		int searchDepth = 1; //sets maximum search depth to 1

		while (!arb.isTimeUp() && searchDepth <= board.numEmptyCells()) {
			minimax(root, searchDepth, true, arb);
			arb.setMove(root.chosenMove);
			searchDepth++; //increase search depth
		}
	}


	private int minimax(GameTree node, int depth, boolean maxminizingPlayer, Arbitrator arb) {
		if (depth == 0 || node.isTerminal() || arb.isTimeUp()) {
			node.value = evaluateNode(node);
			return node.value;
		}

		if (node.isLeaf()) {

			int moveId = maxminizingPlayer ? id : enemyId;

			for(int i = 0; i < cols; i++) {
				if(!node.board.isColumnFull(i)) {
					node.board.move(i, moveId);

					node.addChild(i, new Connect4Board(node.board));
					node.board.unmove(i, moveId);

				}
			}
		}

		if(maxminizingPlayer) {
			int value = Integer.MIN_VALUE;
			for(GameTree child: node.children) {
				int newVal = minimax(child, depth -1, false, arb);
				if(newVal > value) {
					value = newVal;
					node.value = value;
					node.chosenMove = child.move;
				}
				else if(newVal == value) {
					int currMoveDistFromCenter = Math.abs(cols/2 - node.chosenMove);
					int newMoveDistFromCenter = Math.abs(cols/2 - child.move);
					if(newMoveDistFromCenter < currMoveDistFromCenter)
						node.chosenMove = child.move;
				}
			}
			return value;
		}
		else {
			int value = Integer.MAX_VALUE;
			for(GameTree child: node.children) {
				int newVal = minimax(child, depth -1, true, arb);
				if(newVal < value) {
					value = newVal;
					node.value = value;
					node.chosenMove = child.move;
				}
				else if (newVal == value) {
					int currMoveDistFromCenter = Math.abs(cols/2 - node.chosenMove);
					int newMoveDistFromCenter = Math.abs(cols/2 - child.move);
					if(newMoveDistFromCenter < currMoveDistFromCenter)
						node.chosenMove = child.move;
				}

			}
			return value;
		}

	}

//evaluates the board and the turns that have been taken
	private int evaluateNode(GameTree node) {
		int myScore = calcScore(node.board, id);
		int oppScore = calcScore(node.board, enemyId);
		return myScore - oppScore;
	}

//heuristic evaluation function for the current board
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

	private class GameTree{
		private Connect4Board board;
		private int move;
		private ArrayList<GameTree> children;
		private int chosenMove;
		private int value;

		public GameTree(int move, Connect4Board board) {
			this.move = move;
			this.board = board;
			children = new ArrayList<GameTree>();
		}

		public void addChild(int move, Connect4Board board) {
			children.add(new GameTree(move,board));
		}

		public boolean isLeaf() {
			return children.size() == 0;
		}

		public boolean isTerminal() {
			return board.isFull();
		}
	}
}
