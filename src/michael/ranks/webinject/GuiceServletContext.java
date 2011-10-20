package michael.ranks.webinject;


import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.ParamException;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class GuiceServletContext extends GuiceServletContextListener {
	@SuppressWarnings("serial")
	@Singleton
	public final static class GuiceContainerCheat extends GuiceContainer{
		@Inject
		public GuiceContainerCheat(Injector injector) {
			super(injector);
	
		}
	}
	
	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ServletModule());
	}
	
	protected class ServletModule extends JerseyServletModule {
			@Override
			protected void configureServlets() {
				// service class for ranks
				bind(GuiceContainer.class).to(GuiceContainerCheat.class).asEagerSingleton();				
				bind(PersistenceMapper.class);
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Validate.class),
	                    new ValidationInterceptor());
			    
				Map<String, String> params = new HashMap<String, String>();
				params.put("com.sun.jersey.config.property.classnames", "michael.ranks.neo4j.GuiceServletjContest.PersistenceMapper.class");
			                   
				// Route all requests through GuiceContainer
				serve("/*").with(GuiceContainer.class, params);
			}
	}		

	@Provider 
	@Singleton
	public static class PersistenceMapper implements ExceptionMapper<ParamException> { 

	    @Override 
	    public Response toResponse(ParamException arg0) { 
	    	arg0.getParameterName();
	    	
	    	Response response = Response.serverError().entity("bad data type for:"+arg0.getParameterName()).type(MediaType.TEXT_PLAIN).build();
    	
			return response;
	    } 

	} 	
}
