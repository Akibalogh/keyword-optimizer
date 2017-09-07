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

import com.google.api.ads.adwords.axis.v201708.cm.ApiException;
import com.google.api.ads.adwords.axis.v201708.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201708.cm.Paging;
import com.google.api.ads.adwords.axis.v201708.o.Attribute;
import com.google.api.ads.adwords.axis.v201708.o.AttributeType;
import com.google.api.ads.adwords.axis.v201708.o.StringAttribute;
import com.google.api.ads.adwords.axis.v201708.o.TargetingIdea;
import com.google.api.ads.adwords.axis.v201708.o.TargetingIdeaPage;
import com.google.api.ads.adwords.axis.v201708.o.TargetingIdeaSelector;
import com.google.api.ads.adwords.axis.v201708.o.TargetingIdeaService;
import com.google.api.ads.adwords.axis.v201708.o.TargetingIdeaServiceInterface;
import com.google.api.ads.common.lib.utils.Maps;
import com.google.common.collect.ImmutableMap;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

  /**
   * Creates a new {@link TisBasedSeedGenerator} based on the given service and customer id.
   *
   * @param tis the API interface to the TargetingIdeaService
   * @param matchTypes match types to be used for seed keyword creation
   * @param campaignConfiguration additional campaign-level settings for keyword evaluation
   */
  public TisBasedSeedGenerator(
      TargetingIdeaServiceInterface tis,
      Set<KeywordMatchType> matchTypes,
      CampaignConfiguration campaignConfiguration) {
    super(matchTypes, campaignConfiguration);
    this.tis = tis;
  }

  /**
   * @return returns a selector for the {@link TargetingIdeaService}
   */
  protected abstract TargetingIdeaSelector getSelector();
  
  @Override
  protected ImmutableMap<String, IdeaEstimate> getKeywordsAndEstimates()
      throws KeywordOptimizerException {
    TargetingIdeaSelector selector = getSelector();
    Map<String, IdeaEstimate> keywordsAndEstimates = new HashMap<String, IdeaEstimate>();
    
    try {
      int offset = 0;

      TargetingIdeaPage page = null;
      do {
        selector.setPaging(new Paging(offset, PAGE_SIZE));
        page = tis.get(selector);

        if (page.getEntries() != null) {
          for (TargetingIdea targetingIdea : page.getEntries()) {
            Map<AttributeType, Attribute> attributeData = Maps.toMap(targetingIdea.getData());
            
            StringAttribute keywordAttribute =
                (StringAttribute) attributeData.get(AttributeType.KEYWORD_TEXT);
            IdeaEstimate estimate = KeywordOptimizerUtil.toSearchEstimate(attributeData);
            
            keywordsAndEstimates.put(keywordAttribute.getValue(), estimate);
          }
        }
        offset += PAGE_SIZE;
      } while (offset < page.getTotalNumEntries());

    } catch (ApiException e) {
      throw new KeywordOptimizerException("Problem while querying the targeting idea service", e);
    } catch (RemoteException e) {
      throw new KeywordOptimizerException("Problem while connecting to the AdWords API", e);
    }

    return ImmutableMap.copyOf(keywordsAndEstimates);
  }
  
}
