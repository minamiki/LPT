
import java.util.*;

public class PlayerSkeleton {
	
	Random rnd = new Random();
	
	// weight vector determines how much a heuristic should contribute to the utility
	float[] w = { 
					1.0f, // for height of block
					2.5f  // for contact area
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
		float bestScore = Float.MIN_VALUE;
		//find the move that gives the highest score
		for (int j = 0; j < scores.length; j++)
		{
			if (scores[j] == bestScore)
			{
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
		score += w[0] * computeHeightScore(s, move);
		score += w[1] * computeContactAreaScore(s, move);
		return score;
	}
	
	// Heuristics
	public float computeHeightScore(State s, int[] move)
	// gives a score based on the height
	// lower height is better
	{
		float score = 0;
		int pieceID = s.getNextPiece();
		int height = s.getTop()[move[1]]-State.getpBottom()[pieceID][move[0]][0];
		for(int c = 1; c < State.getpWidth()[pieceID][move[0]];c++) 
		{
			height = Math.max(height,s.getTop()[move[1]+c]-State.getpBottom()[pieceID][move[0]][c]);
		}
		score = (float) Math.pow(State.ROWS - height, 3);
		return score;
	}
	
	public float computeContactAreaScore(State s, int[] move)
	// gives a score based on how many edges are in contact with other blocks
	// more contact is better
	{
		float score = 0;
		int pieceID = s.getNextPiece();

		int height = s.getTop()[move[1]]-State.getpBottom()[pieceID][move[0]][0];
		int[][] field = s.getField().clone();
		for(int c = 1; c < State.getpWidth()[pieceID][move[0]];c++) 
		{
			height = Math.max(height,s.getTop()[move[1]+c]-State.getpBottom()[pieceID][move[0]][c]);
		}
		//for each block that will be filled in, check contact area
		for(int i = 0; i < State.getpWidth()[pieceID][move[0]]; i++) 
		{
			for(int h = height+State.getpBottom()[pieceID][move[0]][i]; h < height+State.getpTop()[pieceID][move[0]][i]; h++) 
			{
				// boundary check
				if (h > 20) continue;
				int left = Math.max(move[1], move[1]-1);
				int right = Math.min(move[1]+1, 9);
				int down = Math.max(h, h-1);
				if (field[h][left] != 0)
					score += 1;
				if (field[h][right] != 0)
					score += 1;
				if (field[down][move[1]] != 0)
					score += 1;
			}
		}
		return score;
	}
	//end Heuristics
	
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
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}
