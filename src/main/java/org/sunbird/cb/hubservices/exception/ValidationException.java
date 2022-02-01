package org.sunbird.cb.hubservices.exception;

public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return message;
	}

	private final String message;

	public ValidationException(String message) {
		super(message);
		this.message = message;
	}

}
