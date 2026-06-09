@import java.util.List
@import de.hdawg.rankinginfo.domain.Ranking
@param List<Ranking> players = null
<div id="results">
@if(players == null)
    <div class="rounded shadow mt-3">
        <header class="bg-gray-100 px-4 py-3 rounded-t font-semibold border-b border-gray-200">
            <span>Spielersuche</span>
        </header>
        <div class="p-4">
            <h6 class="text-sm font-semibold mb-2">Der direkte Weg zum Spielerprofil</h6>
            Über die Spielersuche kann gezielt nach einer Spielerin oder einem Spieler gesucht werden.
            Wenn Du keine Suchkriterien angibst und auf 'suchen' klickst, erhältst du alle Spieler,
            die im System vorhanden sind - diese Liste kann allerdings ziemlich lang sein.
            <br/><br/>
            Bei den Suchkriterien stehen die Suche nach der <b>DTB-ID</b>, <b>Nachnamen oder Teilen des Nachnamens</b>
            oder dem <b>Geburtsjahrgang</b> zur Verfügung. Gibst Du eine DTB-ID ein, wird immer nur nach dieser
            gesucht. Die Suche nach Nachname und Geburtsjahr sind kombinierbar.
            <br/><br/>
            Filterbare Ranglisten und Informationen zu den im System verfügbaren Datenständen sind ebenfalls abrufbar:
        </div>
        <footer class="flex border-t border-gray-200 rounded-b">
            <a class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50" href="/listings">Ranglisten</a>
            <a class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50 border-l border-gray-200" href="/clubs">Vereinssuche</a>
            <a class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50 border-l border-gray-200" href="/federations">Verbandsstatistik</a>
        </footer>
    </div>
@elseif(!players.isEmpty())
    <table class="w-full" role="table" aria-label="Spielerliste">
        <thead>
        <tr class="bg-gray-100">
            <th scope="col" class="py-2 px-2 text-left">Nachname, Vorname</th>
            <th scope="col" class="py-2 px-2">DTB-ID</th>
            <th scope="col" class="py-2 px-2 hidden md:table-cell">Verein</th>
        </tr>
        </thead>
        <tbody>
        @for(var p : players)
        <tr class="border-b border-gray-200 even:bg-gray-50">
            <td class="py-1 px-2">
                <a href="/players/${p.dtbId()}" class="text-brand">${p.lastname()}, ${p.firstname()}</a>
            </td>
            <td class="py-1 px-2 text-center">${p.dtbId()}</td>
            <td class="py-1 px-2 hidden md:table-cell">${p.club()}</td>
        </tr>
        @endfor
        </tbody>
    </table>
@else
    <div>Es wurden keine Spieler gefunden.</div>
@endif
</div>
