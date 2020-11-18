package cessda.eqb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HarvesterReport
{
	public static final Logger hlog = LoggerFactory.getLogger( HarvesterReport.class );

	public static void info( String m )
{
		hlog.info( m );
	}

	public void filesCountLocally( File[] directories )
{
		for ( File file : directories )
	{
			try ( Stream<Path> fileList = Files.list( file.toPath() ) )
		{
				hlog.info( "\t" + fileList.count() + "\tFiles in folder " + file.getAbsolutePath() );
			}
			catch (IOException e)
		{
				hlog.error( e.getMessage() );
			}
		}
	}
}
