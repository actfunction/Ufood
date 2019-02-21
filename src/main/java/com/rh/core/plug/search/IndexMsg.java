package com.rh.core.plug.search;

import com.rh.core.base.Bean;
import com.rh.core.plug.search.ARhIndex;
import com.rh.core.util.msg.Msg;
import com.rh.core.util.msg.MsgCenter;

/**
 * index异步消息对象
 * 
 * @author wanglong
 * 
 */
public class IndexMsg implements Msg {


	// index message
	private ARhIndex indexMsg = null;

	/**
	 * build Index Async Message
	 * @param rhindex RhIndex
	 */
	public IndexMsg(ARhIndex rhindex) {
		indexMsg = rhindex;
	}

	/**
	 * get index message
	 * 
	 * @return index msg
	 */
	public ARhIndex getIndex() {
		return this.indexMsg;
	}

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getType() {
        return MsgCenter.INDEX_MSG_TYPE;
    }

    @Override
    public Bean getBody() {
        return indexMsg;
    }
	
}
