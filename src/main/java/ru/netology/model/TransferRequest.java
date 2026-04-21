package ru.netology.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record TransferRequest(
        @NotBlank @Pattern(regexp = "\\d{16}")
        @JsonProperty("cardFromNumber")
        String cardFromNumber,

        @NotBlank @Pattern(regexp = "(0[1-9]|1[0-2])/[0-9]{2}")
        @JsonProperty("cardFromValidTill")
        String cardFromValidTill,

        @NotBlank @Pattern(regexp = "\\d{3}")
        @JsonProperty("cardFromCVV")
        String cardFromCVV,

        @NotBlank @Pattern(regexp = "\\d{16}")
        @JsonProperty("cardToNumber")
        String cardToNumber,

        @NotNull
        @JsonProperty("amount")
        Amount amount
) {
    public record Amount(
            @NotNull @Positive
            @JsonProperty("value")
            Integer value,

            @NotBlank
            @JsonProperty("currency")
            String currency
    ) {}
}