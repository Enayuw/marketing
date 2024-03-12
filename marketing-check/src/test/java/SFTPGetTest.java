import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

import cn.hutool.core.collection.ListUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import org.apache.commons.collections4.ListUtils;

public class SFTPGetTest {


    public SFTPChannel getSFTPChannel() {
        return new SFTPChannel();
    }

    public static void main(String[] args) throws Exception {

        ArrayList<String> objects = new ArrayList<>();
        objects.add("123");
        objects.add("123");
        objects.add("343");
        objects.add("434");
        objects.add("4343");
        objects.add("123");
        objects.add("eee");
        objects.add("dfd");
        objects.add("123");
        objects.add("dfd");
        objects.add("123");
        objects.add("123");
        objects.add("fdfd");
        objects.add("dfdfd");
        objects.add("fdfd");
        objects.add("nmjjj");

        List<List<String>> partition = ListUtils.partition(objects, 2);
        partition.forEach(t -> {
            System.out.println("list对象：" + System.identityHashCode(t));
        });

        partition.forEach(t -> {
            new Thread(() -> {
                try {
                    System.out.println(t);
                    t.removeIf(m -> "123".equals(m));
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }).start();
        });
//        SFTPGetTest test = new SFTPGetTest();
//
//        Map<String, String> sftpDetails = new HashMap<String, String>();
//        // 设置主机ip，端口，用户名，密码
//        sftpDetails.put(SFTPConstants.SFTP_REQ_HOST, "172.16.81.135");
//        sftpDetails.put(SFTPConstants.SFTP_REQ_USERNAME, "test");
//        sftpDetails.put(SFTPConstants.SFTP_REQ_PASSWORD, "test");
//        sftpDetails.put(SFTPConstants.SFTP_REQ_PORT, "23");
//
//        SFTPChannel channel = test.getSFTPChannel();
//        ChannelSftp chSftp = channel.getChannel(sftpDetails, 60000);
//
//        String filename = "/home/omc/ylong/sftp/INTPahcfg.tar.gz";
//        SftpATTRS attr = chSftp.stat(filename);
//        long fileSize = attr.getSize();
//
//        String dst = "D:\\INTPahcfg.tar.gz";
//        OutputStream out = new FileOutputStream(dst);
//        try {
//            Vector ls = chSftp.ls("/");
//            System.out.println(ls.get(0).toString());
//            /*   chSftp.get(filename, dst, new FileProgressMonitor(fileSize)); // 代码段1*/
//
//            // chSftp.get(filename, out, new FileProgressMonitor(fileSize)); // 代码段2
//
//            /**
//             * 代码段3
//             *
//             InputStream is = chSftp.get(filename, new MyProgressMonitor());
//             byte[] buff = new byte[1024 * 2];
//             int read;
//             if (is != null) {
//             System.out.println("Start to read input stream");
//             do {
//             read = is.read(buff, 0, buff.length);
//             if (read > 0) {
//             out.write(buff, 0, read);
//             }
//             out.flush();
//             } while (read >= 0);
//             System.out.println("input stream read done.");
//             }
//             */
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            chSftp.quit();
//            channel.closeChannel();
//        }
    }
}
