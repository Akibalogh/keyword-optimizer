// Copyright 2018 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.adwords.keywordoptimizer;

import static com.google.api.ads.adwords.keywordoptimizer.KeywordOptimizerUtil.createMoney;
import static org.junit.Assert.assertEquals;

import com.google.api.ads.adwords.axis.v201809.cm.Money;
import com.google.api.ads.adwords.axis.v201809.o.StatsEstimate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class KeywordOptimizerUtilTest {

  private static final double DOUBLE_ERROR = 0.0001;

  @Test
  public void testStatsEstimateMean() {
    Money lowCpc = new Money();
    lowCpc.setMicroAmount(1_000_000L);
    Money lowTotal = new Money();
    lowTotal.setMicroAmount(10_000_000L);
    StatsEstimate lowEstimate = new StatsEstimate(lowCpc, 3.0, 0.01, 1000.0F, 100_000.0F, lowTotal);

    Money highCpc = new Money();
    highCpc.setMicroAmount(9_000_000L);
    Money highTotal = new Money();
    highTotal.setMicroAmount(90_000_000L);
    StatsEstimate highEstimate =
        new StatsEstimate(highCpc, 7.0, 0.09, 9000.0F, 900_000.0F, highTotal);
    StatsEstimate estimate = KeywordOptimizerUtil.calculateMean(lowEstimate, highEstimate);
    assertEquals(
        "Expected average CPC", 5_000_000L, estimate.getAverageCpc().getMicroAmount().longValue());
    assertEquals("Expected average position", 5.0, estimate.getAveragePosition(), DOUBLE_ERROR);
    assertEquals("Expected average click rate", 0.05, estimate.getClickThroughRate(), DOUBLE_ERROR);
    assertEquals("Expected clicks per day", 5000.0F, estimate.getClicksPerDay(), DOUBLE_ERROR);
    assertEquals("Expected impressions", 500_000.0F, estimate.getImpressionsPerDay(), DOUBLE_ERROR);
    assertEquals(
        "Expected total cost", 50_000_000L, estimate.getTotalCost().getMicroAmount().longValue());
  }

  @Test
  public void testCreateMoney() {
    Money money = createMoney(100_000_000_000L);
    assertEquals("Expected money equality", new Money(null, 100_000_000_000L), money);
  }
}
