package com.mobile.blue.launcher.service;

import java.util.List;
import java.util.Map;

public interface ProjectService {

	Map<String, Object> selectById(Long paincbuyProjectId);

	List<Map<String, Object>> selectCanPreProject();

	String searchProjects();

	Map<String, Object> selectCanPreProjectOne();

}
