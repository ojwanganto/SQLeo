package com.sqleo.environment.ctrl.commands;

public class CommandExecutionResult {

	public static final int FAILED = -1;
	public static final int INVALID = 0;
	public static final int SUCCESS = 1;

	private int code;
	private String detail;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public boolean isSuccess() {
		return code > 0;
	}
	public boolean isInvalid() {
		return code == 0;
	}
	public boolean isFailed() {
		return code < 0;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

}
