package com.mobile.blue.launcher.dao.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobile.blue.launcher.dao.WalletDao;
import com.mobile.blue.launcher.dao.mapper.AppWalletMapper;
import com.mobile.blue.launcher.model.AppWallet;
import com.mobile.blue.launcher.model.Example.AppWalletExample;
import com.mobile.blue.launcher.model.Example.AppWalletExample.Criteria;

@Service("walletDao")
public class WalletDaoOmpl implements WalletDao {
	@Autowired
	private AppWalletMapper appWalletMapper;

	@Override
	public BigDecimal walletMoney(AppWalletExample example, Criteria criteria) {
		List<AppWallet> list = appWalletMapper.selectByExample(example);
		if (list == null || list.size() <= 0) {
			return new BigDecimal("0");
		}
		BigDecimal count = list.get(0).getMoney();
		criteria.getAllCriteria().clear();
		example.clear();
		return count;
	}

	@Override
	public int countByExample(AppWalletExample example, Criteria criteria) {
		int count = appWalletMapper.countByExample(example);
		criteria.getAllCriteria().clear();
		example.clear();
		return count;
	}

	@Override
	public int updateWalletPassword(long userId, String newPassword) {
		AppWallet wallet = new AppWallet();
		wallet.setUserId(userId);
		wallet.setPayPassword(newPassword);
		return appWalletMapper.updateByPrimaryKeySelective(wallet);
	}

	@Override
	public List<AppWallet> selectByExample(AppWalletExample example, Criteria criteria) {
		List<AppWallet> list = appWalletMapper.selectByExample(example);
		criteria.getAllCriteria().clear();
		example.clear();
		return list;
	}

	@Override
	public int updateWall(AppWallet wall) {
		return appWalletMapper.updateByPrimaryKeySelective(wall);
	}

}
