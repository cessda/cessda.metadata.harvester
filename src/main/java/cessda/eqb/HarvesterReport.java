package cessda.eqb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
public class HarvesterReport
{

	private static final Logger hlog = LoggerFactory.getLogger( HarvesterReport.class );

	public static void info( String m )
	{
		hlog.info( m );
	}

	public void filesCountLocally( Path[] directories )
	{

		for ( Path file : directories )
		{
			try ( Stream<Path> fileList = Files.list( file ) )
			{
				hlog.info( "\t{}\tFiles in folder {}", fileList.count(), file.toAbsolutePath() );
			}
			catch ( IOException e )
			{
				hlog.error( e.getMessage() );
			}
		}
	}
}
