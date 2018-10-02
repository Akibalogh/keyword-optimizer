// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.adwords.keywordoptimizer;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DefaultRoundStrategyTest {

  private static final Integer MAXIMUM_STEPS = 1;
  private static final Double MINIMUM_IMPROVEMENT = 0.1;
  private static final int MAXIMUM_POPULATION_SIZE = 1000;
  private static final int MAXIMUM_ALTERNATIVES = 1000;

  /**
   * Test to ensure that a campaign strategy is finished if provided with an empty keyword
   * collection
   */
  @Test
  public void testEmptyFinished() {
    DefaultRoundStrategy strategy =
        new DefaultRoundStrategy(
            MAXIMUM_STEPS, MINIMUM_IMPROVEMENT, MAXIMUM_POPULATION_SIZE, MAXIMUM_ALTERNATIVES);
    KeywordCollection collection = new KeywordCollection(CampaignConfiguration.builder().build());
    assertFalse(strategy.isFinished(collection));
  }
}
