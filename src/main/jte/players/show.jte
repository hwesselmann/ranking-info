@import java.util.List
@import java.util.Map
@import java.util.Set
@import de.hdawg.rankinginfo.domain.Player
@import de.hdawg.rankinginfo.web.viewmodel.CurrentRankingRow
@import de.hdawg.rankinginfo.web.viewmodel.CompleteRankingRow
@param Player player = null
@param List<CurrentRankingRow> currentRankings = null
@param List<CompleteRankingRow> completeRankings = null
@param String diagramDataJson = "[]"
@param String diagramScoreDataJson = "[]"
@param String recent12mDataJson = "[]"
@param String recent12mScoreDataJson = "[]"
@param Map<Integer, Set<String>> availableData = null
@param String danger = null
@template.layout(
    title = player != null ? player.firstname() + " " + player.lastname() + " | Ranking-Info" : "Spielerprofil | Ranking-Info",
    danger = danger,
    extraHead = @`
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/chartkick"></script>
    `,
    content = @`
    @if(player != null)
    <div class="mt-5">

        <!-- Header: Name / Club -->
        <div class="ri-card ri-card-pad mb-5">
            <div class="flex flex-wrap items-center justify-between gap-3">
                <div class="flex items-center gap-3">
                    <img src="/images/${player.nationality().toLowerCase()}.svg"
                         width="34" alt="${player.nationality()}" style="display:inline-block;">
                    <div>
                        <p class="text-xl font-semibold text-ink leading-tight">${player.firstname()} ${player.lastname()}</p>
                        <p class="text-xs text-muted">DTB-ID ${player.dtbId()}</p>
                    </div>
                </div>
                <div class="text-sm text-right">
                    <a href="/clubs/${player.club()}" class="text-brand-light font-medium">${player.club()}</a>
                    <span class="text-faint">/</span> <span class="text-muted italic">${player.federation()}</span>
                </div>
            </div>
        </div>

        <!-- Section 1: Current position + 12-month positions chart -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
                <p class="text-sm font-semibold text-ink mb-2">Aktuelle Position</p>
                <div class="ri-table-wrap">
                    <table class="ri-table">
                        <thead>
                        <tr>
                            <th scope="col" class="center">Altersklasse</th>
                            <th scope="col">Rang</th>
                            <th scope="col"></th>
                            <th scope="col">Punkte</th>
                            <th scope="col"></th>
                        </tr>
                        </thead>
                        <tbody>
                        @if(currentRankings != null)
                        @for(var r : currentRankings)
                        <tr style="${"m00".equals(r.ageGroup()) || "w00".equals(r.ageGroup()) ? "background:#EAF1FA" : ""}">
                            <td class="center">${"m00".equals(r.ageGroup()) || "w00".equals(r.ageGroup()) ? "Aktive" : r.ageGroup()}</td>
                            <td class="font-medium">${r.position()}</td>
                            <td>
                                @if(r.positionChange() == null || "0".equals(r.positionChange()))
                                <span>@template.fragments.icons.neutral()</span>
                                @elseif(r.positionChange().startsWith("+"))
                                <span class="ri-icon-green">@template.fragments.icons.arrow_up()</span>
                                @elseif(r.positionChange().startsWith("-"))
                                <span class="ri-icon-red">@template.fragments.icons.arrow_down()</span>
                                @endif
                            </td>
                            <td class="font-medium">${r.score()}</td>
                            <td>
                                @if(r.scoreChange() != null && r.scoreChange().startsWith("+"))
                                <span class="ri-icon-green">@template.fragments.icons.arrow_up()</span>
                                @elseif(r.scoreChange() != null && r.scoreChange().startsWith("-"))
                                <span class="ri-icon-red">@template.fragments.icons.arrow_down()</span>
                                @endif
                                @if(r.scoreChange() != null)
                                <span class="text-muted">${r.scoreChange().replace("+", "")}</span>
                                @endif
                            </td>
                        </tr>
                        @endfor
                        @endif
                        </tbody>
                    </table>
                </div>
            </div>
            <div>
                <p class="text-sm font-semibold text-ink mb-2">Verlauf Altersklasse (12 Monate)</p>
                <div class="ri-card ri-card-pad">
                    <div id="chart-1" style="height: 300px; width: 100%; text-align: center; color: #999; line-height: 300px; font-size: 14px;">Loading...</div>
                    <script>
                        (function() {
                            var createChart = function() {
                                new Chartkick["LineChart"]("chart-1", $unsafe{recent12mDataJson},
                                    {"curve": false, "legend": "bottom", "library": {"scales": {"y": {"reverse": true}}}});
                            };
                            if (typeof Chartkick !== "undefined") { createChart(); }
                            else { window.addEventListener("chartkick:load", createChart, true); }
                        })();
                    </script>
                </div>
            </div>
        </div>

        <!-- Section 2: 12-month score chart + Clubs + Available data -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
                <p class="text-sm font-semibold text-ink mb-2">Verlauf Punkte (12 Monate)</p>
                <div class="ri-card ri-card-pad">
                    <div id="chart-2" style="height: 300px; width: 100%; text-align: center; color: #999; line-height: 300px; font-size: 14px;">Loading...</div>
                    <script>
                        (function() {
                            var createChart = function() {
                                new Chartkick["ColumnChart"]("chart-2", $unsafe{recent12mScoreDataJson},
                                    {"legend": false});
                            };
                            if (typeof Chartkick !== "undefined") { createChart(); }
                            else { window.addEventListener("chartkick:load", createChart, true); }
                        })();
                    </script>
                </div>
            </div>
            <div>
                <p class="text-sm font-semibold text-ink mb-2">Bisherige Vereine</p>
                <div class="ri-table-wrap mb-5">
                    <table class="ri-table">
                        <thead>
                        <tr><th scope="col">Verein</th></tr>
                        </thead>
                        <tbody>
                        @for(var c : player.clubs())
                        <tr><td>${c.name()} <span class="text-faint">/</span> <span class="text-muted">${c.federation()}</span></td></tr>
                        @endfor
                        </tbody>
                    </table>
                </div>

                <p class="text-sm font-semibold text-ink mb-2">Verfügbare Daten</p>
                <div class="ri-table-wrap">
                    <table class="ri-table">
                        <thead>
                        <tr>
                            <th scope="col">Jahr</th>
                            <th scope="col" class="center">Q1</th>
                            <th scope="col" class="center">Q2</th>
                            <th scope="col" class="center">Q3</th>
                            <th scope="col" class="center">Q4</th>
                        </tr>
                        </thead>
                        <tbody>
                        @if(availableData != null)
                        @for(var entry : availableData.entrySet())
                        <tr>
                            <td class="font-medium">${entry.getKey()}</td>
                            @for(var q : java.util.List.of("Q1", "Q2", "Q3", "Q4"))
                            <td class="center">
                                @if(entry.getValue().contains(q))<span class="ri-icon-teal">@template.fragments.icons.check_circle()</span>@else<span class="text-faint">–</span>@endif
                            </td>
                            @endfor
                        </tr>
                        @endfor
                        @endif
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Section 3: All-time charts -->
        <div class="mb-4">
            <p class="text-sm font-semibold text-ink mb-2">Verlauf gesamt</p>
            <div class="ri-card ri-card-pad mb-4">
                <div id="chart-3" style="height: 300px; width: 100%; text-align: center; color: #999; line-height: 300px; font-size: 14px;">Loading...</div>
                <script>
                    (function() {
                        var createChart = function() {
                            new Chartkick["LineChart"]("chart-3", $unsafe{diagramDataJson},
                                {"curve": false, "legend": "bottom", "library": {"scales": {"y": {"reverse": true}}}});
                        };
                        if (typeof Chartkick !== "undefined") { createChart(); }
                        else { window.addEventListener("chartkick:load", createChart, true); }
                    })();
                </script>
            </div>
            <p class="text-sm font-semibold text-ink mb-2">Punkte gesamt</p>
            <div class="ri-card ri-card-pad">
                <div id="chart-4" style="height: 300px; width: 100%; text-align: center; color: #999; line-height: 300px; font-size: 14px;">Loading...</div>
                <script>
                    (function() {
                        var createChart = function() {
                            new Chartkick["ColumnChart"]("chart-4", $unsafe{diagramScoreDataJson},
                                {"legend": false});
                        };
                        if (typeof Chartkick !== "undefined") { createChart(); }
                        else { window.addEventListener("chartkick:load", createChart, true); }
                    })();
                </script>
            </div>
        </div>

        <!-- Section 4: Complete history table -->
        <div class="mb-5">
            <p class="text-sm font-semibold text-ink mb-2">Gesamtverlauf</p>
            <div class="ri-table-wrap">
                <table class="ri-table" role="table" aria-label="Gesamtverlauf">
                    <thead>
                    <tr>
                        <th scope="col">@template.fragments.icons.calendar()</th>
                        <th scope="col" class="center">@template.fragments.icons.chart_bar()</th>
                        @for(var ag : java.util.List.of("U11","U12","U13","U14","U15","U16","U17","U18","Aktive"))
                        <th scope="col" class="center">${ag}</th>
                        @endfor
                    </tr>
                    </thead>
                    <tbody>
                    @if(completeRankings != null)
                    @for(var r : completeRankings)
                    <tr>
                        <td class="font-medium">${r.date()}</td>
                        <td class="center">${r.score()}</td>
                        @for(var ag : java.util.List.of("U11","U12","U13","U14","U15","U16","U17","U18","Aktive"))
                        <td class="center">${r.ageGroupPositions().containsKey(ag) ? String.valueOf(r.ageGroupPositions().get(ag)) : ""}</td>
                        @endfor
                    </tr>
                    @endfor
                    @endif
                    </tbody>
                </table>
            </div>
        </div>

    </div>
    @endif
`)
