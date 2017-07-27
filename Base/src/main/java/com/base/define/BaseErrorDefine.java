package com.base.define;

import com.base.i18n.I18NData;
import com.base.message.BaseErrorMessage;

/**
 * Created by Administrator on 2017/7/26.
 */
public final class BaseErrorDefine {
    //BaseService
    public final static BaseErrorMessage ERROR_EXECUTE_SAVEINGORENULL=BaseErrorMessage.getMessage(new I18NData("BaseService.saveIngoreNull.FAILED"),"601");
    public final static BaseErrorMessage ERROR_EXECUTE_SAVEBATCH=BaseErrorMessage.getMessage(new I18NData("BaseService.saveBatch.FAILED"),"602");
    public final static BaseErrorMessage ERROR_EXECUTE_DELETEWITHNOREFERRED=BaseErrorMessage.getMessage(new I18NData("BaseService.saveWithNoRepeatRefer.FAILED"),"603");
    public final static BaseErrorMessage ERROR_EXECUTE_SAVEWITHNOREPEATREFER=BaseErrorMessage.getMessage(new I18NData("BaseService.deleteWithNoReferred.FAILED"),"604");
}
