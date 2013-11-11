/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.aggregations.bucket.single.filter;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.elasticsearch.common.lucene.ReaderContextAware;
import org.elasticsearch.common.lucene.docset.DocIdSets;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.search.aggregations.AggregationExecutionException;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.bucket.single.SingleBucketAggregator;
import org.elasticsearch.search.aggregations.context.AggregationContext;
import org.elasticsearch.search.aggregations.factory.AggregatorFactories;
import org.elasticsearch.search.aggregations.factory.AggregatorFactory;

import java.io.IOException;

/**
 * Aggregate all docs that match a filter.
 */
public class FilterAggregator extends SingleBucketAggregator implements ReaderContextAware {

    private final Filter filter;

    private Bits bits;

    public FilterAggregator(String name,
                            org.apache.lucene.search.Filter filter,
                            AggregatorFactories factories,
                            AggregationContext aggregationContext,
                            Aggregator parent) {
        super(name, factories, aggregationContext, parent);
        this.filter = filter;
    }

    @Override
    public InternalAggregation buildAggregation(long owningBucketOrdinal) {
        return new InternalFilter(name, docCount(owningBucketOrdinal), buildSubAggregations(owningBucketOrdinal));
    }

    @Override
    public void collect(int doc, long owningBucketOrdinal) throws IOException {
        if (bits.get(doc)) {
            collectSubAggregators(doc, owningBucketOrdinal);
            counts = BigArrays.grow(counts, owningBucketOrdinal + 1);
            counts.increment(owningBucketOrdinal, 1);
        }
    }

    @Override
    public void setNextReader(AtomicReaderContext reader) {
        try {
            bits = DocIdSets.toSafeBits(reader.reader(), filter.getDocIdSet(reader, reader.reader().getLiveDocs()));
        } catch (IOException ioe) {
            throw new AggregationExecutionException("Failed to aggregate filter aggregator [" + name + "]", ioe);
        }
    }

    public static class Factory extends AggregatorFactory {

        private org.apache.lucene.search.Filter filter;

        public Factory(String name, Filter filter) {
            super(name, InternalFilter.TYPE.name());
            this.filter = filter;
        }

        @Override
        public BucketAggregationMode bucketMode() {
            return BucketAggregationMode.MULTI_BUCKETS;
        }

        @Override
        public Aggregator create(AggregationContext context, Aggregator parent, long expectedBucketsCount) {
            FilterAggregator aggregator = new FilterAggregator(name, filter, factories, context, parent);
            context.registerReaderContextAware(aggregator);
            return aggregator;
        }

    }
}


