package com.mobile.blue.launcher.dao;

import com.mobile.blue.launcher.model.AppUserExt;
import com.mobile.blue.launcher.model.Example.AppUserExtExample;
import com.mobile.blue.launcher.model.Example.AppUserExtExample.Criteria;

public interface UserExtDao {

	int insertUserExt(AppUserExt userExt);

	int updateUserExt(long userId, byte type, int number);

	int selectUserExt(AppUserExtExample example, Criteria criteria);

}
