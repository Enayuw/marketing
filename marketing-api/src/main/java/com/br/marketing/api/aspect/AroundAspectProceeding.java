package com.br.marketing.api.aspect;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * The interface Around aspect proceeding.
 */
public interface AroundAspectProceeding {
    /**
     * Proceeding.
     *
     * @param joinPoint the join point
     * @throws Throwable the throwable
     */
    public void proceeding(ProceedingJoinPoint joinPoint) throws Throwable;
}
