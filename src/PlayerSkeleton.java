
import java.util.*;

public class PlayerSkeleton {
	
	Random rnd = new Random();
	
	// weight vector determines how much a heuristic should contribute to the utility
	public float[] w = {
							-1.0f, 	// punish for height of block
							1.187f, 	// reward for contact area
							-1.450f,  // punish for gaps
							0.665f 	// reward for clearing lines
						};

	//implement this function to have a working system
	public int[] pickMove(State s, int[][] legalMoves) 
	{
		//an array for storing scores for each legal move
		float[] scores = new float[legalMoves.length];
		//loop through all legal moves and compute score, move is of the form [orient, slot]
		for (int i = 0; i < legalMoves.length; i++)
		{
			scores[i] = computeScore(s, legalMoves[i]);
		}
		int bestMove = 0;
		float bestScore = -Float.MAX_VALUE;
		//find the move that gives the highest score
		for (int j = 0; j < scores.length; j++)
		{
			if (scores[j] == bestScore)
			{
				// flip a coin to decide if we want to use this move as the best
				if (rnd.nextFloat() > 0.5)
				{
					bestMove = j;
					bestScore = scores[j];
				}
			}
			else if (scores[j] > bestScore)
			{
				bestMove = j;
				bestScore = scores[j];
			}
		}
		return legalMoves[bestMove];
	}
	
	public float computeScore(State s, int[] move)
	{
		float score = 0;
		// add heuristics computation like this here
		// we favor filling lower positions
		float heightScore = computeHeightScore(s, move);
		float contactAreaScore = computeContactAreaScore(s, move);
		float gapScore = computeGapScore(s, move);
		float completeRowsScore = computeCompletedRowsScore(s, move);
		score += w[0] * heightScore;
		score += w[1] * contactAreaScore;
		score += w[2] * gapScore;
		score += w[3] * completeRowsScore;
		return score;
	}
	
// Heuristics
	public float computeHeightScore(State s, int[] move)
	// gives a score based on the midpoint of the landing height
	{
		float score = 0;
		int nextPiece = s.getNextPiece();
		int orient = move[0];
		int slot = move[1]; 
		int height = s.getTop()[slot] - State.getpBottom()[nextPiece][orient][0];
		for(int c = 1; c < State.getpWidth()[nextPiece][orient];c++) {
			height = Math.max(height, s.getTop()[slot+c] - State.getpBottom()[nextPiece][orient][c]);
		}
		int top = height;
		for(int i = 0; i < State.getpWidth()[nextPiece][orient]; i++) 
		{
			top = Math.max(top, height + State.getpTop()[nextPiece][orient][i] - State.getpBottom()[nextPiece][orient][i]);
		}
		score = (float) 0.5 * (height + top);
		return score;
	}
	
	public float computeContactAreaScore(State s, int[] move)
	// gives a score based on how many edges are in contact with other blocks
	// more contact is better, bottom contact is better than left/right contact
	{
		float score = 0;
		int nextPiece = s.getNextPiece();
		int orient = move[0];
		int slot = move[1]; 
		int height = s.getTop()[slot] - State.getpBottom()[nextPiece][orient][0];
		for(int c = 1; c < State.getpWidth()[nextPiece][orient];c++) {
			height = Math.max(height, s.getTop()[slot+c] - State.getpBottom()[nextPiece][orient][c]);
		}
		int [][] field = s.getField();
		for(int i = 0; i < State.getpWidth()[nextPiece][orient]; i++) {
			for(int h = height+State.getpBottom()[nextPiece][orient][i]; h < height+State.getpTop()[nextPiece][orient][i]; h++) {
				if (h >= State.ROWS) break;
				int left = i - 1 + slot;
				int right = i + 1 + slot;
				int down = h - 1;
				if (left >= 0)
				{
					if (field[h][left] != 0)
						score += 1;
				}
				else
					score += 1;
				if (right < State.COLS)
				{
					if (field[h][right] != 0)
						score += 1;
				}
				else
					score += 1;
				if (down >= 0)
				{
					if (field[down][i+slot] != 0)
						score += 1;
				}
			}
		}
		return score;
	}
	
	public float computeGapScore(State s, int[] move)
	// gives a score based on the number of gaps created by the move
	{
		float score = 0;
		int nextPiece = s.getNextPiece();
		int orient = move[0];
		int slot = move[1];
		int height = s.getTop()[slot] - State.getpBottom()[nextPiece][orient][0];
		for(int c = 1; c < State.getpWidth()[nextPiece][orient];c++) {
			height = Math.max(height, s.getTop()[slot+c] - State.getpBottom()[nextPiece][orient][c]);
		}
		int[][] field = arrayCopy2D(s.getField(), State.ROWS, State.COLS);
		int turn = s.getTurnNumber();
		for(int i = 0; i < State.getpWidth()[nextPiece][orient]; i++) {
			for(int h = height+State.getpBottom()[nextPiece][orient][i]; h < height+State.getpTop()[nextPiece][orient][i]; h++) {
				// boundary check
				if (h > 20) continue;
				field[h][i+slot] = turn;
				for (int y = h; y >= h - 2 && y >= 0; y--)
				{
					if (field[y][i+slot] == 0)
						score += 1;
				}
			}
		}
		return score;
	}
	
	public float computeCompletedRowsScore(State s, int[] move)
	//give a score based on number of completed rows
	{
		float score = 0;
		int nextPiece = s.getNextPiece();
		int orient = move[0];
		int slot = move[1];
		int height = s.getTop()[slot] - State.getpBottom()[nextPiece][orient][0];
		for(int c = 1; c < State.getpWidth()[nextPiece][orient];c++) {
			height = Math.max(height, s.getTop()[slot+c] - State.getpBottom()[nextPiece][orient][c]);
		}
		int[][] field = arrayCopy2D(s.getField(), State.ROWS, State.COLS);
		int turn = s.getTurnNumber();
		for(int i = 0; i < State.getpWidth()[nextPiece][orient]; i++) {
			for(int h = height+State.getpBottom()[nextPiece][orient][i]; h < height+State.getpTop()[nextPiece][orient][i]; h++) {
				// boundary check
				if (h > 20) continue;
				field[h][i+slot] = turn;
			}
		}
		int rowsCleared = 0;
		// how many units of the piece contributed to clearing the row
		int contribution = 0;
		for(int r = height+State.getpHeight()[nextPiece][orient]-1; r >= height; r--) {
			if (r > 20) continue;
			boolean full = true;
			for(int c = 0; c < State.COLS; c++) {
				if (field[r][c] == turn)
					contribution += 1;
				if(field[r][c] == 0) {
					full = false;
					contribution = 0;
					break;
				}
			}
			if(full) {
				rowsCleared++;
			}
		}
		score = rowsCleared * contribution;
		return score;
	}
//end Heuristics
	
	public int[][] arrayCopy2D(int[][] src, int rows, int cols)
	{
		int[][] copy = new int[rows][cols];
		for (int y = 0; y < rows; y++)
			for (int x = 0; x < cols; x++)
				copy[y][x] = src[y][x];
		return copy;
	}
	
	public void printField(int[][] field)
	{
		System.out.println("START");
		for (int i = field.length-1; i >= 0; i--)
		{
			int[] row = field[i];
			for (int space : row)
				System.out.print(space + " ");
			System.out.println();
		}
		System.out.println("END");
	}
	
	public static void main(String[] args) {
		Random rnd = new Random();
		int lastCleared = 0;
		int bestCleared = 0;
		float[] bestw = {};
		float[] lastw = {};
		int counter = 0;
		float[] w = { -1.0f, 1.187f, -1.450f, 0.665f };
		for (int i = 0; i < 25; i++)
		{
			State s = new State();
			new TFrame(s);
			PlayerSkeleton p = new PlayerSkeleton();
			
			p.w = w;
			
			while(!s.hasLost()) {
				s.makeMove(p.pickMove(s,s.legalMoves()));
				s.draw();
				s.drawNext(0,0);
				try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			System.out.println(i + ". You have completed "+s.getRowsCleared()+" rows. " + 
					String.format("w0:%.3f, w1:%.3f, w2:%.3f, w3:%.3f", w[0], w[1], w[2], w[3]));
			
//			if (s.getRowsCleared() > bestCleared)
//			{
//				bestCleared = s.getRowsCleared();
//				bestw = p.w;
//				int swap = rnd.nextInt(w.length);
//				for (int j = 0; j < swap; j++)
//				{
//					float noise = 2 * rnd.nextFloat() - 1.0f;
//					w[j] = (p.w[j] + w[j]) / 2 + (0.5f * noise);
//				}
//			}
//			else if (s.getRowsCleared() > lastCleared)
//			{
//				lastCleared = s.getRowsCleared();
//				lastw = p.w;
//				int swap = rnd.nextInt(w.length);
//				for (int j = 0; j < swap; j++)
//				{
//					float noise = 2 * rnd.nextFloat() - 1.0f;
//					w[j] = (p.w[j] + w[j]) / 2 + (0.5f * noise);
//				}
//			}
//			else
//			{
//				counter += 1;
//				w = lastw;
//				for (int j = 0; j < w.length; j++)
//				{
//					float noise = 2 * rnd.nextFloat() - 1.0f;
//					w[j] = (p.w[j] + w[j]) / 2 + (0.5f * noise);
//				}
//				if (counter >= 5 && lastCleared < bestCleared)
//				{
//					counter = 0;
//					w = bestw;
//				}
//			}
		}
//		System.out.println("You have completed BEST "+bestCleared+" rows. " + 
//				String.format("w0:%.3f, w1:%.3f, w2:%.3f, w3:%.3f", bestw[0], bestw[1], bestw[2], bestw[3]));
	}
}
