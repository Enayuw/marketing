package com.br.marketing.datarelayservice.controller;

import com.br.marketing.dto.carclue.CarClueCallBackReqDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/carclue")
@RestController
public class CarClueController {

    /**
     * 线索回调
     * @return
     */
    @PostMapping("/callBack")
    public String callBack(@RequestBody CarClueCallBackReqDTO reqDTO){
        return null;
    }
}
