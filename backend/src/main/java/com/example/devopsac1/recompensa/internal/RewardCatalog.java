package com.example.devopsac1.recompensa.internal;

import com.example.devopsac1.recompensa.api.RewardView;
import com.example.devopsac1.shared.ResourceNotFoundException;
import java.util.List;

public final class RewardCatalog {
    private RewardCatalog() { }
    public static List<RewardView> items() {
        return List.of(
                new RewardView("material-extra", "Material de estudo", "Direito ao material complementar de estudos", 50),
                new RewardView("curso-bonus", "Curso bônus", "Crédito de um curso adicional registrado no seu histórico", 100),
                new RewardView("mentoria", "Mentoria", "Direito a uma sessão de orientação registrado no seu histórico", 150));
    }
    public static RewardView require(String code) {
        return items().stream().filter(r -> r.code().equals(code)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa não encontrada: " + code));
    }
}
