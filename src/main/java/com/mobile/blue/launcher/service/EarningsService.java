package com.mobile.blue.launcher.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface EarningsService {

	List<Map<String, Object>> selectInvestList(long userID,HttpServletRequest request,int nextpage);

	String selectInvestOne(long userId, long projectId);

	int selectPigcount(long userId);

	String selectMyPig(long userId,HttpServletRequest request,int nextpage);

	String sendFriendPig(long userId, long friendId, long earningsId, int number);

	double selectEarnings(long userId);

	List<Map<String, Object>> myearningslist(HttpServletRequest request, long userId, int nextPage);

	Map<String, Object> selectIndex(long userId);

	String earningsRanking(HttpServletRequest request, long userId, int nextPage);

	int updateReturnWay(byte beforeDealType, byte dealType, long earningId);

}
