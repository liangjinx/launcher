package com.mobile.blue.launcher.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobile.blue.launcher.dao.WalletChangeLogDao;
import com.mobile.blue.launcher.dao.mapper.AppWalletChangeLogMapper;
import com.mobile.blue.launcher.model.AppWalletChangeLog;

@Service("walletChangeLogDao")
public class WalletChangeLogDaoImpl implements WalletChangeLogDao {
	@Autowired
	private AppWalletChangeLogMapper appWalletChangeLogMapper;

	@Override
	public int addChangeLog(AppWalletChangeLog changelog) {
		return appWalletChangeLogMapper.insertSelective(changelog);
	}

}
