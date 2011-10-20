package michael.ranks.gae;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import michael.ranks.GamesData;
import michael.ranks.RanksRestlet;
import michael.ranks.webinject.GuiceServletContext;
import michael.ranks.webinject.Validate;
import michael.ranks.webinject.ValidationInterceptor;

import com.google.appengine.api.datastore.ReadPolicy.Consistency;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.GuiceServletContextListener;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyOpts;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class GAEGuiceServletContext extends GuiceServletContext {
	
	protected Collection<Class<?>> objectifyEntitiesToRegister() {
		List<Class<?>> entities = new ArrayList<Class<?>>();
		
		entities.add(GAEPlayer.class);
		
		return entities;
	}
	
	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new GuiceServletContext.ServletModule() {
			@Override
			protected void configureServlets() {
				// service class for ranks
				bind(GamesData.class).to(GAEGamesData.class);
				
		    	// bind REST controllers
				bind(RanksRestlet.class);

				super.configureServlets();
			}
			
			
			@SuppressWarnings("unused")
			@Provides
			@Singleton
			protected ObjectifyFactory createFactory(Injector injector) {
				final ObjectifyOpts opts = new ObjectifyOpts();
				opts.setConsistency( Consistency.EVENTUAL);
				opts.setGlobalCache(true);
				ObjectifyFactory factory = new ObjectifyFactory() {
					@Override
					public Objectify begin() {
						return super.begin(opts);
					}
				};

				injector.injectMembers(factory);
				
				for(Class<?> entity : objectifyEntitiesToRegister()) {
					factory.register(entity);	
				}
				
				return factory;
			}				
		});
	}
}
