package com.mobile.blue.launcher.service;

public interface UserExtService {

	int insertUserExt(Long userId);

	int updateUserExt(long userId, byte type, int number);

	int selectUserExt(long userId);

	int insertUserExt(long userId, long projectId, int number, byte type);

}
