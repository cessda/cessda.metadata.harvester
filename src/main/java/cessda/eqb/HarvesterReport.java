package cessda.eqb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HarvesterReport
{

	public static Logger hlog = LoggerFactory.getLogger( HarvesterReport.class );

	public void info( String m )
	{

		hlog.info( m );

	}

	public void filesCountLocally( File[] directories )
	{

		for ( File file : directories )
		{
			try
			{
				hlog.info( "\t" + Files.list( file.toPath() ).count() + "\tFiles in folder " + file.getAbsolutePath() );
			}
			catch (IOException e)
			{
				hlog.error( e.getMessage() );
			}
		}

	}
}
