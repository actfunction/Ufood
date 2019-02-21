package com.rh.core.plug.search;


/**
 * index message transform
 * @author wanglong
 */
public interface IndexTransformer {

	/**
	 * index message transform 
	 * @param indexMsg index message
	 */
	void transform(ARhIndex indexMsg);

}
