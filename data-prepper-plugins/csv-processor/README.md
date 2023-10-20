# CSV Processor
This is a processor that takes in an Event and parses its CSV data into columns.
## Basic Usage
### User Specified Column Names
To get started, create the following `pipelines.yaml`.
```yaml
csv-pipeline:
  source:
    file:
      path: "/full/path/to/ingest.csv"
      record_type: "event"
  processor:
    - csv:
        column_names: ["col1", "col2"]
  sink:
    - stdout:
```
#### Example With User Specified Column Names Config:
If you wish to test the CSV Processor with the above config then you may find the following example useful. Create the following file `ingest.csv` and replace the `path` in the file source of your `pipeline.yaml` with the path of this file.
```
1,2,3
```

When run, the processor will parse the message. Notice that since there are only two column names specified in the config, the third column name is autogenerated.
```
{"message": "1,2,3", "col1": "1", "col2": "2", "column3": "3"}
```
### Auto Detect Column Names
The following configuration auto detects the header of a CSV file ingested through S3 Source. See the [S3 Source Documentation](https://github.com/opensearch-project/data-prepper/tree/main/data-prepper-plugins/s3-source) for more information.
```yaml
csv-s3-pipeline:
  source:
    s3:
      notification_type: "sqs"
      codec:
        newline:
          skip_lines: 1
          header_destination: "header"
      compression: none
      sqs:
        queue_url: "https://sqs.<region>.amazonaws.com/<account id>/<queue name>"
      aws:
        region: "<region>"
  processor:
    - csv:
        column_names_source_key: "header"
  sink:
    - stdout:
```
#### Example With Auto Detect Column Names Config:
If you wish to test the CSV Processor with the above config then you may find the following example useful. Upload the following file `ingest.csv` to the S3 bucket that your SQS queue is attached to:
```
Should,skip,this,line
a,b,c
1,2,3
```
When run, the processor will process the following event:
```json
{"header": "a,b,c", "message": "1,2,3"}
```
And will parse it into the following. (Note that since `delete_header` is `true`, the header is deleted):
```json
{"message": "1,2,3", "a": "1", "b": "2", "c": "3"}
```
## Configuration
* `source` — The field in the `Event` that will be parsed.
  * Default: `message`

* `quote_character` — The character used as a text qualifier for a single column of data.
  * Default: Double quote (`"`)

* `delimiter` — The character separating each column.
  * Default: `,`

* `delete_header` (Optional, boolean) — If specified, the header on the `Event` (`column_names_source_key`) will be deleted after the `Event` is parsed. If there’s no header on the `Event` then nothing will happen.
  * Default: `true`

* `column_names_source_key` — (Optional) The field in the Event that specifies the CSV column names, which will be autodetected. If there must be extra column names, they will be autogenerated according to their index.
  * There is no default.
  * Note: If `column_names` is also defined, the header in `column_names_source_key` will be used to generate the Event fields.
  * Note: If too few columns are specified in this field, the remaining column names will be autogenerated. If too many column names are specified in this field, then the extra column names will be omitted.

* `column_names` — (Optional) User-specified names for the CSV columns.
  * Default: `[column1, column2, ..., columnN]` if there are `N` columns of data in the CSV record and `column_names_source_key` is not defined.
  * Note: If `column_names_source_key` is defined, the header in `column_names_source_key` will be used to generate the Event fields.
  * Note: If too few columns are specified in this field, the remaining column names will be autogenerated. If too many column names are specified in this field, then the extra column names will be omitted.

## Metrics

Apart from common metrics in [AbstractProcessor](https://github.com/opensearch-project/data-prepper/blob/main/data-prepper-api/src/main/java/org/opensearch/dataprepper/model/processor/AbstractProcessor.java), the CSV Processor includes the following custom metric.

**Counter**

* `csvInvalidEvents`: The number of invalid Events. An invalid Event causes an Exception to be thrown when parsed. This is most commonly due to an unclosed quote. 

# CSV Sink/Output Codec

This is an implementation of CSV Sink Codec that parses the Dataprepper Events into CSV rows and writes them into the underlying OutputStream.

## Usages

CSV Codec can be configured with sink plugins (e.g. S3 Sink) in the Pipeline file.

## Configuration Options

```
pipeline:
  ...
  sink:
    - s3:
        aws:
          region: us-east-1
          sts_role_arn: arn:aws:iam::123456789012:role/Data-Prepper
          sts_header_overrides:
        max_retries: 5
        bucket: bucket_name
        object_key:
          path_prefix: my-elb/%{yyyy}/%{MM}/%{dd}/
        threshold:
          event_count: 2000
          maximum_size: 50mb
          event_collect_timeout: 15s
        codec:
          csv:
            delimiter: ","
            header:
              - Year
              - Age
              - Ethnic
              - Sex
              - Area
              - count
            exclude_keys:
              - s3
        buffer_type: in_memory
```

### Note:

1) If the user wants the tags to be a part of the resultant CSV Data and has given `tagsTargetKey` in the config file, the user also has to modify the header to accommodate the tags. Another header field  has to be provided in the headers:

   ```
   header:
              - Year
              - Age
              - Ethnic
              - Sex
              - Area
              - <yourTagsTargetKey>
   ```
   Please note that if this is not done, then the codec will throw an error:
   `"CSV Row doesn't conform with the header."`

## AWS Configuration

### Codec Configuration:

1) `exclude_keys`: Those keys of the events that the user wants to exclude while converting them to CSV Rows.
2) `delimiter`: The user can provide the delimiter of choice.
3) `header`: The user can provide the desired header for the resultant CSV file.

## Developer Guide
This plugin is compatible with Java 8 and up. See
- [CONTRIBUTING](https://github.com/opensearch-project/data-prepper/blob/main/CONTRIBUTING.md)
- [monitoring](https://github.com/opensearch-project/data-prepper/blob/main/docs/monitoring.md)