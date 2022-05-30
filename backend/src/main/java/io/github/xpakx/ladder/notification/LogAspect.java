package io.github.xpakx.ladder.notification;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Around("@annotation(LogResponse)")
    public Object log(ProceedingJoinPoint jointPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = jointPoint.proceed();
        long time = System.currentTimeMillis()-start;
        LOG.info("Time: {}ms", time);

        return proceed;
    }
}
