import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class mySimpleTest {
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
}
