@import java.util.List
@import de.hdawg.rankinginfo.web.viewmodel.ClubSummary
@param List<ClubSummary> clubs = null
<div id="results">
@if(clubs == null)
    <div class="rounded shadow mt-3">
        <header class="bg-gray-100 px-4 py-3 rounded-t font-semibold border-b border-gray-200">
            <span>Vereinssuche</span>
        </header>
        <div class="p-4">
            Finde alle Vereine, die Spieler auf der aktuellen Aktiven- oder Jugendrangliste haben, sowie die Anzahl der aktuellen Ranglistenspieler.
            <br/><br/>
            Filterbare Ranglisten, Spielerinfos und Informationen zu den im System verfügbaren Datenständen sind ebenfalls abrufbar:
        </div>
        <footer class="flex border-t border-gray-200 rounded-b">
            <a class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50" href="/players">Spielersuche</a>
            <a class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50 border-l border-gray-200" href="/listings">Ranglisten</a>
            <a class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50 border-l border-gray-200" href="/federations">Verbandsstatistik</a>
        </footer>
    </div>
@elseif(!clubs.isEmpty())
    <table class="w-full" role="table" aria-label="Vereinsliste">
        <thead>
        <tr class="bg-gray-100">
            <th scope="col" class="py-2 px-2 text-left">Verein</th>
            <th scope="col" class="py-2 px-2">Jugend</th>
            <th scope="col" class="py-2 px-2">Herren/Damen</th>
        </tr>
        </thead>
        <tbody>
        @for(var club : clubs)
        <tr class="border-b border-gray-200 even:bg-gray-50">
            <td class="py-1 px-2">
                <a href="/clubs/${club.name()}" class="text-brand">${club.name()}</a>
            </td>
            <td class="py-1 px-2 text-center">${club.youthCount()}</td>
            <td class="py-1 px-2 text-center">${club.adultCount()}</td>
        </tr>
        @endfor
        </tbody>
    </table>
@else
    <div>Es wurden keine Vereine gefunden.</div>
@endif
</div>
