package michael.ranks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import michael.ranks.webinject.Validate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.multipart.FormDataParam;

@Path("/ranks/")
@Singleton
public class RanksRestlet{
	@Inject	GamesData gamesData;
	
	@GET
	@Path("/winners")
	public StreamingOutput listWinners() {
		final List<Player> ranksList = gamesData.getWinners();
		
	    return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				OutputStreamWriter writer = new OutputStreamWriter(output); 

				for (Player rank: ranksList) {
					writer.write("<p>"+rank.name+":"+rank.wins+" wins</p>\n");
				}
				writer.flush();
			}
	    };
	}

	@GET
	@Path("/losers")
	public StreamingOutput listLosers() {
		final List<Player> ranksList = gamesData.getLosers();
		
	    return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				OutputStreamWriter writer = new OutputStreamWriter(output); 

				for (Player rank: ranksList) {
					writer.write("<p>"+rank.name+":"+rank.loses+" loses</p>\n");
				}
				writer.flush();
			}
	    };
	}
	
	@POST
	@Validate({Validate.Rules.NOT_EMPTY, Validate.Rules.DATATYPE})
	public Response saveGame(@FormParam("player1") String player1,
							 @FormParam("player2") String player2,
							 @FormParam("score1") int score1,
							 @FormParam("score2") int score2) throws URISyntaxException {
		gamesData.add(player1, score1, player2, score2);
        return Response.seeOther(URI.create("/ranks/winners")).build();
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadResults(@FormDataParam("file")InputStream file) throws URISyntaxException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		String line;
		while((line = reader.readLine())!=null) {
			StringTokenizer st = new StringTokenizer(line,",");
			try {
				gamesData.add(st.nextToken(), Integer.parseInt(st.nextToken()), st.nextToken(), Integer.parseInt(st.nextToken()));
			} catch (NumberFormatException e) {
			}
		}
		
		return Response.seeOther(URI.create("/ranks/winners")).build();
	}	
	

	/*
	@POST 
	@Path("/uploadThreaded")
	@Consumes(MediaType.MULTIPART_FORM_DATA)	
	public void threadedUploadResults(@FormDataParam("file")InputStream file) throws IOException, InterruptedException, ExecutionException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(0, 8, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(8));
		Queue<Future<?>> queue = new ArrayQueue<Future<?>>(8);
		
		String readerLine;
		while((readerLine = reader.readLine())!=null) {
			final String line = readerLine;
			Future<?> future = poolExecutor.submit(new Runnable() {
				@Override
				public void run() {
					StringTokenizer st = new StringTokenizer(line,",");
					try {
						gamesData.add(st.nextToken(), Integer.parseInt(st.nextToken()), st.nextToken(), Integer.parseInt(st.nextToken()));
					} catch (NumberFormatException e) {
					}
				}
			});
			if(queue.size()>=8) {
				queue.poll();
			}
			queue.add(future);
		}
		for (Future<?> future : queue) {
			future.get();
		}
		poolExecutor.shutdown();
	}*/
	
	@POST
	@Path("/clear")
	public Response clearAll() {
		gamesData.clearData();
		return Response.seeOther(URI.create("/")).build();
	}
}
