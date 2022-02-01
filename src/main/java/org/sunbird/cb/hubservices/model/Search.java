package org.sunbird.cb.hubservices.model;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class Search {

	@NotBlank
	private String field;
	@NotBlank
	@Size(min = 1)
	private List<Object> values;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public List<Object> getValues() {
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}
}
