package org.sunbird.cb.hubservices.util;

public class Constants {

	public static final String FIELD_KEY = "fieldKey";
	public static final String OFFSET = "offset";
	public static final String LIMIT = "limit";
	public static final String USER_ID = "userId";
	public static final String ACCEPT = "Accept";
	public static final String RESPONSE = "response";
	public static final String OSID = "osid";
	public static final String PROFILE_DETAILS_PROFESSIOANAL_DETAILS = "profileDetails.professionalDetails";
	public static final String PROFILE_DETAILS_EMPLOYMENT_DETAILS = "profileDetails.employmentDetails";
	public static final String PROFILE_DETAILS_PERSONAL_DETAILS = "profileDetails.personalDetails";

	public enum Graph {
		ID("id"), STATUS(ResponseStatus.STATUS), UUID("UUID"), PROPS("props"), CREATED_AT("createdAt"),
		UPDATED_AT("updatedAt");

		private String value;

		private Graph(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	private static final String UTIL_CLASS = "Utility class";

	public enum DIRECTION {
		IN, OUT
	}

	public static class Status {
		private Status() {
			throw new IllegalStateException(UTIL_CLASS);
		}

		public static final String APPROVED = "Approved";
		public static final String REJECTED = "Rejected";
		public static final String PENDING = "Pending";
		public static final String DELETED = "Deleted";

	}

	public static class Message {
		private Message() {
			throw new IllegalStateException(UTIL_CLASS);
		}

		public static final String CONNECTION_EXCEPTION_OCCURED = "Connection exception occurred: {}";
		public static final String FAILED_CONNECTION = "Failed user connections: ";
		public static final String USER_ID_INVALID = "user_id cant be null or empty";
		public static final String ROOT_ORG_INVALID = "rootOrg cant be null or empty";
		public static final String SENT_NOTIFICATION_ERROR = "Notification event send error occurred: {}";
		public static final String SENT_NOTIFICATION_SUCCESS = "Notification event send : {}";
	}

	public static class ResponseStatus {
		private ResponseStatus() {
			throw new IllegalStateException(UTIL_CLASS);
		}

		public static final String SUCCESSFUL = "Successful";
		public static final String FAILED = "Failed";
		public static final String MESSAGE = "message";
		public static final String DATA = "data";
		public static final String STATUS = "status";
		public static final String PAGENO = "pageNo";
		public static final String HASPAGENEXT = "hasNextPage";
		public static final String TOTALHIT = "totalHit";
	}

	public static class Parmeters {
		private Parmeters() {
			throw new IllegalStateException(UTIL_CLASS);
		}

		public static final String ROOT_ORG = "rootOrg";

	}

	public static class Profile {
		private Profile() {
			throw new IllegalStateException(UTIL_CLASS);
		}

		public static final String FIRST_NAME = "firstname";
		public static final String SUR_NAME = "surname";
		public static final String PERSONAL_DETAILS = "personalDetails";
		public static final String HUB_MEMBER = "Hub member";

	}

}
