// Copyright 2017 Google Inc. All Rights Reserved.
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

package com.google.api.ads.adwords.keywordoptimizer.formula;

/**
 * This class represents a number of type double in the syntax tree for the formula grammar.
 */
public class ASTDouble extends SimpleNode {

  private double numericValue;

  public ASTDouble(int id) {
    super(id);
  }

  public double getValue() {
    return numericValue;
  }

  public void setValue(double value) {
    this.numericValue = value;
  }

  @Override
  public double calculateScore(FormulaContext context) {
    // The numerical value is the score.
    return numericValue;
  }

  @Override
  public Object jjtAccept(FormulaParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString() {
    return Double.toString(numericValue);
  }
}
