package me.rhys.anticheat.filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import java.util.regex.Pattern;

@SuppressWarnings("RegExpRedundantEscape")
public class LogFilter implements Filter {
    public static final Pattern LOG4J_RCE_PATTERN = Pattern.compile(".*\\$\\{[^}]*\\}.*");

    public Result getFilterType(Object... message) {

        for (Object o : message) {
            if (!(o instanceof String) || !LOG4J_RCE_PATTERN.matcher(((String) o)).matches()) continue;
            return Result.DENY;
        }

        return Result.NEUTRAL;
    }

    @Override
    public Result getOnMismatch() {
        return Result.NEUTRAL;
    }

    @Override
    public Result getOnMatch() {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object... objects) {
        return this.getFilterType(objects);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object o, Throwable throwable) {
        return this.getFilterType(o);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        return this.getFilterType(message.getFormattedMessage());
    }

    @Override
    public Result filter(LogEvent logEvent) {
        return this.getFilterType(logEvent.getMessage().getFormattedMessage());
    }
}