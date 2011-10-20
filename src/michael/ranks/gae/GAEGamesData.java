package michael.ranks.gae;


import java.util.ArrayList;
import java.util.List;

import michael.ranks.GamesData;
import michael.ranks.Player;

import com.google.inject.Inject;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;

public class GAEGamesData implements GamesData{
	@Inject ObjectifyFactory objFactory;
	
	@Override
	public List<Player> getWinners() {
		return getPlayers("-wins");
	}

	@Override
	public List<Player> getLosers() {
		return getPlayers("-loses");
	}

	protected List<Player> getPlayers(String orderBy) {
		List<Player> result = new ArrayList<Player>();
		
		Objectify objectify = objFactory.begin();
		for(GAEPlayer row : objectify.query(GAEPlayer.class).order(orderBy).fetch()) {
			Player player = new Player();
			player.loses = row.loses;
			player.name = row.name;
			player.wins = row.wins;
			
			result.add(player);
		}
		
		return result ;
	}
	@Override
	public void add(String player1, int score1, String player2, int score2) {

		// load player, update it and save it
		Objectify objectify = objFactory.begin();
		GAEPlayer gaePlayer1 = objectify.find(GAEPlayer.class, player1);
		if(gaePlayer1==null) {
			gaePlayer1 = new GAEPlayer();
			gaePlayer1.name = player1;
		}
		GAEPlayer gaePlayer2 = objectify.find(GAEPlayer.class, player2);
		if(gaePlayer2==null) {
			gaePlayer2 = new GAEPlayer();
			gaePlayer2.name = player2;
		}
		
		if(score1>score2) {
			gaePlayer1.wins++;
			gaePlayer2.loses++;
		} else {
			gaePlayer2.wins++;
			gaePlayer1.loses++;			
		}
		objectify.put(gaePlayer1);
		objectify.put(gaePlayer2);
	}

	@Override
	public void clearData() {
		Objectify objectify = objFactory.begin();
		objectify.delete(objectify.query(GAEPlayer.class).fetchKeys());
	}
}
