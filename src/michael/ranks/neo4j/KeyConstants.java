package michael.ranks.neo4j;

import org.neo4j.graphdb.RelationshipType;

public class KeyConstants {
	public static enum Outcomes implements RelationshipType{
		WINNER, LOSER;
	}	
	public static enum Indexes {
		GAME_OUTCOME, PLAYER, WINS, LOSES;
	}
	public static enum Fields {
		NAME, ID, WINS, LOSES
	}
}
