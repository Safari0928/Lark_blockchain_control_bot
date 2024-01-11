package com.tqxd.kafkaDoris.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobDispatch {

    private final AiaScan aiaScan;
    private final TaskScheduler taskScheduler;

    @Value("${config-file-path}")
    private String configFilePath;

    private long lastReadTime = 0;
    private boolean onOff = true;
    private Map<String, String> lastReadConfig = new HashMap<>();
    private int queryIntervalSeconds = 20; // Varsayılan sorgu aralığı
    private boolean onceLock = false;

    @PostConstruct
    private void init() {
        try {
            Map<String, String> config = readConfigFile(configFilePath);
            queryIntervalSeconds = Math.max(20, Integer.parseInt(config.getOrDefault("查询间隔秒", "20")));
            taskScheduler.scheduleAtFixedRate(this::performScan, queryIntervalSeconds * 1000L);
        } catch (IOException e) {
            log.error("Error initializing JobDispatch", e);
        }
    }

    private void performScan() {
        try {
            checkForExternalConfigChanges();
            Map<String, String> config = readConfigFile(configFilePath);
            String status = config.get("开关");

            if ("0".equals(status)) {
                if (onOff) {
                    aiaScan.sendToMessage("查询已关闭");
                    onOff = false;
                }
                return;
            }
            onOff = true;

            scanAiaTransaction(config);
        } catch (IOException e) {
            log.error("Error during scan", e);
        }
    }

    public void scanAiaTransaction(Map<String, String> config) {
        try {
            String endTimeStr = config.getOrDefault("结束区块时间", "");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            Date endTime = endTimeStr.isEmpty() ? null : dateFormat.parse(endTimeStr);

            if (endTime == null) {
                aiaScan.scanTransaction(config);
                updateConfigFile(configFilePath, dateFormat.format(now));
                onceLock = false;
            } else {
                if (!onceLock) {

                    aiaScan.scanTransaction(config);
                    aiaScan.sendToMessage("指定范围查询结束！");
                    onceLock = true;
                }
            }
        } catch (IOException | ParseException e) {
            log.error("Error reading or updating configuration file", e);
        }
    }


    private Map<String, String> readConfigFile(String filePath) throws IOException {
        Map<String, String> config = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    config.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return config;
    }

    private void updateConfigFile(String filePath, String newStartTime) throws IOException {
        File file = new File(filePath);
        StringBuilder newContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("开始区块时间")) {
                    newContent.append("开始区块时间: ").append(newStartTime).append("\n");
                } else {
                    newContent.append(line).append("\n");
                }
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(newContent.toString());
        }
    }


   /* private void checkForExternalConfigChanges() {
        File configFile = new File(configFilePath);
        long currentModifiedTime = configFile.lastModified();

        if (currentModifiedTime > lastReadTime) {
            Map<String, String> currentConfig = null;
            try {
                currentConfig = readConfigFile(configFilePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (!currentConfig.equals(lastReadConfig)) {
                Map<String, String> reorderedConfig = new LinkedHashMap<>();
                reorderedConfig.put("开始区块时间", currentConfig.get("开始区块时间"));
                reorderedConfig.put("结束区块时间", currentConfig.get("结束区块时间"));
                reorderedConfig.put("交易量", currentConfig.get("交易量"));
                reorderedConfig.put("代币", currentConfig.get("代币"));
                reorderedConfig.put("FROM", currentConfig.get("FROM"));
                reorderedConfig.put("TO", currentConfig.get("TO"));
                reorderedConfig.put("查询间隔秒", currentConfig.get("查询间隔秒"));
                try {
                    aiaScan.sendToMessage("查询信息更新: " + reorderedConfig);
                    onceLock = false;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                lastReadConfig = new HashMap<>(currentConfig);
            }
            lastReadTime = currentModifiedTime;
        }
    }
    */

    private void checkForExternalConfigChanges() throws IOException {
        File configFile = new File(configFilePath);
        long currentModifiedTime = configFile.lastModified();

        if (currentModifiedTime > lastReadTime) {
            Map<String, String> currentConfig = null;
            try {
                currentConfig = readConfigFile(configFilePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            boolean dataChanged = false;
            for (Map.Entry<String, String> entry : currentConfig.entrySet()) {
                String key = entry.getKey();
                String currentValue = entry.getValue();

                if (!key.equals("开始区块时间") && !Objects.equals(currentValue, lastReadConfig.get(key))) {
                    dataChanged = true;

                    lastReadConfig.put(key, currentValue);
                }
            }

            if (dataChanged) {
                aiaScan.sendToMessage( " 更新: " + currentConfig);
                lastReadTime = currentModifiedTime;
            }
        }
    }


}
