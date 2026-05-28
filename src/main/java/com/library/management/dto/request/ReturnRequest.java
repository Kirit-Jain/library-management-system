package com.library.management.dto.request;

import com.library.management.enums.BookCondition;

import lombok.Data;

@Data
public class ReturnRequest {

    private Long borrowingId;

    private BookCondition condition;

    private String notes;
}
