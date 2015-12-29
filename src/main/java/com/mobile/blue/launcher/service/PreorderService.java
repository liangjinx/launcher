package com.mobile.blue.launcher.service;

public interface PreorderService {

	String saveUserPreSet(long userId, long projectId, byte type, boolean isCancel, int number);

}
