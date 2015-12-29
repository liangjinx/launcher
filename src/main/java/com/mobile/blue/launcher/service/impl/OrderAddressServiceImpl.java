package com.mobile.blue.launcher.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobile.blue.launcher.dao.OrderAddressDao;
import com.mobile.blue.launcher.model.AppOrderAddress;
import com.mobile.blue.launcher.model.Example.AppOrderAddressExample;
import com.mobile.blue.launcher.model.Example.AppOrderAddressExample.Criteria;
import com.mobile.blue.launcher.service.AreaService;
import com.mobile.blue.launcher.service.OrderAddressService;

@Service("orderAddressService")
public class OrderAddressServiceImpl implements OrderAddressService {
	List<AppOrderAddress> list = null;
	@Autowired
	private OrderAddressDao orderAddressDao;
	@Autowired
	private AreaService areaService;

	@Override
	public Map<String, Object> selectAddressByOrderId(Long orderId) {
		AppOrderAddressExample example = new AppOrderAddressExample();
		Criteria criteria = example.createCriteria();
		criteria.andOrderIdEqualTo(orderId);
		list = orderAddressDao.selectByExample(example, criteria);
		Map<String, Object> map = null;
		if (list.size() > 0) {
			map = new HashMap<String, Object>();
			map.put("province", areaService.selectCityInProvince(list.get(0).getProvince()).get(0).getName());
			map.put("city", areaService.selectCityInProvince(list.get(0).getCity()).get(0).getName());
			map.put("address", list.get(0).getAddress());
		}
		return map;
	}

//	@Override
//	public int insertOrderAddress(Long orderId, Long userId, Long orderAdrr, String remak) throws Exception {
//		List<AppUserAddress> orderList = UserAddressService.selectUserAllAdress(null,true,userId);
//		for (AppUserAddress add : orderList) {
//			if (orderAdrr == add.getAddressId()) {
//				orderAddressDao.insertOrder(orderId, add, remak);
//				break;
//			}
//		}
//		return 0;
//	}

}
