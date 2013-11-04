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

package org.elasticsearch.search.aggregations.factory;

import org.elasticsearch.search.aggregations.AggregationInitializationException;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.context.AggregationContext;

/**
 * A factory that knows how to create an {@link Aggregator} of a specific type.
 */
public abstract class AggregatorFactory {

    /**
     * A factory for aggregators that can only serve as leaf aggregators.
     */
    public abstract static class LeafOnly extends AggregatorFactory {

        public LeafOnly(String name, String type) {
            super(name, type);
        }

        /**
         * Disallows the registration of sub-factories.
         */
        public AggregatorFactory subFactories(AggregatorFactories subFactories) {
            throw new AggregationInitializationException("Aggregator [" + name + "] of type [" + type + "] cannot accept sub-aggregations");
        }
    }

    protected String name;
    protected String type;
    protected AggregatorFactory parent;
    protected AggregatorFactories factories = AggregatorFactories.EMPTY;

    /**
     * Constructs a new aggregator factory.
     *
     * @param name  The aggregation name
     * @param type  The aggregation type
     */
    public AggregatorFactory(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Registers sub-factories with this factory. The sub-factory will be responsible for the creation of sub-aggregators under the
     * aggregator created by this factory.
     *
     * @param subFactories  The sub-factories
     * @return  this factory (fluent interface)
     */
    public AggregatorFactory subFactories(AggregatorFactories subFactories) {
        this.factories = subFactories;
        this.factories.setParent(this);
        return this;
    }

    /**
     * Validates the state of this factory (makes sure the factory is properly configured)
     */
    public final void validate() {
        doValidate();
        factories.validate();
    }

    /**
     * @return  The parent factory if one exists (will always return {@code null} for top level aggregator factories).
     */
    public AggregatorFactory parent() {
        return parent;
    }

    /**
     * @return  The bucket aggregation mode of the aggregation this factory creates
     * @see     Aggregator.BucketAggregationMode
     *
     * @todo    right now, this method means a factory can only create one type of aggregation... we might want to change that in the future
     */
    public abstract Aggregator.BucketAggregationMode bucketMode();

    /**
     * Creates the aggregator
     *
     * @param context               The aggregation context
     * @param parent                The parent aggregator (if this is a top level factory, the parent will be {@code null})
     * @param expectedBucketsCount  If this is a sub-factory of another factory, this will indicate the number of bucket the parent aggregator
     *                              may generate (this is an estimation only). For top level factories, this will always be 0
     *
     * @return                      The created aggregator
     */
    public abstract Aggregator create(AggregationContext context, Aggregator parent, int expectedBucketsCount);

    public void doValidate() {
    }

}
