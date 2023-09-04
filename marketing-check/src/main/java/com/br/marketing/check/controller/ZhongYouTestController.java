package com.br.marketing.check.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 描述：： 中邮测试辅助
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYouTestController
 * @author: it-yml
 * @create: 2023-08-02 17:11
 * @Version 1.0
 * --------------------------------------
 **/
@RestController
@RequestMapping("/test/")
@Slf4j
public class ZhongYouTestController {
    @PostMapping("/test")
    public void transfersmyTest(@RequestBody String fileName, HttpServletResponse response) {
        log.info("test");
    }
}
