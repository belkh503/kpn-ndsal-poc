package com.kpn.ndsal.sessionmanager.config;

import static java.util.Objects.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.messaging.handler.annotation.Header;

import com.kpn.ndsal.sessionmanager.util.Constants;
import com.kpn.ndsal.sessionmanager.util.LoggingUtils;
import com.kpn.ndsal.sessionmanager.util.MDCConstants;

@Aspect
@Configuration
@EnableAspectJAutoProxy
public class LoginAspectConfig {

    /**
     * Pointcut that matches all repositories, services and Web REST endpoints.
     */
    @Pointcut("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public void kafkaListenerPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Pointcut that matches all repositories, services and Web REST endpoints.
     */
    @Pointcut("@annotation(org.springframework.stereotype.Service)")
    public void springBeanPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut("within(com.kpn.ndsal.sessionmanager.service..*) || within(com.kpn.ndsal.sessionmanager.kafka..*)")
    public void applicationPackagePointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Retrieves the {@link Logger} associated to the given {@link JoinPoint}.
     *
     * @param joinPoint
     *            join point we want the logger for.
     * @return {@link Logger} associated to the given {@link JoinPoint}.
     */
    private Logger logger(JoinPoint joinPoint) {
        return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
    }

    /**
     * Advice that set correlationId in MDC context
     *
     * @param joinPoint
     *            join point for advice.
     * @return result.
     * @throws Throwable
     *             throws {@link IllegalArgumentException}.
     */
    @Before("kafkaListenerPointcut()")
    public void setCorrelationIdInMDCAround(JoinPoint joinPoint) {
        MethodSignature methodSig = (MethodSignature) joinPoint.getSignature();
        Method method = methodSig.getMethod();
        Parameter[] parameters = method.getParameters();
        int i = 0;
        while (i < parameters.length) {
            final Parameter param = parameters[i];
            Header header = param.getAnnotation(Header.class);
            if (nonNull(header) && Constants.KAFKA_CORRELATION_ID_HEADER.equals(header.value())) {
                Object correlationId = joinPoint.getArgs()[i];
                MDC.put(MDCConstants.MCD_CORRELATION_ID_KEY, correlationId.toString());
                break;
            }
            i++;
        }
    }

    /**
     * Advice that unset correlationId
     *
     * @param joinPoint
     *            join point for advice.
     */
    @After("kafkaListenerPointcut()")
    public void unsetCorrelationIdInMDCAround(JoinPoint joinPoint) {
        MDC.remove(MDCConstants.MCD_CORRELATION_ID_KEY);
    }

    /**
     * Advice that logs methods throwing exceptions.
     *
     * @param joinPoint
     *            join point for advice.
     * @param e
     *            exception.
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        logger(joinPoint).error("Exception in {}() with cause = \'{}\' and exception = \'{}\'",
                joinPoint.getSignature().getName(), e.getCause() != null ? e.getCause() : "NULL", e.getMessage(), e);

    }

    /**
     * Advice that logs when a method is entered and exited.
     *
     * @param joinPoint
     *            join point for advice.
     * @return result.
     * @throws Throwable
     *             throws {@link IllegalArgumentException}.
     */
    @Around("applicationPackagePointcut() && springBeanPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = logger(joinPoint);
        return LoggingUtils.logAround(joinPoint, log);
    }
}
