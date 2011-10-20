package michael.ranks.neo4j;

import java.util.SortedMap;
import java.util.TreeMap;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.transaction.LockManager;
import org.neo4j.kernel.impl.transaction.LockType;

import com.google.inject.Inject;

public class LocksUtil {
	private final EmbeddedGraphDatabase db;
	private Node playerIndexNode;
	 
	@Inject
	public LocksUtil(EmbeddedGraphDatabase db) {
		this.db = db;
		playerIndexNode = db.index().forNodes("indexNodes").query("index", "player").getSingle();
		if(playerIndexNode==null) {
			Transaction tx = db.beginTx();
			playerIndexNode = db.createNode();
			playerIndexNode.setProperty("dummy", "lock");
			db.index().forNodes("indexNodes").add(playerIndexNode, "index", "player");
			tx.success();
			tx.finish();
		}		
	}
	
	/**
	 * locks until tx is commited
	 */
	public void writeLockPlayerIndex() {
		LockManager lockManager = db.getConfig().getLockManager(); 
		lockManager.getWriteLock(playerIndexNode);
		db.getConfig().getLockReleaser().addLockToTransaction( playerIndexNode, LockType.WRITE );		
	}
	
	public void aquireReadLockPlayerIndex() {
		LockManager lockManager = db.getConfig().getLockManager(); 
		lockManager.getReadLock(playerIndexNode);			
	}

	public void releaseReadLockPlayerIndex() {
		LockManager lockManager = db.getConfig().getLockManager(); 
		lockManager.releaseReadLock(playerIndexNode, null);	
	}
	
	public void writeLockNodesInOrder(Node... nodes) {
		SortedMap<Long,Node> nodeMap = new TreeMap<Long,Node>();
		for (int i = 0; i < nodes.length; i++) {
			nodeMap.put(nodes[i].getId(), nodes[i]);
		}
		LockManager lockManager = db.getConfig().getLockManager(); 
		
		for (Node node : nodeMap.values()) {
			lockManager.getWriteLock(node);
			db.getConfig().getLockReleaser().addLockToTransaction(node, LockType.WRITE);			
		}
	}
}
