package com.bio.sample.dao;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bio.sample.model.Type;
import com.bio.sample.service.FileDiscoverer;
import com.bio.sample.service.FileProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Wraps base logic of the access to the Elasticsearch.
 *
 */
public abstract class BaseDao {

	/** Refer to the elastic index. */
	public static final String INDEX = "sampledb";

	/** The generic doc type of the application. */
	public static final String DOC_TYPE = "_doc";

	protected final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** Used for object serialization, deserialization. */
	protected final ObjectMapper objectMapper;

	public BaseDao() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
		objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
	}

	protected RestHighLevelClient getClient() {
		return new RestHighLevelClient(
				RestClient.builder(
						new HttpHost("localhost", 9200, "http"), 
						new HttpHost("localhost", 9201, "http")));
	}

	private String toJson(Object type) throws JsonProcessingException {
		return objectMapper.writeValueAsString(type);
	}
	
	/**
	 * Create an index request that contains the new document creation.
	 * 
	 * @param path Reference to the given file about the meta data is being generated.
	 * @throws IOException If the file processing or JSON creation is failed.
	 */
	protected IndexRequest createIndexRequest(Path path) throws IOException {
		return new IndexRequest(INDEX, DOC_TYPE)
				.source(toJson(createType(path)), XContentType.JSON);
	}

	/**
	 * Creates the new index in Elastic.
	 * 
	 * @param path
	 *            Reference to the given file about the meta data is being
	 *            generated.
	 * @param field
	 *            Used for checking the existence of the given file.
	 * @throws IOException
	 *             If the index creation is failed.
	 */
	protected void createIndex(Path path, String field) throws IOException {
		try (RestHighLevelClient client = getClient();) {

			List<DeleteRequest> deletes = createDeleteRequest(path, client);

			BulkRequest bulkRequest = new BulkRequest();
			deletes.forEach(bulkRequest::add);

			// Check the existence of the sample
			SearchResponse sampleResponse = check(field, path.toString(), client);

			// Create it if it is not exist before
			if (sampleResponse.getHits().getHits().length == 0) {

				try {
					logger.info("Load file {}", path);

					bulkRequest.add(createIndexRequest(path));
				} catch (Exception ex) {
					logger.error(String.format("File %s processing failed", path), ex);
				}
			}

			if (!bulkRequest.requests().isEmpty()) {
				// Executes the bulk request
				client.bulk(bulkRequest);
			}
		}
	}

	/**
	 * Collected paths that discovered by the {@link FileDiscoverer} are temporarily
	 * persisted in Elastic as an internal document. When the file are being processed by the {@link FileProcessor}
	 * this document must be removed.
	 * Document removing can be wrapped in a delete request.
	 * 
	 * @param path Reference to the given file about the meta data is being generated.
	 * @param client REST client of the Elastic.
	 */
	protected List<DeleteRequest> createDeleteRequest(Path path, RestHighLevelClient client) throws IOException {
		// Fetch the File documents that contain the path info
		// These documents must be removed
		SearchResponse fileResponse = client
				.search(new SearchRequest(INDEX).types(DOC_TYPE)
				.source(new SearchSourceBuilder()
						.query(QueryBuilders.matchQuery("file.path", path.toString()))));

		return Arrays.stream(fileResponse.getHits().getHits())
				.map(SearchHit::getId)
				.map(id -> new DeleteRequest(INDEX, DOC_TYPE, id))
				.collect(Collectors.toList());
	}

	protected SearchResponse check(String field, String text, RestHighLevelClient client) throws IOException {
		return client.search(new SearchRequest(INDEX).types(DOC_TYPE)
				.source(new SearchSourceBuilder().query(QueryBuilders.matchQuery(field, text))));
	}

	/**
	 * Create a document type based on the path.
	 * @param path Reference to the given file about the meta data is being generated.
	 * @throws IOException If the file meta data cannot be generated.
	 */
	protected abstract Type createType(Path path) throws IOException;
}