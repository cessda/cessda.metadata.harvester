package eu.cessda.eqb.harvester;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.spi.ContextAwareBase;
import net.logstash.logback.encoder.LogstashEncoder;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

class LoggingConstants extends ContextAwareBase implements Configurator
{
    static final String REPO_NAME = "repo_name";
    static final String OAI_RECORD = "oai_record";
    static final String OAI_ERROR_CODE = "oai_error_code";
    static final String OAI_ERROR_MESSAGE = "oai_error_message";
    static final String OAI_URL = "oai_url";
    static final String OAI_SET = "oai_set";
    static final String EXCEPTION_MESSAGE = "exception_message";
    static final String EXCEPTION_NAME = "exception_name";

    private LoggingConstants() {}

    @Override
    public void configure( LoggerContext loggerContext )
    {
        var consoleAppender = new ConsoleAppender<ILoggingEvent>();
        consoleAppender.setContext( loggerContext );

        var jsonEncoder = new LogstashEncoder();
        jsonEncoder.setContext( loggerContext );
        jsonEncoder.start();

        consoleAppender.setEncoder( jsonEncoder );
        consoleAppender.start();

        var rootLogger = loggerContext.getLogger( ROOT_LOGGER_NAME );
        rootLogger.addAppender(consoleAppender);
    }
}
