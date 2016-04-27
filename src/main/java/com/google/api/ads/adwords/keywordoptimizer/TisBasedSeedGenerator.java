// Copyright 2016 Google Inc. All Rights Reserved.
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

import com.google.api.ads.adwords.axis.v201603.cm.ApiException;
import com.google.api.ads.adwords.axis.v201603.cm.Paging;
import com.google.api.ads.adwords.axis.v201603.o.Attribute;
import com.google.api.ads.adwords.axis.v201603.o.AttributeType;
import com.google.api.ads.adwords.axis.v201603.o.StringAttribute;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdea;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdeaPage;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdeaSelector;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdeaService;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdeaServiceInterface;
import com.google.api.ads.common.lib.utils.Maps;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Base class for {@link SeedGenerator}s using the {@link TargetingIdeaService} for creating seed
 * keywords. Delegates the creation of the {@link TargetingIdeaSelector} to derived classes and 
 * implements the extraction of plain text keywords from the results of the 
 * {@link TargetingIdeaService}.
 */
public abstract class TisBasedSeedGenerator extends AbstractSeedGenerator {
  // Page size for retrieving results. All pages are used anyways (not just the first one), so 
  // using a reasonable value here.
  public static final int PAGE_SIZE = 100;

  protected TargetingIdeaServiceInterface tis;
  private final Long clientCustomerId;

  /**
   * Creates a new {@link TisBasedSeedGenerator}.
   *
   * @param context holding shared objects during the optimization process
   * @param campaignConfiguration additional campaign-level settings for keyword evaluation
   */
  public TisBasedSeedGenerator(
      OptimizationContext context, CampaignConfiguration campaignConfiguration) {
    super(campaignConfiguration);
    tis = context.getAdwordsApiUtil().getService(TargetingIdeaServiceInterface.class);
    clientCustomerId = context.getAdwordsApiUtil().getClientCustomerId();
  }

  /**
   * @return returns a selector for the {@link TargetingIdeaService}
   */
  protected abstract TargetingIdeaSelector getSelector();

  @Override
  protected Collection<String> getKeywords() throws KeywordOptimizerException {
    final TargetingIdeaSelector selector = getSelector();
    Collection<String> keywords = new ArrayList<String>();

    try {
      int offset = 0;

      TargetingIdeaPage page = null;

      do {
        selector.setPaging(new Paging(offset, PAGE_SIZE));

        page = AwapiRateLimiter.getInstance().run(new AwapiCall<TargetingIdeaPage>() {
          @Override
          public TargetingIdeaPage invoke() throws ApiException, RemoteException {
            return tis.get(selector);
          }
        }, clientCustomerId);

        if (page.getEntries() != null) {
          for (TargetingIdea targetingIdea : page.getEntries()) {
            Map<AttributeType, Attribute> data = Maps.toMap(targetingIdea.getData());

            StringAttribute keyword = (StringAttribute) data.get(AttributeType.KEYWORD_TEXT);
            keywords.add(keyword.getValue());
          }
        }
        offset += PAGE_SIZE;
      } while (offset < page.getTotalNumEntries());

    } catch (ApiException e) {
      throw new KeywordOptimizerException("Problem while querying the targeting idea service", e);
    } catch (RemoteException e) {
      throw new KeywordOptimizerException("Problem while connecting to the AdWords API", e);
    }

    return keywords;
  }
}
