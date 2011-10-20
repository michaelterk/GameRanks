package michael.ranks.neo4j;


import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.inject.servlet.GuiceFilter;


public class WebServerNeo4j {
	public static void main(String... args) throws Exception {
	      // Create the server.
	      Server server = new Server(8080);
	       
	      // Create a servlet context and add the jersey servlet.
	      ServletContextHandler sch = new ServletContextHandler(server, "/");
	       
	      // Add our Guice listener that includes our bindings
	      sch.addEventListener(new Neo4JGuiceServletContext());
	       
	      // Then add GuiceFilter and configure the server to 
	      // reroute all requests through this filter. 
	      sch.addFilter(GuiceFilter.class, "/*", null);
	       
	      // Must add DefaultServlet for embedded Jetty. 
	      // Failing to do this will cause 404 errors.
	      sch.addServlet(DefaultServlet.class, "/");
	      
	      ResourceHandler publicDocs = new ResourceHandler();
	      publicDocs.setResourceBase("./gae-war");

	      HandlerList hl = new HandlerList();
	      hl.setHandlers(new Handler[]{publicDocs, sch});
	      server.setHandler(hl);
	      
	      // Start the server
	      server.start();
	      registerShutdownHook(server);
	      server.join();		
	}
	
	private static void registerShutdownHook(final Server server)	{
	    Runtime.getRuntime().addShutdownHook( new Thread() {
	        @Override
	        public void run()
	        {
	            try {
					server.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
	        }});
	}	
}
