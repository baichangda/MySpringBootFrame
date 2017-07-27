package com.base.define;

import com.base.message.BaseErrorMessage;

/**
 * Created by Administrator on 2017/7/26.
 */
public final class BaseErrorDefine {
    //BaseService
    public final static BaseErrorMessage ERROR_EXECUTE_SAVEINGORENULL=BaseErrorMessage.getMessage("执行非空保存方法失败","601");
    public final static BaseErrorMessage ERROR_EXECUTE_SAVEBATCH=BaseErrorMessage.getMessage("执行批量方法失败","602");
    public final static BaseErrorMessage ERROR_EXECUTE_DELETEWITHNOREFERRED=BaseErrorMessage.getMessage("执行无被引用删除失败","603");
    public final static BaseErrorMessage ERROR_EXECUTE_SAVEWITHNOREPEATREFER=BaseErrorMessage.getMessage("执行无重复引用保存失败","604");
}
