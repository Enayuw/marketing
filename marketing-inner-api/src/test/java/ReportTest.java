import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;


@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
public class ReportTest {

    public static void main(String[] args) {
        String value = "12.345678";
        String v1 = value == null ? "0" : NumberUtil.decimalFormat(",###", new BigDecimal(String.valueOf(value)).doubleValue());
        String v2 = value == null ? "0" :NumberUtil.decimalFormat(",##0.00", new BigDecimal(String.valueOf(value)).doubleValue());
        String v3 = value == null ? "0%" :NumberUtil.formatPercent(new BigDecimal(String.valueOf(value)).doubleValue(), 2);
        log.warn(v1);
        log.warn(v2);
        log.warn(v3);
    }
}
