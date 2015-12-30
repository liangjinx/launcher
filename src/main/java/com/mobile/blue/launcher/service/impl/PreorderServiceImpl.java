package com.mobile.blue.launcher.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobile.blue.launcher.dao.PreorderDao;
import com.mobile.blue.launcher.model.AppPreorder;
import com.mobile.blue.launcher.model.Example.AppPreorderExample;
import com.mobile.blue.launcher.model.Example.AppPreorderExample.Criteria;
import com.mobile.blue.launcher.service.PreorderService;
import com.mobile.blue.launcher.service.ProjectService;
import com.mobile.blue.launcher.service.UserExtService;
import com.mobile.blue.util.ResultUtil;
import com.mobile.blue.util.constant.StatusConstant.Status;
import com.mobile.blue.util.constant.TypeConstant;

@Service("preorderService")
public class PreorderServiceImpl implements PreorderService {
	@Autowired
	private PreorderDao preorderDao;
	@Autowired
	private UserExtService userExtService;
	@Autowired
	private ProjectService projectServicer;

	@Override
	public String saveUserPreSet(long userId, long projectId, byte type, boolean isCancel, int number) {
		boolean isupdate = false;
		if (type == TypeConstant.USER_SETTING_TYPE_2) {
			// 分期抢购
			if (!isCancel) {
				// 如果不是取消设置的每期都强,则用户选择性的抢购项目
				if (number < 0) {
					return ResultUtil.getResultJson(Status.saleNumNotEnough.getStatus(),
							Status.saleNumNotEnough.getMsg());
				}
				AppPreorderExample example = new AppPreorderExample();
				Criteria criteria = example.createCriteria();
				criteria.andUserIdEqualTo(userId);
				criteria.andProjectIdEqualTo(projectId);
				int flag = preorderDao.countByExample(example, criteria);
				int value = 0;
				if (flag == 1) {
					// 表示已经保存过该预抢的项目，则更新该预抢的项目
					value = preorderDao.updatePreOrder(userId, projectId, number);
				} else {
					// 表示以前没有预抢过该项目，则插入数据到数据库
					value = preorderDao.insertPreorder(userId, projectId, number);
				}
				if (value >= 1) {
					//表示保存用户预强成功
					int count = userExtService.selectUserExt(userId);
					if (count != 1) {
						userExtService.insertUserExt(userId, projectId, number, type);
						isupdate = true;
					} else {
						if (userExtService.updateUserExt(userId, type, 0) >= 1)
							isupdate = true;
					}
				}
			} else {
				if (preorderDao.deleteUserPreorder(userId, projectId) >= 1)
					isupdate = true;
			}
		} else if (type == TypeConstant.USER_SETTING_TYPE_1) {
			// 表示每期都抢
			if (isCancel) {
				// 取消抢购该项目，则把类型改成不抢购
				type = 0;
			} else {
				// 保存最近可以抢购的项目
				List<Map<String, Object>> projectlist = projectServicer.selectCanPreProject();
				if (projectlist == null || projectlist.size() <= 0) {

				} else {
					for (Map<String, Object> map : projectlist) {
						preorderDao.insertPreorder(userId, Long.parseLong(map.get("paincbuy_project_id").toString()),
								number);
					}
				}
			}
			int count = userExtService.selectUserExt(userId);
			if (count != 1) {
				userExtService.insertUserExt(userId, projectId, number, type);
				isupdate = true;
			} else {
				if (userExtService.updateUserExt(userId, type, number) >= 1)
					isupdate = true;
			}

		}
		if (isupdate)
			return ResultUtil.getResultJson(Status.success.getStatus(), Status.success.getMsg());
		return ResultUtil.getResultJson(Status.saveFail.getStatus(), Status.saveFail.getMsg());
	}

	@Override
	public int selectByUserIdAndProjectId(long userId, Long paincbuyProjectId) {
		AppPreorderExample example = new AppPreorderExample();
		Criteria criteria = example.createCriteria();
		List<AppPreorder> list = new ArrayList<>();
		criteria.andUserIdEqualTo(userId);
		criteria.andProjectIdEqualTo(paincbuyProjectId);
		list=preorderDao.selectByexample(example,criteria);
		if(list==null || list.size()<=0){
			return 0;
		}
		return list.get(0).getNum();
	}
}
