package de.hdawg.rankinginfo.repository;

import java.util.ArrayList;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import de.hdawg.rankinginfo.persistence.RankingEntity;

class RankingSpecifications {

  private RankingSpecifications() {}

  static Specification<RankingEntity> matchesFilter(RankingQueryFilter filter) {
    return (root, query, cb) -> {
      var predicates = new ArrayList<Predicate>();
      predicates.add(cb.equal(root.get("date"), filter.date()));
      predicates.add(cb.equal(root.get("ageGroup"), filter.ageGroup()));
      predicates.add(cb.between(root.get("dtbId"), filter.dtbIdStart(), filter.dtbIdEnd()));
      predicates.add(cb.equal(root.get("yobRanking"), filter.yobRanking()));
      predicates.add(cb.equal(root.get("ageGroupRanking"), filter.ageGroupRanking()));
      predicates.add(cb.equal(root.get("yearEndRanking"), filter.yearEndRanking()));

      if (filter.federation() != null) {
        predicates.add(cb.equal(root.get("federation"), filter.federation()));
      }
      if (filter.club() != null) {
        predicates.add(
            cb.like(cb.lower(root.get("club")), "%" + filter.club().toLowerCase() + "%"));
      }
      if (filter.dtbIds() != null) {
        predicates.add(root.get("dtbId").in(filter.dtbIds()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
