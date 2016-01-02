package com.mobile.blue.launcher.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobile.blue.launcher.dao.OrderDao;
import com.mobile.blue.launcher.model.AppOrder;
import com.mobile.blue.launcher.model.AppUser;
import com.mobile.blue.launcher.model.Example.AppOrderExample;
import com.mobile.blue.launcher.model.Example.AppOrderExample.Criteria;
import com.mobile.blue.launcher.service.MessageService;
import com.mobile.blue.launcher.service.OrderAddressService;
import com.mobile.blue.launcher.service.OrderExtFeeService;
import com.mobile.blue.launcher.service.OrderService;
import com.mobile.blue.launcher.service.ProjectService;
import com.mobile.blue.launcher.service.SysconfigService;
import com.mobile.blue.launcher.service.UserBasicService;
import com.mobile.blue.util.DateUtil;
import com.mobile.blue.util.PageParameter;
import com.mobile.blue.util.ResultUtil;
import com.mobile.blue.util.constant.BasicConstant;
import com.mobile.blue.util.constant.OrderConstant;
import com.mobile.blue.util.constant.StatusConstant.Status;
import com.mobile.blue.util.constant.SysConstant;
import com.mobile.blue.util.util.RandomUtils;
import com.mobile.blue.view.OrderRankingVo;
import com.mobile.blue.view.RequestOrderVo;

@Service("orderService")
public class OrderServiceImpl implements OrderService {
	List<AppOrder> list = null;
	private Log logger = LogFactory.getLog(this.getClass());
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private OrderAddressService orderAddressService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private UserBasicService userBasicService;
	@Autowired
	private SysconfigService sysconfigService;
	@Autowired
	private OrderExtFeeService orderExtFeeService;
	@Autowired
	@Autowired
	private ProjectService projectService;
//	private orderse
	@Override
	public Map<String, Object> selectByUserIdAndProjectId(long userId, long projectId) {
		AppOrderExample example = new AppOrderExample();
		Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(userId);
		criteria.andRelationIdEqualTo(projectId);
		criteria.andStatusGreaterThanOrEqualTo(OrderConstant.ORDER_STATUS_3);
		criteria.andTypeEqualTo(OrderConstant.ORDER_TYPE_1);
		list = orderDao.selectByExample(example, criteria);
		Map<String, Object> map = new HashMap<String, Object>();
		if (list.size() > 0) {
			map.put("order_code", list.get(0).getOrderCode());
			map.put("num", list.get(0).getNum());
			map.put("total_money", list.get(0).getTotalMoney());
			map.put("order_id", list.get(0).getOrderId());
		}
		return map;
	}

	@Override
	public List<Map<String, Object>> selectOrderByUserId(long userId, byte type, HttpServletRequest request, int nextpage) {
		AppOrderExample example = new AppOrderExample();
		Criteria criteria = example.createCriteria();
		PageParameter page = (PageParameter) request.getSession().getAttribute("orderByuserId");
		if (page == null) {
			page = new PageParameter(0, BasicConstant.limit_page_size);
			if (type != 0) {
				criteria.andStatusEqualTo(type);
			}
			example.setOrderByClause("ctime desc");
			criteria.andUserIdEqualTo(userId);
			criteria.andIsShowEqualTo(new Integer(1).byteValue());
			int count = orderDao.countByExample(example, criteria);
			page.setCount(count);
		} else {
			if(nextpage>page.getTotal()){
				return null;
			}
			page.setCurrent(nextpage);
		}
		if (type != 0) {
			criteria.andStatusEqualTo(type);
		}
		example.setOrderByClause("ctime desc");
		criteria.andUserIdEqualTo(userId);
		criteria.andIsShowEqualTo(new Integer(1).byteValue());
		example.setPage(page);
		list = orderDao.selectByExample(example, criteria);
		List<Map<String, Object>> returnlist = new ArrayList<Map<String, Object>>();
		if (list != null && list.size() > 0) {
			request.getSession().setAttribute("orderByuserId", page);
			Map<String, Object> map = null;
			for (AppOrder order : list) {
				map = new HashMap<String, Object>();
				map.put("projectName", order.getRelationName());
				map.put("projectId", order.getRelationId());
				map.put("price", order.getPrice());
				map.put("num", order.getNum());
				map.put("totalMoney", order.getTotalMoney());
				map.put("status", order.getStatus());
				map.put("ctime", order.getCtime() == null ? null : order.getCtime().getTime());
				map.put("orderId", order.getOrderId());
				map.put("orderCode", order.getOrderCode());
				map.put("type", order.getType());
				map.put("payTime", order.getPayTime() == null ? null : order.getPayTime().getTime());
				map.put("userId", order.getUserId());
				map.put("remark", order.getRemark());
				map.put("payType", order.getPayType());
				map.put("overTime", order.getOverTime() == null ? null : order.getOverTime().getTime());
				map.put("productImg", order.getProductImg());
				map.put("subOrderId", order.getSubOrderId());
				map.put("confirmTime", order.getConfirmTime() == null ? null : order.getConfirmTime().getTime());
				map.put("prepayOrderCode", order.getPrepayOrderCode());
				returnlist.add(map);
			}
		}
		return returnlist;
	}

	@Override
	public String oneOrderByOrderId(long userId, long orderId) {
		AppOrderExample example = new AppOrderExample();
		Criteria criteria = example.createCriteria();
		criteria.andOrderIdEqualTo(orderId);
		criteria.andUserIdEqualTo(userId);
		list = orderDao.selectByExample(example, criteria);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		for (AppOrder order : list) {
			if (order.getType().intValue() == OrderConstant.ORDER_TYPE_2
					|| order.getType().intValue() == OrderConstant.ORDER_TYPE_3) {
				Map<String, Object> map = orderAddressService.selectAddressByOrderId(order.getOrderId());
				returnMap.put("province", map.get("province"));
				returnMap.put("city", map.get("city"));
				returnMap.put("address", map.get("province").toString() + map.get("city") + map.get("address"));
			}
		}
		return ResultUtil.getResultJson(returnMap, Status.success.getStatus(), Status.success.getMsg());
	}
	//
	// @Override
	// public String deleteOrder(long userId, long orderId) {
	// if (orderDao.deleteOrder(orderId) >= 1) {
	// int status = orderDao.selectByExample(example,
	// criteria).get(0).getStatus();
	// BasicConstant.setOrderMessage(orderId, status, "删除");
	// messageService.addMessage(userId, BasicConstant.getOrderMessage(), 1,
	// orderId, new Integer(1).byteValue());
	// }
	// return ResultUtil.getResultJson(Status.success.getStatus(),
	// Status.success.getMsg());
	// }
	//
	// // 取消订单,增加记录消息
	// @Override
	// public String cancleOrder(long userId, long orderId) {
	// AppOrder order = new AppOrder();
	// order.setUserId(userId);
	// order.setOrderId(orderId);
	// order.setStatus(new Integer(-1).byteValue());
	// if (orderDao.updateOrder(order) >= 1) {
	// criteria.andOrderIdEqualTo(orderId);
	// criteria.andUserIdEqualTo(userId);
	// int status = orderDao.selectByExample(example,
	// criteria).get(0).getStatus();
	// BasicConstant.setOrderMessage(orderId, status, "取消");
	// messageService.addMessage(userId, BasicConstant.getOrderMessage(), 1,
	// orderId, new Integer(1).byteValue());
	// }
	// return ResultUtil.getResultJson(Status.success.getStatus(),
	// Status.success.getMsg());
	// }

	@Override
	public Object addOrder(RequestOrderVo orders) throws Exception {
		AppOrderExample example = new AppOrderExample();
		Criteria criteria = example.createCriteria();
		if (orders.getUserId() == 0) {
			return ResultUtil.getResultJson(Status.missParam.getStatus(), Status.missParam.getMsg());
		}
		AppOrder order = getOrder(orders, 2);
		if (orderDao.insertOrder(order) > 0) {
			// criteria.andCtimeEqualTo(order.getCtime());
			// criteria.andUserIdEqualTo(order.getUserId());
			// order = orderDao.selectByExample(example, criteria).get(0);
			// orderAddressService.insertOrderAddress(order.getOrderId(),order.getUserId(),orders.getOrderAdrr(),orders.getRemark());
			// 发送消息,生成订单成功
			criteria.andUserIdEqualTo(order.getUserId());
			criteria.andOrderCodeEqualTo(order.getOrderCode());
			order = orderDao.selectByExample(example, criteria).get(0);
			String ss = "";
			switch (order.getStatus()) {
			case -1:
				ss = "已取消";
				break;
			case 1:
				ss = "未付款";
				break;
			case 2:
				ss = "付款中";
				break;
			case 3:
				ss = "已付款";
				break;
			case 4:
				ss = "已选择处理方式";
				break;
			case 5:
				ss = "已确认收货";
				break;
			default:
				ss = "新增";
			}
			String msg = "您的订单编号为【" + order.getOrderCode() + "】 的" + ss + "的订单" + ss + "成功";
			messageService.addMessage(order.getUserId(), msg, 1, order.getOrderId(), new Integer(1).byteValue());
			Map<String, Object> returnmap = new HashMap<String, Object>();
			returnmap.put("ctime", order.getCtime() == null ? null : order.getCtime().getTime());
			returnmap.put("orderCode", order.getOrderCode());
			return returnmap;
			// return order.getOrderCode();
		}
		return null;
	}

	@Override
	public int updateOrderBasic(RequestOrderVo order) {
		AppOrderExample example = new AppOrderExample();
		Criteria criteria = example.createCriteria();
		if (orderDao.updateOrder(getOrder(order, 1)) >= 1) {
			AppOrder aa = orderDao.selectByExample(example, criteria).get(0);
			String ss = "";
			switch (order.getStatus()) {
			case -1:
				ss = "已取消";
				break;
			case 1:
				ss = "未付款";
				break;
			case 2:
				ss = "付款中";
				break;
			case 3:
				ss = "已付款";
				break;
			case 4:
				ss = "已选择处理方式";
				break;
			case 5:
				ss = "已确认收货";
				break;
			default:
				ss = "新加";
			}
			String msg = "您的订单编号为【" + aa.getOrderCode() + "】 的" + ss + "的订单" + ss + "成功";
			messageService.addMessage(order.getUserId(), msg, order.getStatus(), order.getOrderId(),
					new Integer(1).byteValue());
			return 1;
		}
		return 0;
	}

	private AppOrder getOrder(RequestOrderVo ordervo, int type) {
		AppOrder order = new AppOrder();
		order.setOrderId(ordervo.getOrderId());
		order.setUserId(ordervo.getUserId());
		order.setRelationId(ordervo.getRelationId());
		order.setStatus(ordervo.getStatus());
		order.setNum(ordervo.getNum());
		order.setPrice(ordervo.getPrice());
		order.setTotalMoney(ordervo.getTotalMoney());
		order.setPayTime(ordervo.getPayTime());
		order.setRemark(ordervo.getRemark());
		order.setConfirmTime(ordervo.getConfirmTime());
		order.setIsShow(ordervo.getIsShow());
		order.setType(ordervo.getType());
		order.setOrderCode(ordervo.getOrderCode());
		order.setRelationName(ordervo.getProjectName());
		if (type == 2) {
			// 表示增加订单
			order.setCtime(DateUtil.getCurrentDate());
			order.setOrderCode(DateUtil.format(DateUtil.getCurrentDate(), "yyMMddmmss") + RandomUtils.getNum(6));
		}
		return order;
	}

	// private String getMessageType(int status) {
	// String updateTyoe = "";
	// switch (status) {
	// case 1:
	// updateTyoe = "删除";
	// break;
	// case 2:
	// updateTyoe = "取消";
	// break;
	// default:
	// updateTyoe = "支付";
	// }
	// return updateTyoe;
	// }

	@Override
	public short selectByUserId(long userId) {
		AppOrderExample example = new AppOrderExample();
		Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(userId);
		criteria.andStatusEqualTo(new Integer(3).byteValue());
		list = orderDao.selectByExample(example, criteria);
		if (list == null || list.size() <= 0) {
			return 0;
		}
		short number = 0;
		for (AppOrder order : list) {
			number = (short) (number + order.getNum());
		}
		return number;
	}

	@Override
	public AppOrder selectByorderId(long orderId) {
		AppOrderExample example = new AppOrderExample();
		Criteria criteria = example.createCriteria();
		criteria.andOrderIdEqualTo(orderId);
		return orderDao.selectByExample(example, criteria).get(0);
	}

	@Override
	public List<Map<String, Object>> selectByprojectIdList(HttpServletRequest request, int nextPage,
			long projectId) {
		AppOrderExample example = new AppOrderExample();
		Criteria criteria = example.createCriteria();
		PageParameter page = (PageParameter) request.getSession().getAttribute(projectId + "");
		if (page == null) {
			page = new PageParameter(0, BasicConstant.limit_page_size);
			example.setDistinct(true);
			example.setOrderByClause("num desc");
			criteria.andRelationIdEqualTo(projectId);
			criteria.andTypeEqualTo(Byte.valueOf("1"));
			int count = orderDao.countByRanking(example, criteria);
			page.setCount(count);
		} else {
			if(nextPage>page.getTotal()){
				return null;
			}
			page.setCurrent(nextPage);
		}
		example.setDistinct(true);
		example.setOrderByClause("num desc");
		criteria.andRelationIdEqualTo(projectId);
		criteria.andTypeEqualTo(Byte.valueOf("1"));
		example.setPage(page);
		List<OrderRankingVo> orderlist = orderDao.selectByRanking(example, criteria);
		logger.info("orderlist:" + orderlist.size());
		request.getSession().setAttribute(projectId + "", page);
		logger.info("page:" + page.toString());
		Map<String, Object> returnmap = null;
		List<Map<String, Object>> returnlist = new ArrayList<Map<String, Object>>();
		if (orderlist != null && orderlist.size() > 0) {
			AppUser user = null;
			for (OrderRankingVo order : orderlist) {
				logger.info("order_user_id:" + order.getUser_id());
				user = userBasicService.selectUserById(order.getUser_id());
				if (user != null) {
					returnmap = new HashMap<String, Object>();
					returnmap.put("headImg", user.getHeadImg());
					returnmap.put("nickname", user.getNickname());
					returnmap.put("num", order.getCount());
					returnlist.add(returnmap);
				}
			}
		}
		return returnlist;
	}

	@Override
	public String addOrderByPeiSong(long fengeWayId, long fentiwayId, String guige,int num,long userId,long relationId) {
		if(fengeWayId==18 && fentiwayId!=0){
			//表示粗分割
			String feiyong=sysconfigService.queryByCode(SysConstant.DIVISION_THICK_FEE);
			Map<String, Object> map = projectService.selectById(relationId);
			//生成订单
			AppOrder order=new AppOrder();
			order.setOrderCode(DateUtil.format(DateUtil.getCurrentDate(), "yyMMddmmss") + RandomUtils.getNum(6));
			order.setCtime(DateUtil.getCurrentDate());
			order.setType(Byte.parseByte("2"));
			order.setStatus(Byte.parseByte("1"));
			order.setNum(Short.parseShort(num+""));
			order.setPrice(price);
			order.setTotalMoney(totalMoney);
			order.setPayTime(null);
			order.setUserId(userId);
			order.setRemark(null);
			order.setProductName(productName);
			order.setPayType(null);
			order.setRelationId(relationId);
			order.setRelationName(relationName);
			order.setOverTime(null);
			order.setProductImg(productImg);
			order.setSubOrderId(null);
			order.setConfirmTime(null);
			order.setPrepayOrderCode(prepayOrderCode);
			order.setIsShow(Byte.parseByte("1"));
			int saveflag=orderDao.insertOrder(order);
			//添加订单扩展信息
			saveflag=orderExtFeeService.addOrderExtFee();
		}else if(fengeWayId==19 && guige!=null && !"".equals(guige)){
			//表示细分割	
		}
		return null;
	}
}
