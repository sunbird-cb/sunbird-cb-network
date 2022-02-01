package org.sunbird.cb.hubservices.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

public class MultiSearch {

	@Override
	public String toString() {
		return "MultiSearch [size=" + size + ", offset=" + offset + ", search=" + search + "]";
	}

	@PositiveOrZero
	private int size = 10;

	@PositiveOrZero
	private int offset = 0;

	@NotBlank
	@Size(min = 1)
	private List<Search> search = new ArrayList<>();

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public List<Search> getSearch() {
		return search;
	}

	public void setSearch(List<Search> search) {
		this.search = search;
	}
}
