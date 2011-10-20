package michael.ranks.neo4j;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import michael.ranks.Player;
import michael.ranks.neo4j.KeyConstants.Fields;
import michael.ranks.neo4j.KeyConstants.Indexes;
import michael.ranks.neo4j.KeyConstants.Outcomes;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.lucene.QueryContext;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GamesData implements michael.ranks.GamesData {
	@Inject Neo4jPlayerData playerData;
	@Inject LocksUtil locksUtil;
	@Inject	EmbeddedGraphDatabase db;
	
	public List<Player> getWinners() {
		return getSortedPlayers(Fields.WINS);
	}

	public List<Player> getLosers() {
		return getSortedPlayers(Fields.LOSES);
	}

	//TODO: to preserve memory, it should return an iterator based on IndexHits instead of loading all into memory.
	protected List<Player> getSortedPlayers(Fields field) {
		List<Player> ranks = new ArrayList<Player>();
		
   	    IndexHits<Node> players = playerData.playerIndex().query(field.toString(), new QueryContext("*").sort(new Sort( 
				  new SortField(field.toString(), SortField.INT, true )))); 
				          
		for (Node node : players) {
			Player player = new Player();
			player.name = (String)node.getProperty(Fields.NAME.toString());
			player.wins = (Integer)node.getProperty(Fields.WINS.toString());
			player.loses = (Integer)node.getProperty(Fields.LOSES.toString());
			ranks.add(player);
		}
		return ranks;		
	}
		
	public Iterator<Relationship> getWinningGames(String player){
		IndexHits<Relationship> indexes = db.index().forRelationships("Outcomes").get(Indexes.GAME_OUTCOME.toString(), "winner");
		
		return indexes;
	}
	
	public void add(String player1, int score1, String player2, int score2) {
		if(score1==score2) {
			// sorry, we ignore ties.
			return;
		}
		Node winner = playerData.findPlayer(player1);
		Node loser = playerData.findPlayer(player2);
		
		Transaction tx = db.beginTx();
		try {
			GameOutcome gameOutcome = saveGameOutcome(winner, score1, loser, score2);
			winner = gameOutcome.winner;
			loser = gameOutcome.loser;
			
			locksUtil.writeLockNodesInOrder(winner, loser);
			
			// refresh while inside the new write lock
			winner = db.getNodeById(winner.getId());
			loser = db.getNodeById(loser.getId());
			
			int winnerWins = (Integer)winner.getProperty(Fields.WINS.toString())+1;
			int loserLoses = (Integer)loser.getProperty(Fields.LOSES.toString())+1;
			
			winner.setProperty(Fields.WINS.toString(), winnerWins);
			loser.setProperty(Fields.LOSES.toString(), loserLoses);
			
			Index<Node> playerIndex = playerData.playerIndex(); 

			playerIndex.remove(winner, Fields.WINS.toString());
			playerIndex.add(winner, Fields.WINS.toString(), Integer.valueOf(winnerWins));
			
			playerIndex.remove(loser, Fields.LOSES.toString());
			playerIndex.add(loser, Fields.LOSES.toString(), Integer.valueOf(loserLoses));
			
			tx.success();
		} finally {
			tx.finish();
		}
	}
	
	protected GameOutcome saveGameOutcome(Node player1, int score1, Node player2, int score2) {
		GameOutcome gameOutcome = new GameOutcome();
	
		Transaction tx = db.beginTx();
		try {
			Node node = db.createNode();
			if(score1>score2) {
				gameOutcome.winner = player1;
				gameOutcome.loser = player2;
				gameOutcome.winScore = score1;
				gameOutcome.loseScore = score2;
				node.setProperty("winnerScore", score1);
				node.setProperty("loserScore", score2);
			} else {
				gameOutcome.winner = player2;
				gameOutcome.loser = player1;
				gameOutcome.winScore = score2;
				gameOutcome.loseScore = score1;				
				node.setProperty("winnerScore", score2);
				node.setProperty("loserScore", score1);
			}
			gameOutcome.node = node;
			// index the game out
			gameOutcomeIndex().add(node, Fields.ID.toString(), node.getId());
			gameOutcome.node.createRelationshipTo(gameOutcome.winner, Outcomes.WINNER);
			gameOutcome.node.createRelationshipTo(gameOutcome.loser, Outcomes.LOSER);
			tx.success();
		} finally {
			tx.finish();
		}
		return gameOutcome;
	}
	
	public void clearData() {
		IndexHits<Node> allGames = gameOutcomeIndex().query(Fields.ID.toString(),"*");
		
		Transaction tx = db.beginTx();
		try
		{
			for (Node node : allGames) {
				for(Relationship relation : node.getRelationships()) {
					relation.delete();
				}
				node.delete();
			}
			
			gameOutcomeIndex().delete();
			tx.success();
		} finally {
			tx.finish();
		}
		playerData.clearData();
	}
	
	private Index<Node> gameOutcomeIndex() {
		return db.index().forNodes(Indexes.GAME_OUTCOME.toString());
	}	
}
