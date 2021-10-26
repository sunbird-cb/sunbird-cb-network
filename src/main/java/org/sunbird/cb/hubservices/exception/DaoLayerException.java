package org.sunbird.cb.hubservices.exception;

import org.springframework.web.bind.annotation.ResponseBody;

@ResponseBody
public class DaoLayerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return message;
	}

	private final String message;

	public DaoLayerException(String message) {
		super(message);
		this.message = message;
	}

}
