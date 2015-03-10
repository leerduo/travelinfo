package com.ustc.dystu.dandelion.utils.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.net.DandelionAPI;
import com.ustc.dystu.dandelion.utils.Logger;


public class ImageService {

	public static InputStream getImage(String path) throws Exception {
		// 构造一个URL对象
		URL url = new URL(path);
		// 使用openConnection打开URL对象
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		// 使用Http协议，设置请求方式为GET
		conn.setRequestMethod("GET");
		conn.setUseCaches(false);
		/**
		 * 不允许重定向,HttpURLConnection默认允许重定向
		 */
		conn.setInstanceFollowRedirects(false);
		int responseCode = conn.getResponseCode();
		Logger.d("DEBUG", "responseCode:" + responseCode);
		if (responseCode != HttpStatus.SC_OK) {
			return null;
		}
		// 通过输入流获取图片数据
		// InputStream inStream = conn.getInputStream();
		// 返回图片的二进制数据
		// return readInputStream(inStream, md5, ctx);
		return conn.getInputStream();
	}

	public static File saveBmpToSd(InputStream inputStream, Context ctx) {
		File file = createCacheProfileDir(ctx);
		File cacheFile = new File(file.getPath() + "/"
				+ DandelionAPI.getInstance(ctx).getUid());
		FileOutputStream outStream = null;
		try {
			cacheFile.createNewFile();
			outStream = new FileOutputStream(cacheFile);
			byte[] buffer = new byte[1024];
			int num = 0;

			while ((num = inputStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, num);
			}
			outStream.flush();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outStream != null) {
					outStream.close();
				}
			} catch (Exception e2) {
			}
		}
		return cacheFile;
	}

	public static Bitmap getBitmapFormSd(Context ctx) {

		File file = createCacheProfileDir(ctx);
		File cacheFile = new File(file.getPath() + "/"
				+ DandelionAPI.getInstance(ctx).getUid());
		Bitmap bitmap = null;
		InputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(cacheFile);
			bitmap = BitmapFactory.decodeStream(fileInputStream);
		} catch (Exception e) {
			Logger.d("MoreFragment", "exception");
			e.printStackTrace();
		} finally {
			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (Exception e2) {
			}
		}
		return bitmap;
	}

	public static File createCacheProfileDir(Context ctx) {
		// 创建本地缓存路径
		File root = Environment.getExternalStorageDirectory();
		String filepath = root.getAbsoluteFile() + "/"
				+ Constants.THUMNAIL_CACHE_PATH + "/profile/";
		File file = new File(filepath);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}
}