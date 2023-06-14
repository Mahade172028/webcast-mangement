package com.wtv.webcastmanagement.Exceptions;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomExceptionResponse {
    Date timestamp;
    String message;
    String description;

}
