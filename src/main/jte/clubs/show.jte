@import java.util.List
@import java.util.Map
@import de.hdawg.rankinginfo.web.viewmodel.PlayerSummaryRow
@param String clubName = ""
@param Map<String, List<PlayerSummaryRow>> players = null
@param String danger = null
@template.layout(title = clubName + " | Ranking-Info", danger = danger, content = @`
    <h1 class="text-2xl font-bold text-left mt-3 mb-1">Spieler aus dem Verein ${clubName}</h1>
    <p class="text-sm text-gray-600 mb-4">Hier finden Sie alle Spieler aus diesem Verein, die in der aktuellen Periode auf der Rangliste geführt sind.</p>
    @if(players != null)
    @for(var entry : players.entrySet())
    <div class="mb-6">
        <h2 class="text-lg font-semibold mb-2">${entry.getKey()}</h2>
        <table class="w-full" role="table">
            <thead>
            <tr class="bg-gray-100">
                <th scope="col" class="py-2 px-2">DTB-ID</th>
                <th scope="col" class="py-2 px-2 text-left">Nachname, Vorname</th>
                <th scope="col" class="py-2 px-2">Position</th>
                <th scope="col" class="py-2 px-2">Punkte</th>
            </tr>
            </thead>
            <tbody>
            @for(var p : entry.getValue())
            <tr class="border-b border-gray-200 even:bg-gray-50">
                <td class="py-1 px-2">
                    <a href="/players/${p.dtbId()}" class="text-brand">${p.dtbId()}</a>
                </td>
                <td class="py-1 px-2">${p.lastname()}, ${p.firstname()}</td>
                <td class="py-1 px-2 text-center">${p.rank()}</td>
                <td class="py-1 px-2 text-center">${p.score()}</td>
            </tr>
            @endfor
            </tbody>
        </table>
    </div>
    @endfor
    @endif
`)
