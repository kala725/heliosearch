/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.analytics.util.valuesource;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.solr.search.QueryContext;
import org.apache.solr.search.function.FuncValues;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.funcvalues.DoubleFuncValues;

/**
 * Abstract {@link ValueSource} implementation which wraps two ValueSources
 * and applies an extendible double function to their values.
 **/
public abstract class DualDoubleFunction extends ValueSource {
  protected final ValueSource a;
  protected final ValueSource b;

  public DualDoubleFunction(ValueSource a, ValueSource b) {
    this.a = a;
    this.b = b;
  }

  protected abstract String name();
  protected abstract double func(int doc, FuncValues aVals, FuncValues bVals);

  @Override
  public String description() {
    return name() + "(" + a.description() + "," + b.description() + ")";
  }

  @Override
  public FuncValues getValues(QueryContext context, AtomicReaderContext readerContext) throws IOException {
    final FuncValues aVals =  a.getValues(context, readerContext);
    final FuncValues bVals =  b.getValues(context, readerContext);
    return new DoubleFuncValues(this) {
      @Override
      public double doubleVal(int doc) {
        return func(doc, aVals, bVals);
      }
      
      @Override
      public boolean exists(int doc) {
        return aVals.exists(doc) & bVals.exists(doc);
      }

      @Override
      public String toString(int doc) {
        return name() + '(' + aVals.toString(doc) + ',' + bVals.toString(doc) + ')';
      }
    };
  }

  @Override
  public void createWeight(QueryContext context) throws IOException {
    a.createWeight(context);
    b.createWeight(context);
  }

  @Override
  public boolean equals(Object o) {
    if (getClass() != o.getClass()) return false;
    DualDoubleFunction other = (DualDoubleFunction)o;
    return this.a.equals(other.a)
        && this.b.equals(other.b);
  }

  @Override
  public int hashCode() {
    int h = a.hashCode();
    h ^= (h << 13) | (h >>> 20);
    h += b.hashCode();
    h ^= (h << 23) | (h >>> 10);
    h += name().hashCode();
    return h;
  }
}
