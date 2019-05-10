package com.test;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.UnrarCallback;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TestForUnrarWithPass {

    @Test
    public void test001() throws IOException {
        unrar("D:/test/test.rar", "123");
    }

//    /**
//     * rar解密
//     *srcRarFile 为压缩包路径
//     *password 为解压缩密码
//     *destPath 为输入路径
//     */
//    public String UnRar(File srcRarFile, String password, String destPath) throws IOException {
//        if (null == srcRarFile || !srcRarFile.exists()) {
//            throw new IOException("指定压缩文件不存在.");
//        }
//        Archive archive = null;
//        FileOutputStream out = null;
//        try {
//            archive = new Archive(srcRarFile, password, false);
//            archive.getMainHeader().print();
//
//            FileHeader fileHeader = archive.nextFileHeader();
//            if (srcRarFile.isDirectory() && !srcRarFile.exists()) {
//                srcRarFile.mkdir();
//            }
//            while (null != fileHeader) {
//                if (fileHeader.isDirectory()) {
//                    // linux下需要转换反下划线
//                    fileHeader.setFileNameW(fileHeader.getFileNameW().replace("\\", "/"));
//                    File unDirectory = new File(destPath + fileHeader.getFileNameW());
//                    unDirectory.mkdir();
//                } else {
//                    // linux下需要转换反下划线
//                    fileHeader.setFileNameW(fileHeader.getFileNameW().replace("\\", "/"));
//                    File unFile = new File(destPath + fileHeader.getFileNameW().trim());
//                    if (!unFile.exists()) {
//                        if (!unFile.getParentFile().exists()) {
//                            unFile.getParentFile().mkdir();
//                        }
//                        unFile.createNewFile();
//                    }
//                    out = new FileOutputStream(unFile);
//                    archive.extractFile(fileHeader, out);
//                    out.flush();
//                    out.close();
//                }
//                fileHeader = archive.nextFileHeader();
//            }
//            archive.close();
//            return "操作成功";
//        } catch (RarException e) {
//            e.printStackTrace();
//            archive.close();
//            out.flush();
//            out.close();
//            return "操作失败，密码错误或其他异常";
//        } catch (IOException e) {
//            e.printStackTrace();
//            archive.close();
//            out.flush();
//            out.close();
//            return "操作失败，解压异常";
//        }
//    }

    public static final String SEPARATOR = File.separator;

    // =============================== RAR Format ================================
    /**
     * 解压指定RAR文件到当前文件夹
     * @param srcRar 指定解压
     *  @param password 压缩文件时设定的密码
     * @throws IOException
     */
    public static void unrar(String srcRar, String password) throws IOException {
        unrar(srcRar, null, password);
    }

    /**
     * 解压指定的RAR压缩文件到指定的目录中
     * @param srcRar 指定的RAR压缩文件
     * @param destPath 指定解压到的目录
     *  @param password 压缩文件时设定的密码
     * @throws IOException
     */
    public static void unrar(String srcRar, String destPath, String password) throws IOException {
        File srcFile = new File(srcRar);
        if (!srcFile.exists()) {
            return;
        }
        if (null == destPath || destPath.length() == 0) {
            unrar(srcFile, srcFile.getParent(), password);
            return;
        }
        unrar(srcFile,destPath, password);
    }

    /**
     * 解压指定RAR文件到当前文件夹
     * @param srcRarFile 解压文件
     *  @param password 压缩文件时设定的密码
     * @throws IOException
     */
    public static void unrar(File srcRarFile, String password) throws IOException {
        if (null == srcRarFile || !srcRarFile.exists()) {
            throw new IOException("指定文件不存在.");
        }
        unrar(srcRarFile, srcRarFile.getParent(),password);
    }

    /**
     * 解压指定RAR文件到指定的路径
     * @param srcRarFile 需要解压RAR文件
     * @param destPath 指定解压路径
     * @param password 压缩文件时设定的密码
     * @throws IOException
     */
    public static void unrar(File srcRarFile, String destPath, String password) throws IOException {
        if (null == srcRarFile || !srcRarFile.exists()) {
            throw new IOException("指定压缩文件不存在.");
        }
        if (!destPath.endsWith(SEPARATOR)) {
            destPath += SEPARATOR;
        }
        Archive archive = null;
        OutputStream unOut = null;
        try {
            // archive = new Archive(srcRarFile, password, false);
            archive = new Archive(srcRarFile, null, password, false);

            FileHeader fileHeader = archive.nextFileHeader();
            while(null != fileHeader) {
                if (!fileHeader.isDirectory()) {
                    // 1 根据不同的操作系统拿到相应的 destDirName 和 destFileName
                    String destFileName = "";
                    String destDirName = "";
                    if (SEPARATOR.equals("/")) {		// 非windows系统
                        destFileName = (destPath + fileHeader.getFileNameW()).replaceAll("\\\\", "/");
                        destDirName = destFileName.substring(0, destFileName.lastIndexOf("/"));
                    } else {		// windows系统
                        destFileName = (destPath + fileHeader.getFileNameW()).replaceAll("/", "\\\\");
                        destDirName = destFileName.substring(0, destFileName.lastIndexOf("\\"));
                    }
                    // 2创建文件夹
                    File dir = new File(destDirName);
                    if (!dir.exists() || !dir.isDirectory()) {
                        dir.mkdirs();
                    }
                    // 抽取压缩文件
                    unOut = new FileOutputStream(new File(destFileName));
                    archive.extractFile(fileHeader, unOut);
                    unOut.flush();
                    unOut.close();
                }
                fileHeader = archive.nextFileHeader();
            }
            archive.close();
        } catch (RarException e) {
            e.printStackTrace();
        } finally {
            // IOUtils.closeQuietly(unOut);
            if (null != unOut) unOut.close();
        }
    }

}
