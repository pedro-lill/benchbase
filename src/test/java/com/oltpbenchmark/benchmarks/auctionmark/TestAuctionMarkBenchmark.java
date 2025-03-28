/*
 *  Copyright 2015 by OLTPBenchmark Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.oltpbenchmark.benchmarks.auctionmark;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.oltpbenchmark.api.AbstractTestBenchmarkModule;
import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.TransactionType;
import com.oltpbenchmark.benchmarks.auctionmark.procedures.GetItem;
import com.oltpbenchmark.benchmarks.auctionmark.procedures.GetUserInfo;
import com.oltpbenchmark.benchmarks.auctionmark.procedures.NewBid;
import com.oltpbenchmark.benchmarks.auctionmark.procedures.NewComment;
import com.oltpbenchmark.benchmarks.auctionmark.procedures.NewCommentResponse;
import com.oltpbenchmark.benchmarks.auctionmark.procedures.NewFeedback;
import com.oltpbenchmark.benchmarks.auctionmark.procedures.NewItem;
import com.oltpbenchmark.benchmarks.auctionmark.procedures.NewPurchase;
import com.oltpbenchmark.benchmarks.auctionmark.procedures.UpdateItem;
import com.oltpbenchmark.benchmarks.auctionmark.util.CategoryParser;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class TestAuctionMarkBenchmark extends AbstractTestBenchmarkModule<AuctionMarkBenchmark> {

  public static final List<Class<? extends Procedure>> PROCEDURE_CLASSES =
      List.of(
          GetItem.class,
          GetUserInfo.class,
          NewBid.class,
          NewComment.class,
          NewCommentResponse.class,
          NewFeedback.class,
          NewItem.class,
          NewPurchase.class,
          UpdateItem.class);

  @Override
  public List<Class<? extends Procedure>> procedures() {
    return PROCEDURE_CLASSES;
  }

  @Override
  public Class<AuctionMarkBenchmark> benchmarkClass() {
    return AuctionMarkBenchmark.class;
  }

  @Override
  protected void postCreateDatabaseSetup() throws IOException {
    super.postCreateDatabaseSetup();
    AuctionMarkProfile.clearCachedProfile();
  }

  /** testCategoryParser */
  @Test
  public void testCategoryParser() throws Exception {
    CategoryParser categoryParser = new CategoryParser();
    assertNotNull(categoryParser.getCategoryMap());
    assertTrue(categoryParser.getCategoryMap().size() > 0);
  }

  /** testSupplementalClasses */
  @Test
  public void testSupplementalClasses() throws Exception {
    // Check to make sure that we have something...
    Map<TransactionType, Procedure> procs = this.benchmark.getProcedures();
    assertNotNull(procs);
  }
}
