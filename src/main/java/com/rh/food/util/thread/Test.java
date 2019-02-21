package com.rh.food.util.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
 

public class Test {
 public static void main(String[] args) {
	 List<Bean> beans =new ArrayList<Bean>();
	 Bean bean =new Bean();
	 bean.set("GW_ID", "1");
	 bean.set("FILE_PATH", "2");
	 beans.add(bean);

	 SimpleAsyncExecutor<Bean, Boolean> simpleAsyncExecutor=new SimpleAsyncExecutor<Bean, Boolean>();
 	 Boolean flag=simpleAsyncExecutor.execute(beans,  new Callback<Bean, Boolean>("orderInsert") {
         @Override
         public Boolean call(Bean bean, Message message) {
        	 
         	 try {
        		 

         	 }catch(Exception e){
        		 
         		 
        	 }
             return true;
         }
     });
	 //flag=true 批量执行     成功 否则失败
 }
}
