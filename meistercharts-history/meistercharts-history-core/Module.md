# Documentation for History Core

## History API

### Relevant classes to store/retrieve history information:

Distinction to the other classes:

* [HistoryValues] does *only* contain the values - no timestamps
* [HistoryChunk] contains the [HistoryConfiguration], the [HistoryValues] *and* the timestamps. Also has a [RecordingType]
* [com.meistercharts.history.HistoryBucket] contains a [HistoryChunk] and a [com.meistercharts.history.HistoryBucketDescriptor]. Is placed on "event" borders!
