package com.rh.gw.gdjh.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class ETLXstream  extends XStream{
	
	protected MapperWrapper wrapMapper(MapperWrapper next) {
		
	return new MapperWrapper(next) {
		@Override
		public boolean shouldSerializeMember(Class definedIn,String fieldName) {
			if(definedIn==Object.class) {
				try {
					return this.realClass(fieldName)!=null;
				} catch (Exception e) {
				    return false;
				}
				
			}else {
				return super.shouldSerializeMember(definedIn, fieldName);
				
			}
			
		}
		
	};
			
		
	}
	
	

}
