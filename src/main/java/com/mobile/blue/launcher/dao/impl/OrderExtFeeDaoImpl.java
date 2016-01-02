package com.mobile.blue.launcher.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobile.blue.launcher.dao.OrderExtFeeDao;
import com.mobile.blue.launcher.dao.mapper.AppOrderExtFeeMapper;
import com.mobile.blue.launcher.model.AppOrderExtFee;
@Service("orderExtFeeDao")
public class OrderExtFeeDaoImpl implements OrderExtFeeDao {
	@Autowired
	private AppOrderExtFeeMapper appOrderExtFeeMapper;
	@Override
	public int insertOrderExtFee(AppOrderExtFee orderExtFee) {
		return appOrderExtFeeMapper.insertSelective(orderExtFee);
	}

}
