/*-
 * #%L
 * CESSDA Euro Question Bank: Metadata Harvester
 * %%
 * Copyright (C) 2020 CESSDA ERIC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
