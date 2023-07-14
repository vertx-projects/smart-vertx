package com.smart.vertx.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author peng.bo
 * @date 2022/5/23 18:19
 * @desc
 */
public class BeanValidatorsUtil {
    public static void validateWithException(Validator validator, Object object, Class<?>... groups) throws ConstraintViolationException {
        Set constraintViolations = validator.validate(object, groups);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    public static List<String> extractMessage(ConstraintViolationException e) {
        return extractMessage(e.getConstraintViolations());
    }

    public static List<String> extractMessage(Set<? extends ConstraintViolation> constraintViolations) {
        List<String> errorMessages = Lists.newArrayList();
        Iterator var2 = constraintViolations.iterator();

        while(var2.hasNext()) {
            ConstraintViolation violation = (ConstraintViolation)var2.next();
            errorMessages.add(violation.getMessage());
        }

        return errorMessages;
    }

    public static Map<String, String> extractPropertyAndMessage(ConstraintViolationException e) {
        return extractPropertyAndMessage(e.getConstraintViolations());
    }

    public static Map<String, String> extractPropertyAndMessage(Set<? extends ConstraintViolation> constraintViolations) {
        Map<String, String> errorMessages = Maps.newHashMap();
        Iterator var2 = constraintViolations.iterator();

        while(var2.hasNext()) {
            ConstraintViolation violation = (ConstraintViolation)var2.next();
            errorMessages.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        return errorMessages;
    }

    public static List<String> extractPropertyAndMessageAsList(ConstraintViolationException e) {
        return extractPropertyAndMessageAsList(e.getConstraintViolations(), " ");
    }

    public static List<String> extractPropertyAndMessageAsList(Set<? extends ConstraintViolation> constraintViolations) {
        return extractPropertyAndMessageAsList(constraintViolations, " ");
    }

    public static List<String> extractPropertyAndMessageAsList(ConstraintViolationException e, String separator) {
        return extractPropertyAndMessageAsList(e.getConstraintViolations(), separator);
    }

    public static List<String> extractPropertyAndMessageAsList(Set<? extends ConstraintViolation> constraintViolations, String separator) {
        List<String> errorMessages = Lists.newArrayList();
        Iterator var3 = constraintViolations.iterator();

        while(var3.hasNext()) {
            ConstraintViolation violation = (ConstraintViolation)var3.next();
            errorMessages.add(violation.getPropertyPath() + separator + violation.getMessage());
        }

        return errorMessages;
    }
}
