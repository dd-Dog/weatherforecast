package com.flyscale.weatherforecast.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.flyscale.weatherforecast.global.Constants;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by bian on 2018/8/30.
 */

public class FTPUtil {
    private static final String TAG = "FTPUtil";

    public static void upLoadFile(String filePath) {
        try {
            FileInputStream in = new FileInputStream(new File(filePath));
            boolean flag = uploadFile("127.0.0.1", 21, "test", "test", "D:/ftp", "test.txt", in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean downLoadFileFromDefServer(Context context) {
        String hostname = PreferenceUtil.getString(context, Constants.FTP_HOSTNAME, null);
        String portStr = PreferenceUtil.getString(context, Constants.FTP_PORT, null);
        String username = PreferenceUtil.getString(context, Constants.FTP_USERNAME, null);
        String password = PreferenceUtil.getString(context, Constants.FTP_PASSWD, null);
        String remotePath = PreferenceUtil.getString(context, Constants.FTP_DOWNLOAD_FILE_REMOTEPATH, null);
        String localPath = PreferenceUtil.getString(context, Constants.FTP_DOWNLOAD_FILE_LOCALPATH, null);
        String filename = PreferenceUtil.getString(context, Constants.FTP_DOWNLOAD_FILE_NAME, null);
        Log.d(TAG, "downLoadFileFromDefServer::hostname=" + hostname + ",port=" + portStr + ",username=" + username + ",passwd=" + password
                + ",remotePath=" + remotePath + ",localPathi=" + localPath + ",fileName=" + filename);
        if (TextUtils.isEmpty(hostname) || TextUtils.isEmpty(portStr) || TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(remotePath) || TextUtils.isEmpty(localPath) ||
                TextUtils.isEmpty(filename)) {
            return false;
        }
        int port = Integer.parseInt(portStr);

        int successTime = 0;
        for (int i = 0; i < 5; i++) {
            Log.d(TAG, "download task complete start TIME=" + i);
            if (downLoadFile(context, hostname, port, username, password, remotePath, filename, localPath)) {
                successTime++;
                Log.d(TAG, "download task complete SUCCESS!!! TIME=" + i);
            }else {
                Log.d(TAG, "download task complete FAIL!!! TIME=" + i);
            }
        }
        return successTime > 0;
    }

    /**
     * Description: 从FTP服务器下载文件
     *
     *
     * @param context
     * @param hostname   FTP服务器hostname
     * @param port       FTP服务器端口
     * @param username   FTP登录账号
     * @param password   FTP登录密码
     * @param remotePath FTP服务器上的相对路径
     * @param fileName   要下载的文件名
     * @param localPath  下载后保存到本地的路径
     * @return
     */
    public static boolean downLoadFile(Context context, String hostname, int port, String username, String password, String remotePath, String fileName, String localPath) {
        Log.d(TAG, "downLoadFile, hostname=" + hostname + ",port=" + port + ",username=" + username + ",passwd=" + password
                + ",remotePath=" + remotePath + ",localPathi=" + localPath + ",fileName=" + fileName);
        boolean success = false;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(hostname, port);
            //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);//登录
            reply = ftp.getReplyCode();
            Log.d(TAG, "replyCode = " + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                Log.d(TAG, "negtive, disconnect");
                return false;
            }
            ftp.changeWorkingDirectory(remotePath);//转移到FTP服务器目录
            FTPFile[] fs = ftp.listFiles();
            long sizeAll = 0;
            for (FTPFile ff : fs) {
                Log.d(TAG, "FTP Server file  name=" + ff.getName() + ",filesize=" + ff.getSize());
                if (ff.getName().equals(fileName)) {
                    File localpath = new File(localPath);
                    if (!localpath.exists()) {
                        boolean mkdirs = localpath.mkdirs();
                        Log.d(TAG, "mkdirs=" + mkdirs);
                        if (!mkdirs) {
                            return false;
                        }
                    }
                    File localFile = new File(localPath + File.separator + ff.getName());
                    OutputStream is = new FileOutputStream(localFile);
                    Log.d(TAG, "downloading...");
                    ftp.retrieveFile(ff.getName(), is);
                    is.close();
                    sizeAll += ff.getSize();
                }else {
                    Log.d(TAG, "no such file or directory on FTP Server,target fileName=" + fileName);
                    return false;
                }
            }
            ftp.logout();
            success = true;
            Log.d(TAG, "download complelte successfully !!!");
            saveDownload(context, sizeAll);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ignored) {
                }
            }
        }
        return success;
    }

    private static void saveDownload(Context context, long sizeAll) {
        Log.d(TAG, "saveDownload");
        String flowAlreadyStr = PreferenceUtil.getString(context, Constants.TRAFFIC_EXTRA, "0");
        long flowAlready = Long.parseLong(flowAlreadyStr);
        Log.d(TAG, "flow on this time=" + sizeAll/1024 + "KB");
        Log.d(TAG, "flowAlready=" + flowAlready/1024 + "KB");
        PreferenceUtil.put(context, Constants.TRAFFIC_EXTRA, (flowAlready + sizeAll) + "");
    }

    /**
     * Description: 向FTP服务器上传文件
     *
     * @param url      FTP服务器hostname
     * @param port     FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param path     FTP服务器保存目录
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     * @return 成功返回true，否则返回false
     */
    public static boolean uploadFile(String url, int port, String username, String password, String path, String filename, InputStream input) {
        boolean success = false;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(url, port);//连接FTP服务器
            //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);//登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            ftp.changeWorkingDirectory(path);
            ftp.storeFile(filename, input);

            input.close();
            ftp.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }


}
