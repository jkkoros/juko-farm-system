package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.FarmInputLoan;
import com.example.farmmanagement.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FarmInputLoanRepository extends JpaRepository<FarmInputLoan, Long> {

    List<FarmInputLoan> findBySeasonOrderByIdDesc(Season season);

    @Query("SELECT COALESCE(SUM(l.totalCost), 0) FROM FarmInputLoan l WHERE l.season = :season")
    double getTotalLoansBySeason(@Param("season") Season season);

    @Query("SELECT COUNT(DISTINCT l.farmerId) FROM FarmInputLoan l WHERE l.season = :season")
    long getUniqueFarmersBySeason(@Param("season") Season season);

    @Query("SELECT COALESCE(SUM(l.totalCost), 0) FROM FarmInputLoan l WHERE l.farmerId = :farmerId")
    double getTotalDebtByFarmerId(@Param("farmerId") String farmerId);

    // Outstanding debt = total loans - total repayments
    @Query("""
        SELECT COALESCE(SUM(l.totalCost), 0) - 
               COALESCE((SELECT SUM(r.amountRepaid) 
                         FROM LoanRepayment r 
                         WHERE r.farmerId = :farmerId), 0)
        FROM FarmInputLoan l 
        WHERE l.farmerId = :farmerId
        """)
    double getOutstandingDebtByFarmerId(@Param("farmerId") String farmerId);
}
