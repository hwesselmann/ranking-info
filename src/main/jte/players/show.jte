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
    <h1 class="text-2xl font-bold text-left mb-3 mt-3">Spielerprofil</h1>
    @if(player != null)
    <div>

        <!-- Header: Name / Club -->
        <div class="grid grid-cols-2 mb-3">
            <div>
                <img src="/images/${player.nationality().toLowerCase()}.svg"
                     width="35" alt="${player.nationality()}" style="display:inline-block;">
                <span class="text-xl font-bold">${player.firstname()} ${player.lastname()}</span>
                <span class="text-sm text-gray-600">DTB-ID ${player.dtbId()}</span>
            </div>
            <div class="text-right">
                <a href="/clubs/${player.club()}" class="text-brand font-semibold">${player.club()}</a>
                / <span class="italic">${player.federation()}</span>
            </div>
        </div>

        <!-- Section 1: Current position + 12-month positions chart -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
                <p class="text-sm font-semibold mt-2 mb-1">Aktuelle Position</p>
                <table class="w-full mt-1">
                    <tr class="bg-gray-800 text-white font-bold">
                        <th scope="col" class="text-center py-2 px-2">Altersklasse</th>
                        <th scope="col" class="py-2 px-2">Rang</th>
                        <th scope="col" class="py-2 px-2"></th>
                        <th scope="col" class="py-2 px-2">Punkte</th>
                        <th scope="col" class="py-2 px-2"></th>
                    </tr>
                    @if(currentRankings != null)
                    @for(var r : currentRankings)
                    <tr class="border-b border-gray-200${"m00".equals(r.ageGroup()) || "w00".equals(r.ageGroup()) ? " bg-blue-50" : ""}">
                        <td class="text-center py-1 px-2">${"m00".equals(r.ageGroup()) || "w00".equals(r.ageGroup()) ? "Aktive" : r.ageGroup()}</td>
                        <td class="py-1 px-2">${r.position()}</td>
                        <td class="py-1 px-2">
                            @if(r.positionChange() == null || "0".equals(r.positionChange()))
                            <span>@template.fragments.icons.neutral()</span>
                            @elseif(r.positionChange().startsWith("+"))
                            <span class="ri-icon-green">@template.fragments.icons.arrow_up()</span>
                            @elseif(r.positionChange().startsWith("-"))
                            <span class="ri-icon-red">@template.fragments.icons.arrow_down()</span>
                            @endif
                        </td>
                        <td class="py-1 px-2">${r.score()}</td>
                        <td class="py-1 px-2">
                            @if(r.scoreChange() != null && r.scoreChange().startsWith("+"))
                            <span class="ri-icon-green">@template.fragments.icons.arrow_up()</span>
                            @elseif(r.scoreChange() != null && r.scoreChange().startsWith("-"))
                            <span class="ri-icon-red">@template.fragments.icons.arrow_down()</span>
                            @endif
                            @if(r.scoreChange() != null)
                            <span>${r.scoreChange().replace("+", "")}</span>
                            @endif
                        </td>
                    </tr>
                    @endfor
                    @endif
                </table>
            </div>
            <div>
                <p class="text-sm font-semibold mt-2 mb-1">Verlauf Altersklasse (12 Monate)</p>
                <div class="mt-1">
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
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
            <div>
                <p class="text-sm font-semibold mt-2 mb-1">Verlauf Punkte (12 Monate)</p>
                <div class="mt-1">
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
                <span class="text-sm font-semibold">Bisherige Vereine</span>
                <div class="mt-3">
                    <div class="bg-gray-800 text-white font-bold py-2 px-2">Verein</div>
                    @for(var c : player.clubs())
                    <div class="border-b border-gray-200 py-1 px-2">${c.name()} / ${c.federation()}</div>
                    @endfor
                </div>
                <p class="text-sm font-semibold mt-2 mb-1">Verfügbare Daten</p>
                <div class="mt-3">
                    <div class="grid grid-cols-12 bg-gray-800 text-white font-bold">
                        <div class="col-span-4 py-2 px-2">Jahr</div>
                        <div class="col-span-2 py-2 px-2">Q1</div>
                        <div class="col-span-2 py-2 px-2">Q2</div>
                        <div class="col-span-2 py-2 px-2">Q3</div>
                        <div class="col-span-2 py-2 px-2">Q4</div>
                    </div>
                    @if(availableData != null)
                    @for(var entry : availableData.entrySet())
                    <div class="grid grid-cols-12 border-b border-gray-200">
                        <div class="col-span-4 py-1 px-2">${entry.getKey()}</div>
                        @for(var q : java.util.List.of("Q1", "Q2", "Q3", "Q4"))
                        <div class="col-span-2 py-1 px-2">
                            @if(entry.getValue().contains(q))
                            <span class="ri-icon-green">@template.fragments.icons.check_circle()</span>
                            @endif
                        </div>
                        @endfor
                    </div>
                    @endfor
                    @endif
                </div>
            </div>
        </div>

        <!-- Section 3: All-time charts -->
        <div class="mt-4">
            <p class="text-sm font-semibold mt-2 mb-1">Verlauf gesamt</p>
            <div class="mt-1">
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
            <p class="text-sm font-semibold mt-2 mb-1">Punkte gesamt</p>
            <div class="mt-1">
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
        <div class="mb-3 mt-4">
            <span class="text-sm font-semibold">Gesamtverlauf</span>
            <div class="mt-3 overflow-x-auto">
                <div class="grid grid-cols-12 bg-gray-800 text-white font-bold min-w-max">
                    <div class="col-span-2 py-2 px-1">@template.fragments.icons.calendar()</div>
                    <div class="col-span-1 py-2 px-1">@template.fragments.icons.chart_bar()</div>
                    @for(var ag : java.util.List.of("U11","U12","U13","U14","U15","U16","U17","U18","Aktive"))
                    <div class="col-span-1 py-2 px-1">${ag}</div>
                    @endfor
                </div>
                @if(completeRankings != null)
                !{int rowIdx = 0;}
                @for(var r : completeRankings)
                <div class="grid grid-cols-12 border-b border-gray-200 min-w-max${rowIdx % 2 != 0 ? " striped" : ""}">
                    <div class="col-span-2 py-1 px-1">${r.date()}</div>
                    <div class="col-span-1 py-1 px-1">${r.score()}</div>
                    <div class="col-span-1 py-1 px-1">${r.ageGroupPositions().containsKey("U11") ? String.valueOf(r.ageGroupPositions().get("U11")) : ""}</div>
                    <div class="col-span-1 py-1 px-1">${r.ageGroupPositions().containsKey("U12") ? String.valueOf(r.ageGroupPositions().get("U12")) : ""}</div>
                    <div class="col-span-1 py-1 px-1">${r.ageGroupPositions().containsKey("U13") ? String.valueOf(r.ageGroupPositions().get("U13")) : ""}</div>
                    <div class="col-span-1 py-1 px-1">${r.ageGroupPositions().containsKey("U14") ? String.valueOf(r.ageGroupPositions().get("U14")) : ""}</div>
                    <div class="col-span-1 py-1 px-1">${r.ageGroupPositions().containsKey("U15") ? String.valueOf(r.ageGroupPositions().get("U15")) : ""}</div>
                    <div class="col-span-1 py-1 px-1">${r.ageGroupPositions().containsKey("U16") ? String.valueOf(r.ageGroupPositions().get("U16")) : ""}</div>
                    <div class="col-span-1 py-1 px-1">${r.ageGroupPositions().containsKey("U17") ? String.valueOf(r.ageGroupPositions().get("U17")) : ""}</div>
                    <div class="col-span-1 py-1 px-1">${r.ageGroupPositions().containsKey("U18") ? String.valueOf(r.ageGroupPositions().get("U18")) : ""}</div>
                    <div class="col-span-1 py-1 px-1">${r.ageGroupPositions().containsKey("Aktive") ? String.valueOf(r.ageGroupPositions().get("Aktive")) : ""}</div>
                </div>
                !{rowIdx++;}
                @endfor
                @endif
            </div>
        </div>

    </div>
    @endif
`)
