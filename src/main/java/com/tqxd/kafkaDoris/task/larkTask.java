package com.tqxd.kafkaDoris.task;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class larkTask implements Runnable{
    @Override
    public void run() {
            try {
                // 创建URL对象
                URL url = new URL("https://open.larksuite.com/open-apis/bot/v2/hook/975c9e0b-baab-4489-a7cf-1367d1094a04");

                // 创建HttpURLConnection对象
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // 设置请求方法为POST
                conn.setRequestMethod("POST");

                // 设置请求头部信息
                conn.setRequestProperty("Content-Type", "application/json");

                // 启用输出流
                conn.setDoOutput(true);

                // 构建请求内容
                String requestBody = "{\"msg_type\":\"text\",\"content\":{\"text\":\"哈喽\"}}";

                // 获取输出流并写入请求内容
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = requestBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // 获取响应码
                int responseCode = conn.getResponseCode();
                System.out.println("Response Code: " + responseCode);

                // 关闭连接
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
