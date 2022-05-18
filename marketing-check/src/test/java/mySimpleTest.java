import com.br.marketing.check.CkeckApplication;
import com.br.marketing.check.job.CallingToSendJob;
import com.br.marketing.entity.CustomerCalling;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CkeckApplication.class})
public class mySimpleTest {

    @Autowired
    private CallingToSendJob callingToSendJob;
    @Test
    public void replace(){
        String str = "orgName不能为空,name不能为空";
        String name = str.replace("name", "");
        String s = str.replaceFirst("^(name不能为空)$", "");
        String s1 = str.replaceFirst("^(orgName不能为空)$", "");
        System.out.println(name);
        System.out.println(s);
        System.out.println(s1);
    }


    @Test
    public void fortest(){
        out:
        for (int i = 0; i < 100; i++) {
            System.out.println("输出:"+i);
            if(i==10){
                break out;
            }
        }
        System.out.println("结束");

        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        outtwo: for (Integer integer : integers) {
            System.out.println("输出:"+integer);
            if(integer==10){
                break outtwo;
            }
        }
        System.out.println("结束two");
    }
}
