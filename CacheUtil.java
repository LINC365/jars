package com.lkl.linc.app1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


/**
 * 公共存储工具，
 * created by lkl
 * 2019年8月9日11:50:20
 */
public class CacheUtil {
    private static final String ERROR_FILENAME_EMPTY = "文件名不能为空";
    private static final String ERROR_FILEDATA_EMPTY = "存储内容不能为空";
    private static final String ERROR_FILE_NOT_EXIST = "文件不存在";
    private static final int ZERO = 0;
    private static final boolean FALSE=false;
    private static final String EMPTY="";

    private static SharedPreferences sp;
    private static CacheUtil instance;
    private static SharedPreferences.Editor edt;

    private CacheUtil() {
        sp = MyApplication.getInstance().getApplicationContext().getSharedPreferences("mylklapp1", Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor() {
        if (edt == null) {
            synchronized (SharedPreferences.Editor.class) {
                if (edt == null) {
                    edt = sp.edit();
                }
            }
        }
        return edt;
    }

    public static CacheUtil getInstance() {
        if (instance == null) {
            synchronized (CacheUtil.class) {
                if (instance == null) {
                    instance = new CacheUtil();
                }
            }
        }
        return instance;
    }

    public void setInt(String key, int value) {
        getEditor().putInt(key, value);
        apply();
    }

    public int getInt(String key) {
        return sp.getInt(key, ZERO);
    }

    public void setString(String key, String value) {
        getEditor().putString(key, value);
        apply();
    }

    public String getString(String key) {
        return sp.getString(key, EMPTY);
    }

    public void setLong(String key, long value) {
        getEditor().putLong(key, value);
        apply();
    }

    public long getLong(String key) {
        return sp.getLong(key, ZERO);
    }

    public void setBoolean(String key, boolean b) {
        getEditor().putBoolean(key, b);
        apply();
    }

    public boolean getBoolean(String key) {
        return sp.getBoolean(key, FALSE);
    }

    public void removeContent(String name) {
        getEditor().remove(name);
        apply();
    }

    public void clearContent() {
        getEditor().clear();
        apply();
    }

    public void crearContent(String key) {
        getEditor().remove(key);
        apply();
    }

    public void applyNow() {
        handler.post(runnable);
    }

    private void apply() {
        if (!isWating) {
            isWating = true;
            handler.postDelayed(runnable, 500);
        }
        oneTimeSaveCount++;
    }

    private int oneTimeSaveCount = 0;
    private boolean isWating = false;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                getEditor().apply();
                isWating = false;
                oneTimeSaveCount = 0;
            } catch (Exception e) {
                Logger.e("键值保存失败数据量(" + oneTimeSaveCount + ")" + e.getMessage());
                oneTimeSaveCount = 0;
            }
        }
    };

    public void saveDataToFile(final String fileName, final String data, final OnMyFileSaveListener onMyFileSaveListener) {
        if (TextUtils.isEmpty(fileName)) {
            if (onMyFileSaveListener != null) {
                onMyFileSaveListener.onErrorListener(EMPTY, ERROR_FILENAME_EMPTY);
            }
            return;
        }
        if (TextUtils.isEmpty(data)) {
            if (onMyFileSaveListener != null) {
                onMyFileSaveListener.onErrorListener(fileName, ERROR_FILEDATA_EMPTY);
            }
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream out;
                BufferedWriter writer = null;
                try {
                    //设置文件名称，以及存储方式
                    out = MyApplication.getInstance().getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
                    //创建一个OutputStreamWriter对象，传入BufferedWriter的构造器中
                    writer = new BufferedWriter(new OutputStreamWriter(out));
                    //向文件中写入数据
                    writer.write(data);
                    if (onMyFileSaveListener != null) {
                        onMyFileSaveListener.onSavedListener(data.getBytes().length, data.getBytes().length, fileName);
                    }
                } catch (IOException e) {
//                    Logger.e("文件存储失败[" + fileName + "][" + data.substring(data.length() / 3) + "...]" + e.getMessage());
                    if (onMyFileSaveListener != null) {
                        onMyFileSaveListener.onErrorListener(fileName, e.getMessage());
                    }
                } finally {
                    try {
                        assert writer != null;
                        writer.close();
                    } catch (IOException e) {
//                        Logger.e("文件存储writer.close()失败[" + fileName + "]" + e.getMessage());
                        if (onMyFileSaveListener != null) {
                            onMyFileSaveListener.onErrorListener(fileName, "[writer.close()]" + e.getMessage());
                        }
                    }
                }
            }
        }).start();
    }
    private void resetFileContentEmpty(final String fileName, final OnMyFileSaveListener onMyFileSaveListener){
        if (TextUtils.isEmpty(fileName)) {
            if (onMyFileSaveListener != null) {
                onMyFileSaveListener.onErrorListener(EMPTY, ERROR_FILENAME_EMPTY);
            }
            return;
        }
        if (!fileExist(fileName)) {
            if (onMyFileSaveListener != null) {
                onMyFileSaveListener.onErrorListener(fileName, ERROR_FILE_NOT_EXIST);
            }
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream out;
                BufferedWriter writer = null;
                try {
                    //设置文件名称，以及存储方式
                    out = MyApplication.getInstance().getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
                    //创建一个OutputStreamWriter对象，传入BufferedWriter的构造器中
                    writer = new BufferedWriter(new OutputStreamWriter(out));
                    //向文件中写入数据
                    writer.write(EMPTY);
                    if (onMyFileSaveListener != null) {
                        onMyFileSaveListener.onSavedListener(EMPTY.getBytes().length, EMPTY.getBytes().length, fileName);
                    }
                } catch (IOException e) {
//                    Logger.e("文件存储失败[" + fileName + "][" + data.substring(data.length() / 3) + "...]" + e.getMessage());
                    if (onMyFileSaveListener != null) {
                        onMyFileSaveListener.onErrorListener(fileName, e.getMessage());
                    }
                } finally {
                    try {
                        assert writer != null;
                        writer.close();
                    } catch (IOException e) {
//                        Logger.e("文件存储writer.close()失败[" + fileName + "]" + e.getMessage());
                        if (onMyFileSaveListener != null) {
                            onMyFileSaveListener.onErrorListener(fileName, "[writer.close()]" + e.getMessage());
                        }
                    }
                }
            }
        }).start();
    }

    public void getDataFromFile(final String fileName, final OnMyFileGetListener onMyFileGetListener) {
        if (TextUtils.isEmpty(fileName)) {
            if (onMyFileGetListener != null) {
                onMyFileGetListener.onErrorListener(EMPTY, ERROR_FILENAME_EMPTY);
            }
            return;
        }
        if (!fileExist(fileName)) {
            if (onMyFileGetListener != null) {
                onMyFileGetListener.onErrorListener(fileName, ERROR_FILE_NOT_EXIST);
            }
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream in;
                BufferedReader reader = null;
                StringBuilder content = new StringBuilder();
                try {
                    //设置将要打开的存储文件名称
                    in = MyApplication.getInstance().getApplicationContext().openFileInput(fileName);
                    //FileInputStream -> InputStreamReader ->BufferedReader
                    reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    //读取每一行数据，并追加到StringBuilder对象中，直到结束
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    if (onMyFileGetListener != null) {
                        onMyFileGetListener.onGetedListener(content.length(), fileName, content.toString());
                    }
                } catch (IOException e) {
                    if (onMyFileGetListener != null) {
                        onMyFileGetListener.onErrorListener(fileName, e.getMessage());
                    }
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            if (onMyFileGetListener != null) {
                                onMyFileGetListener.onErrorListener(fileName, "[writer.close()]" + e.getMessage());
                            }
                        }
                    }
                }
            }
        }).start();
    }

    public void deleteFile(final String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return;
        }
        if (!fileExist(fileName)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    resetFileContentEmpty(fileName, new OnMyFileSaveListener() {
                        @Override
                        public void onSavedListener(int i1, int i2, String fileName) {
                            if (i1 == 0) {
                                Logger.i("退出登录后，" + fileName + "已置空");
                            }else{
                                Logger.i("退出登录后，无法将" + fileName + "置空");
                            }
                        }

                        @Override
                        public void onErrorListener(String fileName, String errorMsg) {
                            Logger.i("退出登录后，无法将" + fileName + "置空");

                        }
                    });
                } catch (Exception e) {
                    Logger.e("文件删除失败" + e.getMessage());
                }
            }
        }).start();
    }

    private static String[] list_dbfile;

    private static String[] getList_dbfile() {
        if (list_dbfile == null || list_dbfile.length <= 0) {
            list_dbfile = MyApplication.getInstance().getApplicationContext().fileList();
        }
        return list_dbfile;
    }

    public boolean fileExist(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        for (String s : getList_dbfile()) {
            if (fileName.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public interface OnMyFileGetListener {

        void onGetedListener(int i1, String fileName, String data);

        void onErrorListener(String fileName, String errorMsg);
    }

    public interface OnMyFileSaveListener {

        void onSavedListener(int i1, int i2, String fileName);

        void onErrorListener(String fileName, String errorMsg);
    }
}
