/**
 * ExportClass.java
 * com.simon
 *
 * Function： 根据补丁包文件导出（打包）需要发布的补丁文件
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2017-2-27 		Simon
 *
 * Copyright (c) 2016, 91Bee All Rights Reserved.
 */

package com.simon;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ClassName:ExportClass
 * 
 * @author Simon
 * @version
 * @since Ver 1.1
 * @Date 2017-2-27
 * 
 * @see
 */
public class ExportClass {
    private static List<String> fileNamels = new ArrayList<String>();
    private static String filesp = System.getProperty("file.separator");
    private static boolean isExportAll = true;// 是否提取src目录下所有的文件，false:只提取java文件相对应的.class文件，true:提取java文件相对应的.class文件与其它文件
    private static boolean isZip = true;
    private static int listNum = 0;
    private static int copyNum = 0;
    private static int zipNum = 0;

    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("请输入项目名称:");
            String projectName = reader.readLine();
            
            System.out.println("请输入项目路径:");
            String srcPath = reader.readLine();
            if("".equals(srcPath) || null == srcPath) {
                srcPath = "D:\\workspace";
            }else if(srcPath.endsWith(filesp)) {
                srcPath.substring(0, srcPath.length() - 1);
            }
            
            System.out.println("请输入补丁路径:");
            String targetPath = reader.readLine();
            if("".equals(targetPath) || null == targetPath) {
                targetPath = "D:\\patch";
            }else if(targetPath.endsWith(filesp)) {
                targetPath.substring(0, targetPath.length() - 1);
            }
            
            ExportClass.exportAll(targetPath, srcPath + filesp + projectName);
            if(isZip){
                ExportClass.zipFile(targetPath + "\\webapp", targetPath, projectName);
                System.out.println(String.format("======文件路径：%s\\%s.zip======", targetPath, projectName));
            }
            
            System.out.println("======本次操作已全部结束======");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            
        }
    }

    /**
     * exportAll:导出所有文件
     *
     * @param srcRoot      补丁文件所在路径
     * @param projectRoot  项目文件所在路径
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public static void exportAll(String srcRoot, String projectRoot) {
        ExportClass.fileNamels.clear();
        ExportClass.listFile(new File(srcRoot));
        System.out.println(String.format(">>> 文件遍历完成，共【%s】个文件待复制 <<<", listNum));
        System.out.println();
        String src = null;
        String des = null;
        for (String fileName : fileNamels) {
            if (fileName.indexOf(filesp + "filters" + filesp) != -1) {
                src = projectRoot + filesp + "src" + fileName;
                des = srcRoot + filesp + "webapp" + filesp + "WEB-INF" + filesp + "classes" + fileName.substring(fileName.lastIndexOf(filesp));
            } else if (fileName.indexOf(filesp + "resources" + filesp) != -1) {
                src = projectRoot + filesp + "src" + fileName;
                des = srcRoot + filesp + "webapp" + filesp + "WEB-INF" + filesp + "classes" + fileName.substring(fileName.indexOf(filesp + "resources" + filesp) + 10);
            } else if (fileName.indexOf(filesp + "webapp" + filesp) != -1) {
                src = projectRoot + filesp + "src" + fileName;
                des = srcRoot + filesp + "webapp" + fileName.substring(fileName.lastIndexOf(filesp));
            } else {
                src = projectRoot + filesp + "target" + filesp + "classes" + fileName;
                des = srcRoot + filesp + "webapp" + filesp + "WEB-INF" + filesp + "classes" + fileName;
            }
            copyFile(src, des);
        }
        System.out.println(String.format(">>> 文件复制完成，共复制【%s】个文件  <<<", copyNum));
        System.out.println();
    }

    /**
     * listFile:列出svn 更新时导出的文件中 src目录下所有文件的文件名（包括相对src的全路径，java文件名变为相对应的class文件名）
     *
     * @param f svn导出文件
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public static void listFile(File f) {

        if (f.isDirectory()) {
            File[] t = f.listFiles();
            for (int i = 0; i < t.length; i++) {
                listFile(t[i]);
            }
        } else {
            String fileName = f.getAbsolutePath();

            if (fileName.indexOf(filesp + "src" + filesp) != -1) {
                if (fileName.lastIndexOf(filesp + "java" + filesp) != -1) {
                    String javaFile = "";
                    if (fileName.indexOf(".java") != -1) {
                        javaFile = fileName.substring(f.getAbsolutePath().indexOf(filesp + "java" + filesp) + 5, fileName.lastIndexOf(".java")) + ".class";
                    } else {
                        javaFile = fileName.substring(f.getAbsolutePath().indexOf(filesp + "java" + filesp) + 5);
                    }
                    ExportClass.fileNamels.add(javaFile);
                    System.out.println(String.format("文件【%s】", javaFile));
                } else {
                    if (ExportClass.isExportAll) {
                        String otherFile = fileName.substring(f.getAbsolutePath().indexOf(filesp + "src" + filesp) + 4);
                        ExportClass.fileNamels.add(otherFile);
                        System.out.println(String.format("文件【%s】", otherFile));
                    }
                }
                listNum ++;
            }
        }
    }

    /**
     * copyFile:复制文件
     *
     * @param oldPath  文件原路径
     * @param newPath  文件新路径
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private static void copyFile(String oldPath, String newPath) { // 复制文件
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            File newfile = new File(newPath);

            if (!newfile.getParentFile().exists()) {// 目录不存在时，创建目录
                newfile.getParentFile().mkdirs();
            }

            if (oldfile.exists()) { // 文件存在时
                InputStream inStream = new FileInputStream(oldPath); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);

                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                System.out.println(String.format("文件【%s】 复制大小:%s KB", oldPath, (double) bytesum / 1024));
                copyNum++ ;
            } else {
                System.out.println(String.format("文件【%s】不存在！！！", oldPath));
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        }
    }

    /**
     * zipFile:打包文件
     *
     * @param srcPath  待打包文件原路径
     * @param zipPath  打包文件存放路径
     * @param zipName  包文件名
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private static void zipFile(String srcPath, String zipPath, String zipName) { // 压缩文件
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            System.out.println(String.format("待压缩文件目录【%s】不存在！", srcPath));
            return;
        }

        ZipOutputStream zos = null;
        try {
            createDir(zipPath);
            File zipFile = new File(zipPath + filesp + zipName + ".zip");

            File[] srcFiles = srcFile.listFiles();
            if (null == srcFiles || srcFiles.length < 1) {
                System.out.println(String.format("待压缩目录【%s】下无文件！", srcPath));
                return;
            }

            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            for (int i = 0; i < srcFiles.length; i++) {
                if (null != srcFiles[i]) {
                    zipFile(zos, srcFiles[i], srcFiles[i].getName());
                }
            }
            zos.close(); // 输出流关闭
            System.out.println(String.format(">>> 文件压缩完成，共压缩【%s】个文件  <<<", zipNum));
            System.out.println();
        } catch (Exception e) {
            System.out.println("压缩文件操作出错！");
            e.printStackTrace();
        }

    }

    /**
     * createDir:创建目录
     *
     * @param path 目录路径 
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private static void createDir(String path) {
        File targetFile = new File(path);
        if (!targetFile.exists()) {// 目录不存在时，先创建目录
            targetFile.mkdirs();
        }
    }

    /**
     * zipFile:压缩文件
     *
     * @param out      zip文件输出流
     * @param file     待压缩文件（目录）
     * @param fileName 文件（目录）名称
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private static void zipFile(ZipOutputStream out, File file, String fileName) {
        try {
            if (file.isDirectory()) {// 压缩目录
                try {
                    File[] fl = file.listFiles();
                    if (fl.length == 0) {
                        out.putNextEntry(new ZipEntry(fileName + filesp)); // 创建zip实体
                    }
                    for (int i = 0; i < fl.length; i++) {
                        zipFile(out, fl[i], fileName + filesp + fl[i].getName()); // 递归遍历子文件夹
                    }
                } catch (IOException e) {
                    System.out.println("执行目录压缩操作出错！");
                    e.printStackTrace();
                }
            } else { // 压缩单个文件
                out.putNextEntry(new ZipEntry(fileName)); // 创建zip实体
                FileInputStream in = new FileInputStream(file);
                BufferedInputStream bi = new BufferedInputStream(in);
                int b;
                while ((b = bi.read()) != -1) {
                    out.write(b); // 将字节流写入当前zip目录
                }
                out.closeEntry(); // 关闭zip实体
                in.close(); // 输入流关闭
                zipNum++;
                System.out.println(String.format("压缩【%s】完成", fileName));
            }

        } catch (IOException e) {
            System.out.println("压缩文件操作出错！");
            e.printStackTrace();
        }
    }

}
