/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
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
package brooklyn.catalog;

import javax.annotation.Nullable;

import brooklyn.catalog.CatalogItem.CatalogItemType;
import brooklyn.entity.Application;
import brooklyn.entity.Entity;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.policy.Policy;
import brooklyn.policy.PolicySpec;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class CatalogPredicates {

    public static <T,SpecT> Predicate<CatalogItem<T,SpecT>> isCatalogItemType(final CatalogItemType ciType) {
        return new Predicate<CatalogItem<T,SpecT>>() {
            @Override
            public boolean apply(@Nullable CatalogItem<T,SpecT> item) {
                return (item != null) && item.getCatalogItemType()==ciType;
            }
        };
    }

    public static final Predicate<CatalogItem<Application,EntitySpec<? extends Application>>> IS_TEMPLATE = 
            CatalogPredicates.<Application,EntitySpec<? extends Application>>isCatalogItemType(CatalogItemType.TEMPLATE);
    public static final Predicate<CatalogItem<Entity,EntitySpec<?>>> IS_ENTITY = 
            CatalogPredicates.<Entity,EntitySpec<?>>isCatalogItemType(CatalogItemType.ENTITY);
    public static final Predicate<CatalogItem<Policy,PolicySpec<?>>> IS_POLICY = 
            CatalogPredicates.<Policy,PolicySpec<?>>isCatalogItemType(CatalogItemType.POLICY);
    
    public static final Function<CatalogItem<?,?>,String> ID_OF_ITEM_TRANSFORMER = new Function<CatalogItem<?,?>, String>() {
        @Override @Nullable
        public String apply(@Nullable CatalogItem<?,?> input) {
            if (input==null) return null;
            return input.getId();
        }
    };

    @Deprecated
    public static <T,SpecT> Predicate<CatalogItem<T,SpecT>> name(final Predicate<? super String> filter) {
        return displayName(filter);
    }

    public static <T,SpecT> Predicate<CatalogItem<T,SpecT>> displayName(final Predicate<? super String> filter) {
        return new Predicate<CatalogItem<T,SpecT>>() {
            @Override
            public boolean apply(@Nullable CatalogItem<T,SpecT> item) {
                return (item != null) && filter.apply(item.getDisplayName());
            }
        };
    }

    @Deprecated
    public static <T,SpecT> Predicate<CatalogItem<T,SpecT>> registeredTypeName(final Predicate<? super String> filter) {
        return symbolicName(filter);
    }

    public static <T,SpecT> Predicate<CatalogItem<T,SpecT>> symbolicName(final Predicate<? super String> filter) {
        return new Predicate<CatalogItem<T,SpecT>>() {
            @Override
            public boolean apply(@Nullable CatalogItem<T,SpecT> item) {
                return (item != null) && filter.apply(item.getSymbolicName());
            }
        };
    }

    public static <T,SpecT> Predicate<CatalogItem<T,SpecT>> javaType(final Predicate<? super String> filter) {
        return new Predicate<CatalogItem<T,SpecT>>() {
            @Override
            public boolean apply(@Nullable CatalogItem<T,SpecT> item) {
                return (item != null) && filter.apply(item.getJavaType());
            }
        };
    }

    public static <T,SpecT> Predicate<CatalogItem<T,SpecT>> xml(final Predicate<? super String> filter) {
        return new Predicate<CatalogItem<T,SpecT>>() {
            @Override
            public boolean apply(@Nullable CatalogItem<T,SpecT> item) {
                return (item != null) && filter.apply(item.toXmlString());
            }
        };
    }

    /**
     * Returns a predicate that matches the type of an application or entity. This is a string composed of the Java class
     * name and the version separated by a colon.
     *
     * @param typeName The type
     * @return The predicate
     */
    public static <T,SpecT> Predicate<CatalogItem<T,SpecT>> typeName(final String typeName) {
        final int javaClassEnd = typeName.lastIndexOf(':');
        final String javaClassName = typeName.substring(0, javaClassEnd);
        final String version = typeName.substring(javaClassEnd + 1);
        final Predicate<? super String> javaClassPredicate = Predicates.equalTo(javaClassName);
        final Predicate<? super String> versionPredicate = Predicates.equalTo(version);

        return new Predicate<CatalogItem<T,SpecT>>() {
            @Override
            public boolean apply(@Nullable CatalogItem<T,SpecT> item) {
                return (item != null) && javaClassPredicate.apply(item.getJavaType()) && versionPredicate.apply(item.getVersion());
            }
        };
    }
}