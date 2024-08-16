package com.br.marketing.service.bi;

import java.io.IOException;

public interface AnalysisReportService {

    String uploadReportToFastDfs(Long taskId) throws IOException;
}
