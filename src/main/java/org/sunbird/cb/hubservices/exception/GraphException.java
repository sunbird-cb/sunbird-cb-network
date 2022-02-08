package org.sunbird.cb.hubservices.exception;

public class GraphException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public String getErrCode() {
		return errCode;
	}

	private final String errCode;

	public GraphException(String errCode) {
		super();
		this.errCode = errCode;
	}

	public GraphException(String code, String message) {
		super(message);
		this.errCode = code;
	}

}
