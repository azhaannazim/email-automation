package com.email.automation.payload;

import lombok.Data;

@Data
public class EmailRequest {
    private String emailContent;
    private String tone;
}
