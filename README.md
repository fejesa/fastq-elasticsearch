# fastq-elasticsearch
Collects some metadata about FASTQ files and stores them in elasticsearch

Configuration
The meta information about the sample files is stored in JSON document format. Before the fastq-elastic tool is started we have to prepare the mapping in the Elasticsearch. The mapping configuration can be found in the git repo (src/main/resources/sampledb-index.json). 

Next step is the configuration of the fastq-elastic tool. You must set the custom values in the sampledb.conf file.

```
{
    elastic.host = localhost
    elastic.port = 9200

    # Supported file types
    file.extensions = [htr, fastq.gz]

    # List of folders that should be parsed
	  root.folders = [
		  /storage/research,
		  /storage/validation
	]
	
	# List of ignored folders
	folders.exclusive = []
}
```
