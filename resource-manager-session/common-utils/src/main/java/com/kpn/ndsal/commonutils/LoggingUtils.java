package com.kpn.ndsal.commonutils;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Utility class for logging
 *
 * @author MOOSe team
 */
public class LoggingUtils {

    /**
     * private constructor
     */
    private LoggingUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param joinPoint
     * @param log
     * @return
     * @throws Throwable
     * @throws RuntimeException
     */
    public static Object logAround(ProceedingJoinPoint joinPoint, Logger log) throws Throwable {
        if (MDC.get(MDCConstants.DEPTH) == null) {
            MDC.put(MDCConstants.DEPTH, ">");
        } else {
            MDC.put(MDCConstants.DEPTH, MDC.get(MDCConstants.DEPTH) + ">");
        }
        if (log.isDebugEnabled()) {
            log.debug("{} {}.{}() with argument[s] = {}", MDC.get(MDCConstants.DEPTH),
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
                    Arrays.toString(joinPoint.getArgs()));
        } else if (log.isInfoEnabled()) {
            log.info("{} {}.{}()", MDC.get(MDCConstants.DEPTH), joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
        }
        try {
            final Object result = joinPoint.proceed();
            if (log.isDebugEnabled()) {
                log.debug("{} {}.{}() with result = {}", MDC.get(MDCConstants.DEPTH).replace(">", "<"),
                        joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), result);
            } else if (log.isInfoEnabled()) {
                log.info("{} {}.{}()", MDC.get(MDCConstants.DEPTH).replace(">", "<"),
                        joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            }
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}()", Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getName());
            throw e;
        } finally {
            String level = MDC.get(MDCConstants.DEPTH);
            if (level != null) {
                level = level.substring(0, level.length() - 1);
                MDC.put(MDCConstants.DEPTH, level);
            }
        }

    }

}
