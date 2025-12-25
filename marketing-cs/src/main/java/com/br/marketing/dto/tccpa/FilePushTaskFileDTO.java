package com.br.marketing.dto.tccpa;

import lombok.Data;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class FilePushTaskFileDTO {

    private String csvIndex;

    private String fileName;

    private AtomicInteger total;
}
