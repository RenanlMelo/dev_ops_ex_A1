package com.example.devopsac1.recompensa.internal;

import com.example.devopsac1.aluno.api.StudentLock;
import com.example.devopsac1.aluno.api.StudentLookup;
import com.example.devopsac1.carteira.api.WalletOperations;
import com.example.devopsac1.recompensa.api.RedemptionView;
import com.example.devopsac1.recompensa.api.RewardCommand;
import com.example.devopsac1.recompensa.api.RewardQuery;
import com.example.devopsac1.recompensa.api.RewardView;
import com.example.devopsac1.shared.CommittedMetrics;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RewardService implements RewardCommand, RewardQuery {
    private final RedemptionRepository repository;
    private final StudentLock lock;
    private final StudentLookup students;
    private final WalletOperations wallet;
    private final CommittedMetrics metrics;
    public RewardService(RedemptionRepository repository, StudentLock lock, StudentLookup students,
                         WalletOperations wallet, CommittedMetrics metrics) {
        this.repository = repository;
        this.lock = lock;
        this.students = students;
        this.wallet = wallet;
        this.metrics = metrics;
    }

    @Override
    @Transactional
    public RedemptionView redeem(Long studentId, String rewardCode, String requestId) {
        lock.lock(studentId);
        var previous = repository.findById(studentId + ":" + requestId).orElse(null);
        if (previous != null) {
            if (!previous.getRewardCode().equals(rewardCode)) {
                throw new IllegalStateException("Esta solicitação já foi usada para outra recompensa");
            }
            return view(previous);
        }
        RewardView reward = RewardCatalog.require(rewardCode);
        wallet.debitOnce(studentId, "redemption:" + studentId + ":" + requestId, reward.cost(),
                "Resgate: " + reward.name());
        Redemption redeemed = repository.save(new Redemption(studentId, requestId, reward.code(),
                reward.name(), reward.cost()));
        metrics.increment("gamificacao.resgates", 1);
        return view(redeemed);
    }

    @Override
    public List<RewardView> catalog() { return RewardCatalog.items(); }

    @Override
    @Transactional(readOnly = true)
    public List<RedemptionView> redemptions(Long studentId) {
        students.require(studentId);
        return repository.findTop20ByStudentIdOrderByRedeemedAtDescIdAsc(studentId).stream()
                .map(this::view).toList();
    }

    private RedemptionView view(Redemption redeemed) {
        return new RedemptionView(redeemed.getRequestId(), redeemed.getStudentId(), redeemed.getRewardCode(),
                redeemed.getRewardName(), redeemed.getCost(), redeemed.getRedeemedAt());
    }
}
