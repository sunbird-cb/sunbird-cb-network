package org.sunbird.cb.hubservices.exception;


public class NetworkClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return message;
	}

	private final String message;

	public NetworkClientException(String message) {
		super(message);
		this.message = message;
	}

}
