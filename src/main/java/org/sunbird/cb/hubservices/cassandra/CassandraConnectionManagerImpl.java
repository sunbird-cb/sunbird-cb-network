package org.sunbird.cb.hubservices.cassandra;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.sunbird.cb.hubservices.util.Constants;
import org.sunbird.cb.hubservices.util.PropertiesCache;

import com.datastax.driver.core.AtomicMonotonicTimestampGenerator;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DefaultRetryPolicy;

@Component
public class CassandraConnectionManagerImpl implements CassandraConnectionManager {

	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	private Map<String, Session> cassandraSessionMap = new ConcurrentHashMap<>(2);
	List<String> keyspaces = Arrays.asList(Constants.KEYSPACE_SUNBIRD);
	private Cluster cluster;

	@PostConstruct
	private void addPostConstruct() {
		logger.info("CassandraConnectionManagerImpl:: Initiating...");
		registerShutDownHook();
		createCassandraConnection();
		for (String keyspace : keyspaces) {
			getSession(keyspace);
		}
		logger.info("CassandraConnectionManagerImpl:: Initiated.");
	}

	@Override
	public Session getSession(String keyspace) {
		Session session = cassandraSessionMap.get(keyspace);
		if (null != session) {
			return session;
		} else {
			logger.info("CassandraConnectionManagerImpl:: Creating connection for :: " + keyspace);
			Session session2 = cluster.connect(keyspace);
			cassandraSessionMap.put(keyspace, session2);
			return session2;
		}
	}

	private void registerShutDownHook() {
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new ResourceCleanUp());
		logger.info("Cassandra ShutDownHook registered.");
	}

	private void createCassandraConnection() {
		try {
			PropertiesCache cache = PropertiesCache.getInstance();
			PoolingOptions poolingOptions = new PoolingOptions();
			poolingOptions.setCoreConnectionsPerHost(HostDistance.LOCAL,
					Integer.parseInt(cache.getProperty(Constants.CORE_CONNECTIONS_PER_HOST_FOR_LOCAL)));
			poolingOptions.setMaxConnectionsPerHost(HostDistance.LOCAL,
					Integer.parseInt(cache.getProperty(Constants.MAX_CONNECTIONS_PER_HOST_FOR_LOCAl)));
			poolingOptions.setCoreConnectionsPerHost(HostDistance.REMOTE,
					Integer.parseInt(cache.getProperty(Constants.CORE_CONNECTIONS_PER_HOST_FOR_REMOTE)));
			poolingOptions.setMaxConnectionsPerHost(HostDistance.REMOTE,
					Integer.parseInt(cache.getProperty(Constants.MAX_CONNECTIONS_PER_HOST_FOR_REMOTE)));
			poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL,
					Integer.parseInt(cache.getProperty(Constants.MAX_REQUEST_PER_CONNECTION)));
			poolingOptions
					.setHeartbeatIntervalSeconds(Integer.parseInt(cache.getProperty(Constants.HEARTBEAT_INTERVAL)));
			poolingOptions.setPoolTimeoutMillis(Integer.parseInt(cache.getProperty(Constants.POOL_TIMEOUT)));
			String cassandraHost = (cache.getProperty(Constants.CASSANDRA_CONFIG_HOST));
			String[] hosts = null;
			if (StringUtils.isNotBlank(cassandraHost)) {
				hosts = cassandraHost.split(",");
			}
			cluster = createCluster(hosts, poolingOptions);

			final Metadata metadata = cluster.getMetadata();
			String msg = String.format("Connected to cluster: %s", metadata.getClusterName());
			logger.info(msg);

			for (final Host host : metadata.getAllHosts()) {
				msg = String.format("Datacenter: %s; Host: %s; Rack: %s", host.getDatacenter(), host.getAddress(),
						host.getRack());
				logger.info(msg);
			}
		} catch (Exception e) {
			logger.error("Failed to create cassandra connection. Exception: ", e);
			throw e;
		}
	}

	private Cluster createCluster(String[] hosts, PoolingOptions poolingOptions) {
		Cluster.Builder builder = Cluster.builder().addContactPoints(hosts).withProtocolVersion(ProtocolVersion.V3)
				.withRetryPolicy(DefaultRetryPolicy.INSTANCE)
				.withTimestampGenerator(new AtomicMonotonicTimestampGenerator()).withPoolingOptions(poolingOptions);
		builder = builder.withoutJMXReporting();

		ConsistencyLevel consistencyLevel = getConsistencyLevel();
		logger.info("CassandraConnectionManagerImpl:createCluster: Consistency level = " + consistencyLevel);

		if (consistencyLevel != null) {
			builder = builder.withQueryOptions(new QueryOptions().setConsistencyLevel(consistencyLevel));
		}

		return builder.build();
	}

	private ConsistencyLevel getConsistencyLevel() {
		String consistency = PropertiesCache.getInstance().readProperty(Constants.SUNBIRD_CASSANDRA_CONSISTENCY_LEVEL);

		logger.info("CassandraConnectionManagerImpl:getConsistencyLevel: level = " + consistency);

		if (StringUtils.isBlank(consistency))
			return null;

		try {
			return ConsistencyLevel.valueOf(consistency.toUpperCase());
		} catch (IllegalArgumentException exception) {
			logger.info("CassandraConnectionManagerImpl:getConsistencyLevel: Exception occurred with error message = "
					+ exception.getMessage());
		}
		return null;
	}

	class ResourceCleanUp extends Thread {
		@Override
		public void run() {
			try {
				logger.info("started resource cleanup Cassandra.");
				for (Map.Entry<String, Session> entry : cassandraSessionMap.entrySet()) {
					cassandraSessionMap.get(entry.getKey()).close();
				}
				if (cluster != null) {
					cluster.close();
				}
				logger.info("completed resource cleanup Cassandra.");
			} catch (Exception ex) {
				logger.error("Exception while cleaning up cassandra resource.", ex);
			}
		}
	}

}
