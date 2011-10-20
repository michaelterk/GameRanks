package michael.ranks.neo4j;


import michael.ranks.neo4j.KeyConstants.Fields;
import michael.ranks.neo4j.KeyConstants.Indexes;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Neo4jPlayerData {
	@Inject LocksUtil locksUtil;
	@Inject EmbeddedGraphDatabase db;
	
	public Node findPlayer(String name) {
	
		locksUtil.aquireReadLockPlayerIndex();
		Node node = playerIndex().get(Fields.NAME.toString(), name).getSingle();
		locksUtil.releaseReadLockPlayerIndex();
		
		if (node==null) {
			node=createPlayer(name);
		}
		
		return node;
	}

	public Node createPlayer(String name) {
		Transaction tx=db.beginTx();
		Node node;
		
		try {
			locksUtil.writeLockPlayerIndex();
			
			node = playerIndex().get(Fields.NAME.toString(), name).getSingle(); 
			if(node!=null) return node;
			node = db.createNode();		

			node.setProperty(Fields.NAME.toString(), name);
			node.setProperty(Fields.WINS.toString(), Integer.valueOf(0));
			node.setProperty(Fields.LOSES.toString(),Integer.valueOf(0));
			
			playerIndex().add(node, Fields.NAME.toString(), name);
			playerIndex().add(node, Fields.WINS.toString(),Integer.valueOf(0));
			playerIndex().add(node, Fields.LOSES.toString(), Integer.valueOf(0));
			tx.success();
		} finally {
			tx.finish();
		}
		
		return node;
	}

	public void clearData() {
		IndexHits<Node> allPlayers = playerIndex().query(Fields.NAME.toString(),"*");
		
		Transaction tx = db.beginTx();
		try
		{
			for (Node node : allPlayers) {
				for(Relationship relation : node.getRelationships()) {
					relation.delete();
				}
				node.delete();
			}
			
			playerIndex().delete();
			tx.success();
		} finally {
			tx.finish();
		}		
	}
	
	Index<Node> playerIndex() {
		return db.index().forNodes(Indexes.PLAYER.toString());
	}	
}
