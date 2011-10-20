package michael.ranks;

import java.util.List;

public interface GamesData {
	
	public List<Player> getWinners();
	
	public List<Player> getLosers();

	public void add(String player1, int score1, String player2, int score2);
	
	public void clearData();
}
