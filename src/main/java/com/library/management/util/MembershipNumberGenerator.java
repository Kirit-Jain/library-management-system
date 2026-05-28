package com.library.management.util;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class MembershipNumberGenerator {

    private final AtomicInteger counter = new AtomicInteger(1000);

    public String generate () {
        int year = LocalDate.now().getYear();
        int number = counter.getAndIncrement();
        return String.format("LIB-%d-%04d", year, number);
    }
}
