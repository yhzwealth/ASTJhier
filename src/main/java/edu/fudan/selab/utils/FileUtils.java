package edu.fudan.selab.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文件读写
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * LineBreak
     * <p>
     * 换行符
     */
    enum LineBreak {
        /**
         * LF -> \n
         */
        LF("\n"),

        /**
         * CRLF -> \r\n
         */
        CRLF("\r\n");

        private final String content;

        LineBreak(String lineBreak) {
            this.content = lineBreak;
        }

        @Override
        public String toString() {
            return this.content;
        }
    }

    public static String readFile(String url) {
        FileInputStream fis = null;
        StringBuilder line = new StringBuilder();
        try {
            fis = new FileInputStream(url);
            byte[] data = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(data)) != -1) {
                line.append(new String(data, 0, bytesRead));
            }
        } catch (Exception e) {
            logger.error("Cannot read file: " + url);
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line.toString();
    }

    public static void writeFile(String url, String content, boolean append) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(url, append);
            byte[] data = content.getBytes(StandardCharsets.UTF_8);
            fos.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * getDirPath
     * <p>
     * 给定一个文件路径，获取它所在的目录；或者说给定一个文件，获取它的父路径。
     *
     * @param filePath 文件绝对 / 相对文件路径
     * @return
     */
    public static Optional<String> getDirPath(String filePath) {
        String parentPath = new File(filePath).getParent();
        if (parentPath != null) {
            return Optional.of(parentPath);
        }
        return Optional.empty();
    }

    /**
     * getFileName
     * <p>
     * 给定一个文件路径，获取该文件的名字。
     *
     * @param filePath 文件绝对 / 相对文件路径
     * @return
     */
    public static Optional<String> getFileName(String filePath) {
        if (filePath.contains(File.separator)) {
            return Optional.of(new File(filePath).getName());
        }
        return Optional.empty();
    }

    /**
     * getFileAfterCreateFolder
     * <p>
     * 给定一个文件路径，创建这个文件所在的父目录，并且返回这个文件路径生成的 File
     *
     * @param filePath 文件绝对 / 相对文件路径
     * @return
     */
    public static Optional<File> getFileAfterCreateFolder(String filePath) throws IOException {
        Optional<String> dirOpt = FileUtils.getDirPath(filePath);
        if (dirOpt.isEmpty()) {
            return Optional.empty();
        }
        File dir = new File(dirOpt.get());
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException(
                    "getFileAfterCreateFolder cannot create a dir: " + dirOpt.get());
        }
        return Optional.of(new File(filePath));
    }

    /**
     * removeDir
     *
     * @param dirPath 要删除的目录的绝对 / 相对路径
     */
    public static void removeDir(String dirPath) {
        // ignore if FileNotExistedException and IOException
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(dirPath));
        } catch (IOException ignore) {
        }
    }

    public static void integrateFolders(String destPath, List<String> paths) {
        for (String path : paths) {
            handleDir(path, destPath);
        }
    }

    public static List<String> getAllJavaFile(String strPath, String fullyQualifiedPackageName) {
        String path = strPath + File.separator + fullyQualifiedPackageName.replace(".", File.separator);
        File file = new File(path);
        List<String> filePaths = new ArrayList<>();
        return getFilePaths(file, filePaths);
    }

    private static List<String> getFilePaths(File file, List<String> filePaths) {
        File[] files = file.listFiles();
        if (files != null)
            for (File f : files)
                if (f.isDirectory())
                    getFilePaths(f, filePaths);
                else if (f.getName().endsWith(".java"))
                    filePaths.add(f.getPath());
        return filePaths;
    }

    private static void handleDir(String path, String destPath) {
        if (!new File(destPath).exists() && !new File(destPath).mkdirs()) {
            logger.error(destPath + " doesn't exist but cannot be created");
            return;
        }
        File src = new File(path);
        if (src.isFile()) return;
        File[] innerFile = src.listFiles();
        if (innerFile == null) return;

        for (File file : innerFile) {
            String nextSrcPath = path + File.separator + file.getName();
            String nextDestPath = destPath + File.separator + file.getName();
            if (file.isDirectory()) {
                if (new File(nextDestPath).exists()) {
                    handleDir(nextSrcPath, nextDestPath);
                } else {
                    new File(nextSrcPath).renameTo(new File(nextDestPath));
                }
            } else {
                if (new File(nextDestPath).exists()) {
                    logger.error(nextSrcPath + " has existed in " + nextDestPath);
                } else {
                    new File(nextSrcPath).renameTo(new File(nextDestPath));
                }
            }
        }
    }

    public static List<String> getPkgName(List<String> paths){
        ArrayList<String> list = new ArrayList<>();
        for (String path : paths) {
            File file = new File(path);
            File[] listFiles = file.listFiles();
            if (listFiles == null) {
                logger.error("File Structure does not meet standards");
                continue;
            }
            List<String> collect = Arrays.stream(listFiles).map(File::getName).filter(name -> !name.equals("META-INF") && !name.equals(".DS_Store")).collect(Collectors.toList());
            if (collect.size() == 0)
                throw new RuntimeException("Cannot find downloaded package.");
            for (String s : collect) {
                String pkgPath = handlePkg(path + File.separator + s);
                pkgPath = pkgPath.substring(path.length() + 1);
                pkgPath = pkgPath.replace('/','.');
                list.add(pkgPath);
            }
        }
        return list;
    }

    private static String handlePkg(String path){
        File file = new File(path);
        File[] innerFiles = file.listFiles((dir, name) -> !name.equals(".DS_Store"));
        if (!file.exists() || innerFiles == null || innerFiles.length != 1)
            return path;
        File innerFile = innerFiles[0];
        if (innerFile.isDirectory()){
            return handlePkg(path + File.separator + innerFile.getName());
        }
        return path;
    }
}