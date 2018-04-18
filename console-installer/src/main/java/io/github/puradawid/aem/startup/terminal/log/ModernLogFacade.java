package io.github.puradawid.aem.startup.terminal.log;

import org.apache.log4j.Logger;

import java.util.function.Supplier;

public class ModernLogFacade {

    Logger logger;

    public ModernLogFacade(Logger logger) {
        this.logger = logger;
    }

    public void debug(Supplier<String> eval) {
        if (logger.isDebugEnabled()) {
            logger.debug(eval.get());
        }
    }

    public void error(String msg, Throwable ex) {
        logger.error(msg, ex);
    }
}