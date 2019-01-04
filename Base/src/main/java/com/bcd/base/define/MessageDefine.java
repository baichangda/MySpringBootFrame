package com.bcd.base.define;

import com.bcd.base.i18n.I18NData;
import com.bcd.base.message.Message;

/**
 * Created by Administrator on 2017/9/28.
 */
public class MessageDefine {
    public final static Message SUCCESS_SAVE = Message.getMessage(I18NData.getI18NData("COMMON.SAVE_SUCCEEDED"));
    public final static Message SUCCESS_DELETE = Message.getMessage(I18NData.getI18NData("COMMON.DELETE_SUCCEEDED"));
    public final static Message SUCCESS_UPDATE = Message.getMessage(I18NData.getI18NData("COMMON.UPDATE_SUCCEEDED"));
}
