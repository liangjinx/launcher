package com.mobile.blue.launcher.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobile.blue.launcher.service.BankCardService;
import com.mobile.blue.launcher.service.DictService;
import com.mobile.blue.launcher.service.SysconfigService;
import com.mobile.blue.launcher.service.UserAddressService;
import com.mobile.blue.util.ResultUtil;
import com.mobile.blue.util.constant.DictConstant;
import com.mobile.blue.util.constant.StatusConstant.Status;

@Controller
public class PublicController {
	@Autowired
	private DictService dictService;
	@Autowired
	private SysconfigService sysconfigService;
	@Autowired
	private UserAddressService userAddressService;
	@Autowired
	private BankCardService bankCardService;
	/**
	 * 回报方式查询，卷，猪
	 * 
	 * @param request
	 * @param model
	 * @param userId
	 * @param friendId
	 * @param touzhiId
	 * @param number
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryReturnsWay", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	@Transactional
	public @ResponseBody Object queryReturnsWay(HttpServletRequest request, ModelMap model) throws Exception {
		return dictService.queryReturnsWay();
	}

	/**
	 * 粗分割费，粗细分割费 二分体，四分体
	 * 
	 * @param request
	 * @param model
	 * @param userId
	 * @param friendId
	 * @param touzhiId
	 * @param number
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/oneReturnsWaty", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	@Transactional
	public @ResponseBody Object oneReturnsWaty(HttpServletRequest request, ModelMap model) throws Exception {
		return dictService.queryRepayWay(DictConstant.THICK_TYPE);
	}

	/**
	 * 回报方式明细
	 * 
	 * @param request
	 * @param model
	 * @param returTtype
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/returnWayExplain", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	@Transactional
	public @ResponseBody Object returnWayExplain(HttpServletRequest request, ModelMap model, byte returTtype)
			throws Exception {
		return ResultUtil.getResultJson(sysconfigService.returnWayExplain(returTtype), Status.success.getStatus(),
				Status.success.getMsg());
	}


	/**
	 * 费用清单
	 * 
	 * @param request
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/expensesList", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	@Transactional
	public @ResponseBody Object expensesList(HttpServletRequest request, ModelMap model) throws Exception {
		return ResultUtil.getResultJson(sysconfigService.expensesList(), Status.success.getStatus(),
				Status.success.getMsg());
	}
	/**
	 * 查询城市列表
	 * 
	 * @param request
	 * @param model
	 * @param address
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/provinceList", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	@Transactional
	public @ResponseBody Object provinceList(HttpServletRequest request, ModelMap model) throws Exception {
		return userAddressService.selectProvinceList();
	}


	/**
	 * 查询省份下对应的城市列表
	 * 
	 * @param request
	 * @param model
	 * @param cityCode
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/cityInProvinceList", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	@Transactional
	public @ResponseBody Object cityInProvinceList(HttpServletRequest request, ModelMap model, long provinceCode)
			throws Exception {
		return userAddressService.selectCityInProvince(provinceCode);
	}
	

	@RequestMapping(value = "/verifyBankcard", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	@Transactional
	public @ResponseBody Object verifyBankcard(HttpServletRequest request, ModelMap model, String bankCardNumber)
			throws Exception {
		return ResultUtil.getResultJson(bankCardService.verifyBankcard(bankCardNumber), Status.success.getStatus(),
				Status.success.getMsg());
	}

	/**
	 * 查询银行卡信息
	 * 
	 * @param request
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/bankCardNameAndType", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	@Transactional
	public @ResponseBody Object bankCardNameAndType(HttpServletRequest request, ModelMap model) throws Exception {
		return ResultUtil.getResultJson(bankCardService.BankCardNameAndType(), Status.success.getStatus(),
				Status.success.getMsg());
	}

}
