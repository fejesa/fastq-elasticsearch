package com.bio.sample.dao;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Repository;

import com.bio.sample.model.FileType;
import com.bio.sample.model.FileType.FileTypeBuilder;

/**
 * Used for creation file document.
 */
@Repository
public class PathDao extends BaseDao {

	public void createIndex(Path path) throws IOException {

		IndexRequest indexRequest = createIndexRequest(path);

		try (RestHighLevelClient client = getClient();) {
			client.index(indexRequest);
		}
	}

	private Function<String, String> pathField = s -> {
		try {
			return objectMapper.readValue(s, FileType.class).getPath();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	};

	/**
	 * Fetch file documents from the Elasticsearch that should be processed.
	 * @return List of dequeued documents. Currently the up to the next 50 documents are fetched.
	 */
	public List<Path> getPaths() throws IOException {

		// Dequeued up to 50 files
		SearchRequest request = new SearchRequest(INDEX).types(DOC_TYPE)
				.source(new SearchSourceBuilder()
						.query(QueryBuilders.wildcardQuery("file.path", "*")).size(50));

		try (RestHighLevelClient client = getClient();) {
			SearchResponse response = client.search(request);
			return Arrays.stream(response.getHits().getHits())
					.map(SearchHit::getSourceAsString)
					.map(pathField)
					.map(Paths::get)
					.collect(Collectors.toList());
		}
	}
	
	@Override
	protected FileType createType(Path path) throws IOException {
		return new FileTypeBuilder().setPath(path).build();
	}
}