import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.file.FtpUtil2;
import com.br.marketing.service.EmailService;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
@Slf4j
public class TestTimerTask {
    public static void main(String[] args) {
//        Timer t=new Timer();
//        t.schedule(new SignFileCheckTask(t),1000,1000);

        /**java实现2023-08-31至2023-06-30间隔天数*/
        LocalDate localDate = LocalDate.parse("2023-08-31");
        LocalDate localDate1 = LocalDate.parse("2023-06-30");
        long days = localDate.toEpochDay() - localDate1.toEpochDay();

    }
    static class SignFileCheckTask extends TimerTask {
        private Timer t;
        public SignFileCheckTask( Timer t) {
            this.t = t;
        }

        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName()+Thread.currentThread().getState().name()+Thread.currentThread().isAlive()+"   SignFileCheckTask     "+System.currentTimeMillis());
                Thread.sleep(3000);
                    Timer t1=new Timer();
                    t1.schedule(new SignFileCheckTask1(t1),1000);


                t.cancel();
                System.out.println(Thread.currentThread().getName()+Thread.currentThread().getState().name()+Thread.currentThread().isAlive()+"   SignFileCheckTask        "+System.currentTimeMillis());
            }catch (Exception e){
                log.error("SignFileCheckTask error {}",e);
            }
        }
    }

    static class SignFileCheckTask1 extends TimerTask {
        private Timer t;
        public SignFileCheckTask1( Timer t) {
            this.t = t;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(10000);
                System.out.println(Thread.currentThread().getName()+Thread.currentThread().getState().name()+Thread.currentThread().isAlive()+"   SignFileCheckTask1          "+System.currentTimeMillis());

                t.cancel();
            }catch (Exception e){
                log.error("SignFileCheckTask error {}",e);
            }
        }
    }
}
