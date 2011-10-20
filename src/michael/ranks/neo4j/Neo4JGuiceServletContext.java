package michael.ranks.neo4j;

import michael.ranks.RanksRestlet;
import michael.ranks.webinject.GuiceServletContext;

import org.neo4j.kernel.EmbeddedGraphDatabase;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class Neo4JGuiceServletContext extends GuiceServletContext {
	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new GuiceServletContext.ServletModule() {
			@Override
			protected void configureServlets() {
				bind(michael.ranks.GamesData.class).to(GamesData.class);
				bind(Neo4jPlayerData.class);
				
		    	// bind REST controllers
				bind(RanksRestlet.class);

				super.configureServlets();
			}
			
			@Singleton
			@Provides
			public EmbeddedGraphDatabase dbService() {
				return (new DBServer()).start();
			}
		});
	}

}
