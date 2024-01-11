package com.tqxd.kafkaDoris.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@TableName("wallet_aia_transaction")
@Data
public class WalletAiaTransaction implements Serializable {
    private Long id;
    private String txHash;
    private Long txIndex;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date blockTime;
    private String fromAddr;
    private String toAddr;
    private Long blockNumber;
    private BigDecimal amount;
    private String coin;
    private BigDecimal gasPrice;
    private BigDecimal gasUsed;
    private BigDecimal gasLimit;
    private Long blockTimeNum;
    private String blockHash;
    private Integer transactionType;
    private Integer status;
    private BigDecimal minerFee;
    private String inputText;
    private String remark;
    private String memo;
    private Date ctime;
    private String param1;
    private String param2;
    private String param3;
    private String param4;
    private String param5;
}
