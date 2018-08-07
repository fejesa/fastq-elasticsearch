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
Cheat sheet
The most interesting part of the fastq-elastic service is what and how can we retrieve the collected data from the Elasticsearch. The following section shows some data queries that can be applied from the Kibana console.

Another general cheat sheet about the Kibana is http://elasticsearch-cheatsheet.jolicode.com/.

Counts the number of samples
```
GET sampledb/_doc/_count
{
  "query": {
    "wildcard": {
      "sample.samplePath": "*"
    }
  }
}
```

Get sample files that start with 'XXX-KM-34_S34'
```
GET sampledb/_doc/_search
{
  "query": {
    "wildcard": {
      "sample.sampleName.exact": "XXX-KM-34_S34*"
    }
  }
}
```

Get all sample file that name contain 'XXX5S' and field length > 30MB
```
GET sampledb/_doc/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "wildcard": {
            "sample.sampleName.exact": "*XXX5S*"
          }
        }
      ],
      "filter": [
        {
          "range": {
            "sample.fileLength": {
              "gte": "30000000"
            }
          }
        }
      ]
    }
  }
}
```

Get the top 20 duplicated sample files
```
GET sampledb/_doc/_search
{
  "size": 0,
  "aggs": {
    "distinct_sample": {
      "terms": {
        "field": "sample.sampleName.exact",
        "size": 20
      }
    }
  }
}
```

Find largest sample file in MB using aggreagation (in 2 steps)

```
POST sampledb/_doc/_search
{
  "size": 0,
  "aggs": {
    "largest_sample": {
      "max": {
        "field": "sample.fileLength",
        "script": {
          "source": "_value / params.in_mb",
          "params": {
            "in_mb": 1048576
          }
        }
      }
    }
  }
}
```
```
GET sampledb/_doc/_search
{
  "query": {
    "match": {
      "sample.fileLength": 58362878472
    }
  }
}
```

Find top 3 largest sample files using query and sorting (in 1 step)
```
GET sampledb/_doc/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "_script": {
        "type": "number",
        "script": {
          "lang": "painless",
          "source": "doc['sample.fileLength'].value / params.in_mb",
          "params": {
            "in_mb": 1048576
          }
        },
        "order": "desc"
      }
    }
  ],
  "size": 3
}
```

Get the sum of the size of the sample files in GB
```
GET sampledb/_doc/_search
{
  "size": 0,
  "aggs": {
    "largest_sample": {
      "sum": {
        "field": "sample.fileLength",
        "script": {
          "source": "_value / params.in_gb",
          "params": {
            "in_gb": 1073741824
          }
        }
      }
    }
  }
}
```
