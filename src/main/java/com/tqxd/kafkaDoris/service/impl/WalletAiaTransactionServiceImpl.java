package com.tqxd.kafkaDoris.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tqxd.kafkaDoris.entity.domain.WalletAiaTransaction;
import com.tqxd.kafkaDoris.mapper.WalletAiaTransactionMapper;
import com.tqxd.kafkaDoris.service.IWalletAiaTransactionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * aitd流水 服务实现类
 * </p>
 *
 * @author ember
 * @since 2023-06-06
 */
@Service
public class WalletAiaTransactionServiceImpl extends ServiceImpl<WalletAiaTransactionMapper, WalletAiaTransaction> implements IWalletAiaTransactionService {

    @Resource
    WalletAiaTransactionMapper transactionMapper;

    public String findTransactionsInfoByCriteria(String coin, String fromAddr, String toAddr, BigDecimal amount, Date startTime, Date endTime) {

        QueryWrapper<WalletAiaTransaction> queryWrapper = new QueryWrapper<>();



        // 设置查询条件
        queryWrapper.eq("coin", coin)
                .ge("block_time", startTime)
                .le("block_time", endTime)
                .ge("amount", amount);

        // 查询符合条件的交易记录
        List<WalletAiaTransaction> transactions = transactionMapper.selectList(queryWrapper);

        // 如果存在符合条件的数据，则返回信息
        if (!transactions.isEmpty()) {
            StringBuilder transactionInfo = new StringBuilder("警告：");

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (WalletAiaTransaction transaction : transactions) {
                String formattedDate = formatter.format(transaction.getBlockTime());
                transactionInfo.append(" 交易Hash: ").append(transaction.getTxHash())
                        .append(" From: ").append(transaction.getFromAddr())
                        .append(" To: ").append(transaction.getToAddr())
                        .append(" 交易量: ").append(transaction.getAmount())
                        .append(" Block Time: ").append(formattedDate)
                        .append(" | ");
            }

            // 移除最后的分隔符和空格
            if (transactionInfo.length() > 0) {
                transactionInfo.setLength(transactionInfo.length() - 3);
            }

            System.out.println(transactionInfo.toString());
            return transactionInfo.toString();
        } else {
            return null;
        }
    }

    @Override
    public List<WalletAiaTransaction> findTransactionsByCriteria(String coin, String fromAddr, String toAddr, BigDecimal amount, Date startTime, Date endTime) {
        QueryWrapper<WalletAiaTransaction> queryWrapper = new QueryWrapper<>();

        // 设置查询条件
        System.out.println(fromAddr);
        System.out.println(toAddr);
        if (StringUtils.isNotBlank(fromAddr)) {
            queryWrapper.eq("from_addr", fromAddr);
        }
        if (StringUtils.isNotBlank(toAddr)) {
            queryWrapper.eq("to_addr", toAddr);
        }

        queryWrapper
                .ge("block_time", startTime)
                .le("block_time", endTime)
                .ge("amount", amount);

        if (StringUtils.isNotBlank(coin)) {
            queryWrapper.eq("coin", coin);
        }

        // 查询符合条件的交易记录
        List<WalletAiaTransaction> transactions = transactionMapper.selectList(queryWrapper);

        return transactions;
    }

}
