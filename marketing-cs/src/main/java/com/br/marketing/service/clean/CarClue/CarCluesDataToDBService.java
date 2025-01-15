package com.br.marketing.service.clean.CarClue;

import java.util.List;

public interface CarCluesDataToDBService {
    void cleanCallDetailsData(List<String> apiCodes, String date);
}
