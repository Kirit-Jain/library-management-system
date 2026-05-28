package com.library.management.util;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class BarcodeGenerator {

    public String generate () {
        String date = LocalDate.now().toString().replaceAll("-", "");
        String unique = UUID.randomUUID()
            .toString()
            .replaceAll("-", "")
            .substring(0, 8)
            .toUpperCase();
        return String.format("BC-%s-%s", date, unique);
    }
}
