package com.example.devopsac1.conquista.internal;

import java.util.List;

/** Business rules selected for this lab; no persistence/framework dependency. */
public final class GamificationRules {
    public static final long APPROVAL_COINS = 50;
    private GamificationRules() { }
    public static int points(double grade) {
        if (!Double.isFinite(grade) || grade < 0 || grade > 10) {
            throw new IllegalArgumentException("Nota deve estar entre 0 e 10");
        }
        return (int) Math.round(grade * 10);
    }
    public static boolean approved(double grade) { return grade > 7; }
    public static List<BadgeRule> badges() {
        return List.of(
                new BadgeRule("primeira-aprovacao", "Primeiro passo", "Conclua um curso com nota acima de 7", 1, false),
                new BadgeRule("tres-aprovacoes", "Trilha completa", "Conclua três cursos com nota acima de 7", 3, false),
                new BadgeRule("nota-dez", "Excelência", "Conclua um curso com nota 10", 1, true));
    }
    public record BadgeRule(String code, String name, String description, int approvals, boolean perfect) {
        public boolean achieved(long approvedCourses, boolean hasPerfectGrade) {
            return approvedCourses >= approvals && (!perfect || hasPerfectGrade);
        }
    }
}
