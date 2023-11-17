package org.sunbird.cb.hubservices.util;

public class Constants {

	public static final String TO_VALUE = "toValue";
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
	public static final String STATUS = "status";
	public static final String CONNECTIONS = "/connections";
	public static final String CONNECTIONS_PROFILE = "/connections/profile";
	public static final String FIND_RECOMMENDED = "/find/recommended";
	public static final String FIND_SUGGESTS = "/find/suggests";
	public static final String FETCH_REQUESTED = "/fetch/requested";
	public static final String FETCH_REQUESTS_RECEIVED = "/fetch/requests/received";
	public static final String FETCH_ESTABLISHED = "/fetch/established";
	public static final String ADD = "/add";
	public static final String UPDATE = "/update";
	public static final String PAGE_SIZE = "pageSize";
	public static final String PAGE_NO = "pageNo";
	public static final String ADD_OPERATION = "Add";
	public static final String UPDATE_OPERATION = "Update";
	public static final String FAILED = "Failed";
	public static final String FROM_UUID = "fromUUID";
	public static final String TO_UUID = "toUUID";
	public static final String UUID = "UUID";
	public static final String PROPS = "props";
	public static final String VERIFIED_KARMAYOGI = "verifiedKarmayogi";
	public static final String PROFILE_DETAILS_VERIFIED_KARMAYOGI = "profileDetails.verifiedKarmayogi";
	public static final String REDIS_COMMON_KEY = "NETWORK_";
	public static final String QUESTION_ID = "qs_id_";
	public static final String API_REDIS_DELETE = "api.redis.delete";
	public static final String API_REDIS_GET_KEYS = "api.redis.get.keys";
	public static final String API_REDIS_GET_KEYS_VALUE_SET = "api.redis.get.keys&values";
	public static final String SUCCESSFUL = "Successful";
	public static final String UNDER_SCORE = "_";
	public static final String USER_LIST = "userList";
	public static final String KEYSPACE_SUNBIRD = "sunbird";
	public static final String CORE_CONNECTIONS_PER_HOST_FOR_LOCAL = "coreConnectionsPerHostForLocal";
	public static final String CORE_CONNECTIONS_PER_HOST_FOR_REMOTE = "coreConnectionsPerHostForRemote";
	public static final String MAX_CONNECTIONS_PER_HOST_FOR_LOCAl = "maxConnectionsPerHostForLocal";
	public static final String MAX_CONNECTIONS_PER_HOST_FOR_REMOTE = "maxConnectionsPerHostForRemote";
	public static final String MAX_REQUEST_PER_CONNECTION = "maxRequestsPerConnection";
	public static final String HEARTBEAT_INTERVAL = "heartbeatIntervalSeconds";
	public static final String POOL_TIMEOUT = "poolTimeoutMillis";
	public static final String CASSANDRA_CONFIG_HOST = "cassandra.config.host";
	public static final String SUNBIRD_CASSANDRA_CONSISTENCY_LEVEL = "sunbird_cassandra_consistency_level";
	public static final String INSERT_INTO = "INSERT INTO ";
	public static final String DOT = ".";
	public static final String OPEN_BRACE = "(";
	public static final String VALUES_WITH_BRACE = ") VALUES (";
	public static final String QUE_MARK = "?";
	public static final String COMMA = ",";
	public static final String CLOSING_BRACE = ");";
	public static final String EXCEPTION_MSG_FETCH = "Exception occurred while fetching record from ";
	public static final String ID = "id";
	public static final String FIRST_NAME = "firstname";
	public static final String TABLE_USER = "user";
	public static final String CHANNEL = "channel";
	public static final String FULL_NAME = "fullName";

	public enum Graph {
		ID("id"), STATUS(ResponseStatus.STATUS), UUID("UUID"), PROPS("props"), CREATED_AT("createdAt"), CONNECTION_ID("connectionId"),
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
		public static final String PERSONAL_DETAILS = "personalDetails";
		public static final String HUB_MEMBER = "Hub member";

	}

}
