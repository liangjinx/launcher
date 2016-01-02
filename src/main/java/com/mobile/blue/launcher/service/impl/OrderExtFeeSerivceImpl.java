package com.mobile.blue.launcher.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobile.blue.launcher.dao.OrderExtFeeDao;
import com.mobile.blue.launcher.model.AppOrderExtFee;
import com.mobile.blue.launcher.service.OrderExtFeeService;
@Service("orderExtFeeService")
public class OrderExtFeeSerivceImpl implements OrderExtFeeService {
	@Autowired
	private OrderExtFeeDao orderExtFeeDao;
	@Override
	public int addOrderExtFee() {
		AppOrderExtFee orderExtFee=new AppOrderExtFee();
		
		return orderExtFeeDao.insertOrderExtFee(orderExtFee);
	}

}
