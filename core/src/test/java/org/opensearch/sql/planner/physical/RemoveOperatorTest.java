/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.sql.planner.physical;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.opensearch.sql.data.type.ExprCoreType.INTEGER;
import static org.opensearch.sql.data.type.ExprCoreType.STRING;
import static org.opensearch.sql.planner.physical.PhysicalPlanDSL.remove;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.model.ExprValueUtils;
import org.opensearch.sql.expression.DSL;

@ExtendWith(MockitoExtension.class)
class RemoveOperatorTest extends PhysicalPlanTestBase {
  @Mock
  private PhysicalPlan inputPlan;

  @Test
  public void remove_one_field() {
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next())
        .thenReturn(ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET", "response", 200)));
    PhysicalPlan plan = remove(inputPlan, DSL.ref("action", STRING));
    List<ExprValue> result = execute(plan);

    assertThat(
        result,
        allOf(
            iterableWithSize(1),
            hasItems(ExprValueUtils.tupleValue(ImmutableMap.of("response", 200)))));
  }

  @Test
  public void remove_one_field_follow_the_input_order() {
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next())
        .thenReturn(
            ExprValueUtils.tupleValue(
                ImmutableMap.of("action", "GET", "response", 200, "referer", "www.amazon.com")));
    PhysicalPlan plan = remove(inputPlan, DSL.ref("response", INTEGER));
    List<ExprValue> result = execute(plan);

    assertThat(
        result,
        allOf(
            iterableWithSize(1),
            hasItems(
                ExprValueUtils.tupleValue(
                    ImmutableMap.of("action", "GET", "referer", "www.amazon.com")))));
  }

  @Test
  public void remove_two_field() {
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next())
        .thenReturn(
            ExprValueUtils.tupleValue(
                ImmutableMap.of("action", "GET", "response", 200, "referer", "www.amazon.com")));
    PhysicalPlan plan = remove(inputPlan, DSL.ref("response", INTEGER), DSL.ref("referer", STRING));
    List<ExprValue> result = execute(plan);

    assertThat(
        result,
        allOf(
            iterableWithSize(1),
            hasItems(ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET")))));
  }

  @Test
  public void project_ignore_missing_value() {
    when(inputPlan.hasNext()).thenReturn(true, true, false);
    when(inputPlan.next())
        .thenReturn(ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET", "response", 200)))
        .thenReturn(ExprValueUtils.tupleValue(ImmutableMap.of("action", "POST")));
    PhysicalPlan plan = remove(inputPlan, DSL.ref("response", STRING));
    List<ExprValue> result = execute(plan);

    assertThat(
        result,
        allOf(
            iterableWithSize(2),
            hasItems(
                ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET")),
                ExprValueUtils.tupleValue(ImmutableMap.of("action", "POST")))));
  }

  @Test
  public void remove_nothing_with_none_tuple_value() {
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next()).thenReturn(ExprValueUtils.integerValue(1));
    PhysicalPlan plan = remove(inputPlan, DSL.ref("response", STRING), DSL.ref("referer", STRING));
    List<ExprValue> result = execute(plan);

    assertThat(result, allOf(iterableWithSize(1), hasItems(ExprValueUtils.integerValue(1))));
  }

  @Test
  public void invalid_to_retrieve_schema_from_remove() {
    PhysicalPlan plan = remove(inputPlan);
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> plan.schema());
    assertEquals(
        "[BUG] schema can been only applied to ProjectOperator, instead of RemoveOperator",
        exception.getMessage());
  }
}
