package michael.ranks.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class DBServer {
	public EmbeddedGraphDatabase  start() {
		org.neo4j.kernel.EmbeddedGraphDatabase db = new EmbeddedGraphDatabase("./Neo4jdb/default.db");
		registerShutdownHook(db);

		return db;
	}
	
	private void registerShutdownHook(final GraphDatabaseService db )	{
	    Runtime.getRuntime().addShutdownHook( new Thread() {
	        @Override
	        public void run()
	        {
	            db.shutdown();
	        }});
	}
}
