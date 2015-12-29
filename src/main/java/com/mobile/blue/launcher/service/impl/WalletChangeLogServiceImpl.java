package com.mobile.blue.launcher.service.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobile.blue.launcher.dao.WalletChangeLogDao;
import com.mobile.blue.launcher.model.AppWalletChangeLog;
import com.mobile.blue.launcher.service.WalletChangeLogService;
import com.mobile.blue.util.DateUtil;

@Service("walletChangeLogService")
public class WalletChangeLogServiceImpl implements WalletChangeLogService {
	@Autowired
	private WalletChangeLogDao walletChangeLogDao;

	@Override
	public int addChangeLog(Long walletId, long userId, BigDecimal beforeMoney, double changeMoney,
			BigDecimal afterMoney, Long relationId) {
		AppWalletChangeLog changelog = new AppWalletChangeLog();
		changelog.setWalletId(walletId);
		changelog.setUserId(userId);
		changelog.setBeforeMoney(beforeMoney);
		changelog.setAfterMoney(new BigDecimal(changeMoney));
		changelog.setAfterMoney(afterMoney);
		changelog.setChangeType(Byte.valueOf("1"));
		changelog.setChangeTime(DateUtil.getCurrentDate());
		changelog.setRelationId(relationId);
		changelog.setRelationType(Byte.valueOf("3"));
		return walletChangeLogDao.addChangeLog(changelog);
	}

}
