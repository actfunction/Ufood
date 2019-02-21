package com.rh.core;

public class FileUploadResult {
	  private Integer error=0;		//文件上传错误不能抛出，0表示无异常，1代表异常
	    private String url;

	    public Integer getError() {
	        return error;
	    }

	    public void setError(Integer error) {
	        this.error = error;
	    }

	    public String getUrl() {
	        return url;
	    }

	    public void setUrl(String url) {
	        this.url = url;
	    }

	    

	}
