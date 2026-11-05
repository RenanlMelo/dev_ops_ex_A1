package com.example.devopsac1.conquista.internal;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GamificationRulesTest {
    @ParameterizedTest
    @CsvSource({"0,0,false", "4.5,45,false", "7,70,false", "7.1,71,true", "8.5,85,true", "10,100,true"})
    void gradesDeterminePointsAndApproval(double grade, int points, boolean approved) {
        assertEquals(points, GamificationRules.points(grade));
        assertEquals(approved, GamificationRules.approved(grade));
    }
    @Test
    void invalidGradesAreRejected() {
        for (double grade : new double[]{-1, 10.1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> GamificationRules.points(grade));
        }
    }
    @Test
    void badgesRequireApprovalMilestonesOrPerfectGrade() {
        var rules = GamificationRules.badges();
        assertFalse(rules.get(0).achieved(0, false));
        assertTrue(rules.get(0).achieved(1, false));
        assertFalse(rules.get(1).achieved(2, true));
        assertTrue(rules.get(1).achieved(3, false));
        assertFalse(rules.get(2).achieved(3, false));
        assertTrue(rules.get(2).achieved(1, true));
    }
}
