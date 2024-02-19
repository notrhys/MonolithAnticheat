package me.rhys.anticheat.filter;

import org.apache.logging.log4j.core.Logger;

public class LogManager {
    public void setupFilter() {

        ((Logger) org.apache.logging.log4j.LogManager.getRootLogger()).addFilter(new LogFilter());
    }
}
