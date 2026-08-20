package com.beepbox.exception;

public class BoxWeightLimitExceededException extends RuntimeException {
    public BoxWeightLimitExceededException(double totalWeight, double weightLimit) {
        super(String.format("Weight limit exceeded: Total items weight (%.2fg) exceeds box limit (%.2fg)", totalWeight, weightLimit));
    }
}
