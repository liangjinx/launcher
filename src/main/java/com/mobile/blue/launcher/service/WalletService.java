package com.mobile.blue.launcher.service;

import java.math.BigDecimal;
import java.util.Map;

import com.mobile.blue.view.RequestOrderVo;

import net.sf.json.JSONObject;

public interface WalletService {

	BigDecimal walletMoney(long userId);

	int VerifyPassword(long userId, String oldpassword, byte type);

	int updateWalletPassword(long userId, String newPassword);

	String withdrawMoney(long userId, long cardId, double money, String remark);

	String notifys(Map<String, String> params) throws Exception;

	String weixinnotify(JSONObject jsonObject) throws Exception;

	String addorderforWX(String remoteIp, RequestOrderVo order) throws Exception;

	String getPayResult(String orderId) throws Exception;

	String requestForYL(RequestOrderVo order) throws Exception;

	String ylNotify(JSONObject jsonObject) throws Exception;

}
