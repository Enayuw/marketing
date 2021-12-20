package com.br.marketing.check.strategy.sftp;

import com.br.marketing.check.dto.FileContext;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.PhoneSale;
import com.br.marketing.mapper.PhoneSaleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * 应用场景
 *
 * @author Guo Zeqiang
 * @dateTime 2021/12/17 15:51
 */
@Component
@Slf4j
public class SftpEnvironment {

    @Resource
    private PhoneSaleMapper phoneSaleMapper;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Resource
    private DecodeClient decodeClient;

    private SftpStrategy sftpStrategy;

    public void setSftpStrategy(SftpStrategy sftpStrategy) {
        this.sftpStrategy = sftpStrategy;
    }

    public Result<Object> statisticsHead(FileContext context, HashMap<Integer, String> address,
                                         HashMap<Integer, String> extSetField) {
        StringBuilder head = new StringBuilder();
        Assert.notNull(this.sftpStrategy, "strategy is not null");
        String txtFilePathAndName = context.getLocalTxtFilePath().concat(context.getTxtFileName());
        File f = new File(txtFilePathAndName);
        if (f.isFile() && f.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                String s = reader.readLine();
                if (StringUtils.isNotEmpty(s)) {
                    head.append(Pattern.compile("\\p{C}").matcher(s).replaceAll(""));
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        return this.sftpStrategy.statisticsHead(head.toString(), address, extSetField);
    }

    public Result<Object> setDataByPhone(BufferedReader br, ThreadPoolExecutor threadPool, LocalFile localFile, HashMap<Integer, String> address
            , HashMap<Integer, String> extSetFields, AtomicInteger errorMark, Integer line) throws IOException {
        Assert.notNull(this.sftpStrategy, "strategy is not null");
        String row;
        while ((row = br.readLine()) != null) {
            String trim = row.trim();
            if (StringUtils.isNotEmpty(trim)) {
                if (line > 1) {
                    Integer lineNum = line;
                    threadPool.execute(() -> {
                        PhoneSale phoneSale = new PhoneSale();
                        phoneSale.setApiCode(localFile.getApiCode());
                        phoneSale.setLocalId(localFile.getId().toString());
                        try {
                            this.sftpStrategy.setDataByPhone(trim, phoneSale, address, extSetFields, errorMark, lineNum, aesKey, decodeClient);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                        phoneSaleMapper.insertSelective(phoneSale);
                    });
                }
            }
            line++;
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }
}
