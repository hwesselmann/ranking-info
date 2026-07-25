package de.hdawg.rankinginfo.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaginationTest {

  @Test
  @DisplayName("keeps valid page and perPage unchanged")
  void keepsValidValues() {
    var pagination = Pagination.of(3, 50);

    assertThat(pagination.page()).isEqualTo(3);
    assertThat(pagination.perPage()).isEqualTo(50);
  }

  @Test
  @DisplayName("clamps page below one to the first page")
  void clampsPageBelowOne() {
    assertThat(Pagination.of(0, 25).page()).isEqualTo(1);
    assertThat(Pagination.of(-5, 25).page()).isEqualTo(1);
    assertThat(Pagination.of(Integer.MIN_VALUE, 25).page()).isEqualTo(1);
  }

  @Test
  @DisplayName("falls back to the default page size when perPage is below one")
  void fallsBackToDefaultPerPage() {
    assertThat(Pagination.of(1, 0).perPage()).isEqualTo(25);
    assertThat(Pagination.of(1, -3).perPage()).isEqualTo(25);
  }

  @Test
  @DisplayName("caps perPage at the maximum page size")
  void capsPerPage() {
    assertThat(Pagination.of(1, 9999).perPage()).isEqualTo(100);
    assertThat(Pagination.of(1, 100).perPage()).isEqualTo(100);
  }

  @Test
  @DisplayName("offset is zero-based and derived from the clamped values")
  void computesOffset() {
    assertThat(Pagination.of(1, 25).offset()).isZero();
    assertThat(Pagination.of(3, 25).offset()).isEqualTo(50);
    assertThat(Pagination.of(0, 25).offset()).isZero();
  }

  @Test
  @DisplayName("offset does not overflow for very deep pages")
  void offsetDoesNotOverflow() {
    assertThat(Pagination.of(Integer.MAX_VALUE, 100).offset())
        .isEqualTo((Integer.MAX_VALUE - 1L) * 100L);
  }
}
