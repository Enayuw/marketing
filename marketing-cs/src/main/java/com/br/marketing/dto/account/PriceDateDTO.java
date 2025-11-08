package com.br.marketing.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Data
public class PriceDateDTO {

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "生效开始日期")
    private LocalDate effectStartDate;

    private LocalDate effectEndDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceDateDTO that = (PriceDateDTO) o;
        return Objects.equals(price, that.price)
                && Objects.equals(effectStartDate, that.effectStartDate)
                && Objects.equals(effectEndDate, that.effectEndDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(price, effectStartDate, effectEndDate);
    }
}
