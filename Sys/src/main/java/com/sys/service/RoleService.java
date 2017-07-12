package com.sys.service;

import com.base.condition.BaseCondition;
import com.base.condition.impl.StringCondition;
import com.base.service.BaseService;
import com.sys.bean.MenuBean;
import com.sys.bean.RoleBean;
import com.sys.bean.UserBean;
import com.sys.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author acemma
 * Created by Administrator on 2017/4/11.
 */
@Service
public class RoleService  extends BaseService<RoleBean,Long> {

}
