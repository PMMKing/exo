package com.haolb.client.utils;

import android.content.Intent;
import android.net.Uri;

import java.io.File;

public class IntentFactory {
	private static Intent intent;
	private static Uri uri;

	// 文件类型 0:图片 1:音乐2:视频 3:文档 4:应用 5:普通文件6文本消息
	public static Intent getIntentByType(String path, int type) {
		intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		uri = Uri.fromFile(new File(path));
		switch (type) {
		case 0:
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			uri = Uri.fromFile(new File(path));
			intent.setDataAndType(uri, "image/*");
			break;
		case 1:
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("oneshot", 0);
			intent.putExtra("configchange", 0);
			intent.setDataAndType(uri, "audio/*");
			break;
		case 2:
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("oneshot", 0);
			intent.putExtra("configchange", 0);
			intent.setDataAndType(uri, "video/*");
			break;
		case 3:
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(uri, "application/msword");
			break;
		case 4:
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + path),
					"application/vnd.android.package-archive");

			break;
		default:
			break;
		}

		return intent;
	}
}
