package org.sunbird.cb.hubservices.exception;

public class NoRecordFoundException extends DaoLayerException {

	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return message;
	}

	private final String message;

	public NoRecordFoundException(String message) {
		super(message);
		this.message = message;
	}

}
