package com.mobile.blue.launcher.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobile.blue.launcher.dao.ProjectDao;
import com.mobile.blue.launcher.model.AppProject;
import com.mobile.blue.launcher.model.Example.AppProjectExample;
import com.mobile.blue.launcher.model.Example.AppProjectExample.Criteria;
import com.mobile.blue.launcher.service.ProjectService;
import com.mobile.blue.util.DateUtil;
import com.mobile.blue.util.ResultUtil;
import com.mobile.blue.util.constant.StatusConstant.Status;

@Service("projectService")
public class ProjectServiceImpl implements ProjectService {
	List<AppProject> list = null;
	@Autowired
	private ProjectDao projectDao;

	@Override
	public Map<String, Object> selectById(Long paincbuyProjectId) {
		AppProjectExample example = new AppProjectExample();
		Criteria criteria = example.createCriteria();
		criteria.andPaincbuyProjectIdEqualTo(paincbuyProjectId);
		list = projectDao.selectByExample(example, criteria);
		if (list == null || list.size() < 1) {
			return null;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("num", list.get(0).getNum());
		map.put("left_num", list.get(0).getLeftNum());
		map.put("price", list.get(0).getPrice());
		map.put("paincbuy_project_id", list.get(0).getPaincbuyProjectId());
		map.put("name", list.get(0).getName());
		map.put("summary", list.get(0).getSummary());
		map.put("imgs", list.get(0).getImgs());
		map.put("status", list.get(0).getStatus());
		map.put("total_money", list.get(0).getTotalMoney());
		map.put("begin_time", list.get(0).getBeginTime()==null?null:list.get(0).getBeginTime().getTime());
		map.put("end_time", list.get(0).getEndTime()==null?null:list.get(0).getEndTime().getTime());
		map.put("other_fee_detail", list.get(0).getOtherFeeDetail());
		map.put("other_fee", list.get(0).getOtherFee());
		map.put("variety", list.get(0).getVariety());
		map.put("detail", list.get(0).getDetail());
		return map;
	}

	@Override
	public List<Map<String, Object>> selectCanPreProject() {
		AppProjectExample example = new AppProjectExample();
		Criteria criteria = example.createCriteria();
		criteria.andBeginTimeGreaterThanOrEqualTo(DateUtil.getCurrentDate());
		list = projectDao.selectByExample(example, criteria);
		List<Map<String, Object>> returnlist = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = null;
		if (list == null || list.size() <= 0) {
			return returnlist;
		}
		for (AppProject project : list) {
			map = new HashMap<String, Object>();
			map.put("paincbuy_project_id", project.getPaincbuyProjectId());
			returnlist.add(map);
		}
		return returnlist;
	}

	@Override
	public String searchProjects() {
		AppProjectExample example = new AppProjectExample();
		Criteria criteria = example.createCriteria();
		List<Map<String, Object>> returnlist = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = null;
		criteria.andEndTimeGreaterThanOrEqualTo(DateUtil.getCurrentDate());
		list = projectDao.selectByExample(example, criteria);
		for (AppProject project : list) {
			map = new HashMap<String, Object>();
			map.put("paincbuyProjectId", project.getPaincbuyProjectId());
			map.put("leftNum", project.getLeftNum());
			map.put("name", project.getName());
		}
		return ResultUtil.getResultJson(returnlist, Status.success.getStatus(), Status.success.getMsg());
	}

	@Override
	public Map<String, Object> selectCanPreProjectOne() {
		AppProjectExample example = new AppProjectExample();
		Criteria criteria = example.createCriteria();
		Map<String, Object> map = new HashMap<String, Object>();
		criteria.andBeginTimeGreaterThanOrEqualTo(DateUtil.getCurrentDate());
		example.setOrderByClause("begin_time desc");
		list = projectDao.selectByExample(example, criteria);
		if (list == null || list.size() <= 0) {
			criteria.andEndTimeGreaterThanOrEqualTo(DateUtil.getCurrentDate());
			example.setOrderByClause("end_time desc");
			list = projectDao.selectByExample(example, criteria);
		}
		if (list == null || list.size() <= 0) {
			return null;
		}
		AppProject project = list.get(0);
		map.put("paincbuy_project_id", project.getPaincbuyProjectId());
		map.put("name", project.getName());
		map.put("begin_time",project.getBeginTime()==null?null:project.getBeginTime().getTime());
		map.put("end_time", project.getEndTime()==null?null:project.getEndTime().getTime());
		map.put("num", project.getNum());
		map.put("left_num", project.getLeftNum());
		map.put("other_fee", project.getOtherFee());
		map.put("other_fee_detail", project.getOtherFee());
		map.put("detail", project.getDetail());
		map.put("variety", project.getVariety());
		map.put("price", project.getPrice());
		map.put("status", project.getStatus());
		map.put("imgs", project.getImgs());
		return map;
	}

}
