package com.sportclub.challenge.application.port.in.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginCommand(
        @NotBlank(message = "DNI cannot be blank")
        @Size(min = 7, max = 9, message = "DNI must be between 7 and 9 characters")
        String dni
) {
}
