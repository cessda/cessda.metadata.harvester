package eu.cessda.eqb.harvester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import static java.lang.Math.toIntExact;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

public class HttpClient
{
    private HttpClient() {}

    // Constants used by the HTTP client
    private static final String RETRY_AFTER = "Retry-After";

    // Logger
    private static final Logger log = LoggerFactory.getLogger( HttpClient.class );

    private static IOException handleHTTPResponseErrors( HttpURLConnection con, int responseCode )
    {
        IOException exception;
        try ( var stream = decodeHttpInputStream( con ) )
        {
            exception = new IOException( String.format( "Server returned %d, body: %s",
                    responseCode,
                    new String( stream.readAllBytes(), StandardCharsets.UTF_8 )
            ) );
        }
        catch ( IOException e )
        {
            // Make sure the response code is not lost if an IO error occurs
            exception = new IOException( String.format( "Server returned %d", responseCode ), e );
        }
        return exception;
    }

    private static InputStream decodeHttpInputStream( HttpURLConnection con ) throws IOException
    {
        String contentEncoding = con.getHeaderField( "Content-Encoding" );
        log.trace( "contentEncoding={}", contentEncoding );

        final InputStream inputStream;
        if ( con.getResponseCode() < 400 )
        {
            inputStream = con.getInputStream();
        }
        else
        {
            inputStream = con.getErrorStream();
        }

        if ( contentEncoding == null )
        {
            return inputStream;
        }

        switch ( contentEncoding )
        {
            case "compress":
                ZipInputStream zis = new ZipInputStream( inputStream );
                zis.getNextEntry();
                return zis;
            case "gzip":
                return new GZIPInputStream( inputStream );
            case "deflate":
                return new InflaterInputStream( inputStream );
            default:
                return inputStream;
        }
    }

    public static InputStream getHttpResponse( URL requestURL, Duration timeout ) throws IOException
    {
        HttpURLConnection con;
        int responseCode;
        int retries = 0;

        while ( true )
        {
            con = (HttpURLConnection) requestURL.openConnection();
            con.setRequestProperty( "User-Agent", "OAIHarvester/2.0" );
            con.setRequestProperty( "Accept-Encoding", "compress, gzip, identity" );

            // TK added default timeout for dataverses taking too long to respond / stall
            log.trace( "Timeout : {} seconds", timeout );
            con.setConnectTimeout( toIntExact( timeout.toMillis() ) );
            con.setReadTimeout( toIntExact( timeout.toMillis() ) );

            responseCode = con.getResponseCode();
            log.trace( "responseCode={}", responseCode );

            if ( responseCode == HTTP_UNAVAILABLE && retries++ < 3 )
            {
                long retrySeconds = con.getHeaderFieldInt( RETRY_AFTER, -1 );
                if ( retrySeconds == -1 )
                {
                    long now = Instant.now().toEpochMilli();
                    long retryDate = con.getHeaderFieldDate( RETRY_AFTER, now );
                    retrySeconds = retryDate - now;
                }
                if ( retrySeconds > 0 )
                {
                    log.debug( "Server response: Retry-After={}", con.getHeaderField( RETRY_AFTER ) );
                    try
                    {
                        TimeUnit.SECONDS.sleep( retrySeconds );
                    }
                    catch ( InterruptedException ex )
                    {
                        Thread.currentThread().interrupt();
                        return InputStream.nullInputStream();
                    }
                }
            }
            else
            {
                break;
            }
        }

        if ( responseCode >= 400 )
        {
            throw handleHTTPResponseErrors( con, responseCode );
        }

        return decodeHttpInputStream( con );
    }
}
