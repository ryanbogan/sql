/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.ast.expression;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.opensearch.sql.ast.AbstractNodeVisitor;

/**
 * Expression node of Score function.
 * Score takes a relevance-search expression as an argument and returns it
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
public class ScoreFunction extends UnresolvedExpression {
  private final UnresolvedExpression relevanceQuery;
  private final Literal relevanceFieldWeight;

  @Override
  public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {
    return nodeVisitor.visitScoreFunction(this, context);
  }

  @Override
  public List<UnresolvedExpression> getChild() {
    return List.of(relevanceQuery);
  }
}
