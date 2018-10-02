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

import com.google.api.ads.adwords.axis.v201809.cm.Criterion;
import com.google.api.ads.adwords.axis.v201809.cm.Keyword;
import com.google.api.ads.adwords.axis.v201809.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201809.cm.Language;
import com.google.api.ads.adwords.axis.v201809.cm.Location;
import com.google.api.ads.adwords.axis.v201809.cm.Money;
import com.google.api.ads.adwords.axis.v201809.o.Attribute;
import com.google.api.ads.adwords.axis.v201809.o.AttributeType;
import com.google.api.ads.adwords.axis.v201809.o.DoubleAttribute;
import com.google.api.ads.adwords.axis.v201809.o.LanguageSearchParameter;
import com.google.api.ads.adwords.axis.v201809.o.LocationSearchParameter;
import com.google.api.ads.adwords.axis.v201809.o.LongAttribute;
import com.google.api.ads.adwords.axis.v201809.o.MoneyAttribute;
import com.google.api.ads.adwords.axis.v201809.o.MonthlySearchVolume;
import com.google.api.ads.adwords.axis.v201809.o.MonthlySearchVolumeAttribute;
import com.google.api.ads.adwords.axis.v201809.o.SearchParameter;
import com.google.api.ads.adwords.axis.v201809.o.StatsEstimate;
import com.google.api.ads.adwords.axis.v201809.o.TargetingIdeaService;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Utility functions (math, strings, ...) for various other classes in this project.
 */
public class KeywordOptimizerUtil {
  // Attribute types requested from the TargetingIdeaService.
  protected static final AttributeType[] TIS_ATTRIBUTE_TYPES = new AttributeType[] {
        AttributeType.KEYWORD_TEXT,
        AttributeType.SEARCH_VOLUME,
        AttributeType.AVERAGE_CPC,
        AttributeType.COMPETITION,
        AttributeType.TARGETED_MONTHLY_SEARCHES
      };

  private static final String PLACEHOLDER_NULL = "       ---";
  private static final String FORMAT_NUMBER = "%10.3f";
  private static final String FORMAT_MONEY = "%10.2f";
  private static final int MICRO_UNITS = 1000000;

  /**
   * Calculates the mean estimated statistics based on minimum and maximum values.
   *
   * @param min the minimum estimated statistics
   * @param max the maximum estimated statistics
   * @return the mean value for all given stats
   */
  public static StatsEstimate calculateMean(StatsEstimate min, StatsEstimate max) {
    Money meanAverageCpc = calculateMean(min.getAverageCpc(), max.getAverageCpc());
    Double meanAveragePosition = calculateMean(min.getAveragePosition(), max.getAveragePosition());
    Double meanClicks = calculateMean(min.getClicksPerDay(), max.getClicksPerDay());
    Double meanImpressions = calculateMean(min.getImpressionsPerDay(), max.getImpressionsPerDay());
    Double meanCtr = calculateMean(min.getClickThroughRate(), max.getClickThroughRate());
    Money meanTotalCost = calculateMean(min.getTotalCost(), max.getTotalCost());

    StatsEstimate mean = new StatsEstimate();

    if (meanAverageCpc != null) {
      mean.setAverageCpc(meanAverageCpc);
    }

    if (meanAveragePosition != null) {
      mean.setAveragePosition(meanAveragePosition);
    }

    if (meanClicks != null) {
      mean.setClicksPerDay(meanClicks.floatValue());
    }

    if (meanImpressions != null) {
      mean.setImpressionsPerDay(meanImpressions.floatValue());
    }

    if (meanCtr != null) {
      mean.setClickThroughRate(meanCtr);
    }

    if (meanTotalCost != null) {
      mean.setTotalCost(meanTotalCost);
    }

    return mean;
  }

  /**
   * Returns the mean of the two {@link Money} values if neither is null, else returns null.
   *
   * @param value1 first value
   * @param value2 second value
   * @return the mean of the two {@link Money} values
   */
  private static Money calculateMean(Money value1, Money value2) {
    if (value1 == null || value2 == null) {
      return null;
    }

    Double meanAmount = calculateMean(value1.getMicroAmount(), value2.getMicroAmount());
    if (meanAmount == null) {
      return null;
    }

    Money mean = new Money();
    mean.setMicroAmount(meanAmount.longValue());

    return mean;
  }

  /**
   * Returns the mean of the two {@link Number} values if neither is null, else returns null.
   *
   * @param value1 first value
   * @param value2 second value
   * @return the mean of the two {@link Money} values
   */
  private static Double calculateMean(Number value1, Number value2) {
    if (value1 == null || value2 == null) {
      return null;
    }
    return (value1.doubleValue() + value2.doubleValue()) / 2;
  }


  /**
   * Returns a string representation of the given {@link Keyword}. Please note, as not all classes
   * belong to the project itself, toString methods are bundled here.
   * 
   * @param keyword the keyword
   * @return a string representation of the keyword
   */
  public static String toString(Keyword keyword) {
    return keyword.getText() + "[" + keyword.getMatchType().getValue() + "]";
  }

  /**
   * Returns a string representation of the given {@link StatsEstimate}. Please note, as not all
   * classes belong to the project itself, toString methods are bundled here.
   *
   * @param estimate the estimate
   * @return a string representation of the estimate
   */
  public static String toString(StatsEstimate estimate) {
    return String.format(
        "Imp: %s Cli: %s Ctr: %s Pos: %s Cpc: %s Cos: %s",
        format(estimate.getImpressionsPerDay()),
        format(estimate.getClicksPerDay()),
        format(estimate.getClickThroughRate()),
        format(estimate.getAveragePosition()),
        format(estimate.getAverageCpc()),
        format(estimate.getTotalCost()));
  }

  /**
   * Convenience method for creating a new keyword.
   *
   * @param text the keyword text
   * @param matchType the match type (BROAD, PHRASE, EXACT)
   * @return the newly created {@link Keyword}
   */
  public static Keyword createKeyword(String text, KeywordMatchType matchType) {
    Keyword keyword = new Keyword();
    keyword.setMatchType(matchType);
    keyword.setText(text);
    return keyword;
  }

  /**
   * Convenience method for creating a money object.
   *
   * @param microAmount the amount in micros
   * @return the newly created {@link Money} object
   */
  public static Money createMoney(long microAmount) {
    Money money = new Money();
    money.setMicroAmount(microAmount);
    return money;
  }

  /**
   * Formats a given number in a default format (3 decimals, padded left to 10 characters).
   *
   * @param nr a number
   * @return a string version of the number
   */
  private static String format(Float nr) {
    if (nr == null) {
      return PLACEHOLDER_NULL;
    }

    return String.format(FORMAT_NUMBER, nr);
  }

  /**
   * Formats a given number in a default format (3 decimals, padded left to 10 characters).
   *
   * @param nr a number
   * @return a string version of the number
   */
  public static String format(Double nr) {
    if (nr == null) {
      return PLACEHOLDER_NULL;
    }

    return String.format(FORMAT_NUMBER, nr);
  }

  /**
   * Formats a given monetary value in a default format (2 decimals, padded left to 10 characters).
   *
   * @param money a monetary value
   * @return a string version of the monetary value
   */
  public static String format(Money money) {
    long microAmount;
    if (money != null) {
      microAmount = money.getMicroAmount();
    } else {
      return PLACEHOLDER_NULL;
    }

    double amount = (double) microAmount / MICRO_UNITS;
    return String.format(FORMAT_MONEY, amount);
  }

  /**
   * Formats a given number for CSV output (effectively handles null values).
   *
   * @param number a number or null
   * @return a string version of the number or the empty string if number is null.
   */
  public static String formatCsv(@Nullable Number number) {
    return null == number ? "" : number.toString();
  }

  /**
   * Returns all objects in the given list that are instances of the given class.
   *
   * @param input list of objects to look through
   * @param typeClass class of the objects to filter
   * @return an array of all objects in the given list that are instances of the given class
   */
  @SuppressWarnings(value = "unchecked")
  private static <T> T[] getAllOfType(List<?> input, Class<T> typeClass) {
    List<T> allEntriesOfType = new ArrayList<T>();

    for (Object o : input) {
      if (typeClass.isInstance(o)) {
        allEntriesOfType.add((T) o);
      }
    }

    return allEntriesOfType.toArray((T[]) Array.newInstance(typeClass, 0));
  }

  /**
   * Converts a list of given trigger criteria to according {@link SearchParameter}s for the
   * TargetingIdeaService.
   *
   * @param criteria the criteria to be converted
   * @return a list of according {@link SearchParameter}s
   */
  public static List<SearchParameter> toSearchParameters(List<Criterion> criteria) {
    List<SearchParameter> parameters = new ArrayList<>();

    // Take all location criteria and add as one searchParameter.
    Location[] allLocations = KeywordOptimizerUtil.getAllOfType(criteria, Location.class);
    if (allLocations.length > 0) {
      LocationSearchParameter locationParameter = new LocationSearchParameter();
      locationParameter.setLocations(allLocations);
      parameters.add(locationParameter);
    }

    // Take all language criteria and add as one searchParameter.
    Language[] allLanguages = KeywordOptimizerUtil.getAllOfType(criteria, Language.class);
    if (allLanguages.length > 0) {
      LanguageSearchParameter languageParameter = new LanguageSearchParameter();
      languageParameter.setLanguages(allLanguages);
      parameters.add(languageParameter);
    }

    // Any others are not supported right now.
    return parameters;
  }
  
  /**
   * Converts a given map of attribute data from the {@link TargetingIdeaService} to {@link
   * IdeaEstimate} object.
   *
   * @param attributeData map of attribute data as returned by the {@link TargetingIdeaService}
   * @return a {@link IdeaEstimate} object containing typed data
   */
  public static IdeaEstimate toSearchEstimate(Map<AttributeType, Attribute> attributeData) {
    LongAttribute searchVolumeAttribute =
        (LongAttribute) attributeData.get(AttributeType.SEARCH_VOLUME);
    MoneyAttribute averageCpcAttribute =
        (MoneyAttribute) attributeData.get(AttributeType.AVERAGE_CPC);
    DoubleAttribute competitionAttribute =
        (DoubleAttribute) attributeData.get(AttributeType.COMPETITION);
    MonthlySearchVolumeAttribute targetedMonthlySearchesAttribute =
        (MonthlySearchVolumeAttribute) attributeData.get(AttributeType.TARGETED_MONTHLY_SEARCHES);

    double competition = 0D;
    if (competitionAttribute != null && competitionAttribute.getValue() != null) {
      competition = competitionAttribute.getValue();
    }
    long searchVolume = 0L;
    if (searchVolumeAttribute != null && searchVolumeAttribute.getValue() != null) {
      searchVolume = searchVolumeAttribute.getValue();
    }
    Money averageCpc = KeywordOptimizerUtil.createMoney(0L);
    if (averageCpcAttribute != null && averageCpcAttribute.getValue() != null) {
      averageCpc = averageCpcAttribute.getValue();
    }
    MonthlySearchVolume[] targetedMonthlySearches = new MonthlySearchVolume[] {};
    if (targetedMonthlySearchesAttribute != null
        && targetedMonthlySearchesAttribute.getValue() != null) {
      targetedMonthlySearches = targetedMonthlySearchesAttribute.getValue();
    }

    return new IdeaEstimate(competition, searchVolume, averageCpc, targetedMonthlySearches);
  }
}
