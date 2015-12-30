package com.mobile.blue.launcher.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alipay.api.internal.util.AlipaySignature;
import com.mobile.blue.launcher.dao.WalletDao;
import com.mobile.blue.launcher.model.AppWallet;
import com.mobile.blue.launcher.model.Example.AppWalletExample;
import com.mobile.blue.launcher.model.Example.AppWalletExample.Criteria;
import com.mobile.blue.launcher.service.BankCardService;
import com.mobile.blue.launcher.service.OrderService;
import com.mobile.blue.launcher.service.SysconfigService;
import com.mobile.blue.launcher.service.WalletChangeLogService;
import com.mobile.blue.launcher.service.WalletService;
import com.mobile.blue.launcher.service.WithdrawalsService;
import com.mobile.blue.launcher.videoAPI.MySecureProtocolSocketFactory;
import com.mobile.blue.util.DateUtil;
import com.mobile.blue.util.EncryptMD5Util;
import com.mobile.blue.util.RSA;
import com.mobile.blue.util.ResultUtil;
import com.mobile.blue.util.XmlParseUtil;
import com.mobile.blue.util.constant.BasicConstant;
import com.mobile.blue.util.constant.StatusConstant.Status;
import com.mobile.blue.util.payyl.CertUtil;
import com.mobile.blue.util.payyl.SDKConfig;
import com.mobile.blue.util.payyl.SDKUtil;
import com.mobile.blue.util.util.RandomUtils;
import com.mobile.blue.view.PayWXVO;
import com.mobile.blue.view.RequestOrderVo;

import net.sf.json.JSONObject;

@Service("walletService")
public class WalletServiceImpl implements WalletService {
	List<AppWallet> list = null;
	@Autowired
	private WalletDao walletDao;
	@Autowired
	private SysconfigService sysconfigService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private BankCardService bankCardService;
	@Autowired
	private WithdrawalsService WithdrawalsService;
	@Autowired
	private WalletChangeLogService walletChangeLogService;

	@Override
	public BigDecimal walletMoney(long userId) {
		AppWalletExample example = new AppWalletExample();
		Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(userId);
		return walletDao.walletMoney(example, criteria);
	}

	@Override
	public int VerifyPassword(long userId, String oldpassword, byte type) {
		AppWalletExample example = new AppWalletExample();
		Criteria criteria = example.createCriteria();
		int flag = 1;
		criteria.andUserIdEqualTo(userId);
		criteria.andPayPasswordEqualTo(oldpassword);
		list = walletDao.selectByExample(example, criteria);
		if (list.size() > 0) {
			// 表示验证成功
			AppWallet wall = new AppWallet();
			if (type == 2) {
				if (list.get(0).getStatus() != 1) {
					wall.setStatus(new Integer(1).byteValue());
					wall.setUserId(userId);
					wall.setWalletId(list.get(0).getWalletId());
					walletDao.updateWall(wall);
				}
			} else {
				if (list.get(0).getStatus() + 1 == 3) {
					flag = 0;
				}
				wall.setStatus(new Integer(list.get(0).getStatus() + 1).byteValue());
				wall.setUserId(userId);
				wall.setWalletId(list.get(0).getWalletId());
				walletDao.updateWall(wall);
			}
		}
		return flag;
	}

	@Override
	public int updateWalletPassword(long userId, String newPassword) {
		return walletDao.updateWalletPassword(userId, newPassword);
	}

	@Override
	public String withdrawMoney(long userId, long cardId, double money, String remark) {
		AppWalletExample example = new AppWalletExample();
		Criteria criteria = example.createCriteria();
		// 查看我的余额
		criteria.andUserIdEqualTo(userId);
		list = walletDao.selectByExample(example, criteria);
		if (list.get(0) == null || list.size() <= 0) {
			return ResultUtil.getResultJson(Status.userNotExistedNullity.getStatus(),
					Status.userNotExistedNullity.getMsg());
		}
		// 查看卡号是否是该用户的卡号
		Map<String, Object> cardMap = bankCardService.selectByUserIdAndCardId(userId, cardId);
		if (cardMap == null) {
			return ResultUtil.getResultJson(Status.bankCardNotExist.getStatus(), Status.bankCardNotExist.getMsg());
		}
		if (list.get(0).getMoney().doubleValue() >= money) {
			// 表示可以提取现金 ，修改我的余额
			AppWallet wallet = new AppWallet();
			wallet.setMoney(new BigDecimal(list.get(0).getMoney().doubleValue() - money));
			wallet.setWalletId(list.get(0).getWalletId());
			if (walletDao.updateWall(wallet) < 1) {
				return ResultUtil.getResultJson(Status.serverError.getStatus(), Status.serverError.getMsg());
			}
			// 提现表中添加记录
			if (WithdrawalsService.addWithdrawal(userId, money, cardMap.get("account_name").toString(),
					cardMap.get("bank_code").toString(), cardMap.get("bank").toString(), remark,
					list.get(0).getWalletId(), cardId, cardMap.get("card_type").toString()) < 1) {
				return ResultUtil.getResultJson(Status.serverError.getStatus(), Status.serverError.getMsg());
			}
			// 增加钱包变更记录
			if (walletChangeLogService.addChangeLog(list.get(0).getWalletId(), userId, list.get(0).getMoney(), money,
					wallet.getMoney(), WithdrawalsService.selectWithdrawall(userId).get(0).getWithwradalsId()) < 1) {
				return ResultUtil.getResultJson(Status.serverError.getStatus(), Status.serverError.getMsg());
			}

		} else {
			return ResultUtil.getResultJson(Status.takeCashMoneyNullity.getStatus(),
					Status.takeCashMoneyNullity.getMsg());
		}
		return ResultUtil.getResultJson(Status.success.getStatus(), Status.success.getMsg());
	}

	// 支付宝异步通知
	@Override
	public String notifys(Map<String, String> params) throws Exception {
		JSONObject jsonObject = JSONObject.fromObject(params);// 得到解密后的字符串
		String key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCy4uEED9hE+refHlejID1lnVstK6hHHH8HdNugFQMYBFRBmNs6DlGtjedgpE/hBmQiWlbplVXvw1XYSuAn370TUjkqWYUPVzvKctSPMgzrw9QWCrt5Jah92h+nzWxmmyUjRvXKTI3XjmIvOT6zAniQNKW/yXV1jE1ROLe3nz80QQIDAQAB";
		RequestOrderVo order = new RequestOrderVo();
		order.setOrderCode(jsonObject.get("out_trade_no").toString());
		if (AlipaySignature.rsaCheckV2(params, key, "utf-8")) {
			// 验证成功，执行公司业务
			System.out.println(jsonObject.optString("trade_status"));
			if (jsonObject.optString("trade_status").equals("TRADE_SUCCESS")) {
				// 表示支付成功
				order.setPayTime(DateUtil.getCurrentDate());
				order.setPayType(Byte.valueOf("1"));
				order.setIsShow(Byte.parseByte("1"));
				order.setStatus(Byte.parseByte("3"));
			} else if (jsonObject.optString("trade_status").equals("TRADE_FINISHED")) {
				// 交易成功 买家收到货和确认收到货 的全部过程都完成了 就提示交易完成，
				order.setConfirmTime(DateUtil.getCurrentDate());
				order.setStatus(Byte.parseByte("5"));
			} else if (jsonObject.optString("trade_status").equals("WAIT_BUYER_PAY")) {
				// 交易创建
				order.setStatus(Byte.parseByte("2"));
			} else {
				order.setStatus(Byte.parseByte("-1"));
				// 交易关闭
			}
			if (orderService.updateOrderBasic(order) >= 1) {
				return "success";
			}
		}
		return "fail";
	}

	// 微信支付异步通知
	@Override
	public String weixinnotify(JSONObject jsonObject) throws Exception {
		Map<String, Object> map = sysconfigService.queryWxAppidAndAppKey();
		if (!"SUCCESS".equalsIgnoreCase(jsonObject.get("return_code").toString())) {
			return ResultUtil.getResultJson(Status.serverError.getStatus(), Status.serverError.getMsg());
		}
		boolean issign = EncryptMD5Util.verify(CreateLinkString(ParaFilter(jsonObject), "="),
				jsonObject.get("sign").toString(), map.get("appPartnerKey").toString(), "utf-8");
		if (!issign) {
			return ResultUtil.getResultJson(Status.serverError.getStatus(), Status.serverError.getMsg());
		}
		if (!"success".equals(jsonObject.getString("result_code"))) {

		}
		// 验证成功
		RequestOrderVo order = new RequestOrderVo();
		order.setOrderCode(jsonObject.get("out_trade_no").toString());
		order.setStatus(Byte.parseByte("3"));
		order.setPayTime(DateUtil.getCurrentDate());
		order.setPayType(Byte.valueOf("1"));
		order.setIsShow(Byte.parseByte("1"));
		order.setType(Byte.parseByte("1"));
		if (orderService.updateOrderBasic(order) >= 1) {
			return "success";
		}
		return "fail";
	}

	// 微信生成订单
	@Override
	public String addorderforWX(String remoteIp, RequestOrderVo order) throws Exception {
		PayWXVO winxisend = new PayWXVO();
		// 保存数据
		saveWinxi(winxisend, remoteIp, order);
		// 请求微信
		JSONObject jsonObject = requestforService(winxisend.toString(), 445, "HTTPS",
				"https://api.mch.weixin.qq.com/pay/unifiedorder", "text/xml");
		// 验证签名
		if (!"SUCCESS".equalsIgnoreCase(jsonObject.get("return_code").toString())) {
			return ResultUtil.getResultJson(Status.serverError.getStatus(), Status.serverError.getMsg());
		}
		Map<String, Object> returnmap = new HashMap<String, Object>();
		returnmap.put("appid", jsonObject.getString("appid"));
		returnmap.put("mch_id", jsonObject.getString("mch_id"));
		returnmap.put("nonce_str", jsonObject.getString("nonce_str"));
		returnmap.put("sign_type", "MD5");
		returnmap.put("input_charset", "utf-8");
		// 在数据库中生成订单
		return ResultUtil.getResultJson(returnmap, Status.success.getStatus(), Status.success.getMsg());
	}

	private JSONObject requestforService(String param, int port, String httpType, String serviceUrl,
			String applicationType) throws UnsupportedEncodingException, IOException, HttpException, DocumentException {
		ProtocolSocketFactory fcty = new MySecureProtocolSocketFactory();
		Protocol.registerProtocol(httpType, new Protocol(httpType, fcty, port));
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(serviceUrl);
		RequestEntity entity = new StringRequestEntity(param, applicationType, "UTF-8");
		method.setRequestEntity(entity);
		client.executeMethod(method);
		InputStream inputStream = method.getResponseBodyAsStream();
		// 得到微信传值
		String restult = IOUtils.toString(inputStream);
		Document doc_notify_data = DocumentHelper.parseText(restult);
		JSONObject jsonObject = XmlParseUtil.parseAlipayNotifyData(doc_notify_data);
		return jsonObject;
	}

	// total_fee 不能为小数，是分单位
	// 微信提交信息
	@SuppressWarnings("unchecked")
	private void saveWinxi(PayWXVO winxisend, String remoteIp, RequestOrderVo order) throws Exception {
		Map<String, Object> map = sysconfigService.queryWxAppidAndAppKey();
		winxisend.setAppid(map.get("appId").toString());
		winxisend.setMch_id(map.get("mch_id").toString());
		winxisend.setNonce_str(RandomUtils.getNum(32));
		winxisend.setBody(order.getRemark() == null ? "购买猪仔" : order.getRemark());
		winxisend.setOut_trade_no(((Map<String, Object>) orderService.addOrder(order)).get("orderCode").toString());
		winxisend.setTotal_fee(order.getTotalMoney().intValue());
		winxisend.setSpbill_create_ip(remoteIp == null ? "127.0.0.1" : remoteIp);
		winxisend.setNotify_url("http://pctest.bajiewg.com/WXnotify");
		winxisend.setTrade_type("APP");
		// 可以不传的数据
		winxisend.setProduct_id("" + order.getRelationId());
		winxisend.setGoods_tag(null);
		winxisend.setTime_start(DateUtil.format(DateUtil.getCurrentDate(), "yyyyMMddHHmmss"));
		winxisend
				.setTime_expire(DateUtil.format(DateUtil.getAfterHour(DateUtil.getCurrentDate(), 2), "yyyyMMddHHmmss"));
		winxisend.setFee_type("CNY");
		winxisend.setDetail(order.getProjectName());
		winxisend.setLimit_pay("no_credit");
		winxisend.setDevice_info(null);
		winxisend.setAttach(null);
		winxisend.setOpenid("operator");
		winxisend.setSign(map.get("appPartnerKey").toString());
	}

	// 微信查询订单
	@Override
	public String getPayResult(String orderId) throws Exception {
		Map<String, Object> map = sysconfigService.queryWxAppidAndAppKey();
		PayWXVO winxisend = new PayWXVO();
		winxisend.setAppid(map.get("appId").toString());
		winxisend.setMch_id(map.get("mch_id").toString());
		winxisend.setOut_trade_no(orderId);
		winxisend.setNonce_str(RandomUtils.getNum(32));
		winxisend.setSign(map.get("appPartnerKey").toString());
		JSONObject jsonObject = requestforService(winxisend.toString(), 446, "HTTPS",
				"https://api.mch.weixin.qq.com/pay/orderquery", "text/xml");
		// 验证签名
		if (!"SUCCESS".equalsIgnoreCase(jsonObject.get("return_code").toString())) {
			return ResultUtil.getResultJson(Status.serverError.getStatus(), Status.serverError.getMsg());
		}
		boolean issign = EncryptMD5Util.verify(jsonObject.toString(), jsonObject.get("sign").toString(),
				map.get("appPartnerKey").toString(), "utf-8");
		if (!issign) {
			return ResultUtil.getResultJson(Status.serverError.getStatus(), Status.serverError.getMsg());
		}
		if (!"SUCCESS".equals(jsonObject.getString("result_code"))) {

		}
		int trade_state = 0;
		switch (jsonObject.getString("trade_state")) {
		case "SUCCESS":
			// —支付成功
			trade_state = 3;
			break;
		case "REFUND":
			// 转入退款
			trade_state = -1;
			break;
		case "NOTPAY":
			// 未支付
			trade_state = 1;
			break;
		case "USERPAYING":
			// -用户支付中
			trade_state = -2;
			break;
		default:
			trade_state = -1;
			break;
		}
		RequestOrderVo order = new RequestOrderVo();
		order.setOrderCode(orderId);
		order.setStatus(Byte.valueOf(trade_state + ""));
		if (orderService.updateOrderBasic(order) <= 0) {
			return ResultUtil.getResultJson(Status.serverError.getStatus(), Status.serverError.getMsg());
		}
		Map<String, Object> returnmap = new HashMap<String, Object>();
		returnmap.put("trade_type", jsonObject.getString("trade_type"));
		returnmap.put("trade_state", trade_state);
		returnmap.put("bank_type", jsonObject.getString("bank_type"));
		returnmap.put("total_fee", jsonObject.getString("total_fee"));
		returnmap.put("fee_type", jsonObject.getString("fee_type"));
		// returnmap.put("cash_fee", jsonObject.getString("cash_fee"));
		returnmap.put("transaction_id", jsonObject.getString("transaction_id"));
		returnmap.put("out_trade_no", jsonObject.getString("out_trade_no"));
		returnmap.put("time_end", DateUtil.parseDate(jsonObject.getString("time_end")) == null ? null
				: DateUtil.parseDate(jsonObject.getString("time_end")).getTime());
		returnmap.put("trade_state_desc", jsonObject.getString("trade_state_desc"));
		return ResultUtil.getResultJson(returnmap, Status.success.getStatus(), Status.success.getMsg());
	}

	/**
	 * 功能：除去数组中的空值和签名参数
	 * 
	 * @param sArray
	 *            签名参数组
	 * @return 去掉空值与签名参数后的新签名参数组
	 */
	private static Map<String, Object> ParaFilter(Map<String, Object> sArray) {
		Iterator<String> keys = sArray.keySet().iterator();
		Map<String, Object> sArrayNew = new HashMap<String, Object>();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			String value = (String) sArray.get(key);
			if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
					|| key.equalsIgnoreCase("sign_type")) {
				continue;
			}
			sArrayNew.put(key, value);
		}
		return sArrayNew;
	}

	/**
	 * 功能：把数组所有元素按照一定顺序排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
	 * 
	 * @param params
	 *            需要排序并参与字符拼接的参数组
	 * @param pjfs
	 *            拼接方式
	 * @return 拼接后字符串
	 * @throws UnsupportedEncodingException
	 */
	private String CreateLinkString(Map<String, Object> params, String pjfs) throws UnsupportedEncodingException {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);// 排序
		// 去除空值并拼接参数
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < keys.size(); i++) {
			String key = (String) keys.get(i);
			String value = (String) params.get(key);
			if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
				sb.append(key + pjfs + value);
			} else {
				sb.append(key + pjfs + value + "&");
			}
		}
		return sb.toString();
	}
}
