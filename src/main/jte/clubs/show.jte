@import java.util.List
@import java.util.Map
@import de.hdawg.rankinginfo.web.viewmodel.PlayerSummaryRow
@param String clubName = ""
@param Map<String, List<PlayerSummaryRow>> players = null
@param String danger = null
@template.layout(title = clubName + " | Ranking-Info", danger = danger, content = @`
    <h1 class="text-2xl font-semibold mt-5 mb-1">Spieler aus dem Verein ${clubName}</h1>
    <p class="text-sm text-muted mb-5">Alle Spieler aus diesem Verein, die in der aktuellen Periode auf der Rangliste geführt sind.</p>
    @if(players != null)
    @for(var entry : players.entrySet())
    <div class="mb-6">
        <p class="ri-section-label">${entry.getKey()}</p>
        <div class="ri-table-wrap">
            <table class="ri-table" role="table">
                <thead>
                <tr>
                    <th scope="col">DTB-ID</th>
                    <th scope="col">Nachname, Vorname</th>
                    <th scope="col" class="center">Position</th>
                    <th scope="col" class="center">Punkte</th>
                </tr>
                </thead>
                <tbody>
                @for(var p : entry.getValue())
                <tr>
                    <td>
                        <a href="/players/${p.dtbId()}" class="text-brand-light">${p.dtbId()}</a>
                    </td>
                    <td>${p.lastname()}, ${p.firstname()}</td>
                    <td class="center">${p.rank()}</td>
                    <td class="center font-medium">${p.score()}</td>
                </tr>
                @endfor
                </tbody>
            </table>
        </div>
    </div>
    @endfor
    @endif
`)
