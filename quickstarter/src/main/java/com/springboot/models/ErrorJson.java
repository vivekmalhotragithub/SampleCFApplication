package com.springboot.models;

import java.util.Map;

public class ErrorJson {
	
	 @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ErrorJson [status=");
		builder.append(status);
		builder.append(", error=");
		builder.append(error);
		builder.append(", message=");
		builder.append(message);
		builder.append(", timeStamp=");
		builder.append(timeStamp);
		builder.append(", trace=");
		builder.append(trace);
		builder.append("]");
		return builder.toString();
	}

	public Integer status;
	    public String error;
	    public String message;
	    public String timeStamp;
	    public String trace;

	    public ErrorJson(int status, Map<String, Object> errorAttributes) {
	        this.status = status;
	        this.error = (String) errorAttributes.get("error");
	        this.message = (String) errorAttributes.get("message");
	        this.timeStamp = errorAttributes.get("timestamp").toString();
	        this.trace = (String) errorAttributes.get("trace");
	    }

}
