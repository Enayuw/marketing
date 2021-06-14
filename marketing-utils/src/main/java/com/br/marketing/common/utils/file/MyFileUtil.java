package com.br.marketing.common.utils.file;

import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Bairong on 2019/9/2.
 */
@Slf4j
public class MyFileUtil {
    private final static Pattern MYREGEX=Pattern.compile("\\p{C}");




    public static String getMd5(InputStream inputStream) throws IOException {
        return DigestUtils.md5Hex(inputStream);
    }


    /**
     * 将文件hash取模之后放到不同的小文件中
     * @param targetFile 要去重的文件路径
     * @param splitSize 将目标文件切割成多少份hash取模的小文件个数
     * @return
     */
    public static File[] splitFile(String targetFile,int splitSize,StringBuilder head){
        File file = new File(targetFile);
        PrintWriter[] pws = new PrintWriter[splitSize];
        File[] littleFiles = new File[splitSize];
        String parentPath = file.getParent();
        File tempFolder = new File(parentPath + File.separator + "tmp");
        if(!tempFolder.exists()){
            boolean mkdir = tempFolder.mkdir();
            if(!mkdir){
                log.error("创建文件 失败 ");
            }
        }
        for(int i=0;i<splitSize;i++){
            littleFiles[i] = new File(tempFolder.getAbsolutePath() + File.separator + i + ".txt");
            if(littleFiles[i].exists()){
               Path path=littleFiles[i].toPath();
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                pws[i] = new PrintWriter(littleFiles[i]);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        try(BufferedReader reader = new BufferedReader(new FileReader(file));) {
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                tempString = tempString.trim();
                tempString= MYREGEX.matcher(tempString).replaceAll("");
                if(StringUtils.isNotEmpty(tempString)){
                    if(tempString.indexOf("cus_num")!=-1&&(tempString.indexOf("id")!=-1
                            ||tempString.indexOf("name")!=-1||tempString.indexOf("cell")!=-1)){
                        head.append(tempString);
                    }else{
                        //关键是将每行数据hash取模之后放到对应取模值的文件中，确保hash值相同的字符串都在同一个文件里面
                        int index = Math.abs(tempString.hashCode() % splitSize);
                        pws[index].println(tempString);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for(int i=0;i<splitSize;i++){
                if(pws[i] != null){
                    pws[i].close();
                }
            }
        }
        return littleFiles;
    }

      public static  int getTotalLines( File  file)  {
           long startTime = System.currentTimeMillis();
          String tempString;
          int lines=0;
          try( BufferedReader reader = new BufferedReader(new FileReader(file));) {
              while ((tempString = reader.readLine()) != null) {
                  tempString = tempString.trim();
                  tempString= MYREGEX.matcher(tempString).replaceAll("");
                  if(StringUtils.isNotEmpty(tempString)){
                      lines++;
                  }
              }
              long endTime = System.currentTimeMillis();
              log.warn("统计文件行数{},运行时间： {}ms",lines,(endTime - startTime));
          }catch (Exception e){
            log.error("getTotalLines error",e);
          }
            return lines;
        }

    public static StringBuilder gethead(String  filePath) throws IOException {
        Reader r=new FileReader(filePath);
        BufferedReader br=new BufferedReader(r);
        String s = br.readLine();
        if(StringUtils.isEmpty(s)){
            return new StringBuilder();
        }
        s= MYREGEX.matcher(s).replaceAll("");
        r.close();
        br.close();
        return new StringBuilder(s);
    }
    /**
     * 对小文件进行去重合并
     * @param littleFiles 切割之后的小文件数组
     * @param distinctFilePath 去重之后的文件路径
     * @param splitSize 小文件大小
     * @param head
     */
    public static void distinct(File[] littleFiles, String distinctFilePath, int splitSize, StringBuilder head){
        File distinctedFile = new File(distinctFilePath);
        if(distinctedFile.exists()){
            try {
                Files.delete(Paths.get(distinctFilePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            distinctedFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (
        PrintWriter  pw = new PrintWriter(distinctedFile);){
            Set<String> unicSet = new HashSet<String>();
            for(int i=0;i<splitSize;i++){
                if(littleFiles[i].exists()){
                    unique(littleFiles, unicSet, i);
                    for(String s:unicSet){
                        pw.println(s);
                    }
                    unicSet.clear();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e1){
            e1.printStackTrace();
        } finally {
            for(int i=0;i<splitSize;i++){
                //合并完成之后删除临时小文件
                if(littleFiles[i].exists()){
                    Path path=littleFiles[i].toPath();
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void unique(File[] littleFiles, Set<String> unicSet, int i)  {
            try (   FileReader frs = new FileReader(littleFiles[i]);
                    BufferedReader brs = new BufferedReader(frs);){
                String line = null;
                while((line = brs.readLine())!=null){
                    if(StringUtils.isNotEmpty(line)){
                        unicSet.add(line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    private static void uniqueByNum(File[] littleFiles, Map<String,String> unicMap, int i,int indexCusNum)  {
        try (   FileReader frs = new FileReader(littleFiles[i]);
                BufferedReader brs = new BufferedReader(frs);){
            String line = null;
            while((line = brs.readLine())!=null){
                if(StringUtils.isNotEmpty(line)){
                    String s="";
                    try{
                        s = line.split(",")[indexCusNum];
                    }catch (ArrayIndexOutOfBoundsException e){
                        log.warn("indexCusNum {}",indexCusNum);
                        unicMap.put(s,line);
                    }
                    unicMap.put(s,line);
                }
            }
        } catch (Exception e) {
            log.error("Exception",e);
        }
    }

    /**
     * 对小文件按CusNum进行去重合并
     * @param littleFiles 切割之后的小文件数组
     * @param distinctFilePath 去重之后的文件路径
     * @param splitSize 小文件大小
     * @param head
     */
    public static void distinctByCusNum(File[] littleFiles, String distinctFilePath,String distinctFileName, int splitSize, StringBuilder head){
        File dir = new File(distinctFilePath);
        if(!dir.exists()){
            boolean mkdir = dir.mkdir();
            if(!mkdir){
                log.error("mkdir error");
            }
        }
        String concat = distinctFilePath.concat(distinctFileName);
        File distinctedFile = new File(concat);
        if(distinctedFile.exists()){
            try {
                Files.delete(Paths.get(concat));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (PrintWriter pw =new PrintWriter(distinctedFile);){

            Map<String,String> unicMap=new HashMap<>();
            pw.println(head);
            int indexCusNum=findIndex(head.toString().split(","),"cus_num");
            for(int i=0;i<splitSize;i++){
                if(littleFiles[i].exists()){
                    uniqueByNum(littleFiles,unicMap,i,indexCusNum);
                    for(Map.Entry<String,String> entry:unicMap.entrySet()){
                        String value = entry.getValue();
                        pw.println(value);
                    }
                    unicMap.clear();
                }
            }
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException",e);
        } catch (Exception e1){
            log.error("Exception",e1);
        } finally {
            for(int i=0;i<splitSize;i++){
                //合并完成之后删除临时小文件
                if(littleFiles[i].exists()){
                    Path path=littleFiles[i].toPath();
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    /**
     * 将文件hash取模之后放到不同的小文件中
     * @param targetFile 要去重的文件路径
     * @param splitSize 将目标文件切按CusNum割成多少份hash取模的小文件个数
     * @return
     */
    public static File[] splitFileByCusNum(String targetFile,int splitSize){
        File file = new File(targetFile);
        PrintWriter[] pws = new PrintWriter[splitSize];
        File[] littleFiles = new File[splitSize];
        String parentPath = file.getParent();
        File tempFolder = new File(parentPath + File.separator + "tmp");
        if(!tempFolder.exists()){
            boolean mkdir = tempFolder.mkdir();
            if(!mkdir){
                log.error("mkdir error");
            }
        }
        for(int i=0;i<splitSize;i++){
            littleFiles[i] = new File(tempFolder.getAbsolutePath() + File.separator + i + ".txt");
            if(littleFiles[i].exists()){
                Path path=littleFiles[i].toPath();
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                pws[i] = new PrintWriter(littleFiles[i]);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file));){

            String tempString = null;
            int indexCusNum=0;
            int num=0;
            while ((tempString = reader.readLine()) != null) {
                tempString = tempString.trim();
                tempString= MYREGEX.matcher(tempString).replaceAll("");
                if(StringUtils.isNotEmpty(tempString)){
                    if(tempString.indexOf("cus_num")!=-1&&(tempString.indexOf("id")!=-1
                            ||tempString.indexOf("name")!=-1||tempString.indexOf("cell")!=-1)){
                        indexCusNum=findIndex(tempString.split(","),"cus_num");
                    }else{
                        //关键是将每行数据hash取模之后放到对应取模值的文件中，确保hash值相同的字符串都在同一个文件里面
                        String s="";
                        try{
                            s = tempString.split(",")[indexCusNum];
                        }catch (ArrayIndexOutOfBoundsException e){
                            log.warn("indexCusNum {}",indexCusNum);
                        }
                        int index = Math.abs(s.hashCode() % splitSize);
                        pws[index].println(tempString);
                    }
                    num++;
                }
            }
        log.warn("num:{}",num);
        } catch (Exception e) {
            log.error("Exception",e);
        } finally {
            for(int i=0;i<splitSize;i++){
                if(pws[i] != null){
                    pws[i].close();
                }
            }
        }
        return littleFiles;
    }

    /**
     * 查找某个值在数组中的索引
     * @param array 数组
     * @param value 给定的值
     * @return 索引
     */
    public static int findIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 读取流中前面的字符，看是否有bom，如果有bom，将bom头先读掉丢弃
     *
     * @param in
     * @return
     * @throws java.io.IOException
     */
    public static InputStream getInputStream(InputStream in) throws IOException {

        PushbackInputStream testin = new PushbackInputStream(in);
        int ch = testin.read();
        if (ch != 0xEF) {
            testin.unread(ch);
        } else if ((ch = testin.read()) != 0xBB) {
            testin.unread(ch);
            testin.unread(0xef);
        } else if ((ch = testin.read()) != 0xBF) {
            throw new IOException("错误的UTF-8格式文件");
        } else {
            // 不需要做，这里是bom头被读完了
            // System.out.println("still exist bom");
        }
        return testin;

    }
}
