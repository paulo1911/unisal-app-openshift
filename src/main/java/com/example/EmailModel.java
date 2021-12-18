package com.example;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmailModel implements Serializable {

    private String emailTo;
    private String subject;
    private String message;
}
