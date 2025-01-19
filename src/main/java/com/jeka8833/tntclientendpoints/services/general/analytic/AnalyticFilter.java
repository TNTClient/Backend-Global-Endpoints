package com.jeka8833.tntclientendpoints.services.general.analytic;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluatorBase;

public final class AnalyticFilter extends EventEvaluatorBase<ILoggingEvent> {
    @Override
    public boolean evaluate(ILoggingEvent iLoggingEvent) throws NullPointerException {
        return iLoggingEvent.getMessage().equals(AnalyticLoggingConfig.MESSAGE_TEXT);
    }
}
