package com.mobile.blue.launcher.service;

import java.util.Map;

public interface OrderAddressService {

	Map<String, Object> selectAddressByOrderId(Long orderId);

//	int insertOrderAddress(Long orderId, Long userId, Long orderAdrr, String string) throws Exception;

}
