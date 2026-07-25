package de.hdawg.rankinginfo.service;

/// Normalized pagination parameters shared by the REST and gRPC listing endpoints.
///
/// Both protocols accept untrusted `page`/`per_page` values from clients. [#of(int, int)] is the
/// single place that clamps them into a usable range, so the two APIs cannot drift apart:
/// - `page` below `1` is clamped to the first page
/// - `perPage` below `1` falls back to [#DEFAULT_PER_PAGE]
/// - `perPage` above [#MAX_PER_PAGE] is capped
///
/// Instances are always valid, so callers can pass them straight to the repository layer.
///
/// @param page the one-based page number, guaranteed to be `>= 1`
/// @param perPage the page size, guaranteed to be within `1..`[#MAX_PER_PAGE]
public record Pagination(int page, int perPage) {

  /// Page size used when a client omits `per_page` or supplies a non-positive value.
  public static final int DEFAULT_PER_PAGE = 25;

  /// Upper bound on the page size a client may request.
  public static final int MAX_PER_PAGE = 100;

  /// Clamps raw client-supplied pagination values into a valid range.
  ///
  /// @param page the requested one-based page number; values below `1` are clamped to `1`
  /// @param perPage the requested page size; non-positive values fall back to
  ///     [#DEFAULT_PER_PAGE], values above [#MAX_PER_PAGE] are capped
  /// @return normalized pagination parameters
  public static Pagination of(int page, int perPage) {
    int clampedPage = Math.max(page, 1);
    int clampedPerPage = perPage < 1 ? DEFAULT_PER_PAGE : Math.min(perPage, MAX_PER_PAGE);
    return new Pagination(clampedPage, clampedPerPage);
  }

  /// Zero-based row offset for this page, computed in `long` arithmetic so that deep pages cannot
  /// overflow into a negative SQL `OFFSET`.
  ///
  /// @return the number of rows to skip
  public long offset() {
    return (page - 1L) * perPage;
  }
}
