package com.mobile.blue.launcher.service;

import java.math.BigDecimal;

public interface WalletChangeLogService {

	int addChangeLog(Long walletId, long userId, BigDecimal beforeMoney, double changeMoney, BigDecimal afterMoney,
			Long relationId);

}
