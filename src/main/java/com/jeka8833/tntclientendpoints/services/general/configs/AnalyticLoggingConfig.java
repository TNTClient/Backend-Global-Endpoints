package com.jeka8833.tntclientendpoints.services.general.configs;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class AnalyticLoggingConfig {

    private final DecimalFormat cpuUsageFormatter =
            new DecimalFormat("0.###", new DecimalFormatSymbols(Locale.US));

    public AnalyticLoggingConfig(MeterRegistry meterRegistry, ScheduledExecutorService scheduledExecutorService,
                                 @Value("${spring.analytic.send.interval:15s}") Duration sendInterval,
                                 @Value("${logging.loki.enable:false}") boolean enabled) {
        if (!enabled) return;

        Runtime runtime = Runtime.getRuntime();

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                MDC.put("mem", Long.toString(runtime.totalMemory() + getTotalNonHeapMemoryIfPossible()));
                MDC.put("mem.free", Long.toString(runtime.freeMemory()));

                MDC.put("systemCpuUsage",
                        castToString(meterRegistry.get("system.cpu.usage").gauge().value(), cpuUsageFormatter));
                MDC.put("processCpuUsage",
                        castToString(meterRegistry.get("process.cpu.usage").gauge().value(), cpuUsageFormatter));
                MDC.put("threads", castToLongString(meterRegistry.get("jvm.threads.live").gauge().value()));

                log.info("analytics");

                MDC.clear();
            } catch (Throwable e) {
                try {
                    MDC.clear();

                    log.warn("Failed to send analytics", e);
                } catch (Throwable e1) {
                    log.warn("Two exceptions, but only one will be logged", e1);
                }
            }
        }, 0, sendInterval.toNanos(), TimeUnit.NANOSECONDS);
    }

    private long getTotalNonHeapMemoryIfPossible() {
        try {
            return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
        } catch (Throwable ex) {
            return 0;
        }
    }

    private String castToString(double value, DecimalFormat formatter) {
        return Double.isFinite(value) ? formatter.format(value) : "0";
    }

    public String castToLongString(double value) {
        return Double.isFinite(value) ? Long.toString((long) Math.ceil(value)) : "0";
    }
}
