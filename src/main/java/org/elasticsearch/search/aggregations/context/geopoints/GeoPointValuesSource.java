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

package org.elasticsearch.search.aggregations.context.geopoints;

import org.elasticsearch.index.fielddata.GeoPointValues;
import org.elasticsearch.search.aggregations.context.FieldDataSource;
import org.elasticsearch.search.aggregations.context.ValuesSource;

import java.io.IOException;

/**
 *
 */
public interface GeoPointValuesSource extends ValuesSource {

    GeoPointValues values() throws IOException;


    public static class FieldData extends ValuesSource.FieldData<FieldDataSource.GeoPoint> implements GeoPointValuesSource {

        public FieldData(FieldDataSource.GeoPoint source) {
            super(source);
        }

        @Override
        public GeoPointValues values() throws IOException {
            return source.geoPointValues();
        }
    }

}
