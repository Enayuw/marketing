package demo;


import com.github.pagehelper.util.StringUtil;
import java.io.File;
import java.io.IOException;

public class FileUtil {

    public static File createNewFile(String path) {
        if (StringUtil.isEmpty(path)) {
            return null;
        }

        File file = new File(path);
        return (createNewFile(file) ? file : null);
    }

    public static boolean createNewFile(File file) {
        if (file == null) {
            return false;
        }

        if (file.exists()) {
            return true;
        }

        mkParentDirs(file);

        try {
            return file.createNewFile();
        } catch (IOException ie) {
            throw new RuntimeException("Create file error", ie);
        }
    }

    public static boolean mkParentDirs(File file) {
        if (file == null) {
            return false;
        }

        File parentFile = file.getParentFile();
        if (parentFile == null) {
            return true;
        }

        if (parentFile.exists() == false) {
            return parentFile.mkdirs();
        }

        return true;
    }
}
