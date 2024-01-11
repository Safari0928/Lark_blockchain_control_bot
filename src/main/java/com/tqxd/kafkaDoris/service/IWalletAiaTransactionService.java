package com.tqxd.kafkaDoris.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tqxd.kafkaDoris.entity.domain.WalletAiaTransaction;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * aitd流水 服务类
 * </p>
 *
 * @author ember
 * @since 2023-06-06
 */
public interface IWalletAiaTransactionService extends IService<WalletAiaTransaction> {
    String findTransactionsInfoByCriteria(String coin, String fromAddr, String toAddr, BigDecimal amount, Date startTime, Date endTime);

    List<WalletAiaTransaction> findTransactionsByCriteria(String coin, String fromAddr, String toAddr, BigDecimal amount, Date startTime, Date endTime);
}
