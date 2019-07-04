package cessda.eqb;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class HelloWorldController
{
	private static final Logger logger = LoggerFactory.getLogger( HelloWorldController.class );

	@RequestMapping( "/" )
	protected void redirectToSwaggerUi( HttpServletResponse response ) throws IOException
	{
		response.sendRedirect( "swagger-ui.html" );
	}

	@ApiOperation( value = "Say hello world" )
	@RequestMapping(
			method = RequestMethod.GET,
			path = "/hello-world",
			produces = TEXT_PLAIN_VALUE )
	public String greet()
	{
		logger.info( "Greeting requested" );
		return "Hello world!";
	}
}
