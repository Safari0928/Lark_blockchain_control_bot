package com.tqxd.kafkaDoris.component;

import com.tqxd.kafkaDoris.entity.domain.WalletAiaTransaction;
import com.tqxd.kafkaDoris.service.IWalletAiaTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiaScan {
    @Resource
    IWalletAiaTransactionService walletAiaTransactionService;

    public void scanTransaction(Map<String, String> config) {
        String coin = config.get("代币");
        String fromConfig = config.getOrDefault("FROM", "");
        String toConfig = config.getOrDefault("TO", "");
        String[] fromAddresses = fromConfig.isEmpty() ? new String[]{null} : fromConfig.split(",");
        String[] toAddresses = toConfig.isEmpty() ? new String[]{null} : toConfig.split(",");
        String amount = config.get("交易量");
        String start = config.get("开始区块时间");
        String end = config.get("结束区块时间");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = dateFormat.parse(start);
            endDate = (end != null && !end.isEmpty()) ? dateFormat.parse(end) : new Date();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        boolean isFromEmpty = fromConfig.isEmpty();
        boolean isToEmpty = toConfig.isEmpty();

        if (isFromEmpty && isToEmpty) {
            // Her iki adres de boşsa, tüm işlemleri tarayın
            processTransactions(coin, null, null, amount, startDate, endDate);
        } else {
            for (String from : fromAddresses) {
                for (String to : toAddresses) {
                    processTransactions(coin, from, to, amount, startDate, endDate);
                }
            }
        }

        log.info("start transactionsByCriteria");
    }

    private void processTransactions(String coin, String from, String to, String amount, Date startDate, Date endDate) {
        List<WalletAiaTransaction> transactions = walletAiaTransactionService.findTransactionsByCriteria(coin, from, to, new BigDecimal(amount), startDate, endDate);

        for (WalletAiaTransaction transaction : transactions) {
            try {
                String formattedTransactionInfo = formatTransactionInfo(transaction);
                sendToMessage(formattedTransactionInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String formatTransactionInfo(WalletAiaTransaction transaction) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = formatter.format(transaction.getBlockTime());
        return "警告！: 交易Hash: " + transaction.getTxHash() +
                " From: " + transaction.getFromAddr() +
                " To: " + transaction.getToAddr() +
                " 代币: " + transaction.getCoin() +
                " 交易量: " + transaction.getAmount() +
                " 区块时间: " + formattedDate;
    }

    protected void sendToMessage(String content) throws IOException {
        URL url = new URL("https://open.larksuite.com/open-apis/bot/v2/hook/daeeb5cb-3e88-4a28-96f2-27660f14797d");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String requestBody = "{\"msg_type\":\"text\",\"content\":{\"text\":\"" + content + "\"}}";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        conn.disconnect();
    }

}
